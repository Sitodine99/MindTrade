package com.example.mindtrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class GeneralFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general, container, false)

        // Vincular las vistas
        val tradingStyleTextView: TextView = view.findViewById(R.id.tradingStyleTextView)
        val indicatorsTextView: TextView = view.findViewById(R.id.strategyIndicatorsTextView)
        val timeframesTextView: TextView = view.findViewById(R.id.strategyTimeframesTextView)
        val ratingBar: RatingBar = view.findViewById(R.id.strategyRatingBar)

        // Obtener datos desde los argumentos
        val tradingStyles = arguments?.getStringArray("tradingStyles")?.joinToString(", ")
        val indicators = arguments?.getStringArray("indicators")?.joinToString(", ")
        val timeframes = arguments?.getStringArray("timeframes")?.joinToString(", ")
        val rating = arguments?.getFloat("rating", 0f) ?: 0f

        // Configurar las vistas
        tradingStyleTextView.text = tradingStyles ?: "Sin estilos"
        indicatorsTextView.text = indicators ?: "Sin indicadores"
        timeframesTextView.text = timeframes ?: "Sin temporalidades"
        ratingBar.rating = rating

        return view
    }
}
