package de.rki.coronawarnapp.ui.main.home

import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.submission.ui.homecards.NoTest
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.submission.ui.homecards.TestUnregisteredCard
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.homecards.LowRiskCard
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.ui.main.home.items.DiaryCard
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.spyk
import io.mockk.verify
import org.joda.time.Instant
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest : BaseUITest() {

    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var submissionStateProvider: SubmissionStateProvider
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var testResultNotificationService: TestResultNotificationService
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var appConfigProvider: AppConfigProvider

    private lateinit var viewModel: HomeFragmentViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = homeFragmentViewModelSpy()

        with(viewModel) {
            every { tracingHeaderState } returns MutableLiveData()
            every { homeItems } returns MutableLiveData(emptyList())
            every { refreshRequiredData() } just Runs
            every { popupEvents } returns SingleLiveEvent()
            every { showLoweredRiskLevelDialog } returns MutableLiveData()
            every { observeTestResultToSchedulePositiveTestResultReminder() } just Runs
        }

        setupMockViewModel(object : HomeFragmentViewModel.Factory {
            override fun create(): HomeFragmentViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun onResumeCallsRefresh() {
        // AppTheme is required here to prevent xml inflation crash
        launchFragment<HomeFragment>(themeResId = R.style.AppTheme)
        verify(exactly = 1) { viewModel.refreshRequiredData() }
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        val headerState = MutableLiveData<TracingHeaderState>(TracingHeaderState.TracingActive)
        val lowRiskItem = LowRiskCard.Item(
            state = LowRisk(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = true,
                lastExposureDetectionTime = Instant.now(),
                allowManualUpdate = true,
                daysWithEncounters = 1,
                activeTracingDays = 1
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val homeItems = MutableLiveData(
            listOf(
                lowRiskItem,
                TestUnregisteredCard.Item(NoTest, onClickAction = {}),
                DiaryCard.Item(onClickAction = { }),
                FAQCard.Item(onClickAction = { })
            )
        )

        every { viewModel.tracingHeaderState } returns headerState
        every { viewModel.homeItems } returns homeItems
        launchFragmentInContainer2<HomeFragment>()
        Thread.sleep(1000)
        Screengrab.screenshot(HomeFragment::class.simpleName)
    }

    private fun homeFragmentViewModelSpy() = spyk(
        HomeFragmentViewModel(
            dispatcherProvider = TestDispatcherProvider,
            errorResetTool = errorResetTool,
            tracingRepository = tracingRepository,
            tracingStateProviderFactory = tracingStateProviderFactory,
            testResultNotificationService = testResultNotificationService,
            appConfigProvider = appConfigProvider,
            tracingStatus = tracingStatus,
            submissionRepository = submissionRepository,
            submissionStateProvider = submissionStateProvider,
            cwaSettings = cwaSettings
        )
    )
}

@Module
abstract class HomeFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun homeScreen(): HomeFragment
}
