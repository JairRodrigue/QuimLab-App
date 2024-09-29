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
import com.google.firebase.auth.FirebaseAuth
import com.teta.quimlab.MainActivity
import com.teta.quimlab.R
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import com.teta.quimlab.SobreOQuimLabActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var infoSection: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar) // Certifique-se de que a ProgressBar esteja no seu layout

        buttonLogin.setOnClickListener { realizarLogin() }

        infoSection = findViewById(R.id.info_section)
        infoSection.setOnClickListener {
            abrirSobreQuimLab()
        }

        findViewById<TextView>(R.id.create_account).setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        findViewById<TextView>(R.id.forgot_password).setOnClickListener {
            startActivity(Intent(this, EsqueceuSenhaActivity::class.java))
        }

        // Inicia a animação da ProgressBar
        startProgressBarAnimation()
    }

    private fun realizarLogin() {
        val email = editTextEmail.text.toString().trim()
        val senha = editTextPassword.text.toString().trim()

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            mostrarMensagemErro("Por favor, preencha todos os campos.")
            return
        }

        if (!isEmailValido(email)) {
            mostrarMensagemErro("Formato de e-mail inválido.")
            return
        }

        showProgressBar() // Mostra a ProgressBar antes de iniciar o login

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                hideProgressBar() // Esconde a ProgressBar ao finalizar

                if (task.isSuccessful) {
                    val usuario = auth.currentUser
                    if (usuario != null && !usuario.isEmailVerified) {
                        mostrarMensagemErro("Por favor, verifique seu e-mail.")
                    } else {
                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        limparCampos()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    handleLoginError(task.exception?.message)
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

    private fun limparCampos() {
        editTextEmail.text.clear()
        editTextPassword.text.clear()
    }

    private fun abrirSobreQuimLab() {
        startActivity(Intent(this, SobreOQuimLabActivity::class.java))
    }

    private fun handleLoginError(erro: String?) {
        when {
            erro?.contains("no user record") == true -> {
                mostrarMensagemErro("Esse e-mail não está cadastrado.")
            }
            erro?.contains("wrong password") == true -> {
                mostrarMensagemErro("A senha informada está incorreta.")
            }
            erro?.contains("invalid email") == true -> {
                mostrarMensagemErro("O e-mail informado é inválido.")
            }
            erro?.contains("user-disabled") == true -> {
                mostrarMensagemErro("Essa conta está desativada.")
            }
            erro?.contains("operation-not-allowed") == true -> {
                mostrarMensagemErro("Operação não permitida.")
            }
            erro?.contains("user-not-found") == true -> {
                mostrarMensagemErro("Usuário não encontrado.")
            }
            erro?.contains("email already in use") == true -> {
                mostrarMensagemErro("Esse e-mail já está em uso.")
            }
            erro?.contains("too many requests") == true -> {
                mostrarMensagemErro("Muitas tentativas. Tente novamente mais tarde.")
            }
            erro?.contains("requires recent login") == true -> {
                mostrarMensagemErro("É necessário realizar o login novamente.")
            }
            erro?.contains("user-token-expired") == true -> {
                mostrarMensagemErro("Token de usuário expirado.")
            }
            erro?.contains("invalid-credential") == true -> {
                mostrarMensagemErro("Credenciais inválidas.")
            }
            erro?.contains("network request failed") == true -> {
                mostrarMensagemErro("Falha na requisição de rede. Verifique sua conexão.")
            }
            erro?.contains("too many attempts") == true -> {
                mostrarMensagemErro("Muitas tentativas de login. Tente novamente mais tarde.")
            }
            erro?.contains("internal error") == true -> {
                mostrarMensagemErro("Erro interno do servidor.")
            }
            erro?.contains("The supplied auth credential is incorrect, malformed or has expired.") == true -> {
                mostrarMensagemErro("As credenciais fornecidas estão incorretas, malformadas ou expiraram.")
            }
            else -> {
                mostrarMensagemErro(erro ?: "Erro desconhecido.")
            }
        }
    }
}
