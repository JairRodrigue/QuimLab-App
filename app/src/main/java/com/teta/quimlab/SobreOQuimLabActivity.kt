package com.teta.quimlab

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.ui.authentication.LoginActivity

class SobreOQuimLabActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sobre_o_quimlab)
        supportActionBar?.hide()


        val loginRedirect: TextView = findViewById(R.id.login_redirect)
        loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            finish()
        }
    }
}
