package com.teta.quimlab.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.teta.quimlab.R

class EsqueceuSenhaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSendEmail: Button
    private lateinit var loginRedirect: TextView
    private lateinit var infoSection: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esqueceu_senha)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()


        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSendEmail = findViewById(R.id.buttonSendEmail)


        buttonSendEmail.setOnClickListener { enviarEmailParaRecuperacao() }
        infoSection = findViewById(R.id.info_section)


        infoSection.setOnClickListener {
            abrirSobreQuimLab()
        }

        val loginRedirectTextView = findViewById<TextView>(R.id.login_redirect)
        loginRedirectTextView.setOnClickListener {
            finish()
        }
    }

    private fun enviarEmailParaRecuperacao() {
        val email = editTextEmail.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            mostrarMensagemErro("Por favor, preencha o e-mail.")
            return
        }

        if (!isEmailValido(email)) {
            mostrarMensagemErro("Formato de e-mail inválido.")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "E-mail de recuperação enviado!", Toast.LENGTH_SHORT).show()
                    limparCampos()
                } else {
                    val erro = task.exception?.message
                    mostrarMensagemErro(traduzirErro(erro))
                }
            }
    }

    private fun isEmailValido(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun mostrarMensagemErro(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
    }

    private fun traduzirErro(erro: String?): String {
        return when {
            erro == null -> "Erro desconhecido."
            erro.contains("There is no user record corresponding to this identifier") -> "Esse e-mail não está cadastrado."
            erro.contains("invalid-email") -> "E-mail inválido."
            erro.contains("user-not-found") -> "Usuário não encontrado."
            else -> "$erro"
        }
    }

    private fun limparCampos() {
        editTextEmail.text.clear()
    }
    private fun abrirSobreQuimLab() {
        val intent = Intent(this, SobreOQuimLabActivity::class.java)
        startActivity(intent)
    }
}
