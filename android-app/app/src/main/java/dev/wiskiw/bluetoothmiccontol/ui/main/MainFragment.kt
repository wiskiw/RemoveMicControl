package dev.wiskiw.bluetoothmiccontol.ui.main

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.wiskiw.bluetoothmiccontol.R
import dev.wiskiw.bluetoothmiccontol.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()
    private val viewBinding by viewBinding(FragmentMainBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val audioManager: AudioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        viewBinding.muteMicSwitch.isChecked = audioManager.isMicrophoneMute

        viewBinding.muteMicSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            viewModel.onMicSwitched(isChecked)
            audioManager.isMicrophoneMute = isChecked
        }
    }
}
