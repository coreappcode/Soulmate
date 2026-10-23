package com.example.calls

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

object CallPushTokenStore {
    private const val PREFERENCES = "call_push_token"
    private const val OWNER_UID = "owner_uid"
    private const val TOKEN_HASH = "token_hash"

    fun registerCurrentUser(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> register(context, uid, token) }
            .addOnFailureListener { error ->
                Log.e("CallPushTokenStore", "Could not get the Firebase messaging token.", error)
            }
    }

    fun register(context: Context, uid: String, token: String) {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val tokenHash = sha256(token)
        if (preferences.getString(OWNER_UID, null) == uid &&
            preferences.getString(TOKEN_HASH, null) == tokenHash
        ) {
            return
        }

        FirebaseFunctions.getInstance("asia-southeast1")
            .getHttpsCallable("registerCallDevice")
            .call(mapOf("token" to token))
            .addOnSuccessListener {
                preferences.edit()
                    .putString(OWNER_UID, uid)
                    .putString(TOKEN_HASH, tokenHash)
                    .apply()
            }
            .addOnFailureListener { error ->
                Log.e("CallPushTokenStore", "Could not register this device for incoming calls.", error)
            }
    }

    suspend fun ensureDeviceId(context: Context): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Sign in before starting or answering a call.")
        val token = FirebaseMessaging.getInstance().token.await()
        val tokenHash = sha256(token)
        FirebaseFunctions.getInstance("asia-southeast1")
            .getHttpsCallable("registerCallDevice")
            .call(mapOf("token" to token))
            .await()
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(OWNER_UID, uid)
            .putString(TOKEN_HASH, tokenHash)
            .apply()
        return tokenHash
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
}
