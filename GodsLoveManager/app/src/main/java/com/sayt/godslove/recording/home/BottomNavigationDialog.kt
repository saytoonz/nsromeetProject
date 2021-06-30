package com.sayt.godslove.recording.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sayt.godslove.databinding.FragmentBottomNavDrawerBinding

class BottomNavigationDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentBottomNavDrawerBinding.inflate(inflater, container, false).run {
            navigationView.setNavigationItemSelectedListener {
                dismiss()
                NavigationUI.onNavDestinationSelected(it, findNavController())
            }
            root
        }
    }
}