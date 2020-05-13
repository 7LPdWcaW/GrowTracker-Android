package me.anon.grow3.util

import android.content.Context
import me.anon.grow3.BaseApplication

public val Context.application get() = applicationContext as BaseApplication
public val Context.component get() = application.appComponent
