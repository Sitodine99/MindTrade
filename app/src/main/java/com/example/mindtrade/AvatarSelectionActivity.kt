package com.example.mindtrade

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Calendar

class AvatarSelectionActivity : AppCompatActivity() {

    private var userId: String? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var avatarImage: ImageView
    private lateinit var aliasInput: EditText
    private lateinit var dobPicker: DatePicker
    private lateinit var continueButton: Button
    private var selectedAvatar: String = "default_avatar" // Almacena el avatar seleccionado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar_selection)

        // Recuperar el ID de usuario desde la Intent
        userId = intent.getStringExtra("USER_ID")
        Log.d("AvatarSelectionActivity", "User ID recibido: $userId")

        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Referencias a los elementos de la UI
        avatarImage = findViewById(R.id.avatarImage)
        aliasInput = findViewById(R.id.aliasInput)
        dobPicker = findViewById(R.id.dobPicker)
        continueButton = findViewById(R.id.buttonLogin)

        // Configurar el evento de clic en el avatar para abrir el cuadro de selección
        avatarImage.setOnClickListener {
            openAvatarSelectionDialog()
        }

        // Configurar el evento de clic en el botón Continuar
        continueButton.setOnClickListener {
            val alias = aliasInput.text.toString()
            val dateOfBirth = getDateOfBirth()

            if (alias.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un alias", Toast.LENGTH_SHORT).show()
            } else if (dateOfBirth.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona tu fecha de nacimiento", Toast.LENGTH_SHORT).show()
            } else {
                saveUserData(selectedAvatar, alias, dateOfBirth)
            }
        }
    }

    private fun openAvatarSelectionDialog() {
        val avatars = arrayOf("Avatar 1", "Avatar 2", "Avatar 3") // Nombres de avatares
        val avatarImages = intArrayOf(R.drawable.avatarbebe, R.drawable.avatarhombre, R.drawable.avatarmujer) // Recursos de imágenes

        // Crear y mostrar un cuadro de diálogo
        AlertDialog.Builder(this)
            .setTitle("Selecciona tu Avatar")
            .setItems(avatars) { dialog, which ->
                selectedAvatar = avatars[which]  // Actualizar el avatar seleccionado
                avatarImage.setImageResource(avatarImages[which])  // Mostrar el avatar seleccionado
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getDateOfBirth(): String {
        val day = dobPicker.dayOfMonth
        val month = dobPicker.month + 1 // Los meses en DatePicker empiezan desde 0
        val year = dobPicker.year
        return "$day/$month/$year"
    }

    private fun saveUserData(avatar: String, alias: String, dateOfBirth: String) {
        if (userId != null) {
            val userData = mapOf(
                "avatar" to avatar,
                "alias" to alias,
                "dateOfBirth" to dateOfBirth
            )

            // Guardar avatar, alias y fecha de nacimiento en Firebase
            db.collection("users").document(userId!!).set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()

                    // Redirigir a MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
