package org.wordpress.android.ui.comments.viewmodels

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.R
import org.wordpress.android.TEST_DISPATCHER
import org.wordpress.android.fluxc.model.CommentModel
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.persistence.comments.CommentsDao.CommentEntity
import org.wordpress.android.fluxc.store.CommentStore.CommentError
import org.wordpress.android.fluxc.store.CommentStore.CommentErrorType.GENERIC_ERROR
import org.wordpress.android.fluxc.store.CommentsStore
import org.wordpress.android.fluxc.store.CommentsStore.CommentsActionPayload
import org.wordpress.android.fluxc.store.CommentsStore.CommentsData.CommentsActionData
import org.wordpress.android.models.usecases.LocalCommentCacheUpdateHandler
import org.wordpress.android.test
import org.wordpress.android.ui.comments.unified.CommentEssentials
import org.wordpress.android.ui.comments.unified.CommentIdentifier.NotificationCommentIdentifier
import org.wordpress.android.ui.comments.unified.CommentIdentifier.ReaderCommentIdentifier
import org.wordpress.android.ui.comments.unified.CommentIdentifier.SiteCommentIdentifier
import org.wordpress.android.ui.comments.unified.UnifiedCommentsEditViewModel
import org.wordpress.android.ui.comments.unified.UnifiedCommentsEditViewModel.EditCommentActionEvent
import org.wordpress.android.ui.comments.unified.UnifiedCommentsEditViewModel.EditCommentActionEvent.CANCEL_EDIT_CONFIRM
import org.wordpress.android.ui.comments.unified.UnifiedCommentsEditViewModel.EditCommentActionEvent.CLOSE
import org.wordpress.android.ui.comments.unified.UnifiedCommentsEditViewModel.EditCommentActionEvent.DONE
import org.wordpress.android.ui.comments.unified.UnifiedCommentsEditViewModel.EditCommentUiState
import org.wordpress.android.ui.comments.unified.UnifiedCommentsEditViewModel.FieldType
import org.wordpress.android.ui.comments.unified.UnifiedCommentsEditViewModel.FieldType.USER_EMAIL
import org.wordpress.android.ui.comments.unified.usecase.GetCommentUseCase
import org.wordpress.android.ui.pages.SnackbarMessageHolder
import org.wordpress.android.ui.utils.UiString.UiStringRes
import org.wordpress.android.util.NetworkUtilsWrapper
import org.wordpress.android.viewmodel.ResourceProvider

@InternalCoroutinesApi
class UnifiedCommentsEditViewModelTest : BaseUnitTest() {
    @Mock lateinit var commentsStore: CommentsStore
    @Mock lateinit var resourceProvider: ResourceProvider
    @Mock lateinit var networkUtilsWrapper: NetworkUtilsWrapper
    @Mock lateinit var getCommentUseCase: GetCommentUseCase
    @Mock private lateinit var localCommentCacheUpdateHandler: LocalCommentCacheUpdateHandler

    private lateinit var viewModel: UnifiedCommentsEditViewModel

    private var uiState: MutableList<EditCommentUiState> = mutableListOf()
    private var uiActionEvent: MutableList<EditCommentActionEvent> = mutableListOf()
    private var onSnackbarMessage: MutableList<SnackbarMessageHolder> = mutableListOf()

    private val site = SiteModel().apply {
        id = LOCAL_SITE_ID
    }

    private val siteLocalCommentId = 1000
    private val siteRemoteCommentId = 4321L
    private val siteCommentIdentifier = SiteCommentIdentifier(siteLocalCommentId, siteRemoteCommentId)

    private val notificationCommentIdentifier = NotificationCommentIdentifier(NOTIFICATION_COMMENT_RAW.remoteCommentId)

    @Before
    fun setup() = test {
        whenever(commentsStore.getCommentByLocalId(siteLocalCommentId.toLong())).thenReturn(listOf(COMMENT_ENTITY))
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(true)

        viewModel = UnifiedCommentsEditViewModel(
                mainDispatcher = TEST_DISPATCHER,
                bgDispatcher = TEST_DISPATCHER,
                commentsStore = commentsStore,
                resourceProvider = resourceProvider,
                networkUtilsWrapper = networkUtilsWrapper,
                localCommentCacheUpdateHandler = localCommentCacheUpdateHandler,
                getCommentUseCase = getCommentUseCase
        )

        setupObservers()
    }

