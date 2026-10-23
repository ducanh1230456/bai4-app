package com.example.studentmanagement.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: String,
    val fullName: String,
    val email: String,
    val avatarUri: String
) : Serializable
