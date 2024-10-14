package com.teta.quimlab

data class Postagem(
    val id: String = "",
    val titulo: String = "",
    val mensagem: String = "",
    val fotoUrl: String? = null,
    val videoUrl: String? = null,
    val arquivoUrl: String? = null
)
