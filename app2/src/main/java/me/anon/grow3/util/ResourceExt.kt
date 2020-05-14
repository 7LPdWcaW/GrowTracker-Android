package me.anon.grow3.util

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

@ColorInt
public fun @receiver:ColorRes Int.resolve(context: Context): Int = context.resources.getColor(this)
