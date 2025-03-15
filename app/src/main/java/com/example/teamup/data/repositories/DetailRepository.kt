package com.example.teamup.data.repositories

import com.example.teamup.data.model.CourseModel
import com.example.teamup.data.sources.CourseData

class DetailRepository {
    fun getById(id: Int): CourseModel {
        return CourseData.data.first {
            it.id == id
        }
    }

    companion object {
        @Volatile
        private var instance: DetailRepository? = null

        fun getInstance(): DetailRepository = instance ?: synchronized(this) {
            DetailRepository().apply {
                instance = this
            }
        }
    }
}