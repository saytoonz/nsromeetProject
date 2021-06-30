package com.sayt.godslove.recording.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.selection.SelectionTracker
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayt.godslove.R
import com.sayt.godslove.recording.RecorderActivity.Companion.ACTION_TOGGLE_RECORDING
import com.sayt.godslove.recording.data.Recording
import com.sayt.godslove.recording.recordings.RecordingListFragment
import com.sayt.godslove.recording.services.RecorderService
import com.sayt.godslove.recording.services.RecorderState
import com.sayt.godslove.recording.services.UriType
import com.sayt.godslove.recording.settings.PreferenceHelper
import com.sayt.godslove.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions


class HomeFragment : RecordingListFragment() {

    private lateinit var bottomBar: BottomAppBar

    private lateinit var fab: FloatingActionButton

    override val layoutRes = R.layout.fragment_home

    private lateinit var recorderState: LiveData<RecorderState.State>

    private lateinit var preferences: PreferenceHelper

    private lateinit var alertDialog: AlertDialog.Builder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = PreferenceHelper(requireContext()).apply {
            // preferenceHelper.doPostInitCheck()
            // preferenceHelper.checkOutputDirectory()
            // preferenceHelper.checkRecordAudio()
            if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.RECORD_AUDIO
                    )
                    != PackageManager.PERMISSION_GRANTED
            ) {
                if (recordAudio) {
                    recordAudio = false
                }
            }
            saveLocation?.let { uri ->
                if (uri.type == UriType.SAF) {
                    requireContext().contentResolver.takePersistableUriPermission(
                            uri.uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    requireContext().contentResolver.persistedUriPermissions.filter { it.uri == uri.uri }
                            .apply {
                                if (isEmpty()) {
                                    resetSaveLocation()
                                }
                            }
                }
            }
        }

        alertDialog = context?.let { AlertDialog.Builder(it) }!!

        recorderState = viewModel.recorderState

        // Respond to app shortcut
        requireActivity().intent.action?.let {
            when (it) {
                ACTION_TOGGLE_RECORDING -> if (RecorderState.State.STOPPED == recorderState.value)
                    startRecording()
                else {
                    stopRecording()
                    requireActivity().finish()
                }
            }
        }

        view.findViewById<Toolbar?>(R.id.toolbar)?.title = getString(R.string.home_recordings_title)
        // Configure fab
        fab = view.findViewById(R.id.fab)
        fab.visibility = View.GONE
        fab.apply {
            setOnClickListener {
                when {
                    selectionTracker.hasSelection() -> selectionTracker.clearSelection()
                    isRecording -> stopRecording()
                    else -> {
                        if (preferences.saveLocation == null) {
                            showChooseTreeUri()
                        } else {
                            startRecording()
                        }
                    }
                }
            }
            setOnLongClickListener {
                Toast.makeText(requireContext(), R.string.home_fab_record_hint, Toast.LENGTH_SHORT)
                        .show()
                true
            }
        }


        // Configure bottom bar
        bottomBar = view.findViewById<BottomAppBar>(R.id.bar).apply {
            hideOnScroll = true
            setNavigationOnClickListener {
                if (selectionTracker.hasSelection()) {
                    selectionTracker.clearSelection()
                } else {
                    //findNavController().navigate(HomeFragmentDirections.actionHomeToBottomNavigationDialog())
                    val fragmentManager = requireActivity().supportFragmentManager
                    val bottomFragment: BottomSheetDialogFragment
                    bottomFragment = fragmentManager.findFragmentByTag("bottom_fragment")
                            as BottomSheetDialogFragment? ?: BottomNavigationDialog()
                    bottomFragment.show(fragmentManager, "bottom_fragment")
                }
            }
            inflateMenu(R.menu.home)
            setOnMenuItemClickListener {
                if (NavigationUI.onNavDestinationSelected(
                                it,
                                Navigation.findNavController(bottomBar)
                        )
                ) {
                    true
                } else {
                    when (it.itemId) {
                        R.id.more_settings -> {
                            findNavController().navigate(HomeFragmentDirections.actionHomeToMoreSettingsDialog())
                            true
                        }
                        R.id.share -> {
                            val recording = selectionTracker.selection.first()
                            if (selectionTracker.selection.size() == 1) {
                                showShareRecordingDialog(recording)
                            } else {
                                shareVideos(selectionTracker.selection.toList())
                            }
                            true
                        }
                        R.id.delete -> {
                            val recording = selectionTracker.selection.first()
                            if (selectionTracker.selection.size() == 1) {
                                showDeleteRecordingDialog(recording)
                            } else {
                                showDeleteRecordingsDialog(selectionTracker.selection.toList())
                            }
                            true
                        }
                        R.id.rename -> {
                            val recording = selectionTracker.selection.first()
                            showRenameRecordingDialog(requireContext(), recording)
                            true
                        }
                        else -> super.onOptionsItemSelected(it)
                    }
                }
            }
        }

        selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Recording>() {
            override fun onSelectionChanged() {
                when {
                    !selectionTracker.hasSelection() -> bottomBarOnReset()
                    selectionTracker.selection.size() == 1 -> bottomBarOnItemSelected()
                    selectionTracker.selection.size() == 2 -> bottomBarOnItemsSelected()
                }
            }
        })

        recorderState.observe(viewLifecycleOwner, Observer {
            when (it) {
                RecorderState.State.RECORDING -> {
                    onRecording()
                }
                RecorderState.State.PAUSED -> {
                    onRecordingPaused()
                }
                RecorderState.State.STOPPED -> {
                    onRecordingStopped()
                }
                else -> {
                    onRecordingStopped()
                }
            }
        })


        val what = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("what", "")
        if (what.equals("recordings")) {
            Toast.makeText(context, "Recordings", Toast.LENGTH_LONG).show()
        }

        if (what.equals("call")) {
            context?.let {
                AlertDialog.Builder(it)
//                        .setCancelable(false)
                        .setTitle("Record Call")
                        .setMessage("Would you like to record this call as it goes on?" +
                                "\n\nRecording this call uses screen recorder to take continues" +
                                " shots of your phone screen and therefore would " +
                                "record everything that shows on you phone screen and " +
                                "the surrounding sounds, do you want to continue?")
                        .setPositiveButton(android.R.string.yes) { dialog, _ ->
                            when {
                                selectionTracker.hasSelection() -> selectionTracker.clearSelection()
                                isRecording -> {
                                    stopRecording()
                                    if (preferences.saveLocation == null) {
                                        showChooseTreeUri()
                                    } else {
                                        startRecording()
                                    }
                                }
                                else -> {
                                    if (preferences.saveLocation == null) {
                                        showChooseTreeUri()
                                    } else {
                                        startRecording()
                                    }
                                }
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton(android.R.string.no) { dialog, _ ->
                            val what = PreferenceManager.getDefaultSharedPreferences(context)
                                    .getString("id", "")
                            val subject = PreferenceManager.getDefaultSharedPreferences(context)
                                    .getString("subject", "Godslove")

                            val options = JitsiMeetConferenceOptions.Builder()
                                    .setRoom(what)
                                    .setSubject(subject)
                                    .build()

                            JitsiMeetActivity.launch(it, options)
                            dialog.dismiss()
                        }
                        .show()
            }
        }


    }

    private val isRecording: Boolean
        get() = recorderState.value?.run {
            this != RecorderState.State.STOPPED
        } ?: false

    private fun onRecording() {
        configureFab(true)
    }

    private fun onRecordingStopped() {
        configureFab(false)
    }

    private fun onRecordingPaused() {
        configureFab(true)
    }

    private fun configureFab(isRecording: Boolean) {
        fab.setImageDrawable(if (isRecording) R.drawable.ic_stop else R.drawable.ic_record)
    }

    private fun startRecording() {
        // Request Screen recording permission
        val projectionManager =
                requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
                projectionManager.createScreenCaptureIntent(),
                SCREEN_RECORD_REQUEST_CODE
        )
    }

    private fun stopRecording() {
        RecorderService.stop(requireContext())
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString("id", "").apply()
    }

    private fun bottomBarOnReset() {
        configureFab(isRecording)
        bottomBar.navigationIcon = requireContext().getDrawable(R.drawable.ic_menu)
        bottomBar.replaceMenu(R.menu.home)
        bottomBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
    }

    private fun bottomBarOnItemSelected() {
        bottomBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
        bottomBar.navigationIcon = requireContext().getDrawable(R.drawable.ic_cancel)
        bottomBar.replaceMenu(R.menu.item_selected)
    }

    private fun bottomBarOnItemsSelected() {
        bottomBar.menu.findItem(R.id.rename)?.apply { isVisible = false }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SCREEN_RECORD_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    RecorderService.start(requireContext(), resultCode, data)

                    val what = PreferenceManager.getDefaultSharedPreferences(context)
                            .getString("id", "")
                    val subject = PreferenceManager.getDefaultSharedPreferences(context)
                            .getString("subject", "Godslove")

                    if (!TextUtils.isEmpty(what)) {
                        val options = JitsiMeetConferenceOptions.Builder()
                                .setRoom(what).setSubject(subject).build()
                        JitsiMeetActivity.launch(context, options)
                    }else{
                        Toast.makeText(context, "Can't ", Toast.LENGTH_LONG).show()
                    }
                }

                requireActivity().intent.action?.takeIf { it == ACTION_TOGGLE_RECORDING }?.let {
                    requireActivity().finish()
                }
            }
            REQUEST_DOCUMENT_TREE -> {
                if (resultCode == Activity.RESULT_OK) {
                    onTreeUriResult(resultCode, data)
                }
                if (preferences.saveLocation != null) {
                    startRecording()
                }
            }
        }
    }

    private fun onTreeUriResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri: Uri = data!!.data!!
            requireContext().contentResolver.apply {
                takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                persistedUriPermissions.filter { it.uri == uri }.apply {
                    if (isNotEmpty()) {
                        PreferenceHelper(requireContext()).setSaveLocation(uri, UriType.SAF)
                    }
                }
            }
        }
    }

    private fun showChooseTreeUri() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_location_dialog_title)
                .setPositiveButton(R.string.choose_location_action) { _, _ ->
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.putExtra(
                            "android.provider.extra.INITIAL_URI",
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(intent, REQUEST_DOCUMENT_TREE)
                }
                .create().show()
    }

    private val String.filename: String
        get() = substringBeforeLast(".")

    private val String.extension: String
        get() {
            val ext = substringAfterLast(".")
            if (ext.isEmpty())
                return ""
            return ".$ext"
        }

    private fun showRenameRecordingDialog(context: Context, recording: Recording) {
        val inflater = LayoutInflater.from(context)

        @SuppressLint("InflateParams")
        val view = inflater.inflate(R.layout.dialog_rename_file, null)
        val input = view.findViewById<EditText>(R.id.input)
        input.setText(recording.title.filename)
        input.selectAll()
        view.findViewById<TextView>(R.id.extension).text = recording.title.extension

        MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_title_rename)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    val newName = input.text.toString().trim { it <= ' ' } + recording.title.extension
                    rename(recording, newName)
                    selectionTracker.clearSelection()
                    dialog.cancel()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                .setView(view)
                .show()
    }

    private fun showDeleteRecordingDialog(recording: Recording) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_file_msg)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    delete(recording)
                    selectionTracker.clearSelection()
                    dialog.cancel()
                }
                .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
                .show()
    }

    private fun showDeleteRecordingsDialog(recordings: List<Recording>) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_all_msg)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    delete(recordings)
                    selectionTracker.clearSelection()
                    dialog.cancel()
                }
                .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
                .show()
    }

    private fun showShareRecordingDialog(recording: Recording) {
        startActivity(
                Intent.createChooser(
                        createShareIntent(recording.uri),
                        getString(R.string.notification_finish_title)
                )
        )
    }

    private fun createShareIntent(uri: Uri) = Intent()
            .setAction(Intent.ACTION_SEND)
            .setType("video/*")
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .putExtra(Intent.EXTRA_STREAM, uri)

    private fun shareVideos(recordings: List<Recording>) {
        val videoList = ArrayList<Uri>()
        for (recording in recordings) {
            videoList.add(recording.uri)
        }
        val shareIntent = Intent()
                .setAction(Intent.ACTION_SEND_MULTIPLE)
                .setType("video/*")
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, videoList)
        startActivity(
                Intent.createChooser(
                        shareIntent,
                        getString(R.string.notification_finish_title)
                )
        )
    }

    private fun FloatingActionButton.setImageDrawable(res: Int) {
        this.setImageDrawable(requireContext().getDrawable(res))
    }


    override fun onResume() {
        super.onResume()

        val what = PreferenceManager.getDefaultSharedPreferences(context).getString("id", "")
        if (TextUtils.isEmpty(what) && isRecording) {
            stopRecording()
        }
    }


    companion object {
        const val SCREEN_RECORD_REQUEST_CODE = 1003
        const val REQUEST_DOCUMENT_TREE = 22
    }
}