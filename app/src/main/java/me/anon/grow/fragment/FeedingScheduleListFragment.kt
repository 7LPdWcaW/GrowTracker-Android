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
import me.anon.controller.adapter.FeedingScheduleAdapter
import me.anon.grow.FeedingScheduleDetailsActivity
import me.anon.grow.R
import me.anon.grow.databinding.ScheduleListViewBinding
import me.anon.lib.SnackBar
import me.anon.lib.manager.ScheduleManager
import java.util.*

/**
 * Fragment for displaying list of feeding schedules
 */
class FeedingScheduleListFragment : Fragment()
{
	private val adapter = FeedingScheduleAdapter()

	private lateinit var binding: ScheduleListViewBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= ScheduleListViewBinding.inflate(inflater, container, false).let {
		binding = it
		it.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		adapter.items = ScheduleManager.instance.schedules
		binding.recyclerView.adapter = adapter
		binding.recyclerView.layoutManager = LinearLayoutManager(activity)
		binding.recyclerView.addItemDecoration(DividerItemDecoration(activity, VERTICAL))

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

		binding.fabAdd.setOnClickListener {
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
			binding.empty.visibility = View.VISIBLE
			binding.recyclerView.visibility = View.GONE
		}
		else
		{
			binding.empty.visibility = View.GONE
			binding.recyclerView.visibility = View.VISIBLE
		}
	}
}
