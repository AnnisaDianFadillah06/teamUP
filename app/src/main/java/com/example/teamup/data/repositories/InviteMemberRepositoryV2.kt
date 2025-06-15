package com.example.teamup.data.repositories

import android.util.Log
import com.example.teamup.data.model.MemberInviteModelV2
import com.example.teamup.data.model.ProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class InviteMemberRepositoryV2 private constructor() {
    private val TAG = "InviteMemberRepositoryV2"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var instance: InviteMemberRepositoryV2? = null

        fun getInstance(): InviteMemberRepositoryV2 {
            return instance ?: synchronized(this) {
                instance ?: InviteMemberRepositoryV2().also { instance = it }
            }
        }
    }

    suspend fun getInvitationById(inviteId: String): MemberInviteModelV2? {
        return try {
            val doc = firestore.collection("invitations").document(inviteId).get().await()
            doc.toObject(MemberInviteModelV2::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPendingInvitations(): List<MemberInviteModelV2> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return emptyList()

            // Get invitations where current user is recipient and status is WAITING (pending from their perspective)
            val result = firestore.collection("invitations")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("status", "WAITING")
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                MemberInviteModelV2(
                    id = data["id"] as? String ?: "",
                    name = data["senderName"] as? String ?: "",
                    email = data["senderEmail"] as? String ?: "",
                    status = "PENDING",
                    teamId = data["teamId"] as? String ?: "",
                    teamName = data["teamName"] as? String ?: "",
                    profileImageUrl = data["senderProfileImageUrl"] as? String ?: "",
                    university = data["senderUniversity"] as? String ?: "",
                    major = data["senderMajor"] as? String ?: "",
                    skills = (data["senderSkills"] as? List<String>) ?: emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending invitations: ${e.message}")
            emptyList()
        }
    }

    suspend fun getWaitingInvitations(): List<MemberInviteModelV2> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return emptyList()

            // Get invitations where current user is sender and status is WAITING
            val result = firestore.collection("invitations")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("status", "WAITING")
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                MemberInviteModelV2(
                    id = data["id"] as? String ?: "",
                    name = data["recipientName"] as? String ?: "",
                    email = data["recipientEmail"] as? String ?: "",
                    status = "WAITING",
                    teamId = data["teamId"] as? String ?: "",
                    teamName = data["teamName"] as? String ?: "",
                    profileImageUrl = data["recipientProfileImageUrl"] as? String ?: "",
                    university = data["recipientUniversity"] as? String ?: "",
                    major = data["recipientMajor"] as? String ?: "",
                    skills = (data["recipientSkills"] as? List<String>) ?: emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting waiting invitations: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendInvitations(members: List<ProfileModel>, teamId: String, teamName: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            val currentUserId = currentUser.uid
            val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
            val currentUserData = currentUserDoc.data ?: return false

            val senderName = currentUserData["name"] as? String ?: ""
            val senderEmail = currentUserData["email"] as? String ?: ""
            val senderProfileImageUrl = currentUserData["profileImageUrl"] as? String ?: ""
            val senderUniversity = currentUserData["university"] as? String ?: ""
            val senderMajor = currentUserData["major"] as? String ?: ""
            val senderSkills = currentUserData["skills"] as? List<String> ?: emptyList()

            val batch = firestore.batch()
            val invitationsRef = firestore.collection("invitations")

            members.forEach { member ->
                val invitationId = UUID.randomUUID().toString()
                val invitationData = hashMapOf(
                    "id" to invitationId,
                    "senderId" to currentUserId,
                    "senderName" to senderName,
                    "senderEmail" to senderEmail,
                    "senderProfileImageUrl" to senderProfileImageUrl,
                    "senderUniversity" to senderUniversity,
                    "senderMajor" to senderMajor,
                    "senderSkills" to senderSkills,
                    "recipientId" to member.id,
                    "recipientName" to member.name,
                    "recipientEmail" to member.email,
                    "recipientProfileImageUrl" to member.profilePictureUrl,
                    "recipientUniversity" to member.university,
                    "recipientMajor" to member.major,
                    "recipientSkills" to member.skills,
                    "teamId" to teamId,
                    "teamName" to teamName,
                    "status" to "WAITING",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                batch.set(invitationsRef.document(invitationId), invitationData)
            }

            batch.commit().await()
            Log.d(TAG, "Successfully sent ${members.size} invitations")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending invitations: ${e.message}")
            false
        }
    }

    suspend fun acceptInvitation(inviteId: String): Boolean {
        return try {
            val invitationDoc = firestore.collection("invitations").document(inviteId).get().await()
            if (!invitationDoc.exists()) return false

            val invitationData = invitationDoc.data ?: return false
            val teamId = invitationData["teamId"] as? String ?: return false
            val senderId = invitationData["senderId"] as? String ?: return false

            // Update invitation status
            firestore.collection("invitations").document(inviteId)
                .update(
                    mapOf(
                        "status" to "ACCEPTED",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            // Update team: increment memberCount and add senderId to members
            firestore.collection("teams").document(teamId)
                .update(
                    mapOf(
                        "memberCount" to FieldValue.increment(1),
                        "members" to FieldValue.arrayUnion(senderId),
                        "isFull" to FieldValue.serverTimestamp() // Optional: Update isFull if needed
                    )
                )
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun rejectInvitation(inviteId: String): Boolean {
        return try {
            // Update invitation status
            firestore.collection("invitations").document(inviteId)
                .update(
                    mapOf(
                        "status" to "REJECTED",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting invitation: ${e.message}")
            false
        }
    }
}