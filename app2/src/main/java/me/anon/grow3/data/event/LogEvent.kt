package me.anon.grow3.data.event

import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log

sealed class LogEvent
{
	data class Added(val log: Log, val diary: Diary) : LogEvent()
	data class Deleted(val log: Log, val diary: Diary) : LogEvent()
	data class Duplicated(val log: Log, val diary: Diary) : LogEvent()
	data class Copied(val log: Log, val diary: Diary) : LogEvent()
	data class Modified(val log: Log, val diary: Diary) : LogEvent()
}
