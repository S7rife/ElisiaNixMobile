package ru.feip.elisianix.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.feip.elisianix.R

class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}