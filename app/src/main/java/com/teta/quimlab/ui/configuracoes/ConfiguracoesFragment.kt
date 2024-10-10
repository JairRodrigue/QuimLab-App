package com.teta.quimlab.ui.configuracoes

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.teta.quimlab.PerfilActivity
import com.teta.quimlab.R
import com.teta.quimlab.SobreOQuimLabActivity  // Import your new activity here
import com.teta.quimlab.databinding.FragmentConfiguracoesBinding
import com.teta.quimlab.ui.authentication.LoginActivity

class ConfiguracoesFragment : Fragment() {

    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracoesBinding.inflate(inflater, container, false)

        binding.irParaPerfil.setOnClickListener {
            startActivity(Intent(requireActivity(), PerfilActivity::class.java))
        }

        binding.optionLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.redefinirSenha.setOnClickListener {
            showPasswordResetDialog()
        }

        binding.optionAbout.setOnClickListener {  // Add this listener
            startActivity(Intent(requireActivity(), SobreOQuimLabActivity::class.java))
        }

        return binding.root
    }

    // Função para mostrar o diálogo de confirmação de logout
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)

        builder.setTitle("Confirmar Logout")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sim") { _: DialogInterface, _: Int -> performLogout() }
            .setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.roxo_padrao))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            dialog.findViewById<TextView>(resources.getIdentifier("alertTitle", "id", requireContext().packageName))?.setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
        }

        dialog.show()
    }

    // Função para realizar o logout
    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    // Função para mostrar o diálogo de redefinição de senha
    private fun showPasswordResetDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        builder.setTitle("Redefinir Senha")

        // Criação do campo de input para o e-mail
        val input = EditText(requireContext()).apply {
            hint = "Digite seu e-mail"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            setHintTextColor(ContextCompat.getColor(requireContext(), R.color.branco_transparente))
        }
        builder.setView(input)

        // Configuração dos botões
        builder.setPositiveButton("Enviar") { _: DialogInterface, _: Int ->
            val email = input.text.toString()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(requireContext(), "Por favor, insira um e-mail", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val dialog = builder.create()

        // Customizando o estilo da caixa de diálogo
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.roxo_padrao))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            input.setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            input.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.branco_transparente))
            dialog.window?.setBackgroundDrawableResource(R.color.preto3)
        }

        dialog.show()
    }

    // Função para enviar o e-mail de redefinição de senha
    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "E-mail de redefinição enviado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Falha ao enviar o e-mail. Verifique o e-mail inserido.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}