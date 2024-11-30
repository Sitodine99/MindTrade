package strategycards

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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

        // Configurar estilo de fuente para texto tipo código
        algorithmTextView.typeface = android.graphics.Typeface.MONOSPACE

        // Configurar evento para copiar el texto al portapapeles
        algorithmTextView.setOnClickListener {
            copyToClipboard(algorithmCode)
        }

        return view
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Algorithm Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Código copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }
}
