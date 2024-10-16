package com.teta.quimlab

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private val context: Context, private var postagens: List<Map<String, Any>>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_postagem, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val postagem = postagens[position]
        holder.bind(postagem)
    }

    override fun getItemCount(): Int {
        return postagens.size
    }

    // Método para atualizar a lista de postagens e notificar o RecyclerView sobre as mudanças
    fun updatePostagens(novasPostagens: List<Map<String, Any>>) {
        this.postagens = novasPostagens
        notifyDataSetChanged()
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeUsuarioTextView: TextView = itemView.findViewById(R.id.nome_perfil)
        private val fotoPerfilImageView: ImageView = itemView.findViewById(R.id.foto_perfil)
        private val tituloTextView: TextView = itemView.findViewById(R.id.tituloTextView)
        private val mensagemTextView: TextView = itemView.findViewById(R.id.mensagemTextView)

        fun bind(postagem: Map<String, Any>) {
            val titulo = postagem["titulo"] as? String ?: "Sem título"
            val mensagem = postagem["mensagem"] as? String ?: "Sem mensagem"
            val usuarioId = postagem["usuarioId"] as? String

            tituloTextView.text = titulo
            mensagemTextView.text = mensagem

            // Busca os dados do usuário diretamente do Firestore
            if (usuarioId != null) {
                carregarDadosDoUsuario(usuarioId)
            } else {
                nomeUsuarioTextView.text = "Usuário desconhecido"
                fotoPerfilImageView.setImageResource(R.drawable.user_icon)
            }
        }

        private fun carregarDadosDoUsuario(usuarioId: String) {
            firestore.collection("usuarios").document(usuarioId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nomeUsuario = document.getString("nome") ?: "Usuário desconhecido"
                        val fotoPerfilUrl = document.getString("imageUrl")

                        nomeUsuarioTextView.text = nomeUsuario
                        carregarImagemDePerfil(fotoPerfilUrl)
                    } else {
                        nomeUsuarioTextView.text = "Usuário desconhecido"
                        fotoPerfilImageView.setImageResource(R.drawable.user_icon)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PostAdapter", "Erro ao carregar dados do usuário: ", exception)
                    nomeUsuarioTextView.text = "Erro ao carregar usuário"
                    fotoPerfilImageView.setImageResource(R.drawable.user_icon)
                }
        }

        private fun carregarImagemDePerfil(fotoPerfilUrl: String?) {
            if (!fotoPerfilUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(fotoPerfilUrl)
                    .placeholder(R.drawable.user_icon)
                    .error(R.drawable.user_icon)
                    .into(fotoPerfilImageView)
            } else {
                fotoPerfilImageView.setImageResource(R.drawable.user_icon)
            }
        }
    }
}
