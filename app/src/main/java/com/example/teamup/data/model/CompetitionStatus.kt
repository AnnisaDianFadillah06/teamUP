// First let's create an enum class for our status options
package com.example.teamup.data.model

enum class CompetitionVisibilityStatus(val value: String) {
    PUBLISHED("Published"),
    DRAFT("Draft"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String): CompetitionVisibilityStatus {
            return values().find { it.value == value } ?: PUBLISHED
        }
    }
}

enum class CompetitionActivityStatus(val value: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    companion object {
        fun fromString(value: String): CompetitionActivityStatus {
            return values().find { it.value == value } ?: ACTIVE
        }
    }
}