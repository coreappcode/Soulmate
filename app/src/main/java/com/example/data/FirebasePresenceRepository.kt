package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class FirebasePresenceRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL)
) {
    private val presenceReference = database.reference.child("presence")
    private var presenceListener: ValueEventListener? = null
    private var connectionReference: DatabaseReference? = null
    private var activeUid: String? = null

    fun startObserving(
        onOnlineUsersChanged: (Set<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (presenceListener != null) return

        presenceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onOnlineUsersChanged(
                    snapshot.children.mapNotNull { user ->
                        user.key?.takeIf { user.child("connections").hasChildren() }
                    }.toSet()
                )
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        }.also(presenceReference::addValueEventListener)
    }

    fun stopObserving() {
        presenceListener?.let(presenceReference::removeEventListener)
        presenceListener = null
    }

    suspend fun setPublishingEnabled(enabled: Boolean) {
        val uid = auth.currentUser?.uid
        if (!enabled || uid == null) {
            connectionReference?.removeValue()?.await()
            connectionReference = null
            activeUid = null
            return
        }

        if (activeUid == uid && connectionReference != null) return
        connectionReference?.removeValue()?.await()
        connectionReference = null
        activeUid = uid

        val connection = presenceReference.child(uid).child("connections").push()
            ?: throw IllegalStateException("Unable to create a presence connection.")
        try {
            connection.onDisconnect().removeValue().await()
            connection.setValue(ServerValue.TIMESTAMP).await()
            connectionReference = connection
        } catch (exception: Exception) {
            activeUid = null
            throw exception
        }
    }

    companion object {
        private const val DATABASE_URL =
            "https://soulmate-e285e-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
}
