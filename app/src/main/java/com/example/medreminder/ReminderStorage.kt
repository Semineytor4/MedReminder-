package com.example.medreminder

import android.content.Context

/**
 * Verwaltet die nicht-sensiblen Alarmdaten und die History.
 *
 * Die Alarmzeit wird im Device Encrypted Storage gespeichert,
 * weil sie auch direkt nach einem Neustart verfügbar sein muss.
 * Die History wird im normalen CE-Speicher gespeichert, da sie personenbezogene
 * Informationen über die Einnahme enthalten kann.
 */

object ReminderStorage {

    private const val PREF_DE = "de_alarm_data"
    private const val KEY_HOUR = "alarm_hour"
    private const val KEY_MINUTE = "alarm_minute"

    private const val PREF_HISTORY = "ce_history"
    private const val KEY_HISTORY = "history_text"

    fun saveAlarmTime(context: Context, hour: Int, minute: Int) {
        val deContext = context.createDeviceProtectedStorageContext()
        val prefs = deContext.getSharedPreferences(PREF_DE, Context.MODE_PRIVATE)

        prefs.edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
    }

    fun loadAlarmTime(context: Context): Pair<Int, Int>? {
        val deContext = context.createDeviceProtectedStorageContext()
        val prefs = deContext.getSharedPreferences(PREF_DE, Context.MODE_PRIVATE)

        if (!prefs.contains(KEY_HOUR) || !prefs.contains(KEY_MINUTE)) {
            return null
        }

        val hour = prefs.getInt(KEY_HOUR, 8)
        val minute = prefs.getInt(KEY_MINUTE, 0)

        return Pair(hour, minute)
    }

    fun addHistory(context: Context, entry: String) {
        val prefs = context.getSharedPreferences(PREF_HISTORY, Context.MODE_PRIVATE)
        val oldHistory = prefs.getString(KEY_HISTORY, "") ?: ""

        val newHistory = if (oldHistory.isBlank()) {
            entry
        } else {
            "$oldHistory\n$entry"
        }

        prefs.edit()
            .putString(KEY_HISTORY, newHistory)
            .apply()
    }

    fun readHistory(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_HISTORY, Context.MODE_PRIVATE)
        return prefs.getString(KEY_HISTORY, "") ?: ""
    }
}