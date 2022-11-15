package dev.wiskiw.bluetoothmiccontol.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.wiskiw.bluetoothmiccontol.R
import dev.wiskiw.bluetoothmiccontol.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainFragment : Fragment(R.layout.fragment_main) {

    companion object {
        fun newInstance() = MainFragment()

        private val LOG_TAG = "BMC"
    }

    private val viewModel: MainViewModel by viewModel()
    private val viewBinding by viewBinding(FragmentMainBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        viewBinding.muteMicSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (compoundButton.isPressed) {
                viewModel.onMuteMicSwitched(isChecked)
            }
        }

        viewBinding.enableVolumeControlSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (compoundButton.isPressed) {
                viewModel.onEnableVolumeControlSwitched(isChecked)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isMicOffFlow
            .onEach { viewBinding.muteMicSwitch.isChecked = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isVolumeMicControlEnabledFlow
            .onEach { viewBinding.enableVolumeControlSwitch.isChecked = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
