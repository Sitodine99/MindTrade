package com.example.mindtrade

import adapters.StrategyAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.model.Strategy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchStrategyActivity : AppCompatActivity() {

    private lateinit var toggleFiltersButton: Button
    private lateinit var filtersContainer: LinearLayout
    private lateinit var searchButton: Button
    private lateinit var searchNameInput: EditText
    private lateinit var ratingSpinner: Spinner
    private lateinit var indicatorInput: EditText
    private lateinit var recyclerView: RecyclerView

    // Filtros
    private lateinit var checkDayTrading: CheckBox
    private lateinit var checkScalping: CheckBox
    private lateinit var checkSwingTrading: CheckBox
    private lateinit var checkM1: CheckBox
    private lateinit var checkM3: CheckBox
    private lateinit var checkM5: CheckBox
    private lateinit var checkM15: CheckBox
    private lateinit var checkM30: CheckBox
    private lateinit var checkH1: CheckBox
    private lateinit var checkH4: CheckBox
    private lateinit var checkD1: CheckBox
    private lateinit var checkW1: CheckBox
    private lateinit var checkOther: CheckBox
    private lateinit var checkBotYes: CheckBox
    private lateinit var checkBotNo: CheckBox

    private val strategies = mutableListOf<Strategy>()
    private lateinit var strategyAdapter: StrategyAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_strategy)

        // Inicializar vistas
        toggleFiltersButton = findViewById(R.id.toggleFiltersButton)
        filtersContainer = findViewById(R.id.filtersContainer)
        searchButton = findViewById(R.id.searchButton)
        searchNameInput = findViewById(R.id.searchNameInput)
        ratingSpinner = findViewById(R.id.ratingSpinner)
        indicatorInput = findViewById(R.id.indicatorInput)
        recyclerView = findViewById(R.id.recyclerView)

        checkDayTrading = findViewById(R.id.checkDayTrading)
        checkScalping = findViewById(R.id.checkScalping)
        checkSwingTrading = findViewById(R.id.checkSwingTrading)

        checkM1 = findViewById(R.id.checkM1)
        checkM3 = findViewById(R.id.checkM3)
        checkM5 = findViewById(R.id.checkM5)
        checkM15 = findViewById(R.id.checkM15)
        checkM30 = findViewById(R.id.checkM30)
        checkH1 = findViewById(R.id.checkH1)
        checkH4 = findViewById(R.id.checkH4)
        checkD1 = findViewById(R.id.checkD1)
        checkW1 = findViewById(R.id.checkW1)
        checkOther = findViewById(R.id.checkOther)

        checkBotYes = findViewById(R.id.checkBotYes)
        checkBotNo = findViewById(R.id.checkBotNo)

        // Configurar RecyclerView
        strategyAdapter = StrategyAdapter(strategies) { strategy ->
            openStrategyDetail(strategy)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = strategyAdapter

        // Mostrar todas las estrategias al iniciar la actividad
        fetchAllStrategies()

        // Mostrar/ocultar los filtros
        toggleFiltersButton.setOnClickListener {
            if (filtersContainer.visibility == View.VISIBLE) {
                filtersContainer.visibility = View.GONE
                toggleFiltersButton.text = "Mostrar filtros"
            } else {
                filtersContainer.visibility = View.VISIBLE
                toggleFiltersButton.text = "Ocultar filtros"
            }
        }

        // Listener para el botón de búsqueda
        searchButton.setOnClickListener { performSearch() }
    }

    private fun fetchAllStrategies() {
        db.collection("strategies")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Mostrar las estrategias más recientes primero
            .get()
            .addOnSuccessListener { result ->
                strategies.clear()
                for (document in result) {
                    val strategy = document.toObject(Strategy::class.java)
                    strategies.add(strategy)
                }
                strategyAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("SearchStrategyActivity", "Error al cargar estrategias: ${e.message}", e)
                Toast.makeText(this, "Error al cargar estrategias", Toast.LENGTH_SHORT).show()
            }
    }

    private fun performSearch() {
        val nameQuery = searchNameInput.text.toString().trim().lowercase()
        val keywords = nameQuery.split(" ").filter { it.isNotBlank() }

        val selectedTradingStyles = mutableListOf<String>()
        if (checkDayTrading.isChecked) selectedTradingStyles.add("Day Trading")
        if (checkScalping.isChecked) selectedTradingStyles.add("Scalping")
        if (checkSwingTrading.isChecked) selectedTradingStyles.add("Swing Trading")

        val selectedTimeframes = mutableListOf<String>()
        if (checkM1.isChecked) selectedTimeframes.add("M1")
        if (checkM3.isChecked) selectedTimeframes.add("M3")
        if (checkM5.isChecked) selectedTimeframes.add("M5")
        if (checkM15.isChecked) selectedTimeframes.add("M15")
        if (checkM30.isChecked) selectedTimeframes.add("M30")
        if (checkH1.isChecked) selectedTimeframes.add("H1")
        if (checkH4.isChecked) selectedTimeframes.add("H4")
        if (checkD1.isChecked) selectedTimeframes.add("D1")
        if (checkW1.isChecked) selectedTimeframes.add("W1")
        if (checkOther.isChecked) selectedTimeframes.add("Otras")

        val includeBots = checkBotYes.isChecked
        val excludeBots = checkBotNo.isChecked

        // Crear la consulta base
        var query: Query = db.collection("strategies")

        // Filtro por indicadores
        val indicatorQuery = indicatorInput.text.toString().trim()
        if (indicatorQuery.isNotEmpty()) {
            query = query.whereArrayContains("indicators", indicatorQuery)
        }

        // Filtro por estilos de trading
        if (selectedTradingStyles.isNotEmpty()) {
            query = query.whereArrayContainsAny("tradingStyles", selectedTradingStyles)
        }

        // Filtro por marcos temporales
        if (selectedTimeframes.isNotEmpty()) {
            query = query.whereArrayContainsAny("timeframes", selectedTimeframes)
        }

        // Filtro por bots
        if (includeBots && !excludeBots) {
            query = query.whereNotEqualTo("algorithmCode", "")
        } else if (excludeBots && !includeBots) {
            query = query.whereEqualTo("algorithmCode", "")
        }

        // Ordenamiento por rating
        when (ratingSpinner.selectedItem.toString()) {
            "Más valoradas primero" -> query = query.orderBy("rating", Query.Direction.DESCENDING)
            "Menos valoradas primero" -> query = query.orderBy("rating", Query.Direction.ASCENDING)
        }

        // Ejecutar la consulta
        query.get()
            .addOnSuccessListener { result ->
                strategies.clear()
                for (document in result) {
                    val strategy = document.toObject(Strategy::class.java)

                    // Filtrado manual en el cliente
                    if (keywords.isEmpty() || keywords.any { keyword ->
                            strategy.title.contains(keyword, ignoreCase = true)
                        }) {
                        strategies.add(strategy)
                    }
                }

                // Ordenar por número de comentarios y favoritos
                strategies.sortByDescending { it.comments.size + it.favoritedBy.size }

                strategyAdapter.notifyDataSetChanged()
                if (strategies.isEmpty()) {
                    Toast.makeText(this, "No se encontraron estrategias", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("SearchStrategyActivity", "Error al realizar la búsqueda: ${e.message}", e)
                Toast.makeText(this, "Error al buscar estrategias", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openStrategyDetail(strategy: Strategy) {
        Toast.makeText(this, "Estrategia seleccionada: ${strategy.title}", Toast.LENGTH_SHORT).show()
        // Implementa la navegación al fragmento de detalle si es necesario
    }
}
