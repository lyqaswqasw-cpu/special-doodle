package com.example.data.security

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * AdminAuthManager
 * Manages Firebase Authentication for Developer/Admin operations.
 * Enforces that any database write/add/edit/delete operation in the Realtime Database
 * is strictly authorized by an authenticated Admin session in Firebase Auth.
 */
object AdminAuthManager {

    private const val TAG = "AdminAuthManager"
    private const val PREFS_NAME = "foul_admin_auth_prefs"
    private const val KEY_SAVED_ADMIN_EMAIL = "saved_admin_email"

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }

    private val _currentAdmin = MutableStateFlow<FirebaseUser?>(null)
    val currentAdmin: StateFlow<FirebaseUser?> = _currentAdmin.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                _currentAdmin.value = firebaseAuth.currentUser
                Log.d(TAG, "Admin Auth state updated: ${firebaseAuth.currentUser?.email ?: firebaseAuth.currentUser?.uid ?: "Signed Out"}")
            }
            _currentAdmin.value = auth?.currentUser
        } catch (_: Exception) {}
    }

    val isAuthenticated: Boolean
        get() = try { auth?.currentUser != null } catch (_: Exception) { false }

    val adminDisplayName: String
        get() {
            val user = auth?.currentUser ?: return "غير مسجل"
            return user.email?.ifBlank { null } ?: "مسؤول (${user.uid.take(6)})"
        }

    val adminUid: String
        get() = auth?.currentUser?.uid ?: ""

    fun getSavedAdminEmail(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SAVED_ADMIN_EMAIL, "") ?: ""
    }

    fun saveAdminEmail(context: Context, email: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SAVED_ADMIN_EMAIL, email.trim())
            .apply()
    }

    /**
     * Signs in as Admin using Email & Password via Firebase Auth.
     */
    suspend fun signInAdmin(email: String, password: String, context: Context? = null): Result<FirebaseUser> {
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()

        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال البريد الإلكتروني وكلمة المرور للإدمن."))
        }

        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth غير متاح حالياً."))

        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("فشل تسجيل الدخول: لم يتم العثور على مستخدم.")
            _currentAdmin.value = user
            if (context != null) {
                saveAdminEmail(context, trimmedEmail)
            }
            Log.i(TAG, "Admin signed in successfully: ${user.email} (UID: ${user.uid})")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Admin sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Creates a new Admin account in Firebase Auth.
     */
    suspend fun createAdminAccount(email: String, password: String, context: Context? = null): Result<FirebaseUser> {
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()

        if (trimmedEmail.isBlank() || trimmedPass.length < 6) {
            return Result.failure(IllegalArgumentException("يجب أن تتكون كلمة المرور من 6 أحرف/أرقام على الأقل."))
        }

        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth غير متاح حالياً."))

        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("فشل إنشاء الحساب كمسؤول.")
            _currentAdmin.value = user
            if (context != null) {
                saveAdminEmail(context, trimmedEmail)
            }
            Log.i(TAG, "Admin account created successfully: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Admin account creation failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Signs out the current admin from Firebase Auth.
     */
    fun signOut() {
        auth?.signOut()
        _currentAdmin.value = null
        Log.i(TAG, "Admin signed out.")
    }

    /**
     * Ensures an active Firebase Auth user exists before writing to the database.
     */
    fun requireAdminAuthentication(): Result<Unit> {
        return if (isAuthenticated) {
            Result.success(Unit)
        } else {
            Result.failure(SecurityException("غير مصرح: يجب تسجيل الدخول بحساب الإدمن (Firebase Auth) أولاً قبل تنفيذ أي عملية كتابة أو تعديل."))
        }
    }

    /**
     * Helper extension to await Google Task asynchronously.
     */
    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWithException(exception)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
}
