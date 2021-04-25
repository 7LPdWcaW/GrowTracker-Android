package me.anon.view

import android.app.Application
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import me.anon.data.repository.GardensRepository
import me.anon.data.repository.PlantsRepository
import me.anon.data.repository.impl.DefaultGardensRepository
import me.anon.data.repository.impl.DefaultPlantsRepository
import me.anon.data.source.json.JsonGardensDataSource
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

	private val gardensPath get() = getExternalFilesDir(null)!!.absolutePath
	private var _gardensRepository: GardensRepository? = null
	public val gardensRepository: GardensRepository by lazy {
		synchronized(lock) {
			_gardensRepository ?: _gardensRepository ?: let {
				_gardensRepository = DefaultGardensRepository(JsonGardensDataSource(gardensPath))
				_gardensRepository
			} ?: throw IllegalStateException("Unable to load gardens repository")
		}
	}

	override fun onCreate()
	{
		super.onCreate()

		ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this))
	}
}
