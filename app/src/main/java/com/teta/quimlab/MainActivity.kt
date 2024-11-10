package com.teta.quimlab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.teta.quimlab.databinding.ActivityMainBinding
import com.teta.quimlab.ui.authentication.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var commentSection: LinearLayout
    private var isCommentSectionVisible = false // Controle de visibilidade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                displayComments() // Exibe os comentários após autenticação bem-sucedida
            } else {
                Log.e("AuthError", "Autenticação anônima falhou", task.exception)
            }
        }


        // Inflate o layout principal usando o binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar a seção de comentários
        commentSection = findViewById(R.id.commentSection)

        // Configurar o botão de comentário
        val btnComentario = findViewById<ImageButton>(R.id.btn_comment)
        btnComentario.setOnClickListener { toggleCommentSection() }

        // Verificar se o usuário está autenticado
        val usuario = auth.currentUser
        if (usuario == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Configuração da BottomNavigationView e NavController
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Autenticação anônima do Firebase e exibição de comentários
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                displayComments()
            }
        }
    }

    private fun toggleCommentSection() {
        if (isCommentSectionVisible) {
            commentSection.visibility = View.GONE
        } else {
            commentSection.visibility = View.VISIBLE
        }
        isCommentSectionVisible = !isCommentSectionVisible
    }

    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun addComment(view: View) {
        val commentText = findViewById<EditText>(R.id.commentInput).text.toString()

        // Verifica se o comentário não está vazio
        if (commentText.isBlank()) {
            findViewById<EditText>(R.id.commentInput).error = "Comentário não pode ser vazio"
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val userName = "Usuário $userId"
        val userProfilePic = "https://example.com/profile-pic.png" // Placeholder para a foto de perfil

        val comment = mapOf(
            "userId" to userId,
            "userName" to userName,
            "userProfilePic" to userProfilePic,
            "comment" to commentText,
            "timestamp" to System.currentTimeMillis()
        )
        // Adiciona o comentário ao Firestore e atualiza a lista
        db.collection("comments")
            .add(comment)
            .addOnSuccessListener { documentReference ->
                findViewById<EditText>(R.id.commentInput).text.clear()
                displayComments() // Atualiza a exibição de comentários
            }
            .addOnFailureListener { e ->
                findViewById<EditText>(R.id.commentInput).error = "Erro ao enviar comentário"
            }
    }


    private fun displayComments() {
        val commentsList = findViewById<LinearLayout>(R.id.commentsList)
        commentsList.removeAllViews()
        db.collection("comments").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val commentView = layoutInflater.inflate(R.layout.comment, null)
                    val profilePic = commentView.findViewById<ImageView>(R.id.foto_perfil)
                    val userName = commentView.findViewById<TextView>(R.id.nome_perfil)
                    val commentText = commentView.findViewById<TextView>(R.id.mensagemTextView)

                    Glide.with(this@MainActivity).load(document.getString("userProfilePic")).into(profilePic)
                    userName.text = document.getString("userName")
                    commentText.text = document.getString("comment")

                    commentsList.addView(commentView)
                }
            }
        }
    }
}
