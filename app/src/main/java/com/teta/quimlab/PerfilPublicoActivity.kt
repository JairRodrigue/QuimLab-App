package com.teta.quimlab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teta.quimlab.databinding.ActivityPerfilPublicoBinding

class PerfilPublicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilPublicoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilPublicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
