@file:JvmName("Utils")

package com.tumblingworks.backend.common

/**
 * Returns true when [value] is null or empty.
 *
 * Java callers can statically import this function from
 * `com.tumblingworks.backend.common.Utils`.
 */
fun isEmpty(value: String?): Boolean = value.isNullOrEmpty()

/**
 * Returns true when [value] is null, empty, or consists only of whitespace.
 */
fun isBlank(value: String?): Boolean = value.isNullOrBlank()
