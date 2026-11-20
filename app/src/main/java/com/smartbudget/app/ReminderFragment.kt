package com.smartbudget.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar

class ReminderFragment : Fragment(R.layout.fragment_reminders) {
    private var reminderScheduled = false
    private var requestedEnable = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && requestedEnable) {
                enableReminder()
            } else if (requestedEnable) {
                Snackbar.make(requireView(), "Notification permission is needed for reminders.", Snackbar.LENGTH_LONG).show()
            }
            requestedEnable = false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.reminderProgress)
        val status = view.findViewById<TextView>(R.id.tvReminderStatus)
        val schedule = view.findViewById<TextView>(R.id.tvReminderSchedule)
        val action = view.findViewById<MaterialButton>(R.id.btnReminderAction)
        val workInfos = androidx.work.WorkManager.getInstance(requireContext())
            .getWorkInfosForUniqueWorkLiveData(ReminderScheduler.WORK_NAME)

        workInfos.observe(viewLifecycleOwner, Observer { infos ->
            progress.visibility = View.GONE
            reminderScheduled = infos.orEmpty().any {
                it.state == WorkInfo.State.ENQUEUED ||
                    it.state == WorkInfo.State.RUNNING ||
                    it.state == WorkInfo.State.BLOCKED
            }
            if (reminderScheduled) {
                status.text = "Daily expense reminder is scheduled."
                schedule.text = "SmartBudget will remind you to log expenses about once every 24 hours. Android may adjust delivery to save battery."
                action.text = "Turn off daily reminder"
            } else {
                status.text = "No daily reminder is scheduled."
                schedule.text = "A reminder can nudge you to record your expenses. It repeats about once every 24 hours after you enable it."
                action.text = "Enable daily reminder"
            }
        })

        action.setOnClickListener {
            if (reminderScheduled) confirmDisable() else requestEnable()
        }
    }

    private fun requestEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestedEnable = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            enableReminder()
        }
    }

    private fun enableReminder() {
        try {
            ReminderScheduler.setEnabled(requireContext(), true)
            ReminderScheduler.schedule(requireContext())
            Snackbar.make(requireView(), "Daily expense reminder enabled.", Snackbar.LENGTH_SHORT).show()
        } catch (_: Exception) {
            ReminderScheduler.setEnabled(requireContext(), false)
            Snackbar.make(requireView(), "Could not schedule the reminder. Please try again.", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun confirmDisable() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Turn off reminders?")
            .setMessage("The daily expense reminder will stop until you enable it again.")
            .setNegativeButton("Keep on", null)
            .setPositiveButton("Turn off") { _, _ ->
                ReminderScheduler.setEnabled(requireContext(), false)
                ReminderScheduler.cancel(requireContext())
                Snackbar.make(requireView(), "Daily expense reminder turned off.", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }
}
