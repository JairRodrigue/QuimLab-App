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
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilPublicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Perfil Público"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Captura o ID do usuário da Intent
        usuarioId = intent.getStringExtra("usuarioId") ?: ""

        // Carrega os dados do usuário
        carregarDadosDoUsuario(usuarioId)
    }

    private fun carregarDadosDoUsuario(usuarioId: String) {
        val userDocRef = firestore.collection("usuarios").document(usuarioId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                // Obtém e atualiza o nome e o email
                val nome = document.getString("nome") ?: "Usuário Desconhecido"
                val email = document.getString("email") ?: "Email Desconhecido"

                binding.nomeUser .text = nome
                binding.emailUser .text = email

                // Carrega a imagem de perfil se disponível
                val profileImageUrl = document.getString("imageUrl")
                carregarImagemDePerfil(profileImageUrl)
            } else {
                // Caso o documento não seja encontrado
                showToast("Usuário não encontrado.")
            }
        }.addOnFailureListener { exception ->
            // Em caso de falha na leitura do documento
            showToast("Erro ao carregar dados: ${exception.message}")
        }
    }

    private fun carregarImagemDePerfil(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.userIcon)
        } else {
            // Caso a URL não esteja disponível, exibe uma imagem padrão
            Glide.with(this)
                .load(R.drawable.user_icon) // Imagem padrão
                .into(binding.userIcon)
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