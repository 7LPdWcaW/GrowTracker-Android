package me.anon.grow3.util

import android.text.Editable

public fun Editable.toDoubleOrNull(): Double? = toString().toDoubleOrNull()
public fun Editable.toFloatOrNull(): Float? = toString().toFloatOrNull()
