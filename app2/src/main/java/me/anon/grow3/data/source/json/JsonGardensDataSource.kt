package me.anon.grow3.data.source.json

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.anon.grow3.data.model.Garden
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.util.DataResult
import me.anon.grow3.util.loadAsGardens
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class JsonGardensDataSource @Inject constructor(
	@Named("garden_source") private var sourcePath: String,
	@Named("io_dispatcher") private val dispatcher: CoroutineDispatcher
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
			gardens.postValue(withContext(dispatcher) {
				val file = File(sourcePath, "gardens.json")
				val data = file.loadAsGardens { arrayListOf() }

				_loaded.postValue(Result.success(true))
				return@withContext data
			})
		}

		return gardens.value!!
	}
}
