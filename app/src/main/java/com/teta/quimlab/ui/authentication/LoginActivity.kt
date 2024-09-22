package com.teta.quimlab.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        // Configurando os listeners para as TextViews
        val createAccountTextView = findViewById<TextView>(R.id.create_account)
        createAccountTextView.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        val forgotPasswordTextView = findViewById<TextView>(R.id.forgot_password)
        forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, EsqueceuSenhaActivity::class.java)
            startActivity(intent)
        }

        val infoSection = findViewById<LinearLayout>(R.id.info_section)
        infoSection.setOnClickListener {
            val intent = Intent(this, SobreOQuimLabActivity::class.java) // Crie esta Activity
            startActivity(intent)
        }
    }
}
