package com.teta.quimlab

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarPerfil.toolbar)

        // Configuração do FloatingActionButton
        binding.appBarPerfil.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        // Inicialização do DrawerLayout e NavigationView
        val drawerLayout: DrawerLayout = binding.drawerLayout // Certifique-se que o ID está correto
        val navView: NavigationView = binding.navView // Certifique-se que o ID está correto
        val navController = findNavController(R.id.nav_host_fragment_content_perfil)

        // Configuração da AppBarConfiguration com os IDs de destino
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )

        // Configuração da ActionBar com o NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configuração do NavigationView com o NavController
        navView.setupWithNavController(navController)
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
