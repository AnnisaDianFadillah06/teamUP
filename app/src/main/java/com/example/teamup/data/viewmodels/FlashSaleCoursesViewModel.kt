package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.common.UiState
import com.example.teamup.data.model.CourseModel
import com.example.teamup.data.repositories.CoursesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FlashSaleCoursesViewModel(private val repository: CoursesRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<List<CourseModel>>> =
        MutableStateFlow(UiState.Loading)

    val uiState: StateFlow<UiState<List<CourseModel>>> get() = _uiState

    fun getFlashSale() {
        viewModelScope.launch {
            repository.getFlashSale().catch {
                _uiState.value = UiState.Error(it.message.toString())
            }.collect { flashSaleCourses ->
                _uiState.value = UiState.Success(flashSaleCourses)
            }
        }
    }
}