    @Test
    fun `watchers are init on view recreation`() {
        viewModel.start(site, siteCommentIdentifier)

        viewModel.start(site, siteCommentIdentifier)

        assertThat(uiState.first().shouldInitWatchers).isFalse
        assertThat(uiState.last().shouldInitWatchers).isTrue
    }

    @Test
    fun `Should display error SnackBar if mapped CommentEssentials is NOT VALID`() = test {
        whenever(commentsStore.getCommentByLocalId(siteLocalCommentId.toLong())).thenReturn(emptyList())
        viewModel.start(site, siteCommentIdentifier)
        assertThat(onSnackbarMessage.firstOrNull()).isNotNull
    }

    @Test
    fun `Should display correct SnackBar error message if mapped CommentEssentials is NOT VALID`() = test {
        whenever(commentsStore.getCommentByLocalId(siteLocalCommentId.toLong())).thenReturn(emptyList())
        viewModel.start(site, siteCommentIdentifier)
        val expected = UiStringRes(R.string.error_load_comment)
        val actual = onSnackbarMessage.first().message
        assertEquals(expected, actual)
    }

    @Test
    fun `Should show and hide progress after start`() = test {
        viewModel.start(site, siteCommentIdentifier)

        assertThat(uiState[0].showProgress).isTrue
        assertThat(uiState[2].showProgress).isFalse
    }

    @Test
    fun `Should map CommentIdentifier to CommentEssentials for SiteCommentIdentifier`() = test {
        viewModel.start(site, siteCommentIdentifier)
        assertThat(uiState[1].editedComment).isEqualTo(COMMENT_ESSENTIALS)
    }

    @Test
    fun `Should get site comment from local DB when start is called with SiteCommentIdentifier`() = test {
        viewModel.start(site, siteCommentIdentifier)
        verify(commentsStore, times(1)).getCommentByLocalId(siteLocalCommentId.toLong())
    }

    @Test
    fun `Should get notification comment from local DB when start is called with NotificationCommentIdentifier`() {
        test {
            whenever(commentsStore.getCommentByLocalSiteAndRemoteId(site.id, NOTIFICATION_COMMENT_RAW.remoteCommentId))
                    .thenReturn(listOf(COMMENT_ENTITY))
            viewModel.start(site, notificationCommentIdentifier)
            verify(commentsStore, times(1))
                    .getCommentByLocalSiteAndRemoteId(site.id, NOTIFICATION_COMMENT_RAW.remoteCommentId)
        }
    }

    @Test
    fun `Should get notification comment from remote when start is called and comment is not found in local DB`() {
        test {
            whenever(commentsStore.getCommentByLocalSiteAndRemoteId(site.id, NOTIFICATION_COMMENT_RAW.remoteCommentId))
                    .thenReturn(emptyList())
            whenever(commentsStore.fetchComment(site, NOTIFICATION_COMMENT_RAW.remoteCommentId, null))
                    .thenReturn(CommentsActionPayload(CommentsActionData(listOf(COMMENT_ENTITY), 0)))
            viewModel.start(site, notificationCommentIdentifier)
            verify(commentsStore, times(1)).fetchComment(site, NOTIFICATION_COMMENT_RAW.remoteCommentId, null)
        }
    }

    @Test
    fun `Should return default CommentEssentials if remote notification comment response data is null`() = test {
        whenever(commentsStore.getCommentByLocalSiteAndRemoteId(site.id, NOTIFICATION_COMMENT_RAW.remoteCommentId))
                .thenReturn(emptyList())
        whenever(commentsStore.fetchComment(site, NOTIFICATION_COMMENT_RAW.remoteCommentId, null))
                .thenReturn(CommentsActionPayload(null))
        viewModel.start(site, notificationCommentIdentifier)
        assertThat(uiState[1].editedComment).isEqualTo(CommentEssentials())
    }

