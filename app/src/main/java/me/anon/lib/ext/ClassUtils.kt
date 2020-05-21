package me.anon.lib.ext

/**
 * Performs a transformation of an object type into another object type
 */
public inline fun <T, O> T.transform(transformer: T.() -> O): O = transformer(this)
