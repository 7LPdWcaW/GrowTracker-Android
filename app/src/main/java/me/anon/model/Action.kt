package me.anon.model

import android.os.Parcelable
import me.anon.grow.R

abstract class Action(
	open var date: Long = System.currentTimeMillis(),
	open var notes: String? = null
) : Parcelable
{
	enum class ActionName private constructor(val printString: Int, val colour: Int)
	{
		FIM(R.string.action_fim, -0x65003380),
		FLUSH(R.string.action_flush, -0x65001f7e),
		FOLIAR_FEED(R.string.action_foliar_feed, -0x65191164),
		LST(R.string.action_lst, -0x65000a63),
		LOLLIPOP(R.string.action_lolipop, -0x65002e80),
		PESTICIDE_APPLICATION(R.string.action_pesticide_application, -0x65106566),
		TOP(R.string.action_topped, -0x6543555c),
		TRANSPLANTED(R.string.action_transplanted, -0x65000073),
		TRIM(R.string.action_trim, -0x6500546f);

		companion object
		{
			@JvmStatic
			public fun names(): IntArray
			{
				val names = IntArray(values().size)
				for (index in names.indices)
				{
					names[index] = values()[index].printString
				}

				return names
			}
		}
	}

	override fun equals(o: Any?): Boolean
	{
		if (o === this) return true
		if (o !is Action) return false
		if (!super.equals(o)) return false
		return this.date == o.date
	}
}
