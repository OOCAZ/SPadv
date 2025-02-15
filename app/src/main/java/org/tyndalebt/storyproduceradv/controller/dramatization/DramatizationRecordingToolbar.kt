package org.tyndalebt.storyproduceradv.controller.dramatization

import android.view.View
import android.widget.ImageButton
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.tyndalebt.storyproduceradv.R
import org.tyndalebt.storyproduceradv.tools.file.assignNewAudioRelPath
import org.tyndalebt.storyproduceradv.tools.file.getChosenFilename
import org.tyndalebt.storyproduceradv.tools.file.getTempAppendAudioRelPath
import org.tyndalebt.storyproduceradv.tools.media.AudioRecorder
import org.tyndalebt.storyproduceradv.tools.toolbar.MultiRecordRecordingToolbar
import java.io.FileNotFoundException

//Based off of VoiceStudioRecordingToolbar - PROBABLY WILL WORK INCORRECTLY AND MAY OVERWRITE THE VOICE STUDIO AUDIO FILES!!!!!!!!!
/**
 * A class responsible for more advanced recording functionality (allowing multiple recordings to
 * be appended together into one final recording).
 *
 * This class extends the recording, playback, and multi-recording listing functionality of its base
 * classes. A fourth button is added for finishing a set of recordings that are to be appended
 * together. A fifth button is added for sending the finished recording to a web server (not
 * currently implemented).
 */
class DramatizationRecordingToolbar: MultiRecordRecordingToolbar() {
    private lateinit var checkButton: ImageButton
    private lateinit var sendAudioButton: ImageButton //Disable this old sendAudioButton or replace the function for the new "cloud upload thingy"

    private var enableSendAudioButton : Boolean = false

    private var isAppendingOn = false
    private val audioTempName = getTempAppendAudioRelPath()

    override fun onPause() {
        isAppendingOn = false

        super.onPause()
    }

    override fun setupToolbarButtons() {
        super.setupToolbarButtons()

        checkButton = toolbarButton(R.drawable.ic_stop_white_48dp, R.id.finish_recording_button)
        rootView?.addView(checkButton)

        rootView?.addView(toolbarButtonSpace())

        sendAudioButton = toolbarButton(R.drawable.ic_send_audio_48dp, -1) //probably need to remove this or re-adapt functionality
        if(enableSendAudioButton) {
            rootView?.addView(sendAudioButton)

            rootView?.addView(toolbarButtonSpace())
        }
    }

    override fun updateInheritedToolbarButtonVisibility() {
        super.updateInheritedToolbarButtonVisibility()

        if(!isAppendingOn){
            checkButton.visibility = View.INVISIBLE
        }
    }

    override fun showInheritedToolbarButtons() {
        super.showInheritedToolbarButtons()

        checkButton.visibility = View.VISIBLE
        sendAudioButton.visibility = View.VISIBLE
    }

    override fun hideInheritedToolbarButtons() {
        super.hideInheritedToolbarButtons()

        checkButton.visibility = View.INVISIBLE
        sendAudioButton.visibility = View.INVISIBLE
    }

    override fun setToolbarButtonOnClickListeners() {
        super.setToolbarButtonOnClickListeners()

        checkButton.setOnClickListener(checkButtonOnClickListener())
        sendAudioButton.setOnClickListener(sendButtonOnClickListener())
    }


    //Pauses recording if the slide narration is played
    //Some code copied from micButtonOnClickListener()
    override fun stopToolbarMedia() {
        val wasRecording = voiceRecorder?.isRecording == true

        if (wasRecording) {

            stopToolbarVoiceRecording()

            if (isAppendingOn) {
                try {
                    AudioRecorder.concatenateAudioFiles(appContext, getChosenFilename(), audioTempName)
                } catch (e: FileNotFoundException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            } else {
                isAppendingOn = true
            }

            micButton.setBackgroundResource(R.drawable.ic_mic_plus_48dp)
        }
    }


    override fun micButtonOnClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val wasRecording = voiceRecorder?.isRecording == true

            stopToolbarMedia()

            if (!wasRecording) {
                if (isAppendingOn) {
                    recordAudio(audioTempName)
                } else {
                    recordAudio(assignNewAudioRelPath())
                }

                micButton.setBackgroundResource(R.drawable.ic_pause_white_48dp)
                checkButton.visibility = View.VISIBLE
            }
        }
    }

    private fun checkButtonOnClickListener(): View.OnClickListener{
        return View.OnClickListener {
            stopToolbarMedia()

            isAppendingOn = false

            micButton.setBackgroundResource(R.drawable.ic_mic_white_48dp)

            checkButton.visibility = View.INVISIBLE
            sendAudioButton.visibility = View.VISIBLE
        }
    }

    private fun sendButtonOnClickListener(): View.OnClickListener{
        return View.OnClickListener {
            stopToolbarMedia()
        }
    }
}