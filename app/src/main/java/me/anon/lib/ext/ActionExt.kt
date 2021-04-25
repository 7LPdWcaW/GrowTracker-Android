package me.anon.lib.ext

import me.anon.model.Action

/**
 * // TODO: Add class description
 */
public fun ArrayList<Action>.addOrUpdate(action: Action, query: (Action) -> Any)
{
	val actionIndex = this.indexOfLast { query(action) == query(it) }
	if (actionIndex > -1) this[actionIndex] = action else add(action)
}
