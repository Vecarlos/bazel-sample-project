package sample

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import sample.proto.UserProfile 
import sample.createKotlinUser

@RunWith(JUnit4::class)
class ManageUsersTest {

    @Test
    fun `createKotlinUser should populate fields correctly`() {
        val userId = "kt_test_001"
        val displayName = "Kotlin Proto Test User"
        val age = 30
        val isActive = true
        val interests = listOf("kotlin", "protobuf", "bazel", "testing")

        val profile: UserProfile = createKotlinUser(userId, displayName, age, isActive, interests)
        assertEquals(userId, profile.userId)
        assertEquals(displayName, profile.displayName)
        assertEquals(age, profile.age)
        assertEquals( isActive, profile.isActive)
        assertEquals(interests.size, profile.interestsList.size)
        assertTrue(profile.interestsList.containsAll(interests) && interests.containsAll(profile.interestsList))
    }

}