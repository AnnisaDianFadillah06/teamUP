package com.example.teamup.di

import com.example.teamup.data.repositories.*
import com.example.teamup.data.sources.remote.FirebaseCompetitionDataSource
//import com.example.teamup.data.sources.remote.FirebaseTeamDataSource

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
    fun provideFirebaseCompetitionDataSource(): FirebaseCompetitionDataSource {
        return FirebaseCompetitionDataSource()
    }

    fun provideCompetitionRepository(): CompetitionRepository {
        return CompetitionRepository.getInstance(provideFirebaseCompetitionDataSource())
    }
}