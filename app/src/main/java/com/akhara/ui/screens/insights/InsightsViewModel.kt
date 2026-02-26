package com.akhara.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhara.data.intelligence.Insight
import com.akhara.data.intelligence.InsightEngine
import com.akhara.data.intelligence.InsightType
import com.akhara.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InsightsUiState(
    val insights: List<Insight> = emptyList(),
    val isLoading: Boolean = true
)

class InsightsViewModel(repository: WorkoutRepository) : ViewModel() {

    private val engine = InsightEngine(repository)
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val insights = engine.generateAllInsights()
            _uiState.value = InsightsUiState(insights = insights, isLoading = false)
        }
    }

    fun refresh() = loadInsights()

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InsightsViewModel(repository) as T
        }
    }
}
