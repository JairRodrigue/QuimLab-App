package com.teta.quimlab

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.teta.quimlab.databinding.ActivityPerfilPublicoBinding

class PerfilPublicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilPublicoBinding
    private lateinit var usuarioId: String
    private val firestore = Firebase.firestore
    private lateinit var postAdapter: PostAdapter // Adapter para as postagens

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilPublicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Perfil Público"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Captura o ID do usuário da Intent
        usuarioId = intent.getStringExtra("usuarioId") ?: ""

        // Configura o RecyclerView
        configurarRecyclerView()

        // Carrega os dados do usuário
        carregarDadosDoUsuario(usuarioId)

        // Carrega as postagens do usuário
        carregarPostagensDoUsuario(usuarioId)
    }

    private fun configurarRecyclerView() {
        postAdapter = PostAdapter(this, emptyList()) // Inicializa o adapter
        binding.recyclerViewPerfil.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPerfil.adapter = postAdapter
    }

    private fun carregarDadosDoUsuario(usuarioId: String) {
        val userDocRef = firestore.collection("usuarios").document(usuarioId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                // Obtém e atualiza o nome e o email
                val nome = document.getString("nome") ?: "Usuário Desconhecido"
                val email = document.getString("email") ?: "Email Desconhecido"

                binding.nomeUser.text = nome
                binding.emailUser.text = email

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

    private fun carregarPostagensDoUsuario(usuarioId: String) {
        firestore.collection("postagens")
            .whereEqualTo("usuarioId", usuarioId) // Filtra as postagens pelo ID do usuário
            .get()
            .addOnSuccessListener { documents ->
                val postagens = mutableListOf<Map<String, Any>>()
                for (document in documents) {
                    val postagem = document.data
                    postagem["id"] = document.id // Adiciona o ID da postagem
                    postagens.add(postagem)
                }
                postAdapter.updatePostagens(postagens) // Atualiza o adapter com as postagens
            }
            .addOnFailureListener { exception ->
                showToast("Erro ao carregar postagens: ${exception.message}")
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