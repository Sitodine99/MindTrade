package strategycards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mindtrade.R

class DescriptionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_description, container, false)

        // Vincular la vista del TextView
        val descriptionTextView: TextView = view.findViewById(R.id.strategyDescriptionTextView)

        // Obtener la descripción desde los argumentos
        val description = arguments?.getString("description") ?: "Sin descripción"
        descriptionTextView.text = description

        return view
    }
}