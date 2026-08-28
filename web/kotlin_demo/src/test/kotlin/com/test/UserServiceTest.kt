package com.test

import com.kotlin.example.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserServiceTest {

    @Test
    fun helloTest() {

        val service = UserService()

        val result = service.hello("huang")

        assertEquals(
            "Hello huang",
            result
        )
    }
}