package com.teta.quimlab

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private val context: Context, private var postagens: List<Map<String, Any>>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

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

    fun updatePostagens(novasPostagens: List<Map<String, Any>>) {
        this.postagens = novasPostagens
        notifyDataSetChanged()
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeUsuarioTextView: TextView = itemView.findViewById(R.id.nome_perfil)
        private val fotoPerfilImageView: ImageView = itemView.findViewById(R.id.foto_perfil)
        private val tituloTextView: TextView = itemView.findViewById(R.id.tituloTextView)
        private val mensagemTextView: TextView = itemView.findViewById(R.id.mensagemTextView)
        private val imagemView: ImageView = itemView.findViewById(R.id.imagemView)
        private val arquivoTextView: TextView = itemView.findViewById(R.id.arquivoTextView)
        private val btnLike: ImageButton = itemView.findViewById(R.id.btn_like)
        private val likeCountTextView: TextView = itemView.findViewById(R.id.like_count)
        private val btnComentario: ImageButton = itemView.findViewById(R.id.btn_comentario)
        private val entrarPerfilLayout: View = itemView.findViewById(R.id.entrar_perfil)

        private var likeCount = 0
        private var isLiked = false
        private var postId: String? = null

        fun bind(postagem: Map<String, Any>) {
            val titulo = postagem["titulo"] as? String ?: "Sem título"
            val mensagem = postagem["mensagem"] as? String ?: "Sem mensagem"
            val usuarioId = postagem["usuarioId"] as? String
            val fotoUrl = postagem["fotoUrl"] as? String
            val videoUrl = postagem["videoUrl"] as? String
            val arquivoUrl = postagem["arquivoUrl"] as? String
            postId = postagem["id"] as? String

            tituloTextView.text = titulo
            mensagemTextView.text = mensagem

            carregarStatusCurtida()

            if (!fotoUrl.isNullOrEmpty()) {
                imagemView.visibility = View.VISIBLE
                Glide.with(context).load(fotoUrl).into(imagemView)
                imagemView.setOnClickListener {
                    val intent =
                        Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(fotoUrl), "image/*")
                    context.startActivity(intent)
                }
            } else if (!videoUrl.isNullOrEmpty()) {
                imagemView.visibility = View.VISIBLE
                Glide.with(context).load(videoUrl).into(imagemView)
                imagemView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).setDataAndType(
                        Uri.parse(videoUrl), "video/*"
                    )
                    context.startActivity(intent)
                }
            } else {
                imagemView.visibility = View.GONE
            }

            if (!arquivoUrl.isNullOrEmpty()) {
                arquivoTextView.visibility = View.VISIBLE
                arquivoTextView.text = "Arquivo disponível: clique para abrir"
                arquivoTextView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(arquivoUrl))
                    context.startActivity(intent)
                }
            } else {
                arquivoTextView.visibility = View.GONE
            }

            btnLike.setOnClickListener {
                if (isLiked) {
                    descurtirPostagem()
                } else {
                    curtirPostagem()
                }
            }

            if (usuarioId != null) {
                carregarDadosDoUsuario(usuarioId)
            } else {
                nomeUsuarioTextView.text = "Usuário desconhecido"
                fotoPerfilImageView.setImageResource(R.drawable.user_icon)
            }

            entrarPerfilLayout.setOnClickListener {
                val intent = Intent(context, PerfilPublicoActivity::class.java).apply {
                    putExtra("usuarioId", usuarioId)
                    putExtra("nomeUsuario", nomeUsuarioTextView.text.toString())
                }
                context.startActivity(intent)
            }
        }

        private fun carregarStatusCurtida() {
            postId?.let { id ->
                firestore.collection("postagens").document(id).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val curtidas = document.getLong("curtidas") ?: 0
                            val usuariosQueCurtiram =
                                document.get("usuariosQueCurtiram") as? List<String> ?: emptyList()
                            likeCount = curtidas.toInt()
                            isLiked = currentUserId in usuariosQueCurtiram

                            likeCountTextView.text = likeCount.toString()
                            btnLike.setImageResource(if (isLiked) R.drawable.like else R.drawable.like_nc)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PostAdapter", "Erro ao carregar curtidas: ", exception)
                    }
            }
        }

        private fun curtirPostagem() {
            postId?.let { id ->
                firestore.collection("postagens").document(id)
                    .update(
                        "curtidas", FieldValue.increment(1),
                        "usuariosQueCurtiram", FieldValue.arrayUnion(currentUserId)
                    )
                    .addOnSuccessListener {
                        likeCount++
                        isLiked = true
                        likeCountTextView.text = likeCount.toString()
                        btnLike.setImageResource(R.drawable.like)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PostAdapter", "Erro ao curtir postagem: ", exception)
                    }
            }
        }

        private fun descurtirPostagem() {
            postId?.let { id ->
                firestore.collection("postagens").document(id)
                    .update(
                        "curtidas", FieldValue.increment(-1),
                        "usuariosQueCurtiram", FieldValue.arrayRemove(currentUserId)
                    )
                    .addOnSuccessListener {
                        likeCount--
                        isLiked = false
                        likeCountTextView.text = likeCount.toString()
                        btnLike.setImageResource(R.drawable.like_nc)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PostAdapter", "Erro ao descurtir postagem: ", exception)
                    }
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
                Glide.with(context).load(fotoPerfilUrl).placeholder(R.drawable.user_icon)
                    .error(R.drawable.user_icon).into(fotoPerfilImageView)
            } else {
                fotoPerfilImageView.setImageResource(R.drawable.user_icon)
            }
        }
    }
}
