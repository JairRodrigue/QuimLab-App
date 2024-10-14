package com.teta.quimlab

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostAdapter : ListAdapter<Postagem, PostAdapter.PostViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_postagem, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val postagem = getItem(position)
        holder.bind(postagem)
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tituloTextView: TextView = itemView.findViewById(R.id.tituloTextView)
        private val mensagemTextView: TextView = itemView.findViewById(R.id.mensagemTextView)
        private val imagemView: ImageView = itemView.findViewById(R.id.imagemView)
        private val arquivoTextView: TextView = itemView.findViewById(R.id.arquivoTextView)

        fun bind(postagem: Postagem) {
            tituloTextView.text = postagem.titulo
            mensagemTextView.text = postagem.mensagem

            // Tratamento da imagem ou vídeo
            if (!postagem.fotoUrl.isNullOrEmpty()) {
                imagemView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(postagem.fotoUrl)
                    .into(imagemView)

                imagemView.setOnClickListener {
                    // Inicia um visualizador de imagem externo para abrir a imagem em tela cheia
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(postagem.fotoUrl), "image/*")
                    itemView.context.startActivity(intent)
                }
            } else if (!postagem.videoUrl.isNullOrEmpty()) {
                imagemView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(postagem.videoUrl)  // Exibe uma miniatura para o vídeo
                    .into(imagemView)

                imagemView.setOnClickListener {
                    // Inicia um player de vídeo externo para reproduzir o vídeo
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(postagem.videoUrl))
                    intent.setDataAndType(Uri.parse(postagem.videoUrl), "video/*")
                    itemView.context.startActivity(intent)
                }
            } else {
                imagemView.visibility = View.GONE
            }

            // Tratamento do arquivo
            if (!postagem.arquivoUrl.isNullOrEmpty()) {
                arquivoTextView.visibility = View.VISIBLE
                arquivoTextView.text = "Arquivo disponível: clique para abrir"

                arquivoTextView.setOnClickListener {
                    // Abre o arquivo usando um aplicativo adequado
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(postagem.arquivoUrl))
                    itemView.context.startActivity(intent)
                }
            } else {
                arquivoTextView.visibility = View.GONE
            }
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<Postagem>() {
        override fun areItemsTheSame(oldItem: Postagem, newItem: Postagem): Boolean {
            return oldItem.titulo == newItem.titulo
        }

        override fun areContentsTheSame(oldItem: Postagem, newItem: Postagem): Boolean {
            return oldItem == newItem
        }
    }
}
