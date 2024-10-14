package com.teta.quimlab.ui.comunidade

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.teta.quimlab.CriarPostActivity
import com.teta.quimlab.PostAdapter
import com.teta.quimlab.Postagem
import com.teta.quimlab.databinding.FragmentComunidadeBinding

class ComunidadeFragment : Fragment() {

    private var _binding: FragmentComunidadeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComunidadeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configuração do RecyclerView e Adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter()
        binding.recyclerView.adapter = postAdapter

        // Carregar postagens do Firestore
        carregarPostagens()

        // Configurar ação do FAB (Floating Action Button) para criar um novo post
        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), CriarPostActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    private fun carregarPostagens() {
        firestore.collection("postagens")
            .get()
            .addOnSuccessListener { documents ->
                val postagens = documents.map { document ->
                    Postagem(
                        id = document.id, // Atribuindo o ID do documento à postagem
                        titulo = document.getString("titulo") ?: "",
                        mensagem = document.getString("mensagem") ?: "",
                        fotoUrl = document.getString("fotoUrl"),
                        videoUrl = document.getString("videoUrl"),
                        arquivoUrl = document.getString("arquivoUrl")
                    )
                }
                postAdapter.submitList(postagens)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Erro ao carregar postagens: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
