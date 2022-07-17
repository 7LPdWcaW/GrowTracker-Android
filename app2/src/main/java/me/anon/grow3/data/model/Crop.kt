package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.DateUtils
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.toStringOrNull
import java.util.*

@JsonClass(generateAdapter = true)
data class Crop(
	public val id: String = UUID.randomUUID().toString(),
	public var name: String,
	public var genetics: String? = null,
	public var numberOfPlants: Int = 1,
	//public var cloneOf: String? = null,
	public var dateAdded: String = DateUtils.newApiDateString(),
)
{
	public var isDraft = false
}

public fun Crop.patch(
	name: ValueHolder<String>? = null,
	genetics: ValueHolder<String?>? = null,
	numberOfPlants: ValueHolder<Int>? = null,
	dateAdded: ValueHolder<String>? = null,
): Crop
{
	return apply {
		name?.patch { this.name = it }
		genetics?.patch { this.genetics = it.toStringOrNull() }
		numberOfPlants?.patch { this.numberOfPlants = it }
		dateAdded?.patch { this.dateAdded = it }
	}
}