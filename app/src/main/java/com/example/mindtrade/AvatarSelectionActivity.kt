package com.example.mindtrade

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AvatarSelectionActivity : AppCompatActivity() {

    private var userId: String? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var avatarImage: ImageView
    private lateinit var aliasInput: EditText
    private lateinit var dobPicker: DatePicker
    private lateinit var continueButton: Button
    private var selectedAvatarImage: Int = R.drawable.interrogacion
    private var selectedAvatarName: String = "default_avatar"

    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("MindTradePrefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar_selection)

        userId = getUserIdFromPreferences()
        Log.d("AvatarSelectionActivity", "User ID recibido: $userId")

        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        avatarImage = findViewById(R.id.avatarImage)
        aliasInput = findViewById(R.id.aliasInput)
        dobPicker = findViewById(R.id.dobPicker)
        continueButton = findViewById(R.id.buttonLogin)

        avatarImage.setOnClickListener {
            openAvatarSelectionDialog()
        }

        continueButton.setOnClickListener {
            val alias = aliasInput.text.toString()
            val dateOfBirth = getDateOfBirth()

            if (alias.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un alias", Toast.LENGTH_SHORT).show()
            } else if (dateOfBirth.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona tu fecha de nacimiento", Toast.LENGTH_SHORT).show()
            } else {
                saveUserData(selectedAvatarName, alias, dateOfBirth)
            }
        }
    }

    private fun openAvatarSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_avatar_selection, null)
        val avatarGridView = dialogView.findViewById<GridView>(R.id.avatarGridView)

        val avatarImages = intArrayOf(
            R.drawable.avatarbebe, R.drawable.avatarhombre, R.drawable.avatarmujer,
            R.drawable.avataralien, R.drawable.avatarfrankenstein, R.drawable.avatarlobo,
            R.drawable.avatarvampira, R.drawable.avatarpayaso, R.drawable.avatarninja,
            R.drawable.avatarluchadora, R.drawable.avatarmago, R.drawable.avatarhalloween,
            R.drawable.avatarpapanoel, R.drawable.avatarrubio, R.drawable.avatarmoreno
        )

        val avatarNames = arrayOf(
            "avatar_bebe", "avatar_hombre", "avatar_mujer", "avatar_alien", "avatar_frankenstein",
            "avatar_lobo", "avatar_vampira", "avatar_payaso", "avatar_ninja", "avatar_luchadora",
            "avatar_mago", "avatar_halloween", "avatar_papanoel", "avatar_rubio", "avatar_moreno"
        )

        val adapter = AvatarAdapter(this, avatarImages)
        avatarGridView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()

        avatarGridView.setOnItemClickListener { _, _, position, _ ->
            selectedAvatarImage = avatarImages[position]
            selectedAvatarName = avatarNames[position]
            avatarImage.setImageResource(selectedAvatarImage)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getDateOfBirth(): String {
        val day = dobPicker.dayOfMonth
        val month = dobPicker.month + 1
        val year = dobPicker.year
        return "$day/$month/$year"
    }

    private fun saveUserData(avatar: String, alias: String, dateOfBirth: String) {
        if (userId != null) {
            val userData = mapOf(
                "avatarImage" to selectedAvatarImage,
                "avatarName" to avatar,
                "alias" to alias,
                "dateOfBirth" to dateOfBirth,
                "registration_progress" to "avatar_selection",
                "registrationComplete" to true // Indicar que el registro está completo
            )

            // Guardar avatar, alias, fecha de nacimiento y progreso en Firebase
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
