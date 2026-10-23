package com.example.calls

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

data class CallRecord(
    val id: String,
    val callerId: String,
    val calleeId: String,
    val callerDeviceId: String,
    val acceptedDeviceId: String?,
    val callType: String,
    val status: String,
    val expiresAtMillis: Long
)

data class LiveKitCredentials(
    val url: String,
    val token: String,
    val callType: String
)

class CallRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("asia-southeast1")
) {
    suspend fun createCall(calleeId: String, callType: String, callerDeviceId: String): String {
        val response = callable(
            "createCall",
            mapOf(
                "calleeId" to calleeId,
                "callType" to callType,
                "callerDeviceId" to callerDeviceId
            )
        )
        return response.string("callId")
    }

    suspend fun respondToCall(callId: String, accept: Boolean, deviceId: String?) {
        callable(
            "respondToCall",
            mapOfNotNull("callId" to callId, "accept" to accept, "deviceId" to deviceId)
        )
    }

    suspend fun getCredentials(callId: String, deviceId: String): LiveKitCredentials {
        val response = callable("getCallToken", mapOf("callId" to callId, "deviceId" to deviceId))
        return LiveKitCredentials(
            url = response.string("url"),
            token = response.string("token"),
            callType = response.string("callType")
        )
    }

    suspend fun heartbeat(callId: String, deviceId: String) {
        callable("heartbeatCall", mapOf("callId" to callId, "deviceId" to deviceId))
    }

    suspend fun endCall(callId: String) {
        callable("endCall", mapOf("callId" to callId))
    }

    fun observeCall(
        callId: String,
        onCall: (CallRecord) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        firestore.collection("calls").document(callId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else if (snapshot == null || !snapshot.exists()) {
                    onError(IllegalStateException("This call no longer exists."))
                } else {
                    val expiresAt = snapshot.getTimestamp("expiresAt")
                    val callerId = snapshot.getString("callerId")
                    val calleeId = snapshot.getString("calleeId")
                    val callerDeviceId = snapshot.getString("callerDeviceId")
                    val callType = snapshot.getString("callType")
                    val status = snapshot.getString("status")
                    if (expiresAt == null || callerId.isNullOrBlank() ||
                        calleeId.isNullOrBlank() || callerDeviceId.isNullOrBlank() ||
                        callType.isNullOrBlank() ||
                        status.isNullOrBlank()
                    ) {
                        onError(IllegalStateException("The call data is incomplete."))
                    } else {
                        onCall(
                            CallRecord(
                                id = snapshot.id,
                                callerId = callerId,
                                calleeId = calleeId,
                                callerDeviceId = callerDeviceId,
                                acceptedDeviceId = snapshot.getString("acceptedDeviceId"),
                                callType = callType,
                                status = status,
                                expiresAtMillis = expiresAt.toDate().time
                            )
                        )
                    }
                }
            }

    private suspend fun callable(name: String, data: Map<String, Any>): Map<*, *> {
        val result = functions.getHttpsCallable(name).call(data).await()
        return result.data as? Map<*, *>
            ?: throw IllegalStateException("The call service returned an invalid response.")
    }

    private fun Map<*, *>.string(key: String): String =
        this[key] as? String
            ?: throw IllegalStateException("The call service response is missing $key.")

    private fun mapOfNotNull(vararg entries: Pair<String, Any?>): Map<String, Any> =
        entries.mapNotNull { (key, value) -> value?.let { key to it } }.toMap()
}
