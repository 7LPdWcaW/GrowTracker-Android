package me.anon.grow3.util

import android.text.Editable
import android.text.SpannableStringBuilder

public fun String.asEditable(): Editable = SpannableStringBuilder(this)
