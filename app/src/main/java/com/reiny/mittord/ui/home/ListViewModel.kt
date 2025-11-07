package com.reiny.mittord.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reiny.mittord.core.RunAsync
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.database.entity.SemanticObjectEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val runAsync: RunAsync,
    private val repository: DictionaryRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val state: StateFlow<List<SemanticObjectEntity>> = savedStateHandle.getStateFlow("list", emptyList())

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            repository.list().let {
                savedStateHandle["list"] = it
            }
        }
    }
}