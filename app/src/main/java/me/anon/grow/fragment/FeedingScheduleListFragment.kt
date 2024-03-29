package me.anon.grow.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.esotericsoftware.kryo.Kryo
import kotlinx.android.synthetic.main.schedule_list_view.*
import me.anon.controller.adapter.FeedingScheduleAdapter
import me.anon.grow.FeedingScheduleDetailsActivity
import me.anon.grow.R
import me.anon.lib.SnackBar
import me.anon.lib.manager.ScheduleManager
import java.util.*

/**
 * Fragment for displaying list of feeding schedules
 */
class FeedingScheduleListFragment : Fragment()
{
	private val adapter = FeedingScheduleAdapter()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.schedule_list_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		adapter.items = ScheduleManager.instance.schedules
		recycler_view.adapter = adapter
		recycler_view.layoutManager = LinearLayoutManager(activity)
		recycler_view.addItemDecoration(DividerItemDecoration(activity, VERTICAL))

		adapter.onDeleteCallback = { schedule ->
			val index = ScheduleManager.instance.schedules.indexOf(schedule)
			ScheduleManager.instance.schedules.remove(schedule)
			ScheduleManager.instance.save()
			adapter.items = ScheduleManager.instance.schedules
			adapter.notifyDataSetChanged()
			checkAdapter()

			SnackBar().show(activity as AppCompatActivity, R.string.schedule_deleted, R.string.undo, action = {
				ScheduleManager.instance.schedules.add(index, schedule)
				ScheduleManager.instance.save()
				adapter.items = ScheduleManager.instance.schedules
				adapter.notifyDataSetChanged()
				checkAdapter()
			})
		}

		adapter.onCopyCallback = { schedule ->
			val newSchedule = Kryo().copy(schedule)
			newSchedule.id = UUID.randomUUID().toString()
			newSchedule.name += " (copy)"
			ScheduleManager.instance.insert(newSchedule)
			adapter.items = ScheduleManager.instance.schedules
			adapter.notifyDataSetChanged()
			checkAdapter()

			SnackBar().show(activity as AppCompatActivity, R.string.schedule_copied, R.string.undo, action = {
				ScheduleManager.instance.schedules.remove(newSchedule)
				ScheduleManager.instance.save()
				adapter.items = ScheduleManager.instance.schedules
				adapter.notifyDataSetChanged()
				checkAdapter()
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

		checkAdapter()
	}

	private fun checkAdapter()
	{
		if (adapter.itemCount == 0)
		{
			empty.visibility = View.VISIBLE
			recycler_view.visibility = View.GONE
		}
		else
		{
			empty.visibility = View.GONE
			recycler_view.visibility = View.VISIBLE
		}
	}
}
