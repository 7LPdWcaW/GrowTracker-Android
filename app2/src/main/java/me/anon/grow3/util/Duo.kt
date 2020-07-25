package me.anon.grow3.util

/**
 * A dual value holder class of a single type. Second parameter may
 * be null
 */
class Duo<A>(
	var first: A,
	var second: A?
)

public infix fun <A> A.and(that: A?): Duo<A> = Duo(this, that)
