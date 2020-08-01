package dev.jatzuk.mvvmrunning.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.jatzuk.mvvmrunning.R

class CancelTrackingDialog : DialogFragment() {

    private var positiveButtonListener: (() -> Unit)? = null

    fun setPositiveButtonListener(listener: () -> Unit) {
        positiveButtonListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ -> positiveButtonListener?.let { it() } }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            .create()
}
