package org.wordpress.android.ui.publicize

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.ui.publicize.services.PublicizeUpdateServicesV2
import org.wordpress.android.util.EventBusWrapper

@ExperimentalCoroutinesApi
class PublicizeListViewModelTest : BaseUnitTest() {
    private val publicizeUpdateServicesV2: PublicizeUpdateServicesV2 = mock()
    private val eventBusWrapper: EventBusWrapper = mock()
    private val classToTest = PublicizeListViewModel(
        publicizeUpdateServicesV2 = publicizeUpdateServicesV2,
        eventBusWrapper = eventBusWrapper,
        bgDispatcher = testDispatcher(),
    )

    @Test
    fun `Should call update services when onSiteAvailable is called`() {
        val siteModel = SiteModel()
        classToTest.onSiteAvailable(siteModel)
        verify(publicizeUpdateServicesV2).updateServices(eq(siteModel.siteId), any(), any())
    }

    @Test
    fun `Should call update connections when onSiteAvailable is called`() {
        val siteModel = SiteModel()
        classToTest.onSiteAvailable(siteModel)
        verify(publicizeUpdateServicesV2).updateConnections(eq(siteModel.siteId), any(), any())
    }
}
