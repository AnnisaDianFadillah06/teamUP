package com.example.teamup.di

import android.content.Context
import com.example.teamup.data.repositories.CabangLombaRepository
import com.example.teamup.data.repositories.CartRepository
import com.example.teamup.data.repositories.CompetitionRepository
import com.example.teamup.data.repositories.ContentRepository
import com.example.teamup.data.repositories.CoursesRepository
import com.example.teamup.data.repositories.DetailRepository
import com.example.teamup.data.repositories.MyCoursesRepository
import com.example.teamup.data.repositories.NotificationRepository
import com.example.teamup.data.repositories.TeamRepository
import com.example.teamup.data.repositories.WishlistRepository
import com.example.teamup.data.sources.remote.FirebaseCompetitionDataSource
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSource
import com.example.teamup.data.sources.remote.GoogleDriveHelper
import com.example.teamup.data.sources.remote.GoogleDriveTeamDataSource
import com.example.teamup.data.viewmodels.NotificationViewModel

object Injection {
    // Add context property to the Injection object
    private var appContext: Context? = null

    // Initialize method to set context
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun getContext(): Context {
        return appContext ?: throw IllegalStateException(
            "Application context not initialized. Call Injection.initialize() first."
        )
    }

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

    fun provideGoogleDriveHelper(): GoogleDriveHelper {
        return GoogleDriveHelper(getContext())
    }

    fun provideGoogleDriveTeamDataSource(): GoogleDriveTeamDataSource {
        return GoogleDriveTeamDataSource(getContext())
    }

    fun provideTeamRepository(): TeamRepository {
        return TeamRepository.getInstance(provideGoogleDriveTeamDataSource())
    }

    // Competition related injections
    private fun provideFirebaseCompetitionDataSource(): FirebaseCompetitionDataSource {
        return FirebaseCompetitionDataSource()
    }

    fun provideCompetitionRepository(): CompetitionRepository {
        return CompetitionRepository.getInstance()
    }

    fun provideCabangLombaRepository(): CabangLombaRepository {
        return CabangLombaRepository.getInstance()
    }

    // Notification related injections
    private fun provideFirebaseNotificationDataSource(): FirebaseNotificationDataSource {
        return FirebaseNotificationDataSource(getContext())
    }

    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepository.getInstance(provideFirebaseNotificationDataSource())
    }

    fun provideNotificationViewModel(): NotificationViewModel {
        val repository = provideNotificationRepository()
        return NotificationViewModel.getInstance(repository)
    }
}