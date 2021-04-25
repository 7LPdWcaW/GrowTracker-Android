package me.anon.grow3.util

import android.text.Editable

public fun Editable.toDoubleOrNull(): Double? = toString().toDoubleOrNull()
public fun Editable.toFloatOrNull(): Float? = toString().toFloatOrNull()
public fun Editable.toInt(): Int = toString().toInt()
public fun Editable.toIntOrNull(): Int? = toString().toIntOrNull()
