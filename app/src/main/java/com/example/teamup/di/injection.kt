package com.example.teamup.di

import com.example.teamup.data.repositories.CartRepository
import com.example.teamup.data.repositories.CompetitionRepository
import com.example.teamup.data.repositories.ContentRepository
import com.example.teamup.data.repositories.CoursesRepository
import com.example.teamup.data.repositories.DetailRepository
import com.example.teamup.data.repositories.MyCoursesRepository
import com.example.teamup.data.repositories.TeamRepository
import com.example.teamup.data.repositories.WishlistRepository
import com.example.teamup.data.sources.remote.FirebaseCompetitionDataSource
import com.example.teamup.data.sources.remote.FirebaseStorageHelper
import com.example.teamup.data.sources.remote.FirebaseTeamDataSource

object Injection {
    fun provideCourseRepository(): CoursesRepository {
        return CoursesRepository.getInstance()
    }

    fun provideContentRepository(): ContentRepository {
        return ContentRepository.getInstance()
    }

    fun provideWishlistRepository(): WishlistRepository {
        return WishlistRepository.getInstance()
    }

    fun provideDetailRepository(): DetailRepository {
        return DetailRepository.getInstance()
    }

    fun provideCartRepository(): CartRepository {
        return CartRepository.getInstance()
    }

    fun provideMyCoursesRepository(): MyCoursesRepository {
        return MyCoursesRepository.getInstance()
    }

//    fun provideFirebaseTeamDataSource(): FirebaseTeamDataSource {
//        return FirebaseTeamDataSource()
//    }
//
//    fun provideTeamRepository(): TeamRepository {
//        return TeamRepository.getInstance(provideFirebaseTeamDataSource())
//    }

    // Competition related injections
    private fun provideFirebaseCompetitionDataSource(): FirebaseCompetitionDataSource {
        return FirebaseCompetitionDataSource()
    }

    fun provideCompetitionRepository(): CompetitionRepository {
        return CompetitionRepository.getInstance()
    }
    fun provideFirebaseTeamDataSource(): FirebaseTeamDataSource {
        return FirebaseTeamDataSource()
    }

    fun provideTeamRepository(): TeamRepository {
        return TeamRepository.getInstance(provideFirebaseTeamDataSource())
    }

    fun provideFirebaseStorageHelper(): FirebaseStorageHelper {
        return FirebaseStorageHelper()
    }
}