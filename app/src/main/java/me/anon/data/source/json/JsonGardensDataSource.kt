package me.anon.data.source.json

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.anon.data.source.GardensDataSource
import me.anon.lib.helper.MoshiHelper
import me.anon.model.Garden
import java.io.File
import java.io.FileInputStream

/**
 * // TODO: Add class description
 */
class JsonGardensDataSource internal constructor(
	private var sourcePath: String,
	private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GardensDataSource
{
	private var _loaded = MutableLiveData<Result<Boolean>>()
	private val gardens: MutableLiveData<List<Garden>> = MutableLiveData(arrayListOf())

	override fun loaded(): LiveData<Result<Boolean>> = _loaded

	override fun observeGardens(): LiveData<List<Garden>> = gardens

	override suspend fun getGardens(): List<Garden>
	{
		if (_loaded.value == null)
		{
			gardens.postValue(withContext(ioDispatcher) {
				return@withContext try {
					val data = MoshiHelper.parse<List<Garden>>(
						json = FileInputStream(File(sourcePath, "gardens.json")),
						type = Types.newParameterizedType(ArrayList::class.java, Garden::class.java)
					)

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
