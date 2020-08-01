package dev.jatzuk.mvvmrunning.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentSetupBinding
import dev.jatzuk.mvvmrunning.db.UserInfo
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var userInfo: UserInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)

        if (!userInfo.isFirstToggle) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true).build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runsFragment,
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
            if (success) findNavController().navigate(R.id.action_setupFragment_to_runsFragment)
        }
    }

    private fun writePersonalDataToSharedPreferences(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()

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

        userInfo.applyChanges(name, weight.toFloat(), false)

        requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle).text =
            context?.getString(R.string.let_s_go_username, name)
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
