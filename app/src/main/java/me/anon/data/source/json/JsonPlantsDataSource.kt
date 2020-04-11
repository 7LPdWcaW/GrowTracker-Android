package me.anon.data.source.json

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.anon.data.source.PlantsDataSource
import me.anon.lib.helper.MoshiHelper
import me.anon.model.Plant
import java.io.File
import java.io.FileInputStream

/**
 * // TODO: Add class description
 */
class JsonPlantsDataSource internal constructor(
	public var sourcePath: String,
	private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PlantsDataSource
{
	private var loaded = false
	private val plants: MutableLiveData<List<Plant>> = MutableLiveData(arrayListOf())

	override fun observePlants(): LiveData<List<Plant>> = plants

	override suspend fun getPlants(): List<Plant>
	{
		if (!loaded)
		{
			plants.postValue(withContext(ioDispatcher) {
				return@withContext try {
					MoshiHelper.parse<List<Plant>>(FileInputStream(File(sourcePath, "plants.json")), Types.newParameterizedType(ArrayList::class.java, Plant::class.java)).also {
						loaded = true
					}
				}
				catch (e: Exception)
				{
					e.printStackTrace()
					arrayListOf<Plant>()
				}
			})
		}

		return plants.value!!
	}
}
