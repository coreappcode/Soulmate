package com.example.calls

import android.content.Context
import android.content.Intent
import android.util.Log
import io.livekit.android.LiveKit
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CallSessionState(
    val callId: String? = null,
    val deviceId: String? = null,
    val callRecord: CallRecord? = null,
    val callType: String = "audio",
    val isIncoming: Boolean = false,
    val remoteName: String = "",
    val remotePhoto: String = "",
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val microphoneEnabled: Boolean = true,
    val cameraEnabled: Boolean = false,
    val localVideoTrack: VideoTrack? = null,
    val remoteVideoTrack: VideoTrack? = null,
    val error: String? = null
)

class LiveKitCallSession(
    context: Context,
    private val repository: CallRepository = CallRepository()
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(CallSessionState())
    val state: StateFlow<CallSessionState> = _state.asStateFlow()

    private var room: Room? = null
    private var callListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var roomEventsJob: Job? = null
    private var heartbeatJob: Job? = null
    private var ringTimeoutJob: Job? = null
    private var connectionStarted = false
    val currentRoom: Room?
        get() = room

    fun attachIncomingCall(callId: String, callerName: String, callerPhoto: String) {
        _state.update {
            it.copy(
                callId = callId,
                isIncoming = true,
                remoteName = callerName,
                remotePhoto = callerPhoto,
                error = null
            )
        }
        scope.launch {
            try {
                val deviceId = CallPushTokenStore.ensureDeviceId(appContext)
                _state.update { it.copy(deviceId = deviceId) }
                listenToCall(callId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update {
                    it.copy(error = error.localizedMessage ?: "Could not prepare this device for calls.")
                }
            }
        }
    }

    fun startOutgoingCall(calleeId: String, calleeName: String, calleePhoto: String, callType: String) {
        _state.update {
            it.copy(
                isIncoming = false,
                callType = callType,
                remoteName = calleeName,
                remotePhoto = calleePhoto,
                error = null
            )
        }
        scope.launch {
            try {
                val deviceId = CallPushTokenStore.ensureDeviceId(appContext)
                _state.update { it.copy(deviceId = deviceId) }
                val callId = repository.createCall(calleeId, callType, deviceId)
                _state.update { it.copy(callId = callId) }
                listenToCall(callId)
                startRingTimeout(callId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(error = error.localizedMessage ?: "Could not start the call.") }
            }
        }
    }

    fun answerCall() {
        val callId = _state.value.callId ?: return
        scope.launch {
            try {
                val deviceId = CallPushTokenStore.ensureDeviceId(appContext)
                _state.update { it.copy(deviceId = deviceId) }
                _state.update { it.copy(error = null, isConnecting = true) }
                repository.respondToCall(callId, accept = true, deviceId = deviceId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isConnecting = false,
                        error = error.localizedMessage ?: "Could not answer the call."
                    )
                }
            }
        }
    }

    fun declineCall(onComplete: () -> Unit = {}) {
        val callId = _state.value.callId ?: return
        scope.launch {
            try {
                repository.respondToCall(callId, accept = false, deviceId = _state.value.deviceId)
                _state.update { it.copy(error = null) }
                releaseMedia()
                onComplete()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(error = error.localizedMessage ?: "Could not decline the call.") }
            }
        }
    }

    fun endCall(onComplete: () -> Unit = {}) {
        val callId = _state.value.callId
        scope.launch {
            try {
                if (callId != null) repository.endCall(callId)
                _state.update { it.copy(error = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(error = error.localizedMessage ?: "Could not end the call.") }
            } finally {
                releaseMedia()
                onComplete()
            }
        }
    }

    fun toggleMicrophone() {
        val currentRoom = room ?: return
        scope.launch {
            try {
                val enabled = !currentRoom.localParticipant.isMicrophoneEnabled
                currentRoom.localParticipant.setMicrophoneEnabled(enabled)
                _state.update { it.copy(microphoneEnabled = enabled, error = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(error = error.localizedMessage ?: "Could not change microphone state.") }
            }
        }
    }

    fun toggleCamera() {
        val currentRoom = room ?: return
        scope.launch {
            try {
                val enabled = !currentRoom.localParticipant.isCameraEnabled
                currentRoom.localParticipant.setCameraEnabled(enabled)
                refreshTracks()
                _state.update { it.copy(cameraEnabled = enabled, error = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(error = error.localizedMessage ?: "Could not change camera state.") }
            }
        }
    }

    fun close() {
        callListener?.remove()
        callListener = null
        ringTimeoutJob?.cancel()
        releaseMedia()
        scope.cancel()
    }

    private fun listenToCall(callId: String) {
        callListener?.remove()
        callListener = repository.observeCall(
            callId = callId,
            onCall = { call ->
                val current = _state.value
                _state.update {
                    it.copy(
                        callRecord = call,
                        callType = call.callType,
                        remoteName = it.remoteName.ifBlank {
                            if (it.isIncoming) "Soulmate" else it.remoteName
                        }
                    )
                }
                when (call.status) {
                    "accepted" -> {
                        ringTimeoutJob?.cancel()
                        val deviceId = _state.value.deviceId
                        val authorizedDeviceId = if (_state.value.isIncoming) {
                            call.acceptedDeviceId
                        } else {
                            call.callerDeviceId
                        }
                        if (deviceId != null && deviceId != authorizedDeviceId) {
                            _state.update {
                                it.copy(
                                    callRecord = call.copy(status = "ended"),
                                    error = "This call was answered on another device."
                                )
                            }
                            releaseMedia()
                        } else {
                            connectToRoom(callId)
                        }
                    }
                    "declined", "ended", "missed" -> {
                        ringTimeoutJob?.cancel()
                        releaseMedia()
                    }
                    "ringing" -> {
                        if (current.callRecord?.status != "ringing") startRingTimeout(callId)
                        if (call.expiresAtMillis <= System.currentTimeMillis()) endCall()
                    }
                }
            },
            onError = { error ->
                _state.update { it.copy(error = error.localizedMessage ?: "Could not load this call.") }
            }
        )
    }

    private fun startRingTimeout(callId: String) {
        ringTimeoutJob?.cancel()
        ringTimeoutJob = scope.launch {
            delay(45_000)
            if (_state.value.callRecord?.status == "ringing") {
                try {
                    repository.endCall(callId)
                } catch (error: Exception) {
                    Log.e("LiveKitCallSession", "Could not expire a ringing call.", error)
                }
                _state.update {
                    it.copy(error = "No answer. The call has ended.")
                }
                releaseMedia()
            }
        }
    }

    private fun connectToRoom(callId: String) {
        if (connectionStarted || _state.value.isConnected) return
        connectionStarted = true
        scope.launch {
            try {
                _state.update { it.copy(isConnecting = true, error = null) }
                val deviceId = _state.value.deviceId
                    ?: CallPushTokenStore.ensureDeviceId(appContext)
                _state.update { it.copy(deviceId = deviceId) }
                val credentials = repository.getCredentials(callId, deviceId)
                val newRoom = LiveKit.create(appContext = appContext)
                room = newRoom
                roomEventsJob = scope.launch {
                    newRoom.events.collect {
                        refreshTracks()
                    }
                }
                newRoom.connect(url = credentials.url, token = credentials.token)
                newRoom.localParticipant.setMicrophoneEnabled(true)
                if (credentials.callType == "video") {
                    newRoom.localParticipant.setCameraEnabled(true)
                }
                refreshTracks()
                _state.update {
                    it.copy(
                        callType = credentials.callType,
                        isConnecting = false,
                        isConnected = true,
                        microphoneEnabled = true,
                        cameraEnabled = credentials.callType == "video",
                        error = null
                    )
                }
                startForegroundService(callId, credentials.callType == "video")
                startHeartbeat(callId)
            } catch (cancelled: CancellationException) {
                connectionStarted = false
                throw cancelled
            } catch (error: Exception) {
                connectionStarted = false
                _state.update {
                    it.copy(
                        isConnecting = false,
                        isConnected = false,
                        error = error.localizedMessage ?: "Could not connect the call."
                    )
                }
                try {
                    repository.endCall(callId)
                } catch (endError: Exception) {
                    Log.e("LiveKitCallSession", "Could not release the call after a connection failure.", endError)
                }
                releaseMedia()
            }
        }
    }

    private fun refreshTracks() {
        val currentRoom = room ?: return
        val localTrack = currentRoom.localParticipant
            .getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack
        val remoteTrack = currentRoom.remoteParticipants.values
            .firstNotNullOfOrNull { participant ->
                participant.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack
            }
        _state.update {
            it.copy(
                localVideoTrack = localTrack,
                remoteVideoTrack = remoteTrack,
                microphoneEnabled = currentRoom.localParticipant.isMicrophoneEnabled,
                cameraEnabled = currentRoom.localParticipant.isCameraEnabled
            )
        }
    }

    private fun startHeartbeat(callId: String) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && _state.value.isConnected) {
                delay(30_000)
                try {
                    val deviceId = _state.value.deviceId
                        ?: throw IllegalStateException("The call device is no longer registered.")
                    repository.heartbeat(callId, deviceId)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _state.update {
                        it.copy(error = error.localizedMessage ?: "The call connection expired.")
                    }
                    endCall()
                    break
                }
            }
        }
    }

    private fun startForegroundService(callId: String, isVideo: Boolean) {
        val intent = Intent(appContext, CallForegroundService::class.java)
            .putExtra(CallActivity.EXTRA_CALL_ID, callId)
            .putExtra(CallActivity.EXTRA_CALL_TYPE, if (isVideo) "video" else "audio")
            .putExtra(CallActivity.EXTRA_CALLER_NAME, _state.value.remoteName)
        appContext.startForegroundService(intent)
    }

    private fun releaseMedia() {
        heartbeatJob?.cancel()
        roomEventsJob?.cancel()
        roomEventsJob = null
        room?.let { currentRoom ->
            currentRoom.disconnect()
            currentRoom.release()
        }
        room = null
        connectionStarted = false
        appContext.stopService(Intent(appContext, CallForegroundService::class.java))
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false,
                localVideoTrack = null,
                remoteVideoTrack = null
            )
        }
    }
}
