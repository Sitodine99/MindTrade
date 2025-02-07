package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mindtrade.R

class AvatarAdapter(
    private val context: Context,
    private val localAvatars: IntArray, // Avatares predefinidos (drawables)
    private val urlAvatars: List<String>? = null // Avatares personalizados (URLs)
) : BaseAdapter() {

    override fun getCount(): Int = localAvatars.size + (urlAvatars?.size ?: 0)

    override fun getItem(position: Int): Any {
        return if (position < localAvatars.size) {
            localAvatars[position]
        } else {
            urlAvatars!![position - localAvatars.size]
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflar el layout `avatar_item.xml` para cada elemento en el GridView
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.avatar_item, parent, false)
        val avatarImage = view.findViewById<ImageView>(R.id.avatarImage)

        // Determinar si es un drawable local o una URL
        if (position < localAvatars.size) {
            // Es un drawable local
            avatarImage.setImageResource(localAvatars[position])
        } else {
            // Es una URL
            val url = urlAvatars!![position - localAvatars.size]
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.interrogacion_icon) // Imagen predeterminada mientras carga
                .error(R.drawable.interrogacion_icon) // Imagen en caso de error
                .into(avatarImage)
        }

        return view
    }
}