    @Test
    fun `Should return default CommentEssentials if notification comment not found in local DB and in remote`() = test {
        whenever(commentsStore.getCommentByLocalSiteAndRemoteId(site.id, NOTIFICATION_COMMENT_RAW.remoteCommentId))
                .thenReturn(emptyList())
        whenever(commentsStore.fetchComment(site, NOTIFICATION_COMMENT_RAW.remoteCommentId, null))
                .thenReturn(CommentsActionPayload(CommentsActionData(emptyList(), 0)))
        viewModel.start(site, notificationCommentIdentifier)
        assertThat(uiState[1].editedComment).isEqualTo(CommentEssentials())
    }

    @Test
    fun `Should map CommentIdentifier to default CommentEssentials if SiteCommentIdentifier comment not found`() {
        test {
            whenever(commentsStore.getCommentByLocalId(siteLocalCommentId.toLong())).thenReturn(emptyList())
            viewModel.start(site, siteCommentIdentifier)
            assertThat(uiState[1].editedComment).isEqualTo(CommentEssentials())
        }
    }

    @Test
    fun `Should map CommentIdentifier to CommentEssentials for NotificationCommentIdentifier`() = test {
        whenever(
                commentsStore.getCommentByLocalSiteAndRemoteId(site.id, NOTIFICATION_COMMENT_RAW.remoteCommentId)
        ).thenReturn(listOf(COMMENT_ENTITY))
        viewModel.start(site, notificationCommentIdentifier)
        assertThat(uiState[1].editedComment).isEqualTo(COMMENT_ESSENTIALS)
    }

    @Test
    fun `Should map CommentIdentifier to default CommentEssentials if CommentIdentifier not handled`() {
        // ReaderCommentIdentifier is not supported by this class yet
        viewModel.start(site, ReaderCommentIdentifier(0L, 0L, 0L))
        assertThat(uiState[1].editedComment).isEqualTo(CommentEssentials())
    }

    @Test
    fun `onActionMenuClicked triggers snackbar if no network`() = test {
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(false)
        viewModel.onActionMenuClicked()
        assertThat(onSnackbarMessage.firstOrNull()).isNotNull
    }

    @Test
    fun `onActionMenuClicked triggers snackbar if comment update error for site comment type`() = test {
        whenever(commentsStore.updateEditComment(eq(site), any())).thenReturn(
                CommentsActionPayload(CommentError(GENERIC_ERROR, "error"))
        )
        viewModel.start(site, siteCommentIdentifier)
        viewModel.onActionMenuClicked()
        assertThat(onSnackbarMessage.firstOrNull()).isNotNull
    }

    @Test
    fun `onActionMenuClicked triggers snackbar if comment update error for notification comment type`() = test {
        whenever(
                commentsStore.getCommentByLocalSiteAndRemoteId(site.id, NOTIFICATION_COMMENT_RAW.remoteCommentId)
        ).thenReturn(listOf(COMMENT_ENTITY))
        whenever(commentsStore.updateEditComment(eq(site), any())).thenReturn(
                CommentsActionPayload(CommentError(GENERIC_ERROR, "error"))
        )
        viewModel.start(site, notificationCommentIdentifier)
        viewModel.onActionMenuClicked()
        assertThat(onSnackbarMessage.firstOrNull()).isNotNull
    }

    @Test
    fun `onActionMenuClicked triggers DONE action if comment update successfully for site comment type`() = test {
        whenever(commentsStore.updateEditComment(eq(site), any())).thenReturn(
                CommentsActionPayload(
                        CommentsActionData(
                                comments = emptyList(),
                                rowsAffected = 0
                        )
                )
        )
        viewModel.start(site, siteCommentIdentifier)
        viewModel.onActionMenuClicked()
        assertThat(uiActionEvent.firstOrNull()).isEqualTo(DONE)
        verify(localCommentCacheUpdateHandler).requestCommentsUpdate()
    }

