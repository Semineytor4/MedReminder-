package com.example.medreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Plant die Medikamenten-Erinnerungen mit dem Android AlarmManager.
 *
 * Für die Fallstudie werden exakte Alarme verwendet, damit die Erinnerung
 * möglichst genau zur angegebenen Uhrzeit ausgelöst wird.
 */

object ReminderScheduler {

    private const val REQUEST_CODE_DAILY = 100
    private const val REQUEST_CODE_TEST = 101

    /**
     * Plant einen täglichen exakten Alarm zur angegebenen Uhrzeit.
     * Wenn die Uhrzeit für heute bereits vorbei ist,
     * wird der Alarm automatisch für den nächsten Tag geplant.
     */

    fun scheduleDaily(context: Context, hour: Int, minute: Int): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (!canScheduleExactAlarm(context, alarmManager)) {
            return false
        }

        val intent = Intent(context, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        return true
    }

    fun scheduleTestInSeconds(context: Context, seconds: Int): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (!canScheduleExactAlarm(context, alarmManager)) {
            return false
        }

        val intent = Intent(context, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_TEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + seconds * 1000L

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        return true
    }
    /**
     * Prüft, ob die App exakte Alarme planen darf.
     * Ab neueren Android-Versionen kann der Nutzer diese Berechtigung
     * in den Systemeinstellungen erlauben oder verweigern.
     */
    private fun canScheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}