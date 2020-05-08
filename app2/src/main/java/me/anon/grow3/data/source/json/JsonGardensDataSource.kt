package me.anon.grow3.data.source.json

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.anon.grow3.data.model.Garden
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.util.DataResult
import me.anon.grow3.util.MoshiHelper
import java.io.File
import java.io.FileInputStream
import javax.inject.Singleton

/**
 * // TODO: Add class description
 */
@Singleton
class JsonGardensDataSource internal constructor(
	private var sourcePath: String,
	private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GardensDataSource
{
	private var _loaded = MutableLiveData<Result<Boolean>>()
	private val gardens: MutableLiveData<List<Garden>> = MutableLiveData(arrayListOf())

	override fun loaded(): LiveData<DataResult<Boolean>> = TODO()

	override fun observeGardens(): LiveData<DataResult<List<Garden>>> = TODO()//gardens

	override suspend fun getGardens(): List<Garden>
	{
		if (_loaded.value == null)
		{
			gardens.postValue(withContext(ioDispatcher) {
				return@withContext try {
					val file = File(sourcePath, "gardens.json")
					var data: List<Garden> = arrayListOf()
					if (file.exists())
					{
						data = MoshiHelper.parse(
							json = FileInputStream(file),
							type = Types.newParameterizedType(ArrayList::class.java, Garden::class.java)
						)
					}

					_loaded.postValue(Result.success(true))
					data
				}
				catch (e: Exception)
				{
					e.printStackTrace()
					_loaded.postValue(Result.failure(e.cause ?: e.fillInStackTrace()))
					arrayListOf<Garden>()
				}
			})
		}

		return gardens.value!!
	}
}
