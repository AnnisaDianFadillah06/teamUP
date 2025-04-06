package com.example.teamup.data.sources.remote
//
//import android.content.Context
//import android.net.Uri
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.createSupabaseClient
//import io.github.jan.supabase.storage.Storage
//import io.github.jan.supabase.storage.storage
//import java.util.*
//
//object SupabaseManager {
//    private val supabase: SupabaseClient by lazy {
//        createSupabaseClient(
//            supabaseUrl = "https://your-project-id.supabase.co",
//            supabaseKey = "your-anon-key"
//        ) {
//            install(Storage) // ⬅️ Ini harus dikenali setelah dependency storage-kt masuk
//        }
//    }
//
//    suspend fun uploadFileFromUri(
//        context: Context,
//        uri: Uri,
//        bucket: String,
//        folder: String
//    ): String {
//        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
//            ?: throw IllegalArgumentException("Tidak bisa baca URI: $uri")
//
//        val ext = context.contentResolver.getType(uri)?.substringAfter("/") ?: "bin"
//        val fileName = "${folder}_${System.currentTimeMillis()}_${UUID.randomUUID()}.$ext"
//        val path = "$folder/$fileName"
//
//        supabase.storage[bucket].upload(path, bytes, upsert = true)
//        return supabase.storage[bucket].publicUrl(path)
//    }
//}
