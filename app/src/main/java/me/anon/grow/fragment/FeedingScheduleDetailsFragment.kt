package me.anon.grow.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.esotericsoftware.kryo.Kryo
import kotlinx.android.synthetic.main.feeding_date_stub.view.*
import kotlinx.android.synthetic.main.schedule_details_view.*
import me.anon.grow.R
import me.anon.grow.ScheduleDateDetailsActivity
import me.anon.lib.SnackBar
import me.anon.lib.Unit
import me.anon.lib.ext.T
import me.anon.lib.manager.ScheduleManager
import me.anon.model.FeedingSchedule
import me.anon.model.FeedingScheduleDate
import java.util.*
import kotlin.math.floor

class FeedingScheduleDetailsFragment : Fragment()
{
	companion object
	{
		fun newInstance(bundle: Bundle?): FeedingScheduleDetailsFragment
		{
			return FeedingScheduleDetailsFragment().apply {
				this.schedule = bundle?.getParcelable("schedule")
				this.scheduleDates = this.schedule?.schedules ?: arrayListOf()
			}
		}
	}

	private var schedule: FeedingSchedule? = null
	private var scheduleDates = arrayListOf<FeedingScheduleDate>()
	private val measureUnit: Unit by lazy { Unit.getSelectedMeasurementUnit(activity); }
	private val deliveryUnit: Unit by lazy { Unit.getSelectedDeliveryUnit(activity); }

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.schedule_details_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		if (savedInstanceState != null)
		{
			schedule = savedInstanceState.get("schedule") as FeedingSchedule
			scheduleDates = savedInstanceState.getParcelableArrayList<FeedingScheduleDate>("schedule_dates") as ArrayList<FeedingScheduleDate>
		}

		title.setText(schedule?.name)
		description.setText(schedule?.description)

		populateScheduleDates()

		fab_complete.setOnClickListener {
			title.error = null

			if (scheduleDates.isNotEmpty() && title.text.isEmpty())
			{
				title.error = getString(R.string.field_required)
			}
			else
			{
				ScheduleManager.instance.upsert(when (schedule)
				{
					null -> {
						FeedingSchedule(
							name = title.text.toString(),
							description = description.text.toString(),
							_schedules = scheduleDates
						)
					}
					else -> {
						schedule!!.apply {
							name = this@FeedingScheduleDetailsFragment.title.text.toString()
							description = this@FeedingScheduleDetailsFragment.description.text.toString()
							_schedules = this@FeedingScheduleDetailsFragment.scheduleDates
						}
					}
				})

				activity?.finish()
			}
		}
	}

	override fun onSaveInstanceState(outState: Bundle)
	{
		outState.putParcelable("schedule", schedule)
		outState.putParcelableArrayList("schedule_dates", scheduleDates)
		super.onSaveInstanceState(outState)
	}

	public fun onBackPressed(): Boolean
	{
//		if (scheduleIndex > -1)
//		{
//			with (schedule)
//			{
//				if (name.isEmpty() && scheduleDates.isEmpty())
//				{
//					ScheduleManager.instance.scheduleDates.removeAt(scheduleIndex)
//					return true
//				}
//			}
//		}

		return true
	}

	/**
	 * Populates the scheduleDates container
	 */
	private fun populateScheduleDates()
	{
		schedule_dates_container.removeViews(0, schedule_dates_container.indexOfChild(new_schedule))
		scheduleDates.sortWith(compareBy<FeedingScheduleDate> { it.stageRange[0].ordinal }.thenBy { it.dateRange[0] })
		scheduleDates.forEachIndexed { index, date ->
			val feedingView = LayoutInflater.from(activity).inflate(R.layout.feeding_date_stub, schedule_dates_container, false)
			feedingView.title.text = "${date.dateRange[0]}${getString(date.stageRange[0].printString)[0]}"
			if (date.dateRange[0] != date.dateRange[1] || date.stageRange[0] != date.stageRange[1])
			{
				feedingView.title.text = "${feedingView.title.text} - ${date.dateRange[1]}${getString(date.stageRange[1].printString)[0]}"
			}

			var waterStr = ""
			for (additive in date.additives)
			{
				val converted = Unit.ML.to(measureUnit, additive.amount!!)
				val amountStr = if (converted == floor(converted)) converted.toInt().toString() else converted.toString()

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
						val index = scheduleDates.indexOf(date)
						scheduleDates.remove(date)
						populateScheduleDates()

						SnackBar().show(activity as AppCompatActivity, R.string.schedule_deleted, R.string.undo, action = {
							scheduleDates.add(index, date)
							populateScheduleDates()
						})
					}
					.setNegativeButton(R.string.confirm_negative, null)
					.show()
			}

			feedingView.copy.setOnClickListener { view ->
				val newSchedule = Kryo().copy(date)
				newSchedule.id = UUID.randomUUID().toString()
				scheduleDates.add(newSchedule)
				populateScheduleDates()

				SnackBar().show(activity!!, R.string.schedule_copied, R.string.undo, action = {
					scheduleDates.remove(newSchedule)
					populateScheduleDates()
				})
			}

			feedingView.setOnClickListener {
				startActivityForResult(Intent(it.context, ScheduleDateDetailsActivity::class.java).also {
					it.putExtra("schedule_date", date)
				}, 0)
			}
			schedule_dates_container.addView(feedingView, schedule_dates_container.childCount - 1)
		}

		new_schedule.setOnClickListener {
			if (schedule == null)
			{
				scheduleDates = arrayListOf()
				schedule = FeedingSchedule(
					name = title.text.toString(),
					description = description.text.toString(),
					_schedules = scheduleDates
				)
				ScheduleManager.instance.insert(schedule!!)
			}

			startActivityForResult(Intent(it.context, ScheduleDateDetailsActivity::class.java).also {
				it.putExtra("schedule", schedule)
			}, 0)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		super.onActivityResult(requestCode, resultCode, data)

		if (resultCode == Activity.RESULT_OK)
		{
			if (data?.extras?.containsKey("schedule_date") == true)
			{
				val replacement = data?.extras?.get("schedule_date") as FeedingScheduleDate
				val index = scheduleDates.indexOfFirst { it.id == replacement.id }
				if (index > -1) scheduleDates[index] = replacement else scheduleDates.add(replacement)
			}
		}

		populateScheduleDates()
	}
}
