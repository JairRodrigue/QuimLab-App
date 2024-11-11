package com.teta.quimlab

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.teta.quimlab.databinding.ActivityPerfilPublicoBinding

class PerfilPublicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilPublicoBinding
    private lateinit var usuarioId: String
    private lateinit var nomeUsuario: String
    private lateinit var emailUsuario: String
    private val firestore = FirebaseFirestore.getInstance()

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

        // Carregar outros posts do usuário usando o usuarioId
        carregarPostsDoUsuario(usuarioId)
    }

    private fun carregarPostsDoUsuario(usuarioId: String) {
        val db = Firebase.firestore
        val userDocRef = db.collection("users").document(usuarioId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                binding.nomeUser.text = document.getString("nome")
                binding.emailUser.text = document.getString("email")

                // Carregar imagem de perfil
                val profileImageUrl = document.getString("profileImageURL")
                Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.user_icon) // placeholder opcional
                    .into(binding.userIcon)
            }
        }
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
