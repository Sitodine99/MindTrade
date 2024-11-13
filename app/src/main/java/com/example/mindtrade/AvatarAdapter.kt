package com.example.mindtrade

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class AvatarAdapter(private val context: Context, private val avatars: IntArray) : BaseAdapter() {

    override fun getCount(): Int = avatars.size
    override fun getItem(position: Int): Any = avatars[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflar el layout `avatar_item.xml` para cada elemento en el GridView
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.avatar_item, parent, false)
        val avatarImage = view.findViewById<ImageView>(R.id.avatarImage)

        // Configura la imagen del avatar y aplica el fondo circular
        avatarImage.setImageResource(avatars[position])

        return view
    }
}
