package com.teta.quimlab.ui.authentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.R

class SobreOQuimLabActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sobre_o_quimlab)
        supportActionBar?.hide()
    }
}
