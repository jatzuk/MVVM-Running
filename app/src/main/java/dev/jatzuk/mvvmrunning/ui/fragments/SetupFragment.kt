package dev.jatzuk.mvvmrunning.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentSetupBinding
import dev.jatzuk.mvvmrunning.other.Constants.KEY_FIRST_TIME_TOGGLE
import dev.jatzuk.mvvmrunning.other.Constants.KEY_NAME
import dev.jatzuk.mvvmrunning.other.Constants.KEY_WEIGHT
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)

        if (!isFirstAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPreferences()
            if (success) findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            else Snackbar.make(requireView(), "Please enter all the fields", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun writePersonalDataToSharedPreferences(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()

        if (name.isEmpty() or weight.isEmpty()) return false

        sharedPreferences.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        requireActivity().tvToolbarTitle.text =
            context?.getString(R.string.let_s_go_username, name)
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
