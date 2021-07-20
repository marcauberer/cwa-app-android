package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.RatProfileCreateFragmentBinding
import de.rki.coronawarnapp.ui.view.addEmojiFilter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.LocalDate
import javax.inject.Inject

class RATProfileCreateFragment : Fragment(R.layout.rat_profile_create_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val binding: RatProfileCreateFragmentBinding by viewBinding()
    private val viewModel: RATProfileCreateFragmentViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener {
                viewModel.navigateBack()
            }

            profileSaveButton.setOnClickListener {
                it.hideKeyboard()
                viewModel.createProfile()
            }

            // Full name
            firstNameInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.firstNameChanged(it.toString()) }
            lastNameInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.lastNameChanged(it.toString()) }

            // Birth date
            birthDateInputEdit.setOnClickListener { openDatePicker() }
            birthDateInputEdit.doAfterTextChanged {
                if (it.toString().isBlank()) {
                    viewModel.birthDateChanged(null)
                }
            }

            // Address
            streetInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.streetChanged(it.toString()) }
            cityInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.cityChanged(it.toString()) }
            zipCodeInputEdit.doAfterTextChanged { viewModel.zipCodeChanged(it.toString()) }

            // Phone
            phoneInputEdit.doAfterTextChanged {
                // Propagate phone number to view model if it matches the pattern
                if (Patterns.PHONE.matcher(it.toString()).matches()) {
                    viewModel.phoneChanged(it.toString())
                } else {
                    viewModel.phoneChanged("")
                }
            }
            phoneInputEdit.setOnFocusChangeListener { _, hasFocus ->
                // Validate phone number
                if (!hasFocus && !Patterns.PHONE.matcher(phoneInputEdit.text.toString()).matches()) {
                    phoneInputLayout.error = "Invalid phone number"
                } else {
                    phoneInputLayout.error = null
                }
            }
            phoneInputEdit.addTextChangedListener(PhoneNumberFormattingTextWatcher())

            // E-mail
            emailInputEdit.addEmojiFilter().doAfterTextChanged {
                // Propagate email to view model if it matches the pattern
                if (Patterns.EMAIL_ADDRESS.matcher(it.toString()).matches()) {
                    viewModel.emailChanged(it.toString())
                } else {
                    viewModel.phoneChanged("")
                }
            }
            emailInputEdit.setOnFocusChangeListener { _, hasFocus ->
                // Validate email
                if (!hasFocus && !Patterns.EMAIL_ADDRESS.matcher(emailInputEdit.text.toString()).matches()) {
                    emailInputLayout.error = "Invalid email address"
                } else {
                    emailInputLayout.error = null
                }
            }

            viewModel.profile.observe(viewLifecycleOwner) {
                profileSaveButton.isEnabled = it?.isValid == true
            }

            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    CreateRATProfileNavigation.Back -> popBackStack()
                    CreateRATProfileNavigation.ProfileScreen -> doNavigate(
                        RATProfileCreateFragmentDirections
                            .actionRatProfileCreateFragmentToRatProfileQrCodeFragment()
                    )
                }
            }
        }

    private fun openDatePicker() {
        // Only allow date selections in the past
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        MaterialDatePicker.Builder
            .datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .build()
            .apply {
                addOnPositiveButtonClickListener { timestamp ->
                    val localDate = LocalDate(timestamp)
                    binding.birthDateInputEdit.setText(
                        localDate.toDayFormat()
                    )
                    viewModel.birthDateChanged(localDate)
                }
            }
            .show(childFragmentManager, "RATProfileCreateFragment.MaterialDatePicker")
    }
}
