package de.rki.coronawarnapp.vaccination.ui.list

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentVaccinationListBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import javax.inject.Inject

class VaccinationListFragment : Fragment(R.layout.fragment_vaccination_list), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<VaccinationListFragmentArgs>()
    private val binding: FragmentVaccinationListBinding by viewBindingLazy()
    private val viewModel: VaccinationListViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as VaccinationListViewModel.Factory
            factory.create(
                vaccinatedPersonIdentifier = args.vaccinatedPersonId
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }

            viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
                val adapter = VaccinationListAdapter(uiState.listItems) { vaccinationItem ->
                    doNavigate(
                        VaccinationListFragmentDirections.actionVaccinationListFragmentToVaccinationDetailsFragment(
                            vaccinationItem.vaccinationCertificateId
                        )
                    )
                }
                recyclerViewVaccinationList.adapter = adapter

                val isVaccinationComplete = uiState.vaccinationStatus == VaccinatedPerson.Status.COMPLETE

                val background = if (isVaccinationComplete) {
                    R.drawable.vaccination_compelete_gradient
                } else {
                    R.drawable.vaccination_incomplete
                }
                expandedImage.setImageResource(background)

                subtitle.isVisible = isVaccinationComplete

                appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                    title.alpha = titleAlpha
                    subtitle.alpha = subtitleAlpha
                }

                setToolbarOverlay(isVaccinationComplete)
            }

            registerNewVaccinationButton.setOnClickListener {
                Toast.makeText(requireContext(), "TODO \uD83D\uDEA7", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setToolbarOverlay(isVaccinationComplete: Boolean) {

        // subtitle is only visible when vaccination is complete
        val bottomTextView = if (isVaccinationComplete) binding.subtitle else binding.title

        val deviceWidth = requireContext().resources.displayMetrics.widthPixels

        val params: CoordinatorLayout.LayoutParams = binding.recyclerViewVaccinationList.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = bottomTextView.layoutParams as (LinearLayout.LayoutParams)

        textParams.bottomMargin = (deviceWidth / 3) - 24 /* 24 is space between screen border and Card */
        bottomTextView.requestLayout()

        val behavior: AppBarLayout.ScrollingViewBehavior = params.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (deviceWidth / 3) - 24
    }
}