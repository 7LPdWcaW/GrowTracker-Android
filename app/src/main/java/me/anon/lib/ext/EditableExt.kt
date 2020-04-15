package me.anon.lib.ext

import android.text.Editable

/**
 * // TODO: Add class description
 */
public fun Editable.toDoubleOrNull(): Double? = toString().toDoubleOrNull()
public fun Editable.toFloatOrNull(): Float? = toString().toFloatOrNull()
