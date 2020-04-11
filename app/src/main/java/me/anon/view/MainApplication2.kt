package me.anon.view

import android.app.Application
import me.anon.data.repository.DefaultPlantsRepository
import me.anon.data.repository.PlantsRepository
import me.anon.data.source.json.JsonPlantsDataSource

/**
 * // TODO: Add class description
 */
class MainApplication2 : Application()
{
	private val lock = Any()

	private val plantsPath get() = getExternalFilesDir(null)!!.absolutePath
	private var _plantsRepository: PlantsRepository? = null
	public val plantsRepository: PlantsRepository by lazy {
		synchronized(lock) {
			_plantsRepository ?: _plantsRepository ?: let {
				_plantsRepository = DefaultPlantsRepository(JsonPlantsDataSource(plantsPath))
				_plantsRepository
			} ?: throw IllegalStateException("Unable to load plants repository")
		}
	}
}
