package me.anon.grow3.data.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.ui.action.view.*
import me.anon.grow3.ui.logs.view.*
import me.anon.grow3.util.*
import me.anon.grow3.util.DateUtils.newApiDateString
import me.anon.grow3.view.model.Card
import java.util.*

class LogType<T>(
	val name: String,
	val iconRes: Int = -1,
	val type: Class<out T>,
	val viewType: Class<out LogView<*>>,
	val cardType: Class<out Card<*>>,
	val logConstructor: (Diary) -> T,
	val viewConstructor: (Diary, T) -> LogView<*>,
	val cardConstructor: (Diary, T) -> Card<*>,
) where T : Log

object LogConstants
{
	public val types = hashMapOf<String, LogType<*>>(
		"Environment" to LogType(
			"Environment",
			-1,
			Environment::class.java,
			EnvironmentLogView::class.java,
			EnvironmentLogCard::class.java,
			{ Environment() },
			{ a,b -> EnvironmentLogView(a, b) },
			{ a,b -> EnvironmentLogCard(a, b) },
		),
		"Photo" to LogType(
			"Photo",
			-1,
			Photo::class.java,
			PhotoLogView::class.java,
			PhotoLogCard::class.java,
			{ Photo() },
			{ a,b -> PhotoLogView(a, b) },
			{ a,b -> PhotoLogCard(a, b) },
		),
		"StageChange" to LogType(
			"StageChange",
			-1,
			StageChange::class.java,
			StageChangeLogView::class.java,
			StageChangeLogCard::class.java,
			{ StageChange() },
			{ a,b -> StageChangeLogView(a, b) },
			{ a,b -> StageChangeLogCard(a, b) },
		),
		"Transplant" to LogType(
			"Transplant",
			-1,
			Transplant::class.java,
			TransplantLogView::class.java,
			TransplantLogCard::class.java,
			{ Transplant() },
			{ a,b -> TransplantLogView(a, b) },
			{ a,b -> TransplantLogCard(a, b) },
		),
		"Water" to LogType(
			"Water",
			-1,
			Water::class.java,
			WaterLogView::class.java,
			WaterLogCard::class.java,
			{ Water() },
			{ a,b -> WaterLogView(a, b) },
			{ a,b -> WaterLogCard(a, b) },
		),
	)
	public val quickMenu get() = arrayOf(
		types["Environment"]!!,
		types["Photo"]!!,
		types["Transplant"]!!,
		types["Water"]!!,
	)
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "action")
abstract class Log(
	open var id: String = UUID.randomUUID().toString(),
	open var date: String = newApiDateString(),
	open var notes: String = "",
	open var cropIds: List<String> = arrayListOf(),
	open var action: String = "Log"
)
{
	public var isDraft = false

	open fun summary(): CharSequence = ""
	open val typeRes: Int = -1
}

public inline fun <reified T : Log> T.copy(): T
{
	val encoded = this.toJsonString()
	return encoded.fromJsonString<T>().apply {
		id = UUID.randomUUID().toString()
	}
}

public fun <T : Log> T.asView(diary: Diary): LogView<*>
{
	val constructor = LogConstants.types[this.action]?.viewConstructor as? (Diary, T) -> LogView<*>
		?: throw GrowTrackerException.InvalidLog(this)
	return constructor.invoke(diary, this)
}

public fun <T : Log> T.asCard(diary: Diary): Card<*>
{
	val constructor = LogConstants.types[this.action]?.cardConstructor as? (Diary, T) -> Card<*>
		?: throw GrowTrackerException.InvalidLog(this)
	return constructor.invoke(diary, this)
}

data class LogChange(
	var days: Int
) : Delta()

public fun Duo<Log>.difference(): LogChange = LogChange((first.date and second!!.date).dateDifferenceDays())