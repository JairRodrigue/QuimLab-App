package com.teta.quimlab.ui.comunidade

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.teta.quimlab.databinding.FragmentComunidadeBinding
import com.teta.quimlab.CriarPostActivity

class ComunidadeFragment : Fragment() {

    private var _binding: FragmentComunidadeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(ComunidadeViewModel::class.java)

        _binding = FragmentComunidadeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }


        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), CriarPostActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
