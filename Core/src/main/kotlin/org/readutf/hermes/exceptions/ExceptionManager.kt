package org.readutf.hermes.exceptions

import java.util.function.Consumer

class ExceptionManager {
    private var globalExceptionHandler: Consumer<Throwable>? = null
    private val exceptionHandlers = mutableMapOf<Class<out Throwable>, Consumer<Throwable>>()

    fun setGlobalExceptionHandler(consumer: Consumer<Throwable>) {
        globalExceptionHandler = consumer
    }

    fun <U : Throwable> setExceptionHandler(
        clazz: Class<U>,
        consumer: Consumer<U>,
    ) {
        exceptionHandlers[clazz] = consumer as Consumer<Throwable>
    }

    fun handleException(throwable: Throwable): Boolean {
        var handlerFound = false
        val consumer = exceptionHandlers[throwable::class.java]
        if (consumer != null) {
            consumer.accept(throwable)
            handlerFound = true
        }

        if (globalExceptionHandler != null) {
            globalExceptionHandler!!.accept(throwable)
            handlerFound = true
        }

        return handlerFound
    }
}
