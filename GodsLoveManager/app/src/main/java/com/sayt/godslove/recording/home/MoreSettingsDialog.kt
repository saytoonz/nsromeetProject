package com.sayt.godslove.recording.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sayt.godslove.R
import com.sayt.godslove.databinding.FragmentMoreSettingsBinding
import com.sayt.godslove.recording.settings.PreferenceHelper

class MoreSettingsDialog : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentMoreSettingsBinding.inflate(inflater, container, false).run {
            val prefs = PreferenceHelper(requireContext())
            navigationView.menu.findItem(
                when (prefs.sortBy) {
                    PreferenceHelper.SortBy.NAME -> R.id.menu_sort_by_name
                    PreferenceHelper.SortBy.DATE -> R.id.menu_sort_by_last_edit
                    PreferenceHelper.SortBy.SIZE -> R.id.menu_sort_by_size
                    PreferenceHelper.SortBy.DURATION -> TODO()
                }
            ).isChecked = true
            navigationView.menu.findItem(
                when (prefs.orderBy) {
                    PreferenceHelper.OrderBy.ASCENDING -> R.id.menu_order_ascending
                    PreferenceHelper.OrderBy.DESCENDING -> R.id.menu_order_descending
                }
            ).isChecked = true
            navigationView.setNavigationItemSelectedListener {
                when (it.groupId) {
                    R.id.group_sort_by -> {
                        prefs.sortBy = when (it.itemId) {
                            R.id.menu_sort_by_name -> PreferenceHelper.SortBy.NAME
                            R.id.menu_sort_by_last_edit -> PreferenceHelper.SortBy.DATE
                            R.id.menu_sort_by_size -> PreferenceHelper.SortBy.SIZE
                            else -> throw IllegalArgumentException("Unknown sort option.")
                        }
                    }
                    R.id.group_order_by -> {
                        prefs.orderBy = when (it.itemId) {
                            R.id.menu_order_ascending -> PreferenceHelper.OrderBy.ASCENDING
                            R.id.menu_order_descending -> PreferenceHelper.OrderBy.DESCENDING
                            else -> throw IllegalArgumentException("Unknown order option.")
                        }
                    }
                }
                dismiss()
                true
            }
            root
        }
    }
}