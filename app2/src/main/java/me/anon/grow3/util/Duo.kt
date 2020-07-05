package me.anon.grow3.util

class Duo<A>(
	var first: A,
	var second: A?
)

public infix fun <A> A.and(that: A?): Duo<A> = Duo(this, that)
//public operator fun <A> A.plus(second: A?): Duo<A> = Duo(this, second)
