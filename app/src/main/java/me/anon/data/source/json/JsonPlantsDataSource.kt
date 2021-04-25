package me.anon.data.source.json

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Types
import kotlinx.coroutines.*
import me.anon.data.source.PlantsDataSource
import me.anon.lib.helper.MoshiHelper
import me.anon.model.Plant
import java.io.*

/**
 * // TODO: Add class description
 */
class JsonPlantsDataSource internal constructor(
	private var sourcePath: String,
	private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PlantsDataSource
{
	private val _loaded: MutableLiveData<Result<Boolean>> = MutableLiveData()

	private val _plants: MutableLiveData<List<Plant>> = MutableLiveData(arrayListOf())
	override val plants: LiveData<List<Plant>> = _plants

	override fun loaded(): LiveData<Result<Boolean>> = _loaded

	override fun addPlant(plant: Plant)
	{
		_plants.postValue(plants.value?.also { (it as ArrayList).add(plant) })
	}

	override fun setPlant(plant: Plant)
	{
		_plants.postValue(_plants.value?.also {
			val index = (it as ArrayList).indexOfFirst { it.id == plant.id }
			if (index > -1) it[index] = plant
		})
	}

	override suspend fun getPlants(): List<Plant>
	{
		if (_loaded.value == null)
		{
			_plants.postValue(withContext(ioDispatcher) {
				return@withContext try {
					val file = File(sourcePath, "plants.json")
					var data: List<Plant> = arrayListOf()

					if (file.exists())
					{
						data = MoshiHelper.parse(
							json = FileInputStream(file),
							type = Types.newParameterizedType(ArrayList::class.java, Plant::class.java)
						)
					}

					_loaded.postValue(Result.success(true))
					data
				}
				catch (e: Exception)
				{
					e.printStackTrace()
					_loaded.postValue(Result.failure(e.cause ?: e.fillInStackTrace()))
					arrayListOf<Plant>()
				}
			})
		}

		return _plants.value!!
	}

	override suspend fun getPlantById(id: String): Plant? = getPlants().find { it.id == id }

	override suspend fun save()
	{
		_plants.value ?: return
		coroutineScope {
			launch {
				val outstream = FileOutputStream(File(sourcePath, "plants.json"))
				val output = MoshiHelper.toJson(_plants.value ?: arrayListOf(), Types.newParameterizedType(ArrayList::class.java, Plant::class.java))
				val writer = BufferedWriter(OutputStreamWriter(outstream))
				writer.write(output)
				writer.flush()
				writer.close()

				_plants.postValue(plants.value)
			}
		}
	}
}
