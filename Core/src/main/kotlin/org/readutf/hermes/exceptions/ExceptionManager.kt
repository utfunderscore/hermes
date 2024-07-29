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
        val consumer = exceptionHandlers[throwable::class.java]
        if (consumer != null) {
            consumer.accept(throwable)
            return true
        }

        exceptionHandlers
            .filter { (clazz, _) ->
                clazz.isAssignableFrom(throwable.javaClass)
            }.forEach { (_, consumer) -> consumer.accept(throwable) }

        if (globalExceptionHandler != null) {
            globalExceptionHandler!!.accept(throwable)
            return true
        }

        return false
    }
}
