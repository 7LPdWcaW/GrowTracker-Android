package me.anon.grow3.util

public inline fun <reified T : Any> code(): Int = T::class.java.name.hashCode().and(0xffff)
