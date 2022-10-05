package org.wordpress.android.userflags.resolver

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.wordpress.android.provider.query.QueryResult
import org.wordpress.android.resolver.ContentResolverWrapper
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import org.wordpress.android.userflags.JetpackLocalUserFlagsFlag
import org.wordpress.android.userflags.UserFlagsAnalyticsTracker
import org.wordpress.android.userflags.UserFlagsAnalyticsTracker.ErrorType.NoUserFlagsFoundError
import org.wordpress.android.userflags.UserFlagsAnalyticsTracker.ErrorType.QueryUserFlagsError
import org.wordpress.android.userflags.provider.UserFlagsProvider
import org.wordpress.android.util.publicdata.WordPressPublicData
import org.wordpress.android.viewmodel.ContextProvider

class UserFlagsResolverTest {
    private val jetpackLocalUserFlagsFlag: JetpackLocalUserFlagsFlag = mock()
    private val contextProvider: ContextProvider = mock()
    private val wordPressPublicData: WordPressPublicData = mock()
    private val queryResult: QueryResult = mock()
    private val contentResolverWrapper: ContentResolverWrapper = mock()
    private val appPrefsWrapper: AppPrefsWrapper = mock()
    private val userFlagsAnalyticsTracker: UserFlagsAnalyticsTracker = mock()
    private val classToTest = UserFlagsResolver(
            jetpackLocalUserFlagsFlag,
            contextProvider,
            wordPressPublicData,
            queryResult,
            contentResolverWrapper,
            appPrefsWrapper,
            userFlagsAnalyticsTracker
    )
    private val context: Context = mock()
    private val contentResolver: ContentResolver = mock()
    private val mockCursor: MatrixCursor = mock()
    private val wordPressCurrentPackageId = "packageId"
    private val uriValue = "content://$wordPressCurrentPackageId.${UserFlagsProvider::class.simpleName}"

    @Before
    fun setup() {
        whenever(contextProvider.getContext()).thenReturn(context)
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(wordPressPublicData.currentPackageId()).thenReturn(wordPressCurrentPackageId)
        whenever(mockCursor.getString(0)).thenReturn("{}")
        whenever(contentResolverWrapper.queryUri(contentResolver, uriValue)).thenReturn(mockCursor)
    }

    @Test
    fun `Should track start if feature flag is ENABLED and IS first try`() {
        featureEnabled()
        classToTest.tryGetUserFlags({}, {})
        verify(userFlagsAnalyticsTracker).trackStart()
    }

    @Test
    fun `Should trigger failure callback if feature flag is DISABLED`() {
        whenever(appPrefsWrapper.getIsFirstTryUserFlagsJetpack()).thenReturn(true)
        whenever(jetpackLocalUserFlagsFlag.isEnabled()).thenReturn(false)
        val onFailure: () -> Unit = mock()
        classToTest.tryGetUserFlags({}, onFailure)
        verify(onFailure).invoke()
    }

    @Test
    fun `Should trigger failure callback if IS NOT first try`() {
        whenever(appPrefsWrapper.getIsFirstTryUserFlagsJetpack()).thenReturn(false)
        whenever(jetpackLocalUserFlagsFlag.isEnabled()).thenReturn(true)
        val onFailure: () -> Unit = mock()
        classToTest.tryGetUserFlags({}, onFailure)
        verify(onFailure).invoke()
    }

    @Test
    fun `Should save IS NOT first try user flags as FALSE if feature flag is ENABLED and IS first try`() {
        featureEnabled()
        classToTest.tryGetUserFlags({}, {})
        verify(appPrefsWrapper).saveIsFirstTryUserFlagsJetpack(false)
    }

    @Test
    fun `Should NOT query ContentResolver if feature flag is DISABLED`() {
        whenever(appPrefsWrapper.getIsFirstTryUserFlagsJetpack()).thenReturn(true)
        whenever(jetpackLocalUserFlagsFlag.isEnabled()).thenReturn(false)
        classToTest.tryGetUserFlags({}, {})
        verify(contentResolverWrapper, never()).queryUri(any(), any())
    }

    @Test
    fun `Should NOT query ContentResolver if IS NOT the first try`() {
        whenever(appPrefsWrapper.getIsFirstTryUserFlagsJetpack()).thenReturn(false)
        whenever(jetpackLocalUserFlagsFlag.isEnabled()).thenReturn(true)
        classToTest.tryGetUserFlags({}, {})
        verify(contentResolverWrapper, never()).queryUri(any(), any())
    }

    @Test
    fun `Should query ContentResolver if feature flag is ENABLED and IS first try`() {
        featureEnabled()
        classToTest.tryGetUserFlags({}, {})
        verify(contentResolverWrapper).queryUri(contentResolver, uriValue)
    }

    @Test
    fun `Should track failed with error QueryUserFlagsError if cursor is null`() {
        featureEnabled()
        whenever(contentResolverWrapper.queryUri(contentResolver, uriValue)).thenReturn(null)
        classToTest.tryGetUserFlags({}, {})
        verify(userFlagsAnalyticsTracker).trackFailed(QueryUserFlagsError)
    }

    @Test
    fun `Should trigger failure callback if cursor is null`() {
        featureEnabled()
        whenever(contentResolverWrapper.queryUri(contentResolver, uriValue)).thenReturn(null)
        val onFailure: () -> Unit = mock()
        classToTest.tryGetUserFlags({}, onFailure)
        verify(onFailure).invoke()
    }

    @Test
    fun `Should track failed with error NoUserFlagsFoundError if user flags Map is empty`() {
        featureEnabled()
        classToTest.tryGetUserFlags({}, {})
        verify(userFlagsAnalyticsTracker).trackFailed(NoUserFlagsFoundError)
    }

    @Test
    fun `Should trigger failure callback if user flags Map is empty`() {
        featureEnabled()
        val onFailure: () -> Unit = mock()
        classToTest.tryGetUserFlags({}, onFailure)
        verify(onFailure).invoke()
    }

    @Test
    fun `Should track success if user flags Map has entries`() {
        featureEnabled()
        whenever(mockCursor.getString(0)).thenReturn("{ \"key\": \"value\" }")
        classToTest.tryGetUserFlags({}, {})
        verify(userFlagsAnalyticsTracker).trackSuccess()
    }

    @Test
    fun `Should trigger success callback if user flags Map has entries`() {
        featureEnabled()
        whenever(mockCursor.getString(0)).thenReturn("{ \"key\": \"value\" }")
        val onSuccess: () -> Unit = mock()
        classToTest.tryGetUserFlags(onSuccess) {}
        verify(onSuccess).invoke()
    }

    @Test
    fun `Should update string flag on app prefs if user flags Map has entries`() {
        featureEnabled()
        val key = "key"
        val value = "value"
        whenever(mockCursor.getString(0)).thenReturn("{ \"$key\": \"$value\" }")
        val onSuccess: () -> Unit = mock()
        classToTest.tryGetUserFlags(onSuccess) {}
        verify(appPrefsWrapper).setString(UserFlagsPrefKey(key), value)
    }

    private fun featureEnabled() {
        whenever(appPrefsWrapper.getIsFirstTryUserFlagsJetpack()).thenReturn(true)
        whenever(jetpackLocalUserFlagsFlag.isEnabled()).thenReturn(true)
    }
}
