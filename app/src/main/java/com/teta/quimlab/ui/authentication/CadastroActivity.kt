package com.teta.quimlab.ui.authentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.R

class CadastroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)
        supportActionBar?.hide()
    }
}
