package com.teta.quimlab.ui.comunidade

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.teta.quimlab.CriarPostActivity
import com.teta.quimlab.PostAdapter
import com.teta.quimlab.databinding.FragmentComunidadeBinding

class ComunidadeFragment : Fragment() {

    private var _binding: FragmentComunidadeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComunidadeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configuração do RecyclerView e Adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializando o PostAdapter com um contexto e uma lista vazia
        postAdapter = PostAdapter(requireContext(), emptyList())
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
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val postagens = documents.map { document ->
                    mapOf<String, Any>(
                        "id" to document.id,
                        "titulo" to (document.getString("titulo") ?: ""),
                        "mensagem" to (document.getString("mensagem") ?: ""),
                        "fotoUrl" to (document.getString("fotoUrl") ?: ""),
                        "videoUrl" to (document.getString("videoUrl") ?: ""),
                        "arquivoUrl" to (document.getString("arquivoUrl") ?: ""),
                        "nomeUsuario" to (document.getString("nomeUsuario") ?: "Usuário desconhecido"),
                        "fotoPerfilUrl" to (document.getString("fotoPerfilUrl") ?: ""),
                        "usuarioId" to (document.getString("usuarioId") ?: "")
                    )
                }
                postAdapter.updatePostagens(postagens)
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
