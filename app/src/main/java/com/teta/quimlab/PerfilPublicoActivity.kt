package com.teta.quimlab

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.teta.quimlab.databinding.ActivityPerfilPublicoBinding

class PerfilPublicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilPublicoBinding
    private lateinit var usuarioId: String
    private lateinit var nomeUsuario: String
    private lateinit var emailUsuario: String
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilPublicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Captura os dados da Intent
        usuarioId = intent.getStringExtra("usuarioId") ?: ""
        nomeUsuario = intent.getStringExtra("nomeUsuario") ?: "Usuário Desconhecido"
        emailUsuario = intent.getStringExtra("emailUsuario") ?: "Email Desconhecido"

        // Atualiza a UI com os dados do usuário
        binding.nomeUser.text = nomeUsuario
        binding.emailUser.text = emailUsuario

        // Carrega a imagem de perfil e outros posts do usuário usando o usuarioId
        carregarDadosDoUsuario(usuarioId)
    }

    private fun carregarDadosDoUsuario(usuarioId: String) {
        val userDocRef = firestore.collection("users").document(usuarioId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                // Atualiza nome e e-mail caso estejam no Firestore
                val nome = document.getString("nome") ?: nomeUsuario
                val email = document.getString("email") ?: emailUsuario

                // Atualiza na UI
                binding.nomeUser.text = nome
                binding.emailUser.text = email

                // Carrega a imagem de perfil se disponível
                val profileImageUrl = document.getString("profileImageURL")
                if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.user_icon) // placeholder opcional
                        .into(binding.userIcon)
                } else {
                    // Caso a URL não esteja disponível, pode exibir uma imagem padrão ou deixar em branco
                    Glide.with(this)
                        .load(R.drawable.user_icon) // imagem padrão
                        .into(binding.userIcon)
                }
            } else {
                // Caso o documento não seja encontrado
                showToast("Usuário não encontrado.")
            }
        }.addOnFailureListener { exception ->
            // Em caso de falha na leitura do documento
            showToast("Erro ao carregar dados: ${exception.message}")
        }
    }

    // Função auxiliar para mostrar Toasts
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
