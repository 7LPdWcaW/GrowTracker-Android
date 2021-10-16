package me.anon.grow3.data.model

import android.text.Spannable
import android.text.SpannableString
import com.squareup.moshi.JsonClass
import me.anon.grow3.ui.action.view.StageChangeLogView
import me.anon.grow3.ui.logs.view.StageChangeLogCard
import me.anon.grow3.util.asDateTime
import me.anon.grow3.util.plusAssign
import me.anon.grow3.util.string
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

typealias Stage = StageChange

/**
 * See [me.anon.grow3.ui.logs.view.StageChangeLogCard]
 */
@JsonClass(generateAdapter = true)
data class StageChange(
	public var type: StageType
) : Log(action = "StageChange")
{
	override val typeRes: Int = me.anon.grow3.R.string.log_type_stage_change
}

public fun StageChange.logView(diary: Diary) = StageChangeLogView(diary, this)
public fun StageChange.logCard(diary: Diary) = StageChangeLogCard(diary, this)

public fun List<StageChange>.shortSummary(): Spannable
{
	val stringBuilder = StringBuilder()
	if (size - 1 >= 0)
	{
		val firstStage = get(0)
		val currentStage = get(size - 1)
		val now = ZonedDateTime.now()

		val days = ChronoUnit.DAYS.between(currentStage.date.asDateTime(), now)
		val total = ChronoUnit.DAYS.between(firstStage.date.asDateTime(), now)
		stringBuilder += "$days"
		stringBuilder += currentStage.type.strRes.string()[0].toLowerCase()
		stringBuilder += "/$total"
	}

	return SpannableString.valueOf(stringBuilder.toString())
}

public fun List<StageChange>.longSummary(): Spannable
{
	val stringBuilder = StringBuilder()
	for (stageIndex in 0 until size)
	{
		val stage = get(stageIndex)
		var stage2Date = ZonedDateTime.now()
		val next = getOrNull(stageIndex + 1)
		next?.let { next ->
			stage2Date = next.date.asDateTime()
		}

		val stage1Date = stage.date.asDateTime()
		val days = ChronoUnit.DAYS.between(stage1Date, stage2Date)
		stringBuilder += "$days"
		stringBuilder += stage.type.strRes.string()[0].toLowerCase()

		next?.let {
			stringBuilder += "/"
		}
	}

	return SpannableString.valueOf(stringBuilder.toString())
}

/**
 * Data holder for describing the stage at the period of the provided log
 */
data class StageAt(
	val days: Int,
	val stage: Stage,
	val log: Log,
	val total: Int
)
{
	override fun toString(): String
	{
		return "$days${stage.type.strRes.string()[0]}"
	}
}