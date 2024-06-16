package com.github.rushyverse.mojang.api.extension

/**
 * Regex for a valid uuid string format with dashes.
 * A valid UUID should be in the format of: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx (36 characters)
 * with only hex characters.
 */
private val uuidRegex: Regex =
    Regex("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")

/**
 * Regex for a valid uuid string format without dashes.
 * A valid UUID should be in the format of: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx (32 characters) with only hex characters.
 */
private val uuidWithoutDashesRegex: Regex =
    Regex("^([a-fA-F0-9]{8})([a-fA-F0-9]{4})([a-fA-F0-9]{4})([a-fA-F0-9]{4})([a-fA-F0-9]{12})$")

/**
 * Formats a string to a valid uuid string format.
 * If the string is already formatted, it will return the string.
 * If the string is in a valid format, but not in the correct format, it will return the formatted string.
 * If the string is not in a valid format (length, characters, etc), it will throw an [IllegalArgumentException].
 * @receiver The string to format.
 * @return The formatted string.
 */
public fun String.formatUUID(): String {
    return if (matches(uuidRegex)) {
        this
    } else if (matches(uuidWithoutDashesRegex)) {
        replace(uuidWithoutDashesRegex, "$1-$2-$3-$4-$5")
    } else {
        throw IllegalArgumentException("Invalid UUID format for string: [$this]")
    }
}
