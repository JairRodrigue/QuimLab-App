package com.teta.quimlab.ui.inicio

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.R

class TextoDetectadoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texto_detectado)

        // Ativar o botão de voltar na ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Mudar o nome da ActionBar
        supportActionBar?.title = " "

        // Pegue o texto que foi passado pela Intent
        val detectedText = intent.getStringExtra("DETECTED_TEXT")

        // Encontre o TextView no layout e exiba o texto
        val textView: TextView = findViewById(R.id.detected_text_view)
        textView.text = detectedText
    }

    // Trata o clique no botão de voltar da ActionBar
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            // Finaliza a Activity e retorna para a anterior
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
