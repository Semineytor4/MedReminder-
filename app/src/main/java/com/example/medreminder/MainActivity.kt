package com.example.medreminder

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
/**
 * MainActivity ist die zentrale Oberfläche der App.
 *
 * Hier gibt der Nutzer Medikamentenname, Dosierung und Uhrzeit ein.
 * Die Activity speichert sensible Daten im normalen CE-Speicher
 * und die Alarmzeit im DE-Speicher, damit der Alarm auch nach einem
 * Neustart im Direct-Boot-Zustand wieder geplant werden kann.
 */
class MainActivity : ComponentActivity() {

    private lateinit var editMedicationName: EditText
    private lateinit var editDosage: EditText
    private lateinit var editTime: EditText
    private lateinit var textStatus: TextView
    private lateinit var textHistory: TextView

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                textStatus.text = "Hinweis: Ohne Benachrichtigungs-Erlaubnis sieht man keine Notification."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editMedicationName = findViewById(R.id.editMedicationName)
        editDosage = findViewById(R.id.editDosage)
        editTime = findViewById(R.id.editTime)
        textStatus = findViewById(R.id.textStatus)
        textHistory = findViewById(R.id.textHistory)

        val buttonSave: Button = findViewById(R.id.buttonSave)
        val buttonTest: Button = findViewById(R.id.buttonTest)
        val buttonTaken: Button = findViewById(R.id.buttonTaken)

        requestNotificationPermissionIfNeeded()
        updateHistoryView()
        showUnlockStatus()

        buttonSave.setOnClickListener {
            saveReminder()
        }

        buttonTest.setOnClickListener {
            checkExactAlarmPermission()
            val planned = ReminderScheduler.scheduleTestInSeconds(this, 10)

            if (planned) {
                textStatus.text = "Test-Alarm wurde für 10 Sekunden geplant."
            } else {
                textStatus.text = "Exakte Alarme sind noch nicht erlaubt. Bitte in den Einstellungen erlauben."
            }
        }

        buttonTaken.setOnClickListener {
            val time = SimpleDateFormat("HH:mm", Locale.GERMANY).format(Date())
            ReminderStorage.addHistory(this, "Eingenommen um $time Uhr")
            updateHistoryView()
        }
    }

    /**
     * Liest die Eingaben des Nutzers aus, prüft die Uhrzeit und speichert die Daten.
     *
     * Medikamentenname und Dosierung werden im CE-Speicher abgelegt,
     * weil es sich um sensible Gesundheitsdaten handelt.
     * Die Uhrzeit wird zusätzlich im DE-Speicher gespeichert,
     * damit sie auch vor dem ersten Entsperren nach einem Neustart verfügbar ist.
     */

    private fun saveReminder() {
        val name = editMedicationName.text.toString().trim()
        val dosage = editDosage.text.toString().trim()
        val timeText = editTime.text.toString().trim()

        if (name.isEmpty() || dosage.isEmpty() || timeText.isEmpty()) {
            textStatus.text = "Bitte Name, Dosierung und Uhrzeit eingeben."
            return
        }

        val parsedTime = parseTime(timeText)

        if (parsedTime == null) {
            textStatus.text = "Bitte Uhrzeit im Format HH:mm eingeben, z. B. 08:05."
            return
        }

        val hour = parsedTime.first
        val minute = parsedTime.second

        MedicationPrefs.saveMedication(this, name, dosage)
        ReminderStorage.saveAlarmTime(this, hour, minute)

        checkExactAlarmPermission()
        val planned = ReminderScheduler.scheduleDaily(this, hour, minute)

        if (planned) {
            textStatus.text =
                "Gespeichert:\n$dosage $name\nAlarmzeit $timeText"
        } else {
            textStatus.text =
                "Daten gespeichert, aber exakter Alarm ist noch nicht erlaubt."
        }
    }

    private fun parseTime(value: String): Pair<Int, Int>? {
        val parts = value.split(":")
        if (parts.size != 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        if (hour !in 0..23) return null
        if (minute !in 0..59) return null

        return Pair(hour, minute)
    }

    private fun updateHistoryView() {
        val history = ReminderStorage.readHistory(this)

        textHistory.text = if (history.isBlank()) {
            "History ist noch leer."
        } else {
            "History:\n$history"
        }
    }

    /**
     * Prüft, ob der Nutzer das Gerät bereits entsperrt hat.
     * Ist das Gerät entsperrt, sind die CE-Daten verfügbar.
     * Vor dem ersten Entsperren nach einem Neustart sind nur DE-Daten verfügbar.
     */
    private fun showUnlockStatus() {
        val userManager = getSystemService(UserManager::class.java)

        val unlockedText = if (userManager.isUserUnlocked) {
            "Gerät ist entsperrt."
        } else {
            "Gerät ist noch nicht entsperrt."
        }

        textStatus.text = unlockedText
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)

            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }
}