    @Test
    fun `onActionMenuClicked triggers DONE action if comment update successfully for notification comment type`() {
        test {
            whenever(
                    commentsStore.getCommentByLocalSiteAndRemoteId(
                            site.id,
                            NOTIFICATION_COMMENT_RAW.remoteCommentId
                    )
            ).thenReturn(listOf(COMMENT_ENTITY))
            whenever(commentsStore.updateEditComment(eq(site), any())).thenReturn(
                    CommentsActionPayload(
                            CommentsActionData(
                                    comments = emptyList(),
                                    rowsAffected = 0
                            )
                    )
            )
            viewModel.start(site, notificationCommentIdentifier)
            viewModel.onActionMenuClicked()
            assertThat(uiActionEvent.firstOrNull()).isEqualTo(DONE)
            verify(localCommentCacheUpdateHandler).requestCommentsUpdate()
        }
    }

    @Test
    fun `onBackPressed triggers CLOSE when no edits`() {
        viewModel.start(site, siteCommentIdentifier)
        viewModel.onBackPressed()
        assertThat(uiActionEvent.firstOrNull()).isEqualTo(CLOSE)
    }

    @Test
    fun `onBackPressed triggers CANCEL_EDIT_CONFIRM when edits are present`() {
        val emailFieldType: FieldType = mock()
        whenever(emailFieldType.matches(USER_EMAIL)).thenReturn(true)
        whenever(emailFieldType.isValid).thenReturn { true }

        viewModel.start(site, siteCommentIdentifier)
        viewModel.onValidateField("edited user email", emailFieldType)
        viewModel.onBackPressed()

        assertThat(uiActionEvent.firstOrNull()).isEqualTo(CANCEL_EDIT_CONFIRM)
    }

    @Test
    fun `onConfirmEditingDiscard triggers CLOSE`() {
        viewModel.onConfirmEditingDiscard()
        assertThat(uiActionEvent.firstOrNull()).isEqualTo(CLOSE)
    }

    private fun setupObservers() {
        uiState.clear()
        uiActionEvent.clear()
        onSnackbarMessage.clear()

        viewModel.uiState.observeForever {
            uiState.add(it)
        }

        viewModel.uiActionEvent.observeForever {
            it.applyIfNotHandled {
                uiActionEvent.add(this)
            }
        }

        viewModel.onSnackbarMessage.observeForever {
            it.applyIfNotHandled {
                onSnackbarMessage.add(this)
            }
        }
    }

    companion object {
        private const val LOCAL_SITE_ID = 123

        private val COMMENT_ENTITY = CommentEntity(
                id = 1000,
                remoteCommentId = 0,
                remotePostId = 0,
                remoteParentCommentId = 0,
                localSiteId = LOCAL_SITE_ID,
                remoteSiteId = 0,
                authorUrl = "authorUrl",
                authorName = "authorName",
                authorEmail = "authorEmail",
                authorProfileImageUrl = null,
                postTitle = null,
                status = null,
                datePublished = null,
                publishedTimestamp = 0,
                content = "content",
                url = null,
                hasParent = false,
                parentId = 0,
                iLike = false
        )

        private val COMMENT_ESSENTIALS = CommentEssentials(
                commentId = COMMENT_ENTITY.id,
                userName = COMMENT_ENTITY.authorName!!,
                commentText = COMMENT_ENTITY.content!!,
                userUrl = COMMENT_ENTITY.authorUrl!!,
                userEmail = COMMENT_ENTITY.authorEmail!!
        )

        private val NOTIFICATION_COMMENT_RAW = CommentModel().apply {
            id = 1001
            remoteCommentId = 12345L
            remotePostId = 1
            remoteParentCommentId = 1
            localSiteId = LOCAL_SITE_ID
            remoteSiteId = 1
            authorUrl = "notificationAuthorUrl"
            authorName = "notificationAuthorName"
            authorEmail = "notificationAuthorEmail"
            authorProfileImageUrl = null
            postTitle = null
            status = null
            datePublished = null
            publishedTimestamp = 1
            content = "notificationContent"
            url = null
            hasParent = false
            parentId = 0
            iLike = false
        }
    }
}
