package dev.jatzuk.mvvmrunning.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentSettingsBinding
import dev.jatzuk.mvvmrunning.db.UserInfo
import dev.jatzuk.mvvmrunning.other.TargetType
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var userInfo: UserInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldsFromSharedPreferences()

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.tilTargetType.visibility =
                    if (position == 3) View.INVISIBLE else View.VISIBLE
            }
        }

        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPreferences()
            if (success) {
                loadFieldsFromSharedPreferences()
                Snackbar.make(
                    requireView(),
                    getString(R.string.changes_saved),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadFieldsFromSharedPreferences() {
        binding.etName.setText(userInfo.name)
        binding.etWeight.setText(userInfo.weight.toString())


        binding.spinner.setSelection(userInfo.targetType.ordinal)
        binding.etTargetValue.setText(userInfo.targetType.value.toString())
    }

    private fun applyChangesToSharedPreferences(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        val typeString = binding.spinner.selectedItem.toString()
        val targetType = TargetType.valueOf(typeString.toUpperCase(Locale.getDefault()))
        val targetValue = binding.etTargetValue.text.toString()

        if (name.isEmpty()) {
            binding.etName.error = requireContext().getString(
                R.string.error_must_be_provided,
                requireContext().getString(R.string.name)
            )
            return false
        }

        if (weight.isEmpty()) {
            binding.etWeight.error = requireContext().getString(
                R.string.error_must_be_provided,
                requireContext().getString(R.string.weight)
            )
            return false
        }

        if (targetValue.isEmpty() && targetType != TargetType.NONE) {
            binding.etTargetValue.error = requireContext().getString(
                R.string.error_must_be_provided,
                requireContext().getString(R.string.target_value)
            )
            return false
        } else {
            if (targetType != TargetType.NONE) targetType.value = targetValue.toLong()
        }


        userInfo.applyChanges(
            name,
            weight.toFloat(),
            targetType,
            false
        )

        requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle).text =
            context?.getString(R.string.let_s_go_username, userInfo.name)

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
