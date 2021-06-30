package com.sayt.godslove.recording.about

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.sayt.godslove.R
import com.sayt.godslove.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentPrivacyPolicyBinding.inflate(inflater, container, false).run {
            wvPrivacyPolicy.loadUrl("file:///android_asset/privacy_policy.html")
            root
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.privacy_policy, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.open_website) {
            CustomTabsIntent.Builder().build().launchUrl(
                requireContext(),
                Uri.parse(getString(R.string.privacy_policy_website))
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
