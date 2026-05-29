package com.example.medreminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserManager

/**
 * Wird vom AlarmManager aufgerufen, wenn eine Medikamenten-Erinnerung fällig ist.
 *
 * Diese Klasse enthält die wichtigste Datenschutzlogik der App:
 * Vor dem ersten Entsperren wird nur ein neutraler Hinweis angezeigt.
 * Nach dem Entsperren dürfen Medikamentenname und Dosierung angezeigt werden.
 */

class AlarmReceiver : BroadcastReceiver() {

    /**
     * Reagiert auf den ausgelösten Alarm.
     * Zuerst wird geprüft, ob der Nutzer das Gerät bereits entsperrt hat.
     * Danach wird abhängig vom Zustand entweder eine neutrale oder konkrete
     * Notification erstellt.
     */
    override fun onReceive(context: Context, intent: Intent?) {
        val userManager = context.getSystemService(UserManager::class.java)
        val userUnlocked = userManager.isUserUnlocked

        val notificationText = if (userUnlocked) {
            val medication = MedicationPrefs.loadMedication(context)

            if (medication != null) {
                "Bitte nehmen Sie jetzt ${medication.dosage} ${medication.name} ein."
            } else {
                "Zeit für Ihre Medikamente!"
            }
        } else {
            "Zeit für Ihre Medikamente!"
        }

        showNotification(context, notificationText)

        val savedTime = ReminderStorage.loadAlarmTime(context)
        if (savedTime != null) {
            ReminderScheduler.scheduleDaily(context, savedTime.first, savedTime.second)
        }
    }
    /**
     * Erstellt und zeigt die Benachrichtigung für die Medikamenten-Erinnerung.
     * Die Notification enthält zusätzlich eine Aktion "Eingenommen",
     * damit der Nutzer die Einnahme direkt bestätigen kann.
     */
    private fun showNotification(context: Context, text: String) {
        val channelId = "med_reminder_channel"
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Medikamenten-Erinnerungen",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(channel)

        val takenIntent = Intent(context, TakenReceiver::class.java)

        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            200,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("MedReminder")
            .setContentText(text)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_agenda,
                "Eingenommen",
                takenPendingIntent
            )
            .build()

        notificationManager.notify(1, notification)
    }
}