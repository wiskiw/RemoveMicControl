package dev.wiskiw.bluetoothmiccontol

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import dev.wiskiw.bluetoothmiccontol.di.viewModel
import dev.wiskiw.bluetoothmiccontol.ui.main.MainFragment


class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MainAct"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        when (keyCode) {
//            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> viewModel.onDeviceVolumeClicked()
//        }

        return super.onKeyDown(keyCode, event)
    }
}