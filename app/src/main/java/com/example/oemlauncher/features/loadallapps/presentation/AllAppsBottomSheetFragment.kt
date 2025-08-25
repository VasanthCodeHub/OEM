package com.example.oemlauncher.features.loadallapps.presentation

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.oemlauncher.R
import com.example.oemlauncher.databinding.FragmentAllAppsBinding
import com.example.oemlauncher.features.loadallapps.adapter.AppAdapter
import com.example.oemlauncher.features.loadallapps.data.Launchable
import com.example.oemlauncher.features.loadallapps.helper.ContextMenuHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AllAppsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAllAppsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoadAllAppsViewModel by activityViewModels()
    private lateinit var allAdapter: AppAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dlg ->
            val bottomSheet =
                (dlg as BottomSheetDialog).findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) ?: return@setOnShowListener

            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = true
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allAdapter = AppAdapter(requireContext(), emptyList(), onLongPress = { app, anchor ->
            ContextMenuHelper.showContextMenu(anchor, app, viewModel)
        })

        binding.apps.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = allAdapter
        }

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setQuery(s?.toString().orEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        observeVM()
    }

    private fun observeVM() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredApps.collectLatest { allAdapter.submitList(it) }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
