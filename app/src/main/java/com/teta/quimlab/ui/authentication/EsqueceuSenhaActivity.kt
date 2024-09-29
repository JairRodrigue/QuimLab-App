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
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import com.google.firebase.auth.FirebaseAuth
import com.teta.quimlab.R
import com.teta.quimlab.SobreOQuimLabActivity

class EsqueceuSenhaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSendEmail: Button
    private lateinit var infoSection: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esqueceu_senha)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSendEmail = findViewById(R.id.buttonSendEmail)
        progressBar = findViewById(R.id.progressBar) // Certifique-se de que a ProgressBar esteja no seu layout

        buttonSendEmail.setOnClickListener { enviarEmailParaRecuperacao() }
        infoSection = findViewById(R.id.info_section)

        infoSection.setOnClickListener {
            abrirSobreQuimLab()
        }

        val loginRedirectTextView = findViewById<TextView>(R.id.login_redirect)
        loginRedirectTextView.setOnClickListener {
            finish()
        }

        startProgressBarAnimation()
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

        showProgressBar() // Mostra a ProgressBar antes de enviar o e-mail

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                hideProgressBar() // Esconde a ProgressBar ao finalizar

                if (task.isSuccessful) {
                    Toast.makeText(this, "E-mail de recuperação enviado!", Toast.LENGTH_SHORT).show()
                    limparCampos()
                } else {
                    val erro = task.exception?.message
                    mostrarMensagemErro(traduzirErro(erro))
                }
            }
    }

    private fun startProgressBarAnimation() {
        val colorFrom = ContextCompat.getColor(this, R.color.roxo_padrao)
        val colorTo = ContextCompat.getColor(this, R.color.azul_padrao)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 2000 // Duração da animação
        colorAnimation.repeatCount = ValueAnimator.INFINITE
        colorAnimation.repeatMode = ValueAnimator.REVERSE

        colorAnimation.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            progressBar.indeterminateTintList = ColorStateList.valueOf(animatedColor)
        }

        colorAnimation.start()
    }

    private fun showProgressBar() {
        progressBar.visibility = android.view.View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = android.view.View.GONE
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
        startActivity(Intent(this, SobreOQuimLabActivity::class.java))
    }
}
