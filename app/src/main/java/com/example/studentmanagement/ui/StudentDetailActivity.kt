package com.example.studentmanagement.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.studentmanagement.R
import com.example.studentmanagement.database.StudentDatabase
import com.example.studentmanagement.databinding.ActivityStudentDetailBinding
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.repository.StudentRepository
import com.example.studentmanagement.viewmodel.StudentViewModel
import com.example.studentmanagement.viewmodel.StudentViewModelFactory
import kotlinx.coroutines.launch

class StudentDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
    }

    private lateinit var binding: ActivityStudentDetailBinding
    private lateinit var viewModel: StudentViewModel
    private var studentId: Int = -1
    private var currentStudent: Student? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        val database = StudentDatabase.getDatabase(this, lifecycleScope)
        val repository = StudentRepository(database.studentDao())
        val factory = StudentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]

        studentId = intent.getIntExtra(EXTRA_STUDENT_ID, -1)
        if (studentId == -1) {
            Toast.makeText(this, R.string.msg_student_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        loadStudentDetails()
    }

    private fun loadStudentDetails() {
        lifecycleScope.launch {
            val student = viewModel.getStudentById(studentId)
            if (student == null) {
                Toast.makeText(this@StudentDetailActivity, R.string.msg_student_not_found, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            currentStudent = student
            displayStudentInfo(student)
        }
    }

    private fun displayStudentInfo(student: Student) {
        binding.tvFullNameDetail.text = student.fullName
        binding.tvStudentIdDetail.text = student.studentId
        binding.tvEmailDetail.text = student.email

        val avatarPath = student.avatarUri.trim()
        if (avatarPath.isNotEmpty()) {
            Glide.with(this)
                .load(avatarPath)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.ivAvatarDetail)
        } else {
            binding.ivAvatarDetail.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    private fun setupButtons() {
        binding.btnEditDetail.setOnClickListener {
            currentStudent?.let { student ->
                val intent = Intent(this, AddEditStudentActivity::class.java).apply {
                    putExtra(AddEditStudentActivity.EXTRA_STUDENT, student)
                }
                startActivity(intent)
            }
        }

        binding.btnDeleteDetail.setOnClickListener {
            currentStudent?.let { student ->
                showDeleteConfirmationDialog(student)
            }
        }
    }

    private fun showDeleteConfirmationDialog(student: Student) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_confirm_delete_title)
            .setMessage(R.string.dialog_confirm_delete_msg)
            .setPositiveButton(R.string.btn_yes) { _, _ ->
                viewModel.delete(student)
                Toast.makeText(this, R.string.msg_student_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(R.string.btn_no, null)
            .show()
    }
}
