package com.example.medreminder

import android.content.Context

data class Medication(
    val name: String,
    val dosage: String
)
/**
 * Datenklasse für die sensiblen Medikamenteninformationen.
 *
 * Diese Informationen dürfen erst nach dem Entsperren des Geräts
 * verwendet werden, weil sie im Credential Encrypted Storage liegen.
 */
object MedicationPrefs {

    private const val PREF_NAME = "ce_medication_data"
    private const val KEY_NAME = "medication_name"
    private const val KEY_DOSAGE = "medication_dosage"

    fun saveMedication(context: Context, name: String, dosage: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_DOSAGE, dosage)
            .apply()
    }

    fun loadMedication(context: Context): Medication? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val name = prefs.getString(KEY_NAME, null)
        val dosage = prefs.getString(KEY_DOSAGE, null)

        if (name == null || dosage == null) {
            return null
        }

        return Medication(name, dosage)
    }
}