package com.teta.quimlab

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.teta.quimlab.databinding.ActivityPerfilBinding
import androidx.navigation.ui.setupWithNavController

class PerfilActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarPerfil.toolbar)

        // Configuração do FloatingActionButton
        binding.appBarPerfil.fab.setOnClickListener {
            val intent = Intent(this, CriarPostActivity::class.java)
            startActivity(intent)
        }

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
