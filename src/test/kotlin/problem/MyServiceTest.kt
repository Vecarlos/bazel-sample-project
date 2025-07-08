package com.example

import org.junit.Test
import org.junit.Assert.assertEquals

class MyServiceTest {
    @Test
    fun testGreeting() {
        val service = MyService()
        assertEquals("Hello", service.getGreeting())
    }
}