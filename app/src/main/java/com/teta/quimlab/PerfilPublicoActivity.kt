package com.teta.quimlab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.databinding.ActivityPerfilPublicoBinding

class PerfilPublicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilPublicoBinding
    private lateinit var usuarioId: String
    private lateinit var nomeUsuario: String
    private lateinit var emailUsuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilPublicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        // Implementar a lógica para carregar os posts do usuário do Firestore e exibi-los na UI
    }
}
