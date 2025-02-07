package welcome

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import adapters.AvatarAdapter
import android.widget.GridView
import com.bumptech.glide.Glide
import com.example.mindtrade.MainActivity
import com.example.mindtrade.R
import com.example.mindtrade.finishWithFade
import com.example.mindtrade.startActivityWithFade
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AvatarSelectionActivity : AppCompatActivity() {

    private var userId: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var avatarImage: ImageView
    private lateinit var aliasInput: EditText
    private lateinit var dobPicker: DatePicker
    private lateinit var continueButton: Button
    private var selectedAvatarUri: Uri? = null
    private var selectedAvatarName: String = "default_avatar"

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedAvatarUri = uri
                Glide.with(this).load(uri).circleCrop().into(avatarImage)
            }
        }

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
            finishWithFade()
            return
        }

        avatarImage = findViewById(R.id.avatarImage)
        aliasInput = findViewById(R.id.aliasInput)
        dobPicker = findViewById(R.id.dobPicker)
        continueButton = findViewById(R.id.buttonLogin)

        // Leer los datos actuales pasados desde MainActivity
        val currentAvatarUrl = intent.getStringExtra("avatarUrl")
        val currentAvatarName = intent.getStringExtra("avatarName")
        val currentAlias = intent.getStringExtra("alias")
        val currentDateOfBirth = intent.getStringExtra("dateOfBirth")

        // Configurar el avatar actual
        if (!currentAvatarUrl.isNullOrEmpty() && currentAvatarUrl.startsWith("https://")) {
            Glide.with(this).load(currentAvatarUrl).circleCrop().into(avatarImage)
        } else if (!currentAvatarName.isNullOrEmpty()) {
            val avatarResId = getAvatarImageResource(currentAvatarName)
            avatarResId?.let { avatarImage.setImageResource(it) }
        }

        // Configurar el alias actual
        if (!currentAlias.isNullOrEmpty()) {
            aliasInput.setText(currentAlias)
        }

        // Configurar la fecha de nacimiento actual
        if (!currentDateOfBirth.isNullOrEmpty()) {
            val parts = currentDateOfBirth.split("/")
            if (parts.size == 3) {
                val day = parts[0].toIntOrNull() ?: 1
                val month = (parts[1].toIntOrNull() ?: 1) - 1
                val year = parts[2].toIntOrNull() ?: 2000
                dobPicker.updateDate(year, month, day)
            }
        }

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
                when {
                    selectedAvatarUri != null -> {
                        // Caso 1: El usuario subió una imagen
                        uploadAvatarToFirebase { avatarUrl ->
                            saveUserData(avatarUrl, "default_avatar", alias, dateOfBirth)
                        }
                    }
                    selectedAvatarName != "default_avatar" -> {
                        // Caso 2: El usuario seleccionó un avatar local
                        saveUserData("default_avatar_url", selectedAvatarName, alias, dateOfBirth)
                    }
                    else -> {
                        // Caso 3: El usuario no hizo cambios
                        val currentAvatarUrl = intent.getStringExtra("avatarUrl") ?: "default_avatar_url"
                        val currentAvatarName = intent.getStringExtra("avatarName") ?: "default_avatar"
                        saveUserData(currentAvatarUrl, currentAvatarName, alias, dateOfBirth)
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithFade()
            }
        })
    }

    // Método para obtener el recurso de avatar local
    private fun getAvatarImageResource(avatarName: String?): Int? {
        return when (avatarName) {
            "avatar_alien" -> R.drawable.avataralien
            "avatar_bebe" -> R.drawable.avatarbebe
            "avatar_hombre" -> R.drawable.avatarhombre
            "avatar_mujer" -> R.drawable.avatarmujer
            "avatar_frankenstein" -> R.drawable.avatarfrankenstein
            "avatar_lobo" -> R.drawable.avatarlobo
            "avatar_vampira" -> R.drawable.avatar_vampira
            else -> null
        }
    }


    private fun openAvatarSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_avatar_selection, null)
        val avatarGridView = dialogView.findViewById<GridView>(R.id.avatarGridView)

        val avatarImages = intArrayOf(
            R.drawable.avatarbebe, R.drawable.avatarhombre, R.drawable.avatarmujer,
            R.drawable.avataralien, R.drawable.avatarfrankenstein, R.drawable.avatarlobo,
            R.drawable.avatar_vampira
        )

        val avatarNames = arrayOf(
            "avatar_bebe", "avatar_hombre", "avatar_mujer", "avatar_alien", "avatar_frankenstein",
            "avatar_lobo", "avatar_vampira"
        )

        val adapter = AvatarAdapter(this, avatarImages)
        avatarGridView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Subir Imagen") { _, _ -> openImagePicker() }
            .create()

        avatarGridView.setOnItemClickListener { _, _, position, _ ->
            selectedAvatarUri = null
            selectedAvatarName = avatarNames[position]
            avatarImage.setImageResource(avatarImages[position])
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openImagePicker() {
        selectedAvatarName = "default_avatar"
        pickImageLauncher.launch("image/*")
    }

    private fun getDateOfBirth(): String {
        val day = dobPicker.dayOfMonth
        val month = dobPicker.month + 1
        val year = dobPicker.year
        return "$day/$month/$year"
    }

    private fun uploadAvatarToFirebase(onSuccess: (String) -> Unit) {
        val uri = selectedAvatarUri
        if (uri != null) {
            val storageRef = storage.reference.child("avatars/${UUID.randomUUID()}.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        selectedAvatarName = "default_avatar"
                        onSuccess(downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir avatar", Toast.LENGTH_SHORT).show()
                }
        } else {
            onSuccess("default_avatar_url") // URL por defecto
        }
    }

    private fun saveUserData(
        avatarUrl: String,
        avatarName: String,
        alias: String,
        dateOfBirth: String
    ) {
        if (userId != null) {
            val userData = mapOf(
                "avatarUrl" to avatarUrl,
                "avatarName" to avatarName,
                "alias" to alias,
                "dateOfBirth" to dateOfBirth,
                "registration_progress" to "avatar_selection",
                "registrationComplete" to true
            )

            db.collection("users").document(userId!!).set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()

                    // Retrasar la transición visual a MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    // Retrasar el inicio de la actividad para garantizar fluidez
                    findViewById<Button>(R.id.buttonLogin).postDelayed({
                        startActivityWithFade(intent)
                    }, 300) // Retraso de 300ms, ajustable según sea necesario
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
