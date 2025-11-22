package com.example.urbanbuy.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object SaldoManager {

    private fun getPrefsName(): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
        return "saldo_prefs_$uid"
    }

    private const val KEY_SALDO = "saldo"

    fun getSaldo(context: Context): Int {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SALDO, 0)
    }

    fun setSaldo(context: Context, valor: Int) {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SALDO, valor).apply()
    }

    fun sumarSaldo(context: Context, valor: Int) {
        val saldoActual = getSaldo(context)
        setSaldo(context, saldoActual + valor)
    }

    fun descontarSaldo(context: Context, valor: Int): Boolean {
        val saldoActual = getSaldo(context)
        return if (saldoActual >= valor) {
            setSaldo(context, saldoActual - valor)
            true
        } else false
    }
}



