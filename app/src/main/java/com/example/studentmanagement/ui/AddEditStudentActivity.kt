package com.example.studentmanagement.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.studentmanagement.R
import com.example.studentmanagement.database.StudentDatabase
import com.example.studentmanagement.databinding.ActivityAddEditStudentBinding
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.repository.StudentRepository
import com.example.studentmanagement.viewmodel.StudentViewModel
import com.example.studentmanagement.viewmodel.StudentViewModelFactory

class AddEditStudentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT = "extra_student"
    }

    private lateinit var binding: ActivityAddEditStudentBinding
    private lateinit var viewModel: StudentViewModel
    private var existingStudent: Student? = null
    private var selectedAvatarUri: String = ""

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not a persistable URI
            }
            selectedAvatarUri = it.toString()
            binding.etAvatarUrl.setText(selectedAvatarUri)
            loadAvatarPreview(selectedAvatarUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = StudentDatabase.getDatabase(this, lifecycleScope)
        val repository = StudentRepository(database.studentDao())
        val factory = StudentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        existingStudent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_STUDENT, Student::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_STUDENT) as? Student
        }

        setupUI()
    }

    private fun setupUI() {
        if (existingStudent != null) {
            binding.toolbar.title = getString(R.string.title_edit_student)
            val student = existingStudent!!
            binding.etStudentId.setText(student.studentId)
            binding.etFullName.setText(student.fullName)
            binding.etEmail.setText(student.email)
            binding.etAvatarUrl.setText(student.avatarUri)
            selectedAvatarUri = student.avatarUri
            loadAvatarPreview(selectedAvatarUri)
        } else {
            binding.toolbar.title = getString(R.string.title_add_student)
        }

        binding.btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        binding.etAvatarUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputUrl = s?.toString()?.trim() ?: ""
                if (inputUrl != selectedAvatarUri) {
                    selectedAvatarUri = inputUrl
                    loadAvatarPreview(selectedAvatarUri)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            validateAndSave()
        }
    }

    private fun loadAvatarPreview(uriString: String) {
        val trimmed = uriString.trim()
        if (trimmed.isNotEmpty()) {
            Glide.with(this)
                .load(trimmed)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.ivAvatarPreview)
        } else {
            binding.ivAvatarPreview.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    private fun validateAndSave() {
        val studentId = binding.etStudentId.text?.toString()?.trim() ?: ""
        val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val avatarUri = selectedAvatarUri.trim()

        if (studentId.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.msg_fill_required_fields, Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.msg_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }

        if (existingStudent != null) {
            // EDIT mode: Show confirmation dialog "Bạn có muốn sửa thông tin SV không?"
            showEditConfirmationDialog(studentId, fullName, email, avatarUri)
        } else {
            // ADD mode: Save directly
            val newStudent = Student(
                studentId = studentId,
                fullName = fullName,
                email = email,
                avatarUri = avatarUri
            )
            viewModel.insert(newStudent)
            Toast.makeText(this, R.string.msg_student_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showEditConfirmationDialog(
        studentId: String,
        fullName: String,
        email: String,
        avatarUri: String
    ) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_confirm_edit_title)
            .setMessage(R.string.dialog_confirm_edit_msg)
            .setPositiveButton(R.string.btn_yes) { _, _ ->
                val updatedStudent = existingStudent!!.copy(
                    studentId = studentId,
                    fullName = fullName,
                    email = email,
                    avatarUri = avatarUri
                )
                viewModel.update(updatedStudent)
                Toast.makeText(this, R.string.msg_student_updated, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(R.string.btn_no, null)
            .show()
    }
}
