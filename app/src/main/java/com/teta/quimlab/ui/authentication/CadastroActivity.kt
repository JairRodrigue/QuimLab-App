package com.teta.quimlab.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teta.quimlab.R
import com.teta.quimlab.SobreOQuimLabActivity
import com.teta.quimlab.Usuario

class CadastroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonSignup: Button
    private lateinit var loginRedirect: TextView
    private lateinit var infoSection: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonSignup = findViewById(R.id.buttonSignup)
        loginRedirect = findViewById(R.id.login_redirect)
        progressBar = findViewById(R.id.progressBar)

        buttonSignup.setOnClickListener { cadastrarUsuario() }

        loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        infoSection = findViewById(R.id.info_section)
        infoSection.setOnClickListener {
            abrirSobreQuimLab()
        }
    }

    private fun cadastrarUsuario() {
        val nome = editTextFullName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val senha = editTextPassword.text.toString().trim()
        val confirmarSenha = editTextConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha) || TextUtils.isEmpty(confirmarSenha)) {
            mostrarMensagemErro("Por favor, preencha todos os campos.")
            return
        }

        if (!isEmailValido(email)) {
            mostrarMensagemErro("E-mail inválido.")
            return
        }

        if (senha.length < 6 || !senha.matches(".*[A-Za-z].*".toRegex()) || !senha.matches(".*[0-9].*".toRegex())) {
            mostrarMensagemErro("A senha deve ter pelo menos 6 caracteres e incluir pelo menos uma letra e um número.")
            return
        }

        if (senha != confirmarSenha) {
            mostrarMensagemErro("As senhas não coincidem.")
            return
        }

        showProgressBar()

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                hideProgressBar()
                if (task.isSuccessful) {
                    val usuario = auth.currentUser
                    usuario?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            // Salvar dados no Firestore
                            salvarDadosNoFirestore(nome, email)
                            Toast.makeText(this, "Cadastro realizado com sucesso! Verifique seu e-mail para ativação.", Toast.LENGTH_SHORT).show()
                            limparCampos()
                        } else {
                            mostrarMensagemErro("Falha ao enviar e-mail de verificação.")
                        }
                    }
                } else {
                    val erro = task.exception?.message
                    when {
                        erro?.contains("email address is already in use") == true -> {
                            mostrarMensagemErro("Esse e-mail já está em uso por outra conta.")
                        }
                        else -> {
                            mostrarMensagemErro(erro ?: "Erro desconhecido.")
                        }
                    }
                }
            }
    }

    private fun salvarDadosNoFirestore(nome: String, email: String) {
        val usuario = Usuario(nome, email)
        db.collection("usuarios")
            .document(auth.currentUser!!.uid)
            .set(usuario)
            .addOnSuccessListener {
                // Dados salvos com sucesso
            }
            .addOnFailureListener { e ->
                mostrarMensagemErro("Falha ao salvar dados: ${e.message}")
            }
    }

    private fun showProgressBar() {
        progressBar.visibility = android.view.View.VISIBLE
        startProgressBarAnimation()
    }

    private fun hideProgressBar() {
        progressBar.visibility = android.view.View.GONE
    }

    private fun startProgressBarAnimation() {
        val colorFrom = ContextCompat.getColor(this, R.color.roxo_padrao)
        val colorTo = ContextCompat.getColor(this, R.color.azul_padrao)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 2000 
        colorAnimation.repeatCount = ValueAnimator.INFINITE
        colorAnimation.repeatMode = ValueAnimator.REVERSE

        colorAnimation.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            progressBar.indeterminateTintList = ColorStateList.valueOf(animatedColor)
        }

        colorAnimation.start()
    }

    private fun limparCampos() {
        editTextFullName.text.clear()
        editTextEmail.text.clear()
        editTextPassword.text.clear()
        editTextConfirmPassword.text.clear()
    }

    private fun mostrarMensagemErro(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
    }

    private fun isEmailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun abrirSobreQuimLab() {
        val intent = Intent(this, SobreOQuimLabActivity::class.java)
        startActivity(intent)
    }
}
