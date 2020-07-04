package me.anon.grow3.util

import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.MediumType
import me.anon.grow3.data.model.Type

class Duo<A>(
	var first: A,
	var second: A?
)

public infix fun <A> A.and(that: A?): Duo<A> = Duo(this, that)

public operator fun Log.plus(second: Log): Duo<Log> = Duo(this, second)
public operator fun Type.plus(second: Type): Duo<Type> = Duo(this, second)
public operator fun MediumType.plus(second: MediumType): Duo<MediumType> = Duo(this, second)
