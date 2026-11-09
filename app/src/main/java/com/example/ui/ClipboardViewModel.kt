package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ClipboardItem
import com.example.data.ClipboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClipboardUiState(
    val items: List<ClipboardItem> = emptyList(),
    val totalCount: Int = 0,
    val pinnedCount: Int = 0,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isLoading: Boolean = false
)

class ClipboardViewModel(
    private val repository: ClipboardRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val uiState: StateFlow<ClipboardUiState> = combine(
        repository.allClips,
        repository.totalCount,
        repository.pinnedCount,
        _searchQuery,
        _selectedCategory
    ) { allClips, total, pinned, query, category ->
        val filtered = allClips.filter { item ->
            val matchesCat = when (category) {
                "All" -> true
                "Pinned" -> item.isPinned
                else -> item.category.equals(category, ignoreCase = true)
            }
            val matchesQuery = if (query.isBlank()) true else item.text.contains(query.trim(), ignoreCase = true)
            matchesCat && matchesQuery
        }
        ClipboardUiState(
            items = filtered,
            totalCount = total,
            pinnedCount = pinned,
            searchQuery = query,
            selectedCategory = category,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClipboardUiState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun insertClip(text: String, isPinned: Boolean = false, category: String = "General") {
        viewModelScope.launch {
            repository.insertClip(text, isPinned = isPinned, category = category)
        }
    }

    fun togglePin(item: ClipboardItem) {
        viewModelScope.launch {
            repository.togglePin(item)
        }
    }

    fun deleteClip(item: ClipboardItem) {
        viewModelScope.launch {
            repository.deleteClip(item)
        }
    }

    fun clearRecentClips() {
        viewModelScope.launch {
            repository.clearRecentClips()
        }
    }

    fun copyToClipboard(text: String, category: String = "General") {
        repository.copyToSystemClipboard(text, category)
    }

    class Factory(private val repository: ClipboardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClipboardViewModel(repository) as T
        }
    }
}
