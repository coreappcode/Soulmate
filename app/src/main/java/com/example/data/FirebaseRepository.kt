package com.example.data

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class FirebaseDashboardData(
    val exploreDeck: List<User> = emptyList(),
    val conversations: List<ChatConversation> = emptyList(),
    val friends: List<User> = emptyList(),
    val friendRequests: List<FriendRequestItem> = emptyList(),
    val sentRequests: List<SentFriendRequest> = emptyList(),
    val blockedUsers: List<User> = emptyList()
)

class UsernameTakenException : Exception("That username is already in use.")

class FirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    val currentUid: String?
        get() = auth.currentUser?.uid

    fun requireCurrentUid(): String =
        currentUid ?: throw IllegalStateException("Sign in before accessing your profile.")

    suspend fun isUsernameAvailable(username: String): Boolean {
        val uid = currentUid
        val normalized = username.trim().lowercase(Locale.ROOT)
        if (normalized.isBlank()) return false
        val snapshot = firestore.collection("usernames").document(normalized).get().await()
        return !snapshot.exists() || (uid != null && snapshot.getString("uid") == uid)
    }

    suspend fun saveDraft(profile: UserProfileState) {
        val uid = requireCurrentUid()
        firestore.collection("users").document(uid)
            .set(profile.toPrivateMap(), SetOptions.merge())
            .await()
    }

    suspend fun loadProfile(uid: String): UserProfileState? {
        val privateSnapshot = firestore.collection("users").document(uid).get().await()
        val publicSnapshot = firestore.collection("profiles").document(uid).get().await()
        if (!privateSnapshot.exists() && !publicSnapshot.exists()) return null

        val source = if (publicSnapshot.exists()) publicSnapshot else privateSnapshot
        val draft = privateSnapshot.get("draft") as? Map<*, *> ?: emptyMap<Any, Any>()
        fun string(field: String): String =
            source.getString(field) ?: draft[field] as? String ?: ""
        fun list(field: String): List<String> =
            source.get(field).stringList().ifEmpty { draft[field].stringList() }
        fun number(field: String): Int =
            (source.getLong(field) ?: (draft[field] as? Number)?.toLong() ?: 0L).toInt()
        val dialCode = privateSnapshot.getString("countryDialCode").orEmpty()
        val country = CountryCatalog.allCountries.firstOrNull { it.dialCode == dialCode }
            ?: CountryCatalog.allCountries.firstOrNull {
                it.name == privateSnapshot.getString("countryName")
            }
            ?: CountryCatalog.allCountries.first()

        return UserProfileState(
            selectedCountry = country,
            phoneNumber = auth.currentUser?.phoneNumber.orEmpty().let { number ->
                val matched = CountryCatalog.allCountries
                    .filter { number.startsWith(it.dialCode) }
                    .maxByOrNull { it.dialCode.length }
                matched?.let { number.removePrefix(it.dialCode) } ?: number
            },
            isPhoneVerified = auth.currentUser?.phoneNumber != null,
            fullName = string("fullName"),
            dateOfBirth = string("dateOfBirth"),
            age = number("age"),
            gender = string("gender"),
            username = string("username"),
            photos = list("photos"),
            selfieCapturedUrl = privateSnapshot.getString("selfieCapturedUrl"),
            isFaceVerified = publicSnapshot.getBoolean("verified") == true,
            bio = string("bio"),
            selectedInterests = list("interests"),
            selectedLookingFor = list("lookingFor"),
            city = string("city"),
            email = privateSnapshot.getString("email").orEmpty(),
            height = string("height"),
            job = string("job"),
            education = string("education"),
            zodiac = string("zodiac"),
            locationPermissionGranted = privateSnapshot.getBoolean("locationPermissionGranted") == true,
            notificationsPermissionGranted = privateSnapshot.getBoolean("notificationsPermissionGranted") == true,
            isOnboardingComplete = publicSnapshot.getBoolean("isComplete") == true
        )
    }

    suspend fun saveProfile(profile: UserProfileState, complete: Boolean) {
        val uid = requireCurrentUid()
        val profileRef = firestore.collection("profiles").document(uid)
        val userRef = firestore.collection("users").document(uid)
        val privacyRef = userRef.collection("settings").document("privacy")
        val nextUsername = profile.username.trim().lowercase(Locale.ROOT)
        val nextUsernameRef = firestore.collection("usernames").document(nextUsername)

        firestore.runTransaction { transaction ->
            val oldProfile = transaction.get(profileRef)
            val oldUsername = oldProfile.getString("username").orEmpty()
            val nextUsernameSnapshot = transaction.get(nextUsernameRef)
            val oldUsernameRef = oldUsername
                .takeIf { it.isNotBlank() && it != nextUsername }
                ?.let { firestore.collection("usernames").document(it) }
            val oldUsernameSnapshot = oldUsernameRef?.let { transaction.get(it) }
            val privacySnapshot = transaction.get(privacyRef)

            if (nextUsernameSnapshot.exists() && nextUsernameSnapshot.getString("uid") != uid) {
                throw UsernameTakenException()
            }

            if (oldUsernameRef != null && oldUsernameSnapshot?.getString("uid") == uid) {
                transaction.delete(oldUsernameRef)
            }
            transaction.set(nextUsernameRef, mapOf("uid" to uid, "username" to nextUsername))
            transaction.set(
                profileRef,
                profile.toPublicMap(uid, complete, privacySnapshot.data.orEmpty()),
                SetOptions.merge()
            )
            transaction.set(userRef, profile.toPrivateMap(), SetOptions.merge())
        }.await()
    }

    suspend fun uploadProfilePhoto(uri: Uri): String {
        if (uri.scheme == "http" || uri.scheme == "https") {
            return uri.toString()
        }
        val uid = requireCurrentUid()
        val imageRef = storage.reference.child("users/$uid/profile/${UUID.randomUUID()}.jpg")
        imageRef.putFile(
            uri,
            StorageMetadata.Builder().setContentType("image/jpeg").build()
        ).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun deleteProfilePhoto(downloadUrl: String) {
        storage.getReferenceFromUrl(downloadUrl).delete().await()
    }

    suspend fun uploadVerificationSelfie(uri: Uri): String {
        if (uri.scheme == "http" || uri.scheme == "https") {
            return uri.toString()
        }
        val uid = requireCurrentUid()
        val imageRef = storage.reference.child("users/$uid/verification/${UUID.randomUUID()}.jpg")
        imageRef.putFile(
            uri,
            StorageMetadata.Builder().setContentType("image/jpeg").build()
        ).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun uploadChatAttachment(conversationId: String, uri: Uri, mimeType: String): String {
        val uid = requireCurrentUid()
        val extension = mimeType.substringAfter('/', "bin").substringBefore(';')
        val reference = storage.reference.child(
            "conversations/$conversationId/${uid}_${UUID.randomUUID()}.$extension"
        )
        reference.putFile(
            uri,
            StorageMetadata.Builder().setContentType(mimeType).build()
        ).await()
        return reference.downloadUrl.await().toString()
    }

    suspend fun loadDashboard(): FirebaseDashboardData {
        val uid = requireCurrentUid()
        val ownProfile = firestore.collection("profiles").document(uid).get().await()
        val ownInterests = ownProfile.get("interests").stringList().toSet()
        val profileSnapshots = firestore.collection("profiles").limit(100).get().await()
        val swiped = firestore.collection("swipes")
            .whereEqualTo("senderId", uid)
            .get().await().documents.mapNotNull { it.getString("recipientId") }.toSet()
        val blocks = firestore.collection("users").document(uid)
            .collection("blocks").get().await()
        val blockedIds = blocks.documents.map { it.id }.toSet()
        val excluded = swiped + blockedIds + uid
        val profiles = profileSnapshots.documents
            .filter { it.getBoolean("isComplete") == true }
            .filterNot { it.id in excluded }
            .map { it.toUser() }

        val friends = firestore.collection("friendships")
            .whereArrayContains("userIds", uid)
            .get().await().documents.mapNotNull { friendship ->
                friendship.get("userIds").stringList()
                    .firstOrNull { it != uid }
                    ?.let { getPublicUser(it) }
            }

        val incoming = firestore.collection("friendRequests")
            .whereEqualTo("recipientId", uid)
            .whereEqualTo("status", "pending")
            .get().await().documents
            .mapNotNull { request ->
                request.getString("senderId")?.let { senderId ->
                    val user = getPublicUser(senderId) ?: return@mapNotNull null
                    FriendRequestItem(
                        id = request.id,
                        user = user,
                        mutualInterests = user.interests.count { it in ownInterests },
                        timestamp = request.getTimestamp("createdAt").formatTime()
                    )
                }
            }

        val outgoing = firestore.collection("friendRequests")
            .whereEqualTo("senderId", uid)
            .whereEqualTo("status", "pending")
            .get().await().documents
            .mapNotNull { request ->
                request.getString("recipientId")?.let { recipientId ->
                    getPublicUser(recipientId)?.let { user ->
                        SentFriendRequest(
                            id = request.id,
                            user = user,
                            status = "Pending",
                            timestamp = request.getTimestamp("createdAt").formatTime()
                        )
                    }
                }
            }

        val conversations = loadConversations(uid)
        val blockedUsers = blocks.documents.mapNotNull { getPublicUser(it.id) }

        return FirebaseDashboardData(
            exploreDeck = profiles,
            conversations = conversations,
            friends = friends,
            friendRequests = incoming,
            sentRequests = outgoing,
            blockedUsers = blockedUsers
        )
    }

    suspend fun searchProfiles(query: String): List<User> {
        val uid = requireCurrentUid()
        val normalized = query.trim().lowercase(Locale.ROOT)
        if (normalized.isBlank()) return emptyList()

        val usernameResults = firestore.collection("profiles")
            .orderBy("usernameLower")
            .startAt(normalized)
            .endAt("$normalized\uf8ff")
            .limit(20)
            .get().await().documents
        val nameResults = firestore.collection("profiles")
            .orderBy("fullNameLower")
            .startAt(normalized)
            .endAt("$normalized\uf8ff")
            .limit(20)
            .get().await().documents

        return (usernameResults + nameResults)
            .distinctBy { it.id }
            .filterNot { it.id == uid }
            .filter { it.getBoolean("isComplete") == true }
            .map { it.toUser() }
    }

    suspend fun recordSwipe(target: User, liked: Boolean): Boolean {
        val uid = requireCurrentUid()
        require(target.id != uid) { "You cannot swipe on your own profile." }
        val currentSwipe = firestore.collection("swipes").document(swipeId(uid, target.id))
        val oppositeSwipe = firestore.collection("swipes").document(swipeId(target.id, uid))
        val pair = listOf(uid, target.id).sorted()
        val matchId = pair.joinToString("_")
        val matchRef = firestore.collection("matches").document(matchId)
        val conversationRef = firestore.collection("conversations").document(matchId)

        return firestore.runTransaction { transaction ->
            val opposite = transaction.get(oppositeSwipe)
            val match = transaction.get(matchRef)
            val conversation = transaction.get(conversationRef)
            transaction.set(
                currentSwipe,
                mapOf(
                    "senderId" to uid,
                    "recipientId" to target.id,
                    "liked" to liked,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            val isMatch = liked && opposite.getBoolean("liked") == true
            if (isMatch) {
                if (!match.exists()) {
                    transaction.set(
                        matchRef,
                        mapOf("userIds" to pair, "createdAt" to FieldValue.serverTimestamp())
                    )
                }
                if (!conversation.exists()) {
                    transaction.set(
                        conversationRef,
                        mapOf(
                            "userIds" to pair,
                            "archivedBy" to emptyList<String>(),
                            "lastMessage" to "",
                            "updatedAt" to FieldValue.serverTimestamp(),
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            isMatch
        }.await()
    }

    suspend fun undoSwipe(targetId: String) {
        val uid = requireCurrentUid()
        firestore.collection("swipes").document(swipeId(uid, targetId)).delete().await()
    }

    suspend fun ensureConversation(targetId: String): ChatConversation {
        val uid = requireCurrentUid()
        val pair = listOf(uid, targetId).sorted()
        val id = pair.joinToString("_")
        val match = firestore.collection("matches").document(id).get().await()
        val friendship = firestore.collection("friendships").document(id).get().await()
        if (!match.exists() && !friendship.exists()) {
            throw IllegalStateException("You can only start a chat with a match or a friend.")
        }

        val reference = firestore.collection("conversations").document(id)
        val snapshot = reference.get().await()
        if (!snapshot.exists()) {
            reference.set(
                mapOf(
                    "userIds" to pair,
                    "archivedBy" to emptyList<String>(),
                    "lastMessage" to "",
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else if (uid in snapshot.get("archivedBy").stringList()) {
            reference.update("archivedBy", FieldValue.arrayRemove(uid)).await()
        }

        val user = getPublicUser(targetId)
            ?: throw IllegalStateException("That profile is no longer available.")
        return ChatConversation(
            id = id,
            user = user,
            lastMessage = snapshot.getString("lastMessage").orEmpty(),
            timestamp = snapshot.getTimestamp("updatedAt").formatTime()
        )
    }

    suspend fun loadMessages(conversationId: String): List<ChatMessage> {
        val uid = requireCurrentUid()
        return firestore.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get().await().documents.map { message ->
                ChatMessage(
                    id = message.id,
                    text = message.getString("text").orEmpty(),
                    isOwn = message.getString("senderId") == uid,
                    timestamp = message.getTimestamp("createdAt").formatTime(),
                    status = MessageStatus.SENT,
                    imageUrl = message.getString("attachmentUrl"),
                    attachmentType = message.getString("attachmentType")
                )
            }
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        attachmentUrl: String? = null,
        attachmentType: String? = null
    ) {
        val uid = requireCurrentUid()
        val messageRef = firestore.collection("conversations").document(conversationId)
            .collection("messages").document()
        val preview = when {
            text.isNotBlank() -> text
            attachmentType == "image" -> "Sent a photo"
            attachmentType == "audio" -> "Sent an audio file"
            attachmentType == "document" -> "Sent a document"
            else -> throw IllegalArgumentException("A message or attachment is required.")
        }
        firestore.runBatch { batch ->
            batch.set(
                messageRef,
                mapOf(
                    "senderId" to uid,
                    "text" to text,
                    "attachmentUrl" to attachmentUrl,
                    "attachmentType" to attachmentType,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                firestore.collection("conversations").document(conversationId),
                mapOf("lastMessage" to preview, "updatedAt" to FieldValue.serverTimestamp())
            )
        }.await()
    }

    suspend fun clearConversation(conversationId: String) {
        val messages = firestore.collection("conversations").document(conversationId)
            .collection("messages").get().await().documents
        messages.chunked(450).forEach { chunk ->
            firestore.batch().also { batch ->
                chunk.forEach { message -> batch.delete(message.reference) }
                batch.commit().await()
            }
        }
        firestore.collection("conversations").document(conversationId)
            .update("lastMessage", "", "updatedAt", FieldValue.serverTimestamp()).await()
    }

    suspend fun archiveConversation(conversationId: String) {
        val uid = requireCurrentUid()
        firestore.collection("conversations").document(conversationId)
            .update("archivedBy", FieldValue.arrayUnion(uid)).await()
    }

    suspend fun sendFriendRequest(target: User) {
        val uid = requireCurrentUid()
        require(target.id != uid) { "You cannot send a friend request to yourself." }
        val reference = firestore.collection("friendRequests").document("${uid}_${target.id}")
        val existing = reference.get().await()
        when (existing.getString("status")) {
            "pending" -> return
            "accepted" -> throw IllegalStateException("You are already friends with this person.")
            "declined" -> reference.update(
                mapOf(
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            null -> reference.set(
                mapOf(
                    "senderId" to uid,
                    "recipientId" to target.id,
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            else -> throw IllegalStateException("This friend request has an invalid status.")
        }
    }

    suspend fun respondToFriendRequest(requestId: String, accept: Boolean) {
        val uid = requireCurrentUid()
        val requestRef = firestore.collection("friendRequests").document(requestId)
        val request = requestRef.get().await()
        require(request.getString("recipientId") == uid && request.getString("status") == "pending") {
            "This friend request is no longer available."
        }
        val senderId = request.getString("senderId")
            ?: throw IllegalStateException("Friend request has no sender.")
        val pair = listOf(uid, senderId).sorted()
        firestore.runTransaction { transaction ->
            val freshRequest = transaction.get(requestRef)
            require(freshRequest.getString("status") == "pending") {
                "This friend request has already been handled."
            }
            transaction.update(requestRef, "status", if (accept) "accepted" else "declined")
            if (accept) {
                transaction.set(
                    firestore.collection("friendships").document(pair.joinToString("_")),
                    mapOf("userIds" to pair, "createdAt" to FieldValue.serverTimestamp())
                )
            }
        }.await()
    }

    suspend fun cancelFriendRequest(requestId: String) {
        val uid = requireCurrentUid()
        val ref = firestore.collection("friendRequests").document(requestId)
        val request = ref.get().await()
        require(request.getString("senderId") == uid) {
            "You can only cancel a request that you sent."
        }
        ref.delete().await()
    }

    suspend fun removeFriend(friendId: String) {
        val uid = requireCurrentUid()
        val friendshipRef = firestore.collection("friendships")
            .document(listOf(uid, friendId).sorted().joinToString("_"))
        val friendship = friendshipRef.get().await()
        require(friendship.exists()) { "This friendship is no longer available." }
        val requestRefs = listOf(
            firestore.collection("friendRequests").document("${uid}_$friendId"),
            firestore.collection("friendRequests").document("${friendId}_$uid")
        )
        val existingRequests = requestRefs.map { it.get().await() }
        firestore.runBatch { batch ->
            batch.delete(friendshipRef)
            existingRequests.forEachIndexed { index, request ->
                if (request.exists()) batch.delete(requestRefs[index])
            }
        }.await()
    }

    suspend fun blockUser(targetId: String) {
        val uid = requireCurrentUid()
        firestore.collection("users").document(uid).collection("blocks")
            .document(targetId)
            .set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
    }

    suspend fun unblockUser(targetId: String) {
        val uid = requireCurrentUid()
        firestore.collection("users").document(uid).collection("blocks")
            .document(targetId).delete().await()
    }

    suspend fun saveSettings(collection: String, values: Map<String, Any>) {
        val uid = requireCurrentUid()
        firestore.collection("users").document(uid).collection("settings")
            .document(collection).set(values, SetOptions.merge()).await()
    }

    suspend fun savePrivacySettings(
        values: Map<String, Any>,
        showAge: Boolean,
        age: Int
    ) {
        val uid = requireCurrentUid()
        val profileRef = firestore.collection("profiles").document(uid)
        val settingsRef = firestore.collection("users").document(uid)
            .collection("settings").document("privacy")
        firestore.runTransaction { transaction ->
            val profile = transaction.get(profileRef)
            transaction.set(settingsRef, values, SetOptions.merge())
            if (profile.exists()) {
                val profileUpdate = mutableMapOf<String, Any>("showAge" to showAge)
                profileUpdate["age"] = if (showAge) age else FieldValue.delete()
                transaction.update(profileRef, profileUpdate)
            }
        }.await()
    }

    suspend fun loadSettings(collection: String): Map<String, Any?> {
        val uid = requireCurrentUid()
        return firestore.collection("users").document(uid).collection("settings")
            .document(collection).get().await().data.orEmpty()
    }

    suspend fun loadAllSettings(): Map<String, Map<String, Any?>> {
        val uid = requireCurrentUid()
        val documents = firestore.collection("users").document(uid)
            .collection("settings").get().await().documents
        return documents.associate { it.id to it.data.orEmpty() }
    }

    private suspend fun loadConversations(uid: String): List<ChatConversation> {
        val snapshots = firestore.collection("conversations")
            .whereArrayContains("userIds", uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .get().await()
        return snapshots.documents
            .filterNot { uid in it.get("archivedBy").stringList() }
            .mapNotNull { snapshot ->
                val otherId = snapshot.get("userIds").stringList().firstOrNull { it != uid }
                    ?: return@mapNotNull null
                getPublicUser(otherId)?.let { user ->
                    ChatConversation(
                        id = snapshot.id,
                        user = user,
                        lastMessage = snapshot.getString("lastMessage").orEmpty(),
                        timestamp = snapshot.getTimestamp("updatedAt").formatTime()
                    )
                }
            }
    }

    private suspend fun getPublicUser(uid: String): User? {
        val snapshot = firestore.collection("profiles").document(uid).get().await()
        return if (snapshot.exists()) snapshot.toUser() else null
    }

    private fun DocumentSnapshot.toUser(): User = User(
        id = id,
        name = getString("fullName").orEmpty(),
        username = getString("username").orEmpty(),
        age = (getLong("age") ?: 0L).toInt(),
        gender = getString("gender").orEmpty(),
        bio = getString("bio").orEmpty(),
        photos = stringList("photos"),
        distance = "",
        country = getString("countryName").orEmpty(),
        city = getString("city").orEmpty(),
        verified = getBoolean("verified") == true,
        interests = stringList("interests"),
        lookingFor = stringList("lookingFor"),
        height = getString("height").orEmpty(),
        job = getString("job").orEmpty(),
        education = getString("education").orEmpty(),
        zodiac = getString("zodiac").orEmpty(),
        ageVisible = getBoolean("showAge") != false
    )

    private fun UserProfileState.toPublicMap(
        uid: String,
        complete: Boolean,
        privacySettings: Map<String, Any?>
    ): Map<String, Any?> {
        val showAge = privacySettings["showAge"] as? Boolean ?: true
        return mapOf(
            "uid" to uid,
            "fullName" to fullName.trim(),
            "fullNameLower" to fullName.trim().lowercase(Locale.ROOT),
            "username" to username.trim(),
            "usernameLower" to username.trim().lowercase(Locale.ROOT),
            "age" to if (showAge) age else null,
            "showAge" to showAge,
            "gender" to gender,
            "bio" to bio.trim(),
            "photos" to photos,
            "interests" to selectedInterests,
            "lookingFor" to selectedLookingFor,
            "countryName" to selectedCountry.name,
            "city" to city.trim(),
            "height" to height.trim(),
            "job" to job.trim(),
            "education" to education.trim(),
            "zodiac" to zodiac.trim(),
            "verified" to isFaceVerified,
            "isComplete" to complete,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun UserProfileState.toPrivateMap(): Map<String, Any?> = mapOf(
        "phoneNumber" to auth.currentUser?.phoneNumber,
        "email" to email.trim(),
        "countryName" to selectedCountry.name,
        "countryDialCode" to selectedCountry.dialCode,
        "countryFlag" to selectedCountry.flag,
        "selfieCapturedUrl" to selfieCapturedUrl,
        "locationPermissionGranted" to locationPermissionGranted,
        "notificationsPermissionGranted" to notificationsPermissionGranted,
        "draft" to mapOf(
            "fullName" to fullName,
            "dateOfBirth" to dateOfBirth,
            "age" to age,
            "gender" to gender,
            "username" to username,
            "photos" to photos,
            "bio" to bio,
            "interests" to selectedInterests,
            "lookingFor" to selectedLookingFor,
            "city" to city,
            "height" to height,
            "job" to job,
            "education" to education,
            "zodiac" to zodiac
        ),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    private fun DocumentSnapshot.stringList(field: String): List<String> =
        get(field).stringList()

    private fun Any?.stringList(): List<String> =
        (this as? List<*>)?.mapNotNull { it as? String }.orEmpty()

    private fun Timestamp?.formatTime(): String =
        this?.toDate()?.let { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(it) }
            .orEmpty()

    private fun swipeId(senderId: String, recipientId: String) = "${senderId}_$recipientId"
}
