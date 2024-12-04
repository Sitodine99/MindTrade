import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ScreenPagerAdapter(
    private val layouts: List<Int>
) : RecyclerView.Adapter<ScreenPagerAdapter.ScreenViewHolder>() {

    inner class ScreenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        // Asignar un tag único basado en la posición
        view.tag = "f${layouts.indexOf(viewType)}"
        return ScreenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScreenViewHolder, position: Int) {
        // No se necesita enlazar datos adicionales por ahora
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int = layouts[position]
}

