package com.teta.quimlab

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CriarPostActivity : AppCompatActivity() {

    private lateinit var etTitulo: EditText
    private lateinit var etMensagem: EditText
    private lateinit var btnAdicionarVideo: Button
    private lateinit var btnAdicionarFoto: Button
    private lateinit var btnAdicionarArquivo: Button
    private lateinit var fab: FloatingActionButton

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var videoUri: Uri? = null
    private var fotoUri: Uri? = null
    private var arquivoUri: Uri? = null // Variável para armazenar o URI do arquivo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_criar_post)

        supportActionBar?.title = "Criar Postagem"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inicializa os componentes da interface
        etTitulo = findViewById(R.id.etitulo)
        etMensagem = findViewById(R.id.etmensagem)
        btnAdicionarVideo = findViewById(R.id.btnaddVideo)
        btnAdicionarFoto = findViewById(R.id.btnaddFoto)
        btnAdicionarArquivo = findViewById(R.id.btnaddArquivo)
        fab = findViewById(R.id.fab)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fab.setOnClickListener {
            enviarDadosParaFirestore()
        }

        btnAdicionarVideo.setOnClickListener {
            if (fotoUri != null) {
                Toast.makeText(this, "Você não pode selecionar um vídeo e uma foto ao mesmo tempo.", Toast.LENGTH_SHORT).show()
            } else {
                escolherVideo()
            }
        }

        btnAdicionarFoto.setOnClickListener {
            if (videoUri != null) {
                Toast.makeText(this, "Você não pode selecionar uma foto e um vídeo ao mesmo tempo.", Toast.LENGTH_SHORT).show()
            } else {
                escolherFoto()
            }
        }

        btnAdicionarArquivo.setOnClickListener {
            escolherArquivo()
        }
    }

    private fun escolherVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_VIDEO_PICK)
    }

    private fun escolherFoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun escolherArquivo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // Permite selecionar qualquer tipo de arquivo
        startActivityForResult(intent, REQUEST_FILE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_VIDEO_PICK -> {
                    videoUri = data.data
                    atualizarBotao(btnAdicionarVideo, "Vídeo selecionado!")
                }
                REQUEST_IMAGE_PICK -> {
                    fotoUri = data.data
                    atualizarBotao(btnAdicionarFoto, "Foto selecionada!")
                }
                REQUEST_FILE_PICK -> {
                    arquivoUri = data.data
                    atualizarBotao(btnAdicionarArquivo, "Arquivo selecionado!")
                }
            }
        }
    }

    private fun atualizarBotao(botao: Button, texto: String) {
        botao.text = texto
        botao.setBackgroundColor(resources.getColor(R.color.roxo_padrao)) // Muda para roxo_padrao
        botao.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_done_24, 0, 0, 0) // Muda o ícone
    }

    private fun enviarDadosParaFirestore() {
        val titulo = etTitulo.text.toString()
        val mensagem = etMensagem.text.toString()

        // Verifica se os campos obrigatórios estão preenchidos
        if (titulo.isEmpty() || mensagem.isEmpty()) {
            Toast.makeText(this, "Título e mensagem são obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtém o usuário autenticado
        val usuarioAtual = FirebaseAuth.getInstance().currentUser

        if (usuarioAtual != null) {
            val uid = usuarioAtual.uid
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nomeUsuario = document.getString("nome") ?: "Usuário desconhecido"
                        val fotoPerfilUrl = document.getString("imageUrl") ?: ""

                        val postagem = hashMapOf(
                            "titulo" to titulo,
                            "mensagem" to mensagem,
                            "nomeUsuario" to nomeUsuario,
                            "fotoPerfilUrl" to fotoPerfilUrl,
                            "usuarioId" to uid,
                            "timestamp" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("postagens")
                            .add(postagem)
                            .addOnSuccessListener { documentReference ->
                                uploadArquivos(documentReference.id)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao criar postagem: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Erro ao recuperar informações do usuário.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao acessar dados do usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Erro: Usuário não autenticado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadArquivos(postId: String) {
        val uploadTasks = mutableListOf<Task<*>>()

        videoUri?.let {
            val videoRef = storage.reference.child("videos/$postId/${UUID.randomUUID()}.mp4")
            uploadTasks.add(videoRef.putFile(it).continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Erro ao fazer upload do vídeo")
                videoRef.downloadUrl
            }.addOnSuccessListener { uri ->
                firestore.collection("postagens").document(postId)
                    .update("videoUrl", uri.toString())
            })
        }

        fotoUri?.let {
            val fotoRef = storage.reference.child("fotos/$postId/${UUID.randomUUID()}.jpg")
            uploadTasks.add(fotoRef.putFile(it).continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Erro ao fazer upload da foto")
                fotoRef.downloadUrl
            }.addOnSuccessListener { uri ->
                firestore.collection("postagens").document(postId)
                    .update("fotoUrl", uri.toString())
            })
        }

        arquivoUri?.let {
            val arquivoRef = storage.reference.child("arquivos/$postId/${UUID.randomUUID()}")
            uploadTasks.add(arquivoRef.putFile(it).continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Erro ao fazer upload do arquivo")
                arquivoRef.downloadUrl
            }.addOnSuccessListener { uri ->
                firestore.collection("postagens").document(postId)
                    .update("arquivoUrl", uri.toString())
            })
        }

        Tasks.whenAllComplete(uploadTasks).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Postagem criada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao fazer upload de alguns arquivos. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val REQUEST_VIDEO_PICK = 1
        private const val REQUEST_IMAGE_PICK = 2
        private const val REQUEST_FILE_PICK = 3
    }
}
