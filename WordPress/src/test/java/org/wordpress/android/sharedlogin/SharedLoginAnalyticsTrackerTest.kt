package org.wordpress.android.sharedlogin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.wordpress.android.analytics.AnalyticsTracker.Stat
import org.wordpress.android.sharedlogin.SharedLoginAnalyticsTracker.ErrorType
import org.wordpress.android.util.analytics.AnalyticsTrackerWrapper

class SharedLoginAnalyticsTrackerTest {
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper = mock()
    private val classToTest = SharedLoginAnalyticsTracker(analyticsTrackerWrapper)

    @Test
    fun `Should track login start correctly`() {
        classToTest.trackLoginStart()
        verify(analyticsTrackerWrapper).track(Stat.SHARED_LOGIN_START)
    }

    @Test
    fun `Should track login success correctly`() {
        classToTest.trackLoginSuccess()
        verify(analyticsTrackerWrapper).track(Stat.SHARED_LOGIN_SUCCESS)
    }

    @Test
    fun `Should track login failed WPNotLoggedInError correctly`() {
        classToTest.trackLoginFailed(ErrorType.WPNotLoggedInError)
        verify(analyticsTrackerWrapper).track(
                Stat.SHARED_LOGIN_FAILED,
                mapOf("error_type" to "wp_not_logged_in_error")
        )
    }

    @Test
    fun `Should track login failed QueryTokenError correctly`() {
        classToTest.trackLoginFailed(ErrorType.QueryTokenError)
        verify(analyticsTrackerWrapper).track(
                Stat.SHARED_LOGIN_FAILED,
                mapOf("error_type" to "query_token_error")
        )
    }
}
