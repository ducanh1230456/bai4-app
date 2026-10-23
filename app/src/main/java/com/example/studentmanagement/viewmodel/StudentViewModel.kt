package com.example.studentmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.repository.StudentRepository
import kotlinx.coroutines.launch

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    val students: LiveData<List<Student>> = _searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.getAllStudents().asLiveData()
        } else {
            repository.searchStudents(query.trim()).asLiveData()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insert(student: Student) = viewModelScope.launch {
        repository.insertStudent(student)
    }

    fun update(student: Student) = viewModelScope.launch {
        repository.updateStudent(student)
    }

    fun delete(student: Student) = viewModelScope.launch {
        repository.deleteStudent(student)
    }

    suspend fun getStudentById(id: Int): Student? {
        return repository.getStudentById(id)
    }
}
