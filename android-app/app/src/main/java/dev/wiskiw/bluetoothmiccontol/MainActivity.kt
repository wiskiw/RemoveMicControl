package dev.wiskiw.bluetoothmiccontol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.wiskiw.bluetoothmiccontol.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}