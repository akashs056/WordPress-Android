package org.wordpress.android.ui.reader.discover.interests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.wordpress.android.models.ReaderTag
import org.wordpress.android.models.ReaderTagList
import org.wordpress.android.ui.reader.repository.ReaderTagRepository
import javax.inject.Inject

class ReaderInterestsViewModel @Inject constructor(
    private val readerTagRepository: ReaderTagRepository
) : ViewModel() {
    var initialized: Boolean = false

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private val selectedInterests = ReaderTagList()

    fun start() {
        if (initialized) {
            updateUiStateWithSelectedInterests()
            return
        }
        loadInterests()
    }

    private fun loadInterests() {
        viewModelScope.launch {
            val tagList = readerTagRepository.getInterests()
            if (tagList.isNotEmpty()) {
                updateUiState(UiState(transformToInterestsUiState(tagList), tagList))
                if (!initialized) {
                    initialized = true
                }
            }
        }
    }

    fun onInterestAtIndexToggled(index: Int) {
        uiState.value?.let {
            val interestAtIndex = it.interests[index]
            if (!selectedInterests.contains(interestAtIndex)) {
                selectedInterests.add(interestAtIndex)
            } else {
                selectedInterests.remove(interestAtIndex)
            }
        }
    }

    private fun transformToInterestsUiState(interests: ReaderTagList) =
        interests.map { interest ->
            InterestUiState(
                interest.tagTitle
            )
        }

    private fun updateUiStateWithSelectedInterests() {
        val uiState = uiState.value as UiState
        val updatedInterestsUiState = uiState.interestsUiState.mapIndexed { index, interestUiState ->
            interestUiState.copy(isChecked = isInterestSelected(uiState.interests[index]))
        }
        updateUiState(uiState.copy(interestsUiState = updatedInterestsUiState))
    }

    private fun updateUiState(uiState: UiState) {
        _uiState.value = uiState
    }

    private fun isInterestSelected(interest: ReaderTag) = selectedInterests.contains(interest)

    data class UiState(
        val interestsUiState: List<InterestUiState>,
        val interests: ReaderTagList
    )

    data class InterestUiState(
        val title: String,
        val isChecked: Boolean = false
    )
}
