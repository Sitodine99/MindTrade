package com.example.mindtrade

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

// Función de extensión para iniciar una Activity con transición de fade
fun AppCompatActivity.startActivityWithFade(intent: Intent) {
    val options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
    startActivity(intent, options.toBundle())
}

// Función de extensión para finalizar una Activity con transición de fade
fun AppCompatActivity.finishWithFade() {
    finish() // Llama a finish() para cerrar la Activity
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out) // Aplica la transición
}