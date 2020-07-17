package me.anon.grow3.data.source.nitrite

import com.fasterxml.jackson.datatype.joda.JodaModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.source.CacheDataSource
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getCollection
import org.dizitart.kno2.getRepository
import org.dizitart.kno2.nitrite
import org.dizitart.no2.Document
import org.dizitart.no2.NitriteId
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NitriteCacheDataSource @Inject constructor(
	@Named("cache_source") private val sourcePath: String,
	@Named("io_dispatcher") private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CacheDataSource
{
	private val db = nitrite {
		file = File(sourcePath)
		autoCommit = true
		autoCommitBufferSize = 1024
		registerModule(JodaModule())
	}

	override suspend fun cache(log: Log): String
		= withContext(dispatcher) {
			db.getRepository<Log> {
				insert(log)
			}
			db.commit()
			log.id
		}

	override suspend fun retrieveLog(id: String): Log
		= withContext(dispatcher) {
			db.getRepository<Log>()
				.find(Log::id eq id)
				.first()
		}

	override suspend fun cache(crop: Crop): String
		= withContext(dispatcher) {
			db.getRepository<Crop> {
				insert(crop)
			}
			db.commit()
			crop.id
		}

	override suspend fun retrieveCrop(id: String): Crop
		= withContext(dispatcher) {
			db.getRepository<Crop>()
				.find(Crop::id eq id)
				.first()
		}

	override suspend fun cache(diary: Diary): String
		= withContext(dispatcher) {
			db.getRepository<Diary> {
				insert(diary)
			}
			db.commit()
			diary.id
		}

	override suspend fun retrieveDiary(id: String): Diary
		= withContext(dispatcher) {
			db.getRepository<Diary>()
				.find(Diary::id eq id)
				.first()
		}

	override suspend fun cache(map: Map<String, Any?>): String
		= withContext(dispatcher) {
			var _id = NitriteId.newId()
			db.getCollection("map") {
				insert(Document().apply {
					_id = id
					map.forEach { (k, v) ->
						put(k, v)
					}
				})
			}
			db.commit()
			_id.idValue.toString()
		}

	override suspend fun retrieveMap(id: String): Map<String, Any?>
		= withContext(dispatcher) {
			db.getCollection("map")
				.getById(NitriteId.createId(id.toLong()))
		}
}
