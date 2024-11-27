package auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import welcome.AvatarSelectionActivity
import com.example.mindtrade.MainActivity
import com.example.mindtrade.R
import com.example.mindtrade.databinding.ActivityLoginBinding
import com.example.mindtrade.startActivityWithFade
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import welcome.EmotionsActivity
import welcome.PsicoNegativeActivity
import welcome.PsicoPositiveActivity
import welcome.TradingStyleActivity
import welcome.WelcomeActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityLoginBinding
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            saveUserIdToPreferences(currentUser.uid)
            checkRegistrationStatus(currentUser.uid)
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.buttonGoogleRegister.setOnClickListener {
            signInWithGoogle()
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese el correo y la contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            saveUserIdToPreferences(it.uid)
                            checkRegistrationStatus(it.uid)
                        }
                    } else {
                        Toast.makeText(this, "Usuario o contraseña incorrectos: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivityWithFade(intent)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                firebaseAuthWithGoogle(account)
            } else {
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    saveUserIdToPreferences(userId)
                    val userDocRef = db.collection("users").document(userId)
                    userDocRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            checkRegistrationStatus(userId)
                        } else {
                            val userData = hashMapOf("email" to account?.email, "registrationComplete" to false)
                            userDocRef.set(userData).addOnSuccessListener {
                                val welcomeIntent = Intent(this, WelcomeActivity::class.java)
                                welcomeIntent.putExtra("USER_ID", userId)
                                startActivity(welcomeIntent)
                                finish()
                            }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al verificar el usuario en Firestore", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error en la autenticación con Google", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkRegistrationStatus(userId: String) {
        val userDocRef = db.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Verificar si el usuario ha completado el registro
                if (document.getBoolean("registrationComplete") == true) {
                    // Redirigir directamente a MainActivity si el registro está completo
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // Continuar el proceso de registro donde se quedó
                    when {
                        document.getString("trading_style") == null -> {
                            startActivity(Intent(this, TradingStyleActivity::class.java))
                        }
                        document.getString("psico") == null -> {
                            startActivity(Intent(this, EmotionsActivity::class.java))
                        }
                        document.getString("emotion") == null -> {
                            val nextActivity = if (document.getString("psico") == "Psico +") {
                                PsicoPositiveActivity::class.java
                            } else {
                                PsicoNegativeActivity::class.java
                            }
                            startActivity(Intent(this, nextActivity))
                        }
                        document.getString("alias") == null ||
                                document.get("avatarImage") == null ||
                                document.getString("dateOfBirth") == null -> {
                            startActivity(Intent(this, AvatarSelectionActivity::class.java))
                        }
                        else -> {
                            startActivity(Intent(this, WelcomeActivity::class.java))
                        }
                    }
                }
                finish()
            } else {
                Log.d(TAG, "No se encontró el documento del usuario.")
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }.addOnFailureListener {
            Log.e(TAG, "Error al acceder al documento en Firestore", it)
            Toast.makeText(this, "Error al verificar el estado de registro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserIdToPreferences(userId: String) {
        val sharedPreferences = getSharedPreferences("MindTradePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("USER_ID", userId)
            apply()
        }
    }
}