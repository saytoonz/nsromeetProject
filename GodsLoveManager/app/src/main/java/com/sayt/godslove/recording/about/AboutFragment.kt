package com.sayt.godslove.recording.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.sayt.godslove.BuildConfig
import com.sayt.godslove.R
import com.sayt.godslove.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentAboutBinding.inflate(inflater, container, false).run {
            version.text = getString(R.string.version, BuildConfig.VERSION_NAME)
            sourceCode.setOnClickListener {
                CustomTabsIntent.Builder().build().launchUrl(
                    requireContext(),
                    Uri.parse(requireContext().getString(R.string.app_source_code))
                )
            }
            sendFeedback.setOnClickListener {
                sendFeedback()
            }
            privacyPolicy.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_about_to_privacy_policy)
            )
            licenses.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_about_to_licenses)
            )
            root
        }
    }

    private fun sendFeedback() {
        val address = getString(R.string.author_email)
        val subject = getString(R.string.feedback_subject)

        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

        val chooserTitle = getString(R.string.feedback_chooser_title)
        startActivity(Intent.createChooser(emailIntent, chooserTitle))
    }
}
