package strategycards

import adapters.CommentsAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.MainActivity
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import model.Comment
import java.util.*

class ForumFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentEditText: EditText
    private lateinit var sendButton: Button
    private var strategyId: String? = null
    private val commentsAdapter = CommentsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_forum, container, false)

        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView)
        commentEditText = view.findViewById(R.id.commentEditText)
        sendButton = view.findViewById(R.id.sendButton)

        strategyId = arguments?.getString("strategyId")

        commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentsRecyclerView.adapter = commentsAdapter

        strategyId?.let { loadComments(it) }

        sendButton.setOnClickListener {
            val content = commentEditText.text.toString().trim()
            if (content.isNotEmpty()) {
                addComment(content)
            } else {
                Toast.makeText(context, "El comentario no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loadComments(strategyId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("strategies").document(strategyId).get()
            .addOnSuccessListener { document ->
                val strategy = document.toObject(Strategy::class.java)
                val comments = strategy?.comments ?: emptyList()

                // Recuperar los datos actuales del usuario
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val currentAlias = userDoc.getString("alias") ?: "Anónimo"
                        val currentAvatarUrl = userDoc.getString("avatarUrl")
                        val currentAvatarName = userDoc.getString("avatarName") ?: "default_avatar"

                        // Actualizar comentarios con datos actuales del usuario
                        val updatedComments = comments.map { comment ->
                            if (comment.userId == userId &&
                                (comment.avatarUrl != currentAvatarUrl || comment.avatarName != currentAvatarName)
                            ) {
                                comment.copy(
                                    userAlias = currentAlias,
                                    avatarUrl = currentAvatarUrl,
                                    avatarName = currentAvatarName
                                )
                            } else {
                                comment
                            }
                        }

                        // Actualizar Firestore si hay cambios
                        if (updatedComments != comments) {
                            db.collection("strategies").document(strategyId).update(
                                "comments", updatedComments
                            ).addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar comentarios", Toast.LENGTH_SHORT).show()
                            }
                        }

                        // Mostrar los comentarios actualizados
                        commentsAdapter.setComments(updatedComments)

                        // Guardar el último comentario leído
                        val lastCommentTimestamp = updatedComments.maxOfOrNull { it.timestamp } ?: 0
                        setLastReadTimestamp(strategyId, lastCommentTimestamp)

                        // Volver a verificar si hay notificaciones pendientes
                        val mainActivity = activity as? MainActivity
                        mainActivity?.checkForNewComments()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al recuperar datos del usuario", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLastReadTimestamp(strategyId: String, timestamp: Long) {
        val sharedPreferences = requireContext().getSharedPreferences(
            "MindTradePrefs",
            android.content.Context.MODE_PRIVATE
        )
        sharedPreferences.edit().putLong("last_read_comment_$strategyId", timestamp).apply()
    }

    private fun addComment(content: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val userAlias = userDoc.getString("alias") ?: "Anónimo"
                val avatarUrl = userDoc.getString("avatarUrl")
                val avatarName = userDoc.getString("avatarName") ?: "default_avatar"

                val newComment = Comment(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    userAlias = userAlias,
                    avatarUrl = avatarUrl,
                    avatarName = avatarName,
                    content = content,
                    timestamp = System.currentTimeMillis()
                )

                strategyId?.let { id ->
                    val strategyRef = db.collection("strategies").document(id)

                    // Actualizamos el array de comentarios en Firestore
                    strategyRef.update("comments", FieldValue.arrayUnion(newComment))
                        .addOnSuccessListener {
                            Log.d("Firestore", "Comentario añadido correctamente a la estrategia.")
                            commentEditText.text.clear() // Borrar campo de texto
                            loadComments(id) // Recargar comentarios
                            Toast.makeText(context, "Comentario añadido", Toast.LENGTH_SHORT).show()

                            // Notificar al creador de la estrategia
                            markStrategyAsNewComment(id)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error al añadir comentario: ${e.message}")
                            Toast.makeText(context, "Error al añadir comentario", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener datos del usuario: ${e.message}")
                Toast.makeText(context, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }



    private fun markStrategyAsNewComment(strategyId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("strategies").document(strategyId).get()
            .addOnSuccessListener { strategyDoc ->
                val strategyOwnerId = strategyDoc.getString("createdBy") ?: return@addOnSuccessListener
                val strategyTitle = strategyDoc.getString("title") ?: "Estrategia sin nombre"

                // No notificar si el usuario comenta su propia estrategia
                if (strategyOwnerId == currentUserId) return@addOnSuccessListener

                val notificationRef = db.collection("notifications").document(strategyOwnerId)

                notificationRef.get().addOnSuccessListener { document ->
                    val newComments = document.get("newComments") as? MutableList<Map<String, String>> ?: mutableListOf()

                    // Si la estrategia aún no está en las notificaciones, añadirla con su nombre
                    if (newComments.none { it["id"] == strategyId }) {
                        newComments.add(mapOf("id" to strategyId, "title" to strategyTitle))
                    }

                    // Guardamos la notificación en Firestore
                    notificationRef.set(mapOf("newComments" to newComments))
                        .addOnSuccessListener {
                            Log.d("Firestore", "Notificación enviada a $strategyOwnerId con título: $strategyTitle")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error al guardar notificación: ${e.message}")
                        }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error al obtener notificaciones: ${e.message}")
                }
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener estrategia: ${e.message}")
            }
    }

}
