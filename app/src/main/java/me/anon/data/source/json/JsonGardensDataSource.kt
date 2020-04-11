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
	private var loaded = false
	private val gardens: MutableLiveData<List<Garden>> = MutableLiveData(arrayListOf())

	override fun observeGardens(): LiveData<List<Garden>> = gardens

	override suspend fun getGardens(): List<Garden>
	{
		if (!loaded)
		{
			gardens.postValue(withContext(ioDispatcher) {
				return@withContext try {
					MoshiHelper.parse<List<Garden>>(
						json = FileInputStream(File(sourcePath, "gardens.json")),
						type = Types.newParameterizedType(ArrayList::class.java, Garden::class.java)
					).also {
						loaded = true
					}
				}
				catch (e: Exception)
				{
					e.printStackTrace()
					arrayListOf<Garden>()
				}
			})
		}

		return gardens.value!!
	}
}
