package com.example.teamup.data.model

data class ContentModel(
    val Id: Int,
    val CourseId: Int,
    val Title: String,
    val TotalTime: String,
    val Sections: List<SectionModel>
)
