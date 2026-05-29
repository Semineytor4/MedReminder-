package com.example.medreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TakenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val time = SimpleDateFormat("HH:mm", Locale.GERMANY).format(Date())
        ReminderStorage.addHistory(context, "Eingenommen um $time Uhr")
    }
}