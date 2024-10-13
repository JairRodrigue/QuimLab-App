package com.teta.quimlab

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.teta.quimlab.databinding.ActivityPerfilBinding
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PerfilActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityPerfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val storageRef = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarPerfil.toolbar)

        // Inicializa Firebase Auth e Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicialização do DrawerLayout e NavigationView
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_perfil)

        // Configuração da AppBarConfiguration com os IDs de destino
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )

        // Configuração da ActionBar com o NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configuração do NavigationView com o NavController
        navView.setupWithNavController(navController)

        // Encontrar e configurar os botões no NavigationView
        val headerView = navView.getHeaderView(0)
        val btnVerPerfil = headerView.findViewById<Button>(R.id.btn_perfil)
        val btnEditarPerfil = headerView.findViewById<Button>(R.id.btn_editar_perfil)

        // Ação para o botão Ver Perfil (apenas fecha a barra lateral)
        btnVerPerfil.setOnClickListener {
            drawerLayout.closeDrawers() // Fecha a barra lateral
        }

        // Ação para o botão Editar Perfil (abre a EditarPerfilActivity)
        btnEditarPerfil.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }

        // Carregar os dados do usuário, incluindo a imagem do perfil
        loadUserData()

        // Ação para o FloatingActionButton (abre a CriarPostActivity)
        binding.appBarPerfil.fab.setOnClickListener {
            val intent = Intent(this, CriarPostActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid // Obtém o UID do usuário autenticado

        if (userId != null) {
            db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Obtém os dados do usuário
                        val nomeUser = document.getString("nome")
                        val emailUser = document.getString("email")
                        val imageUrl = document.getString("imageUrl")

                        // Atualiza os TextViews do NavigationView com os dados do usuário
                        val headerView = binding.navView.getHeaderView(0)
                        val nomeNavTextView = headerView.findViewById<TextView>(R.id.nome_user)
                        val emailNavTextView = headerView.findViewById<TextView>(R.id.email_user)
                        val userIconNavView = headerView.findViewById<ImageView>(R.id.user_icon)

                        nomeNavTextView.text = nomeUser ?: "Nome não disponível"
                        emailNavTextView.text = emailUser ?: "Email não disponível"

                        // Atualiza os TextViews do content_perfil com os dados do usuário
                        val nomeContentTextView = findViewById<TextView>(R.id.nome_user)
                        val emailContentTextView = findViewById<TextView>(R.id.email_user)
                        val userIconContentView = findViewById<ImageView>(R.id.user_icon)

                        nomeContentTextView.text = nomeUser ?: "Nome não disponível"
                        emailContentTextView.text = emailUser ?: "Email não disponível"

                        // Carregar a imagem do perfil usando Glide se a URL estiver disponível
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this).load(imageUrl).into(userIconNavView)
                            Glide.with(this).load(imageUrl).into(userIconContentView)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Tratar o erro
                    println("Erro ao carregar dados do usuário: ${exception.message}")
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.perfil, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_back -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_perfil)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
