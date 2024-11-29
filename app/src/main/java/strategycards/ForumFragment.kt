//Este fragmento gestionará la vista y la lógica de los comentarios.

package strategycards


import adapters.CommentsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Comment
import com.example.mindtrade.model.Strategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
    ): View? {
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
        db.collection("strategies").document(strategyId).get()
            .addOnSuccessListener { document ->
                val comments = document.toObject(Strategy::class.java)?.comments ?: emptyList()
                commentsAdapter.setComments(comments)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addComment(content: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Recuperar el alias y avatar desde Firestore
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
                    db.collection("strategies").document(id).update(
                        "comments", FieldValue.arrayUnion(newComment)
                    ).addOnSuccessListener {
                        commentEditText.text.clear()
                        loadComments(id)
                        Toast.makeText(context, "Comentario añadido", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error al añadir comentario", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al recuperar datos del usuario", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
