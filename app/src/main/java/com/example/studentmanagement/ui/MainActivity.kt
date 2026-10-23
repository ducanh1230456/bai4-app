package com.example.studentmanagement.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.database.StudentDatabase
import com.example.studentmanagement.databinding.ActivityMainBinding
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.repository.StudentRepository
import com.example.studentmanagement.ui.adapter.StudentAdapter
import com.example.studentmanagement.viewmodel.StudentViewModel
import com.example.studentmanagement.viewmodel.StudentViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: StudentViewModel
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = StudentDatabase.getDatabase(this, lifecycleScope)
        val repository = StudentRepository(database.studentDao())
        val factory = StudentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)

        adapter = StudentAdapter(
            onItemClick = { student ->
                openStudentDetail(student)
            },
            onEditClick = { student ->
                openAddEditStudent(student)
            },
            onDeleteClick = { student ->
                showDeleteConfirmationDialog(student)
            }
        )

        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.fabAddStudent.setOnClickListener {
            openAddEditStudent(null)
        }
    }

    private fun observeViewModel() {
        viewModel.students.observe(this) { studentList ->
            adapter.submitList(studentList)
            if (studentList.isNullOrEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvStudents.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvStudents.visibility = View.VISIBLE
            }
        }
    }

    private fun openStudentDetail(student: Student) {
        val intent = Intent(this, StudentDetailActivity::class.java).apply {
            putExtra(StudentDetailActivity.EXTRA_STUDENT_ID, student.id)
        }
        startActivity(intent)
    }

    private fun openAddEditStudent(student: Student?) {
        val intent = Intent(this, AddEditStudentActivity::class.java).apply {
            if (student != null) {
                putExtra(AddEditStudentActivity.EXTRA_STUDENT, student)
            }
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(student: Student) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_confirm_delete_title)
            .setMessage(R.string.dialog_confirm_delete_msg)
            .setPositiveButton(R.string.btn_yes) { _, _ ->
                viewModel.delete(student)
                Toast.makeText(this, R.string.msg_student_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_no, null)
            .show()
    }
}
