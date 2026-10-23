package com.example.calls

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.VideoTrack
import livekit.org.webrtc.RendererCommon
import kotlinx.coroutines.delay

private enum class PermissionAction {
    START_OUTGOING,
    ANSWER
}

@Composable
fun CallScreen(
    callId: String?,
    calleeId: String?,
    callType: String,
    remoteName: String,
    remotePhoto: String,
    isIncoming: Boolean,
    autoAnswer: Boolean,
    autoDecline: Boolean,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val session = remember { LiveKitCallSession(context) }
    val state by session.state.collectAsState()
    var pendingPermissionAction by remember { mutableStateOf<PermissionAction?>(null) }
    var permissionError by remember { mutableStateOf<String?>(null) }
    val requestedPermissions = remember(callType) {
        buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (callType == "video") add(Manifest.permission.CAMERA)
        }.toTypedArray()
    }

    fun permissionsGranted(): Boolean = requestedPermissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = requestedPermissions.all { permission ->
            results[permission] == true ||
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        val action = pendingPermissionAction
        pendingPermissionAction = null
        if (granted && action != null) {
            when (action) {
                PermissionAction.START_OUTGOING -> {
                    val targetId = calleeId
                    if (targetId.isNullOrBlank()) {
                        permissionError = "The person to call could not be found."
                    } else {
                        session.startOutgoingCall(targetId, remoteName, remotePhoto, callType)
                    }
                }
                PermissionAction.ANSWER -> session.answerCall()
            }
        } else if (!granted) {
            permissionError = "Microphone and camera access are required to place or answer this call."
        }
    }

    fun performAction(action: PermissionAction) {
        pendingPermissionAction = action
        if (permissionsGranted()) {
            pendingPermissionAction = null
            when (action) {
                PermissionAction.START_OUTGOING -> {
                    val targetId = calleeId
                    if (targetId.isNullOrBlank()) {
                        permissionError = "The person to call could not be found."
                    } else {
                        session.startOutgoingCall(targetId, remoteName, remotePhoto, callType)
                    }
                }
                PermissionAction.ANSWER -> session.answerCall()
            }
        } else {
            permissionLauncher.launch(requestedPermissions)
        }
    }

    DisposableEffect(session) {
        onDispose {
            session.close()
        }
    }

    LaunchedEffect(session, isIncoming, callId) {
        if (isIncoming) {
            if (callId.isNullOrBlank()) {
                permissionError = "This incoming call is no longer available."
                return@LaunchedEffect
            }
            session.attachIncomingCall(callId, remoteName, remotePhoto)
            if (autoDecline) {
                session.declineCall(onComplete = onFinish)
            } else if (autoAnswer) {
                performAction(PermissionAction.ANSWER)
            }
        } else {
            performAction(PermissionAction.START_OUTGOING)
        }
    }

    LaunchedEffect(state.callId) {
        state.callId?.let { CallNotificationManager.dismiss(context, it) }
    }

    LaunchedEffect(state.callRecord?.status) {
        if (state.callRecord?.status in setOf("declined", "ended", "missed")) {
            delay(1_200)
            onFinish()
        }
    }

    BackHandler {
        session.endCall(onComplete = onFinish)
    }

    val currentCallType = state.callRecord?.callType ?: callType
    val statusText = when (state.callRecord?.status) {
        "ringing" -> if (isIncoming) "Incoming ${currentCallType} call" else "Calling…"
        "accepted" -> if (state.isConnected) "Connected" else "Connecting…"
        "declined" -> "Call declined"
        "missed" -> "Missed call"
        "ended" -> "Call ended"
        else -> if (isIncoming) "Incoming ${currentCallType} call" else "Starting call…"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF08090D)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (currentCallType == "video" && state.isConnected) {
                val currentRoom = session.currentRoom
                if (currentRoom != null && state.remoteVideoTrack != null) {
                    LiveKitVideoRenderer(
                        room = currentRoom,
                        track = state.remoteVideoTrack,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    VideoWaitingBackground()
                }

                if (currentRoom != null && state.localVideoTrack != null) {
                    LiveKitVideoRenderer(
                        room = currentRoom,
                        track = state.localVideoTrack,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 42.dp, end = 18.dp)
                            .size(width = 112.dp, height = 160.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            } else {
                CallWaitingBackground(
                    name = state.remoteName.ifBlank { remoteName },
                    photo = state.remotePhoto.ifBlank { remotePhoto },
                    status = statusText
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xCC08090D))
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentCallType == "video" && state.isConnected) {
                    Text(
                        text = state.remoteName.ifBlank { remoteName },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(statusText, color = Color.White.copy(alpha = 0.78f), fontSize = 14.sp)
                (state.error ?: permissionError)?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = Color(0xFFFFA7A7),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))

                if (isIncoming && state.callRecord?.status == "ringing") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                    ) {
                        CallActionButton(
                            label = "Decline",
                            color = Color(0xFFDC3545),
                            icon = Icons.Default.CallEnd,
                            onClick = { session.declineCall(onComplete = onFinish) }
                        )
                        CallActionButton(
                            label = "Answer",
                            color = Color(0xFF25A55B),
                            icon = Icons.Default.Call,
                            onClick = {
                                permissionError = null
                                performAction(PermissionAction.ANSWER)
                            }
                        )
                    }
                } else if (state.isConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CallToggleButton(
                            label = if (state.microphoneEnabled) "Mute" else "Unmute",
                            enabled = state.microphoneEnabled,
                            icon = if (state.microphoneEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                            onClick = session::toggleMicrophone
                        )
                        CallActionButton(
                            label = "End",
                            color = Color(0xFFDC3545),
                            icon = Icons.Default.CallEnd,
                            onClick = { session.endCall(onComplete = onFinish) }
                        )
                        if (currentCallType == "video") {
                            CallToggleButton(
                                label = if (state.cameraEnabled) "Camera off" else "Camera on",
                                enabled = state.cameraEnabled,
                                icon = if (state.cameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                onClick = session::toggleCamera
                            )
                        }
                    }
                } else if (!isIncoming || state.callRecord?.status != "ringing") {
                    if (state.isConnecting || (state.callRecord == null && state.error == null && permissionError == null)) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                    CallActionButton(
                        label = "End",
                        color = Color(0xFFDC3545),
                        icon = Icons.Default.CallEnd,
                        onClick = { session.endCall(onComplete = onFinish) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallWaitingBackground(name: String, photo: String, status: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 220.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (photo.isNotBlank()) {
            AsyncImage(
                model = photo,
                contentDescription = name,
                modifier = Modifier
                    .size(148.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF30283D)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "S",
                    color = Color.White,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(status, color = Color.White.copy(alpha = 0.76f), fontSize = 15.sp)
    }
}

@Composable
private fun VideoWaitingBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17141E)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Waiting for video…",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )
    }
}

@Composable
private fun CallActionButton(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .background(color)
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun CallToggleButton(
    label: String,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (enabled) Color(0xFF34323A) else Color.White)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (enabled) Color.White else Color(0xFF17141E),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun LiveKitVideoRenderer(room: Room, track: VideoTrack?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val renderer = remember(room) {
        TextureViewRenderer(context).apply {
            room.initVideoRenderer(this)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        }
    }

    DisposableEffect(renderer) {
        onDispose { renderer.release() }
    }
    DisposableEffect(renderer, track) {
        if (track != null) {
            renderer.setMirror(track is LocalVideoTrack)
            track.addRenderer(renderer)
        }
        onDispose {
            track?.removeRenderer(renderer)
        }
    }
    AndroidView(
        factory = { renderer },
        modifier = modifier
    )
}
