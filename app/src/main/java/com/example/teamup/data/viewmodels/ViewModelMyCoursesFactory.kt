package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.teamup.data.repositories.CartRepository
import com.example.teamup.data.repositories.MyCoursesRepository

class ViewModelMyCoursesFactory(
    private val repository: MyCoursesRepository,
    private val cartRepository: CartRepository
) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyCoursesViewModel::class.java)) {
            return MyCoursesViewModel(repository, cartRepository) as T
        }
        throw  java.lang.IllegalArgumentException("Unknown View Model")
    }
}