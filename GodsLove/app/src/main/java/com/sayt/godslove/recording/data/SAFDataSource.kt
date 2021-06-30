package com.sayt.godslove.recording.data

import android.content.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sayt.godslove.recording.services.RecorderService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class SAFDataSource(val context: Context, val uri: Uri) : DataSource {

    override fun create(
        folderUri: Uri,
        name: String,
        mimeType: String,
        extra: ContentValues?
    ): Uri? {
        return DocumentFile.fromTreeUri(context, folderUri)?.createFile(mimeType, name)?.uri
    }

    override fun delete(uri: Uri) {
        DocumentsContract.deleteDocument(context.contentResolver, uri)
        notifyObservers()
    }

    override fun delete(uris: List<Uri>) {
        uris.forEach { DocumentsContract.deleteDocument(context.contentResolver, it) }
        notifyObservers()
    }

    override fun rename(uri: Uri, newName: String) {
        DocumentsContract.renameDocument(context.contentResolver, uri, newName)
        notifyObservers()
    }

    fun fetchRecordings(): List<Recording> {
        val recordings = ArrayList<Recording>()
        DocumentFile.fromTreeUri(context, uri)?.listFiles()?.forEach {
            if (it.type == "video/mp4") {
                context.contentResolver.openFileDescriptor(it.uri, "r")?.fileDescriptor?.let { fd ->
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(fd)
                    val time: String? =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    retriever.release()
                    time?.toInt()
                }?.let { duration ->
                    // Duration is null when the file is corrupted or it is recording
                    recordings.add(
                        Recording(
                            it.uri, it.name
                                ?: "", duration, it.length(), it.lastModified()
                        )
                    )
                }
            }
        }
        return recordings
    }

    override fun recordings(): Flow<List<Recording>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                offer(fetchRecordings())
            }
        }
        registerReceiver(receiver)
        offer(fetchRecordings())
        awaitClose {
            unregisterReceiver(receiver)
        }
    }

    override fun update(uri: Uri, values: ContentValues) {
        notifyObservers()
    }

    private fun registerReceiver(receiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
            IntentFilter().apply {
                addAction(ACTION_CONTENT_CHANGED)
                addAction(RecorderService.ACTION_RECORDING_COMPLETED)
                addAction(RecorderService.ACTION_RECORDING_DELETED)
            })
    }

    private fun unregisterReceiver(receiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
    }

    private fun notifyObservers() {
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(Intent(RecorderService.ACTION_RECORDING_DELETED))
    }

    companion object {
        const val ACTION_CONTENT_CHANGED = "action_content_changed"
    }
}