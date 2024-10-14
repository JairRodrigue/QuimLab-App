package com.teta.quimlab

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

        fun bind(postagem: Postagem) {
            tituloTextView.text = postagem.titulo
            mensagemTextView.text = postagem.mensagem

            if (!postagem.fotoUrl.isNullOrEmpty()) {
                imagemView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(postagem.fotoUrl)
                    .into(imagemView)
            } else {
                imagemView.visibility = View.GONE
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
