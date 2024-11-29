//Este adaptador gestiona cómo se muestran los comentarios y respuestas

package strategycards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Comment

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<Comment>()

    fun setComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val userAliasTextView: TextView = view.findViewById(R.id.userAliasTextView)
        private val contentTextView: TextView = view.findViewById(R.id.contentTextView)
        private val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)

        fun bind(comment: Comment) {
            userAliasTextView.text = comment.userAlias
            contentTextView.text = comment.content
            timestampTextView.text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", comment.timestamp)
        }
    }
}
