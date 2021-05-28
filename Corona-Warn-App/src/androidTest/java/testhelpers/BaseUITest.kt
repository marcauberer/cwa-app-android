package testhelpers

import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.junit.Rule
import testhelpers.viewmodels.MockViewModelModule

abstract class BaseUITest : BaseTestInstrumentation() {

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    inline fun <reified T : CWAViewModel> setupMockViewModel(factory: CWAViewModelFactory<T>) {
        MockViewModelModule.CREATORS[T::class.java] = factory
    }

    fun clearAllViewModels() {
        MockViewModelModule.CREATORS.clear()
    }
}
