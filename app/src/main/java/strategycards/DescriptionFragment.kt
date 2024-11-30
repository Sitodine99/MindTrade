package strategycards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mindtrade.R

class DescriptionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vincular la vista del contenedor dinámico
        val descriptionLayout: LinearLayout = view.findViewById(R.id.descriptionLayout)

        // Obtener la descripción desde los argumentos
        val description = arguments?.getString("description") ?: "Sin descripción"

        // Extraer secciones de la descripción
        val entryCondition = extractSection(description, "Condición de entrada:")
        val exitCondition = extractSection(description, "Condición de salida:")
        val generalConsiderations = extractSection(description, "Consideraciones generales:")

        // Agregar las secciones al layout dinámico
        addSection(descriptionLayout, "Condición de entrada", entryCondition)
        addSection(descriptionLayout, "Condición de salida", exitCondition)
        addSection(descriptionLayout, "Consideraciones generales", generalConsiderations)
    }

    private fun addSection(parent: LinearLayout, title: String, content: String) {
        // Título en negrita
        val titleView = TextView(requireContext()).apply {
            text = title
            setTextAppearance(R.style.TextAppearance_SemiBold)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 4)
        }
        parent.addView(titleView)

        // Contenido normal
        val contentView = TextView(requireContext()).apply {
            text = content.ifBlank { "No especificado" }
            setTextAppearance(android.R.style.TextAppearance_Small)
            setPadding(0, 0, 0, 12)
        }
        parent.addView(contentView)
    }

    private fun extractSection(description: String, sectionHeader: String): String {
        val sectionStart = description.indexOf(sectionHeader)
        if (sectionStart == -1) return "" // Si no se encuentra la sección, devolver vacío

        // Encontrar el final de la sección actual y el inicio de la siguiente
        val sectionEnd = description.indexOf("\n\n", sectionStart + sectionHeader.length)
        return if (sectionEnd == -1) {
            description.substring(sectionStart + sectionHeader.length).trim()
        } else {
            description.substring(sectionStart + sectionHeader.length, sectionEnd).trim()
        }
    }
}
