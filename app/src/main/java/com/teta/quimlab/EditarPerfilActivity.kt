package com.teta.quimlab

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import android.util.Patterns // Import necessário para a validação do email

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var userIcon: ImageView
    private lateinit var etNome: EditText
    private lateinit var etEmail: EditText
    private var imageUri: Uri? = null
    private var croppedImageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference
    private val firestoreRef = FirebaseFirestore.getInstance().collection("usuarios")
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        supportActionBar?.title = "Editar Perfil"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userIcon = findViewById(R.id.user_icon)
        etNome = findViewById(R.id.etnome)
        etEmail = findViewById(R.id.etemail)
        val cameraIcon: ImageView = findViewById(R.id.camera_icon)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        cameraIcon.setOnClickListener {
            escolherImagem()
        }

        fab.setOnClickListener {
            salvarAlteracoes()
        }

        carregarDadosUsuario()
    }

    private fun escolherImagem() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                imageUri = uri
                userIcon.setImageURI(uri)
                if (!isImageSquare(uri)) {
                    cropImage(uri)
                } else {
                    croppedImageUri = uri
                }
            }
        }
    }

    private fun isImageSquare(uri: Uri): Boolean {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        return bitmap.width == bitmap.height
    }

    private fun cropImage(uri: Uri) {
        val intent = Intent("com.android.camera.action.CROP").apply {
            setDataAndType(uri, "image/*")
            putExtra("crop", "true")
            putExtra("aspectX", 1)
            putExtra("aspectY", 1)
            putExtra("outputX", 300)
            putExtra("outputY", 300)
            putExtra("return-data", true)
        }
        cropResultLauncher.launch(intent)
    }

    private val cropResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val extras = result.data?.extras
            val bitmap = extras?.getParcelable<Bitmap>("data")
            if (bitmap != null) {
                croppedImageUri = saveCroppedImage(bitmap)
                userIcon.setImageBitmap(bitmap)
            }
        }
    }

    private fun saveCroppedImage(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "CroppedImage", null)
        return Uri.parse(path)
    }

    private fun salvarAlteracoes() {
        val nome = etNome.text.toString()
        val email = etEmail.text.toString().trim() // Remover espaços em branco nas extremidades

        if (nome.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha o nome.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validação do formato do email
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, insira um email válido.", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let { user ->
            val userId = user.uid
            if (croppedImageUri != null) {
                val imageRef = storageRef.child("usuarios/$userId/perfil.jpg")

                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, croppedImageUri)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                imageRef.putBytes(data)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            atualizarFirestore(userId, nome, email, uri.toString())
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Erro ao enviar imagem: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                atualizarFirestore(userId, nome, email, null)
            }
        }
    }

    private fun atualizarFirestore(userId: String, nome: String, email: String, imageUrl: String?) {
        val userMap = hashMapOf(
            "nome" to nome,
            "email" to if (email.isEmpty()) " " else email // Enviar espaço em branco se o email estiver vazio
        )
        imageUrl?.let { userMap["imageUrl"] = it }

        firestoreRef.document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao atualizar perfil: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarDadosUsuario() {
        currentUser?.let { user ->
            firestoreRef.document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        etNome.setText(document.getString("nome"))
                        etEmail.setText(document.getString("email"))
                        val imageUrl = document.getString("imageUrl")
                        if (imageUrl != null) {
                            Glide.with(this).load(imageUrl).into(userIcon)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao carregar dados do usuário: ${exception.message}", Toast.LENGTH_SHORT).show()
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
}
