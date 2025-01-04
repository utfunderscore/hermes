package org.readutf.hermes.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExceptionManagerTest {
    private var exceptionManager = ExceptionManager()

    @BeforeEach
    fun each() {
        exceptionManager = ExceptionManager()
    }

    @Test
    fun handleException() {
        assertEquals(false, exceptionManager.handleException(Exception()))
    }

    @Test
    fun setGlobalExceptionHandler() {
        var exceptionHandled = false

        exceptionManager.setGlobalExceptionHandler {
            exceptionHandled = true
        }

        exceptionManager.handleException(Exception())

        assertEquals(true, exceptionHandled)
    }

    @Test
    fun setExceptionHandler() {
        val caughtExceptionTypes = mutableListOf<Class<out Throwable>>()

        exceptionManager.setExceptionHandler(IllegalStateException::class.java) {
            caughtExceptionTypes.add(it::class.java)
        }

        exceptionManager.setExceptionHandler(IllegalArgumentException::class.java) {
            caughtExceptionTypes.add(it::class.java)
        }

        val validExceptions = listOf(IllegalStateException::class.java, IllegalArgumentException::class.java)

        exceptionManager.handleException(IllegalStateException())
        exceptionManager.handleException(IllegalArgumentException())
        exceptionManager.handleException(Exception())

        assertEquals(true, caughtExceptionTypes == validExceptions)
    }
}
