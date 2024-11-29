package strategycards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mindtrade.R

class AlgorithmFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_algorithm, container, false)
        val algorithmTextView: TextView = view.findViewById(R.id.algorithmTextView)

        // Obtener el código algorítmico desde los argumentos
        val algorithmCode = arguments?.getString("algorithmCode") ?: "No se proporcionó código."
        algorithmTextView.text = algorithmCode

        return view
    }
}
