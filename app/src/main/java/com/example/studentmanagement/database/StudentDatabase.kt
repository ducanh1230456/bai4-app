package com.example.studentmanagement.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.studentmanagement.model.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Student::class], version = 1, exportSchema = false)
abstract class StudentDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var INSTANCE: StudentDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): StudentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentDatabase::class.java,
                    "student_database"
                )
                    .addCallback(StudentDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class StudentDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.studentDao())
                    }
                }
            }

            suspend fun populateInitialData(studentDao: StudentDao) {
                val sample1 = Student(
                    studentId = "SV202401",
                    fullName = "Nguyễn Văn An",
                    email = "an.nguyen@student.edu.vn",
                    avatarUri = "https://i.pravatar.cc/300?img=12"
                )
                val sample2 = Student(
                    studentId = "SV202402",
                    fullName = "Trần Thị Bình",
                    email = "binh.tran@student.edu.vn",
                    avatarUri = "https://i.pravatar.cc/300?img=47"
                )
                studentDao.insertStudent(sample1)
                studentDao.insertStudent(sample2)
            }
        }
    }
}
