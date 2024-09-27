package com.teta.quimlab.ui.authentication

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.teta.quimlab.R

class CadastroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonSignup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()


        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonSignup = findViewById(R.id.buttonSignup)


        buttonSignup.setOnClickListener { cadastrarUsuario() }
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


        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cadastro realizado com sucesso! Verifique seu e-mail para ativação.", Toast.LENGTH_SHORT).show()
                    limparCampos()
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
}
