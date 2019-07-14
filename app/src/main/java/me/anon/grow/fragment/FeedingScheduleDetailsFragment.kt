package me.anon.grow.fragment

import android.app.AlertDialog
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.esotericsoftware.kryo.Kryo
import kotlinx.android.synthetic.main.feeding_date_stub.view.*
import kotlinx.android.synthetic.main.schedule_details_view.*
import me.anon.grow.R
import me.anon.grow.ScheduleDateDetailsActivity
import me.anon.lib.SnackBar
import me.anon.lib.Unit
import me.anon.lib.ext.T
import me.anon.lib.helper.FabAnimator
import me.anon.lib.manager.ScheduleManager
import me.anon.model.FeedingSchedule
import me.anon.model.FeedingScheduleDate

/**
 * // TODO: Add class description
 */
class FeedingScheduleDetailsFragment : Fragment()
{
	companion object
	{
		fun newInstance(scheduleIndex: Int = -1): FeedingScheduleDetailsFragment
		{
			return FeedingScheduleDetailsFragment().apply {
				this.scheduleIndex = scheduleIndex
			}
		}
	}

	private var scheduleIndex: Int = -1
	private var schedules = arrayListOf<FeedingScheduleDate>()
	private val measureUnit: Unit by lazy { Unit.getSelectedMeasurementUnit(activity); }
	private val deliveryUnit: Unit by lazy { Unit.getSelectedDeliveryUnit(activity); }

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater?.inflate(R.layout.schedule_details_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		if (scheduleIndex > -1)
		{
			schedules = ScheduleManager.instance.schedules[scheduleIndex].schedules
			title.setText(ScheduleManager.instance.schedules[scheduleIndex].name)
			description.setText(ScheduleManager.instance.schedules[scheduleIndex].description)
		}

		populateSchedules()

		fab_complete.setOnClickListener {
			when (scheduleIndex)
			{
				-1 -> {
					val schedule: FeedingSchedule = FeedingSchedule(
						name = title.text.toString(),
						description = description.text.toString(),
						schedules = schedules
					)

					ScheduleManager.instance.insert(schedule)
				}
				else -> {
					ScheduleManager.instance.schedules[scheduleIndex].apply {
						name = this@FeedingScheduleDetailsFragment.title.text.toString()
						description = this@FeedingScheduleDetailsFragment.description.text.toString()
						schedules = this@FeedingScheduleDetailsFragment.schedules
					}

					ScheduleManager.instance.save()
				}
			}

			activity.finish()
		}
	}

	public fun onBackPressed(): Boolean
	{
		if (scheduleIndex > -1)
		{
			with (ScheduleManager.instance.schedules[scheduleIndex])
			{
				if (name.isEmpty() && schedules.isEmpty())
				{
					ScheduleManager.instance.schedules.removeAt(scheduleIndex)
					return true
				}
			}
		}

		return true
	}

	/**
	 * Populates the schedules container
	 */
	private fun populateSchedules()
	{
		schedules_container.removeViews(0, schedules_container.indexOfChild(new_schedule))
		schedules.forEachIndexed { index, schedule ->
			val feedingView = LayoutInflater.from(activity).inflate(R.layout.feeding_date_stub, schedules_container, false)
			feedingView.title.text = "${schedule.dateRange[0]}${schedule.stageRange[0].printString[0]}"
			if (schedule.dateRange[0] != schedule.dateRange[1])
			{
				feedingView.title.text = "${feedingView.title.text} - ${schedule.dateRange[1]}${schedule.stageRange[1].printString[0]}"
			}

			var waterStr = ""
			for (additive in schedule.additives)
			{
				val converted = Unit.ML.to(measureUnit, additive.amount!!)
				val amountStr = if (converted == Math.floor(converted)) converted.toInt().toString() else converted.toString()

				if (waterStr.isNotEmpty()) waterStr += "<br />"
				waterStr += "• ${additive.description} - ${amountStr}${measureUnit.label}/${deliveryUnit.label}"
			}

			feedingView.additives.text = Html.fromHtml(waterStr)
			if (feedingView.additives.text.isEmpty()) feedingView.additives.visibility = View.GONE

			feedingView.delete.setOnClickListener { view ->
				AlertDialog.Builder(view.context)
					.setTitle(R.string.confirm_title)
					.setMessage(R.string.confirm_delete_schedule)
					.setPositiveButton(R.string.confirm_positive) { dialog, which ->
						val index = schedules.indexOf(schedule)
						schedules.remove(schedule)
						populateSchedules()

						SnackBar().show(activity!!, R.string.schedule_deleted, R.string.undo, {
							FabAnimator.animateUp(fab_complete)
						}, {
							FabAnimator.animateDown(fab_complete)
						}, {
							schedules.add(index, schedule)
							populateSchedules()
						})
					}
					.setNegativeButton(R.string.confirm_negative, null)
					.show()
			}

			feedingView.copy.setOnClickListener { view ->
				val newSchedule = Kryo().copy(schedule)
				val index = schedules_container.indexOfChild(feedingView)
				schedules.add((index < 0) T schedules.size - 1 ?: index, newSchedule)
				populateSchedules()

				SnackBar().show(activity!!, R.string.schedule_copied, R.string.undo, {
					FabAnimator.animateUp(fab_complete)
				}, {
					FabAnimator.animateDown(fab_complete)
				}, {
					schedules.remove(newSchedule)
					populateSchedules()
				})
			}

			feedingView.setOnClickListener {
				startActivityForResult(Intent(it.context, ScheduleDateDetailsActivity::class.java).also {
					it.putExtra("schedule_index", scheduleIndex)
					it.putExtra("date_index", index)
				}, 0)
			}
			schedules_container.addView(feedingView, schedules_container.childCount - 1)
		}

		new_schedule.setOnClickListener {
			if (scheduleIndex < 0)
			{
				schedules = arrayListOf()
				ScheduleManager.instance.insert(FeedingSchedule(
					name = title.text.toString(),
					description = description.text.toString(),
					schedules = schedules
				))
				scheduleIndex = ScheduleManager.instance.schedules.size - 1
			}

			startActivityForResult(Intent(it.context, ScheduleDateDetailsActivity::class.java).also {
				it.putExtra("schedule_index", scheduleIndex)
				it.putExtra("date_index", -1)
			}, 0)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		super.onActivityResult(requestCode, resultCode, data)
		schedules = ScheduleManager.instance.schedules[scheduleIndex].schedules
		populateSchedules()
	}
}
