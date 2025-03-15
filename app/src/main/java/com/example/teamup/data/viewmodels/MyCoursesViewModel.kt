package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.common.MyCoursesState
import com.example.teamup.common.UiState
import com.example.teamup.data.repositories.CartRepository
import com.example.teamup.data.repositories.MyCoursesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyCoursesViewModel(
    private val repository: MyCoursesRepository,
    private val cartRepository: CartRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<MyCoursesState>> =
        MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState<MyCoursesState>>
        get() = _uiState

    fun getMyCourses() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getMyCourses().collect {
                _uiState.value = UiState.Success(MyCoursesState(it))
            }
        }
    }

    fun enroll(id: Int) {
        viewModelScope.launch {
            cartRepository.getAddedCartlist().collect {
                it.forEach { cartModel ->
                    repository.enroll(cartModel.Id)
                }

                cartRepository.removeAll()
            }

        }
    }
}