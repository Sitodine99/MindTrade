package com.example.mindtrade

import adapters.StrategyAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.model.Strategy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import model.Comment
import strategycards.StrategyDetailFragment

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
        Log.d("SearchDebug", "📢 Cargando estrategias desde Firestore...")

        db.collection("strategies")
            .get()
            .addOnSuccessListener { result ->
                strategies.clear()

                for (document in result) {
                    val favoritedByList = (document.get("favoritedBy") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val commentsList = (document.get("comments") as? List<*>)?.mapNotNull { comment ->
                        val commentMap = comment as? Map<*, *>
                        commentMap?.let {
                            Comment(
                                id = it["id"] as? String ?: "",
                                userId = it["userId"] as? String ?: "",
                                userAlias = it["userAlias"] as? String ?: "Anónimo",
                                avatarUrl = it["avatarUrl"] as? String,
                                avatarName = it["avatarName"] as? String,
                                content = it["content"] as? String ?: "",
                                timestamp = it["timestamp"] as? Long ?: System.currentTimeMillis(),
                                replies = emptyList() // No cargamos respuestas por ahora
                            )
                        }
                    } ?: emptyList()

                    val strategy = Strategy(
                        id = document.id,
                        title = document.getString("title") ?: "Sin título",
                        description = document.getString("description") ?: "Sin descripción",
                        author = document.getString("authorAlias") ?: "Anónimo",
                        avatarName = document.getString("avatarName"),
                        avatarUrl = document.getString("avatarUrl"),
                        rating = document.getDouble("rating") ?: 0.0,
                        createdBy = document.getString("createdBy") ?: "",
                        indicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        timeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        symbols = (document.get("symbols") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        algorithmCode = document.getString("algorithmCode") ?: "",
                        entryConditionImageUrl = document.getString("entryConditionImageUrl"),
                        exitConditionImageUrl = document.getString("exitConditionImageUrl"),
                        movements = (document.get("movements") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        favoritedBy = favoritedByList,
                        comments = commentsList
                    )

                    Log.d("SearchDebug", "Estrategia cargada: ${strategy.title} | Favoritos: ${favoritedByList.size} | Comentarios: ${commentsList.size}")

                    strategies.add(strategy)
                }

                Log.d("SearchDebug", "Estrategias cargadas correctamente.")
                strategyAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("SearchDebug", "Error al cargar estrategias: ${e.message}", e)
                Toast.makeText(this, "Error al cargar estrategias", Toast.LENGTH_SHORT).show()
            }
    }



    private fun performSearch() {
        Log.d("SearchDebug", "Reiniciando búsqueda en Firestore...")

        // 🔹 Cargar TODAS las estrategias antes de filtrar
        db.collection("strategies").get()
            .addOnSuccessListener { result ->
                val allStrategies = mutableListOf<Strategy>()

                for (document in result) {
                    val strategy = Strategy(
                        id = document.id,
                        title = document.getString("title") ?: "Sin título",
                        description = document.getString("description") ?: "Sin descripción",
                        author = document.getString("authorAlias") ?: "Anónimo",
                        avatarName = document.getString("avatarName"),
                        avatarUrl = document.getString("avatarUrl"),
                        rating = document.getDouble("rating") ?: 0.0,
                        createdBy = document.getString("createdBy") ?: "",
                        indicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        timeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        symbols = (document.get("symbols") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        algorithmCode = document.getString("algorithmCode") ?: "",
                        entryConditionImageUrl = document.getString("entryConditionImageUrl"),
                        exitConditionImageUrl = document.getString("exitConditionImageUrl"),
                        movements = (document.get("movements") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        favoritedBy = (document.get("favoritedBy") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        comments = (document.get("comments") as? List<*>)?.mapNotNull { comment ->
                            val commentMap = comment as? Map<*, *>
                            commentMap?.let {
                                Comment(
                                    id = it["id"] as? String ?: "",
                                    userId = it["userId"] as? String ?: "",
                                    userAlias = it["userAlias"] as? String ?: "Anónimo",
                                    avatarUrl = it["avatarUrl"] as? String,
                                    avatarName = it["avatarName"] as? String,
                                    content = it["content"] as? String ?: "",
                                    timestamp = it["timestamp"] as? Long ?: System.currentTimeMillis(),
                                    replies = emptyList()
                                )
                            }
                        } ?: emptyList()
                    )
                    allStrategies.add(strategy)
                }

                // 🔹 Aplicamos los filtros sobre TODAS las estrategias cargadas
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
                val indicatorQuery = indicatorInput.text.toString().trim()

                val filteredStrategies = allStrategies.filter { strategy ->
                    val matchesName = keywords.isEmpty() || keywords.any { keyword ->
                        strategy.title.contains(keyword, ignoreCase = true)
                    }

                    val matchesTradingStyle = selectedTradingStyles.isEmpty() || strategy.tradingStyles.any { it in selectedTradingStyles }
                    val matchesTimeframe = selectedTimeframes.isEmpty() || strategy.timeframes.any { it in selectedTimeframes }
                    val matchesBot = when {
                        includeBots && !excludeBots -> strategy.algorithmCode.isNotBlank()
                        excludeBots && !includeBots -> strategy.algorithmCode.isBlank()
                        else -> true
                    }

                    // 🔹 **Filtro por indicador corregido**
                    val matchesIndicator = indicatorQuery.isEmpty() || strategy.indicators.any { indicator ->
                        indicator.contains(indicatorQuery, ignoreCase = true)
                    }

                    matchesName && matchesTradingStyle && matchesTimeframe && matchesBot && matchesIndicator
                }.toMutableList()

                Log.d("SearchDebug", "Estrategias filtradas: ${filteredStrategies.size}")

                // 🔹 Aplicar ordenación
                when (ratingSpinner.selectedItem.toString()) {
                    "Más valoradas primero" -> filteredStrategies.sortByDescending { it.rating }
                    "Menos valoradas primero" -> filteredStrategies.sortBy { it.rating }
                    "Más veces favoritas" -> filteredStrategies.sortByDescending { it.favoritedBy.size }
                    "Más comentadas" -> filteredStrategies.sortByDescending { it.comments.size }
                    "Más movimientos" -> filteredStrategies.sortByDescending { it.movements.size }
                }

                // 🔹 Mostrar estrategias ordenadas en el Log
                filteredStrategies.forEach {
                    Log.d("SearchDebug", "Estrategia: ${it.title}, Rating: ${it.rating}, Favoritos: ${it.favoritedBy.size}, Comentarios: ${it.comments.size}, Movimientos: ${it.movements.size}")
                }

                // 🔹 Actualizar la lista de estrategias en el adaptador
                strategies.clear()
                strategies.addAll(filteredStrategies)
                strategyAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("SearchDebug", "Error en la búsqueda: ${e.message}", e)
                Toast.makeText(this, "Error al buscar estrategias", Toast.LENGTH_SHORT).show()
            }
    }




    private fun openStrategyDetail(strategy: Strategy) {
        val strategyId = strategy.id

        if (strategyId.isNotEmpty()) {
            // Ocultar RecyclerView
            findViewById<RecyclerView>(R.id.recyclerView).visibility = View.GONE
            val fragmentContainer = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.fragmentContainerView)
            fragmentContainer.visibility = View.INVISIBLE // Mantenerlo invisible al inicio

            // Crear y agregar el fragmento
            val fragment = StrategyDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("strategyId", strategy.id)
                    putString("strategyTitle", strategy.title)
                    putString("strategyDescription", strategy.description)
                    putString("strategyAuthor", strategy.author)
                    putString("strategyAvatarName", strategy.avatarName)
                    putString("strategyAvatarUrl", strategy.avatarUrl)
                    putStringArray("strategyIndicators", strategy.indicators.toTypedArray())
                    putStringArray("strategyTimeframes", strategy.timeframes.toTypedArray())
                    putStringArray("tradingStyles", strategy.tradingStyles.toTypedArray())
                    putDouble("strategyRating", strategy.rating)
                    putStringArray("strategySymbols", strategy.symbols.toTypedArray())
                    putString("algorithmCode", strategy.algorithmCode)
                }
            }

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in, // Animación de entrada
                    R.anim.fade_out, // Animación de salida
                    R.anim.fade_in, // Animación al retroceder
                    R.anim.fade_out  // Animación al salir
                )
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null) // Agrega el fragmento a la pila de retroceso
                .commit()

            // Mostrar el contenedor después de un breve retraso
            fragmentContainer.postDelayed({
                fragmentContainer.visibility = View.VISIBLE
            }, 300) // Retraso de 300 ms
        } else {
            Toast.makeText(this, "ID de estrategia no válido.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            // Restaurar el RecyclerView cuando no quedan fragmentos
            fragmentManager.executePendingTransactions()
            val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView)
            if (currentFragment == null) {
                findViewById<RecyclerView>(R.id.recyclerView).visibility = View.VISIBLE
                findViewById<androidx.fragment.app.FragmentContainerView>(R.id.fragmentContainerView).visibility =
                    View.GONE
            }
        } else {
            super.onBackPressed()
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            ) // Transición al regresar
        }
    }
}