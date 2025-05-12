package sample

import sample.proto.UserProfile
import com.google.protobuf.ByteString

fun createKotlinUser(
    userId: String,
    displayName: String,
    age: Int,
    isActive: Boolean,
    interests: List<String>
): UserProfile {
    return UserProfile.newBuilder()
        .setUserId(userId)
        .setDisplayName(displayName)
        .setAge(age)
        .setIsActive(isActive)
        .addAllInterests(interests) 
        .build() 
}