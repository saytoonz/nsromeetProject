package com.sayt.godslove.recording


import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.get
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_recorder.*
import kotlinx.android.synthetic.main.fragment_home.*
import com.sayt.godslove.R
import com.sayt.godslove.recording.home.HomeFragment
import com.sayt.godslove.recording.settings.PreferenceHelper
import com.sayt.godslove.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions

class RecorderActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var what: String
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            PreferenceHelper(this).apply {
                // Apply theme before onCreate
                applyNightMode(nightMode)
                initIfFirstTimeAnd {
                    createNotificationChannels()
                }
            }
        }
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_recorder)


        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("what", intent.getStringExtra("what")).apply()

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("id", intent.getStringExtra("id")).apply()

        if (intent.hasExtra("subject"))
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("subject", intent.getStringExtra("subject")).apply()

        navController = findNavController(R.id.main_nav_host_fragment)

        findViewById<Toolbar?>(R.id.toolbar)?.let {
            setSupportActionBar(it)
            val appBarConfiguration = AppBarConfiguration(
                    setOf(R.id.home, R.id.navigation_dialog, R.id.more_settings_dialog)
            )
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        }

    }

    override fun onSupportNavigateUp() = navController.navigateUp()

    companion object {
        const val ACTION_TOGGLE_RECORDING = "com.sayt.godslove.recording.TOGGLE_RECORDING"
    }
}