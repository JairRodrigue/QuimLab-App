package com.teta.quimlab.ui.inicio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.R

class PesquisaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pesquisa)

        // Ativar o botão de voltar na ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Definir o título da Activity
        supportActionBar?.title = " "
    }

    // Configurar o comportamento do botão de voltar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // Retorna à Activity anterior
        return true
    }
}

