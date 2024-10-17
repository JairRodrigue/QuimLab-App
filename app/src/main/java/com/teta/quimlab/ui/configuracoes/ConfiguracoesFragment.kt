package com.teta.quimlab.ui.configuracoes

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teta.quimlab.PerfilActivity
import com.teta.quimlab.R
import com.teta.quimlab.SobreOQuimLabActivity
import com.teta.quimlab.databinding.FragmentConfiguracoesBinding
import com.teta.quimlab.ui.authentication.LoginActivity

class ConfiguracoesFragment : Fragment() {

    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracoesBinding.inflate(inflater, container, false)

        // Carregar imagem do usuário
        loadUserImage()

        binding.irParaPerfil.setOnClickListener {
            startActivity(Intent(requireActivity(), PerfilActivity::class.java))
        }

        binding.optionLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.redefinirSenha.setOnClickListener {
            showPasswordResetDialog()
        }

        binding.optionAbout.setOnClickListener {
            startActivity(Intent(requireActivity(), SobreOQuimLabActivity::class.java))
        }

        // Configurar botão de exclusão de conta
        binding.optionDeleteAccount1.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        return binding.root
    }

    private fun loadUserImage() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            firestore.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("imageUrl")) {
                        val imageUrl = document.getString("imageUrl")
                        val userIcon = binding.root.findViewById<ImageView>(R.id.user_icon)
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.user_icon) // Ícone padrão enquanto carrega
                            .error(R.drawable.user_icon) // Ícone padrão se houver erro
                            .into(userIcon)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Erro ao carregar a imagem do perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)

        builder.setTitle("Confirmar Logout")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sim") { _: DialogInterface, _: Int -> performLogout() }
            .setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.roxo_padrao))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            dialog.findViewById<TextView>(android.R.id.message)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            dialog.findViewById<TextView>(resources.getIdentifier("alertTitle", "id", requireContext().packageName))
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
        }

        dialog.show()
    }

    private fun performLogout() {
        firebaseAuth.signOut()
        startActivity(Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    private fun showDeleteAccountConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)

        builder.setTitle("Informação Importante")
            .setMessage("Para excluir sua conta, você deve ter feito login recentemente. Deseja continuar?")
            .setPositiveButton("Excluir Conta") { dialog: DialogInterface, _: Int ->
                showFinalConfirmationDialog()
            }
            .setNegativeButton("Cancelar") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.roxo_padrao))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
        }

        dialog.show()
    }

    private fun showFinalConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)

        builder.setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita.")
            .setPositiveButton("Sim") { _: DialogInterface, _: Int -> deleteUserAccount() }
            .setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.roxo_padrao))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
        }

        dialog.show()
    }

    private fun deleteUserAccount() {
        val user = firebaseAuth.currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Conta excluída com sucesso.", Toast.LENGTH_SHORT).show()
                performLogout() // Logout após a exclusão
            } else {
                Toast.makeText(requireContext(), "Erro ao excluir conta. Saia e faça a autenticação novamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPasswordResetDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        builder.setTitle("Redefinir Senha")

        val input = EditText(requireContext()).apply {
            hint = "Digite seu e-mail"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            setHintTextColor(ContextCompat.getColor(requireContext(), R.color.branco_transparente))
        }
        builder.setView(input)

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

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.roxo_padrao))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            input.setTextColor(ContextCompat.getColor(requireContext(), R.color.branco))
            input.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.branco_transparente))
            dialog.window?.setBackgroundDrawableResource(R.color.preto3)
        }

        dialog.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
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
