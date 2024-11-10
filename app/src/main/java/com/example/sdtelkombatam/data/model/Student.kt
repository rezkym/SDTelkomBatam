package com.example.sdtelkombatam.data

data class Student(
    val id: String,
    val name: String,
    val nisn: String,
    val isPresent: Boolean,
    val checkInTime: String? = null,
    val photoUrl: String? = null,
    val className: String,
    val attendance: Map<String, Boolean> = emptyMap() // Riwayat kehadiran
)