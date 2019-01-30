package me.anon.grow.fragment

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import com.esotericsoftware.kryo.Kryo
import com.kenny.snackbar.SnackBar
import kotlinx.android.synthetic.main.schedule_list_view.*
import me.anon.controller.adapter.FeedingScheduleAdapter
import me.anon.grow.FeedingScheduleDetailsActivity
import me.anon.grow.R
import me.anon.lib.helper.FabAnimator
import me.anon.lib.manager.ScheduleManager
import me.anon.lib.show

/**
 * Fragment for displaying list of feeding schedules
 */
class FeedingScheduleListFragment : Fragment()
{
	private val adapter = FeedingScheduleAdapter()

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater?.inflate(R.layout.schedule_list_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		adapter.items = ScheduleManager.instance.schedules
		recycler_view.adapter = adapter
		recycler_view.layoutManager = LinearLayoutManager(activity)
		recycler_view.addItemDecoration(DividerItemDecoration(activity, VERTICAL).also {
			it.setDrawable(resources.getDrawable(R.drawable.left_inset_divider))
		})

		adapter.onDeleteCallback = { schedule ->
			val index = ScheduleManager.instance.schedules.indexOf(schedule)
			ScheduleManager.instance.schedules.remove(schedule)
			ScheduleManager.instance.save()
			adapter.items = ScheduleManager.instance.schedules
			adapter.notifyDataSetChanged()

			SnackBar().show(activity, R.string.schedule_deleted, R.string.undo, {
				FabAnimator.animateUp(fab_add)
			}, {
				FabAnimator.animateDown(fab_add)
			}, {
				ScheduleManager.instance.schedules.add(index, schedule)
				ScheduleManager.instance.save()
				adapter.items = ScheduleManager.instance.schedules
				adapter.notifyDataSetChanged()
			})
		}

		adapter.onCopyCallback = { schedule ->
			val newSchedule = Kryo().copy(schedule)
			newSchedule.name += " (copy)"
			ScheduleManager.instance.insert(newSchedule)
			adapter.items = ScheduleManager.instance.schedules
			adapter.notifyDataSetChanged()

			SnackBar().show(activity, R.string.schedule_copied, R.string.undo, {
				FabAnimator.animateUp(fab_add)
			}, {
				FabAnimator.animateDown(fab_add)
			}, {
				ScheduleManager.instance.schedules.remove(newSchedule)
				ScheduleManager.instance.save()
				adapter.items = ScheduleManager.instance.schedules
				adapter.notifyDataSetChanged()
			})
		}

		fab_add.setOnClickListener {
			startActivity(Intent(activity, FeedingScheduleDetailsActivity::class.java))
		}
	}

	override fun onResume()
	{
		super.onResume()
		adapter.items = ScheduleManager.instance.schedules
		adapter.notifyDataSetChanged()
	}
}
