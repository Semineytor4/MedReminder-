package com.example.medreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reagiert auf den Neustart des Geräts.
 * Besonders wichtig ist ACTION_LOCKED_BOOT_COMPLETED:
 * Dieses Intent wird ausgelöst, wenn das Gerät gestartet wurde,
 * aber der Nutzer es noch nicht entsperrt hat.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        if (
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_BOOT_COMPLETED
        ) {
            val savedTime = ReminderStorage.loadAlarmTime(context)

            if (savedTime != null) {
                ReminderScheduler.scheduleDaily(
                    context,
                    savedTime.first,
                    savedTime.second
                )
            }
        }
    }
}