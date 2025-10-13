package com.example.teamup.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teamup.data.repositories.*
import com.example.teamup.data.repositories.user.UserRepository
import com.example.teamup.data.sources.remote.FirebaseCompetitionDataSource
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSource
import com.example.teamup.data.sources.remote.GoogleDriveHelper
import com.example.teamup.data.sources.remote.GoogleDriveTeamDataSource
import com.example.teamup.data.viewmodels.InvitationViewModel
import com.example.teamup.data.viewmodels.JoinRequestViewModel
import com.example.teamup.data.viewmodels.NotificationViewModel
import com.example.teamup.data.viewmodels.SharedMemberViewModel
import com.example.teamup.data.viewmodels.user.ProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore

object Injection {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun getContext(): Context {
        return appContext ?: throw IllegalStateException(
            "Application context not initialized. Call Injection.initialize() first."
        )
    }

    // ===== EXISTING REPOSITORIES =====

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

    // ===== TEAM & INVITATION REPOSITORIES =====

    fun provideTeamRepository(): TeamRepository {
        return TeamRepositoryImpl.getInstance(provideGoogleDriveTeamDataSource())
    }

    private fun provideJoinRequestRepository(): JoinRequestRepository {
        return JoinRequestRepositoryImpl(FirebaseFirestore.getInstance())
    }

    private fun provideInvitationRepository(): InvitationRepository {
        return InvitationRepositoryImpl(FirebaseFirestore.getInstance())
    }

    // ===== NOTIFICATION REPOSITORY (SINGLETON) =====

    private fun provideFirebaseNotificationDataSource(): FirebaseNotificationDataSource {
        return FirebaseNotificationDataSource(FirebaseFirestore.getInstance())
    }

    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepositoryImpl.getInstance(provideFirebaseNotificationDataSource())
    }

    // ✅ USER REPOSITORY
    fun provideUserRepository(): UserRepository {
        return UserRepository()
    }


    // ===== VIEW MODELS =====

    fun provideJoinRequestViewModel(): JoinRequestViewModel {
        return JoinRequestViewModel(
            joinRequestRepository = provideJoinRequestRepository(),
            teamRepository = provideTeamRepository(),
            notificationRepository = provideNotificationRepository()
        )
    }

    @Composable
    fun provideJoinRequestViewModelWithFactory(): JoinRequestViewModel {
        return viewModel(
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return provideJoinRequestViewModel() as T
                }
            }
        )
    }

    fun provideInvitationViewModel(): InvitationViewModel {
        return InvitationViewModel(
            invitationRepository = provideInvitationRepository(),
            teamRepository = provideTeamRepository(),
            notificationRepository = provideNotificationRepository()
        )
    }

    @Composable
    fun provideInvitationViewModelWithFactory(): InvitationViewModel {
        return viewModel(
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return provideInvitationViewModel() as T
                }
            }
        )
    }

    // ✅ TAMBAH: SharedMemberViewModel (stateless, bisa baru setiap kali)
    fun provideSharedMemberViewModel(): SharedMemberViewModel {
        return SharedMemberViewModel()
    }

    fun provideNotificationViewModel(): NotificationViewModel {
        val repository = provideNotificationRepository()
        return NotificationViewModel.getInstance(repository)
    }

    // ===== COMPETITION REPOSITORIES =====

    private fun provideFirebaseCompetitionDataSource(): FirebaseCompetitionDataSource {
        return FirebaseCompetitionDataSource()
    }

    fun provideCompetitionRepository(): CompetitionRepository {
        return CompetitionRepository.getInstance()
    }

    fun provideCabangLombaRepository(): CabangLombaRepository {
        return CabangLombaRepository.getInstance()
    }



}