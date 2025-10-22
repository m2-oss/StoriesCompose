package ru.m2.squaremeter.stories.data.util

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

internal fun convertToString(from: Any, onFailure: (Throwable) -> Unit): String? {
    var result: String? = null
    try {
        ByteArrayOutputStream().use { os ->
            ObjectOutputStream(os).use {
                it.writeObject(from)
                result = Base64.encodeToString(os.toByteArray(), Base64.DEFAULT)
            }
        }
    } catch (expected: Exception) {
        onFailure(expected)
    }
    return result
}

internal fun convertFromString(from: String, onFailure: (Throwable) -> Unit): Any? {
    var result: Any? = null
    try {
        ByteArrayInputStream(
            Base64.decode(from, Base64.DEFAULT)
        ).use { inputStream ->
            ObjectInputStream(inputStream).use { ois ->
                result = ois.readObject()
            }
        }
    } catch (expected: Exception) {
        onFailure(expected)
    }
    return result
}
