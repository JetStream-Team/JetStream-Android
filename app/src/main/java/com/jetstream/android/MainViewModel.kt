package com.jetstream.android

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val serverIP: String = "",
    val scrollState: ScrollState = ScrollState(0)
)


class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun setServerIP(newIP: String) {
        _uiState.update {it.copy(serverIP = newIP)}
    }

    fun setScrollState(newState: ScrollState) {
        _uiState.update { it.copy(scrollState = newState) }
    }

}