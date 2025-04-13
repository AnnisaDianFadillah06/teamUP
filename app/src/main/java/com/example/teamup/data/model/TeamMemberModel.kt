package com.example.teamup.data.model

import com.google.firebase.firestore.DocumentId

data class TeamMemberModel(
    @DocumentId val id: String = "", // ID dokumen otomatis dari Firestore
    val userId: String = "", // ID referensi ke User
    val teamId: String = "", // ID referensi ke Team
    val name: String = "", // Nama anggota tim
    val profileImage: Int = 0, // Resource ID untuk avatar/gambar profil
    val role: String = "", // Role: "Admin" atau "Member"
    val email: String = "", // nnti ini harusnya ada di tabel user jdi nnti dihapus aja di teammember yaa tpi relasiin
    val isJoined: Boolean = false,
    val isFull: Boolean = false,
    val memberCount: Int = 0,
    val maxMembers: Int = 5,
    val category: String = "",
)