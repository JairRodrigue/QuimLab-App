package com.teta.quimlab.ui.authentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.R

class EsqueceuSenhaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esqueceu_senha)
        supportActionBar?.hide()
    }
}
