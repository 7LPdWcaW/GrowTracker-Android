package me.anon.grow3.data.source.nitrite

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.NitriteFacade
import org.dizitart.kno2.KNO2JacksonMapper
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getRepository
import org.dizitart.kno2.nitrite
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NitriteDiariesDataSource @Inject constructor(
	@Named("diaries_source") private val sourcePath: String,
	@Named("io_dispatcher") private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DiariesDataSource
{
	private val db = nitrite {
		nitriteMapper = KNO2JacksonMapper(NitriteFacade())
		file = File(sourcePath)
		autoCommit = true
		autoCommitBufferSize = 1024
	}

	override fun close()
	{
		db.close()
	}

	override suspend fun addDiary(diary: Diary): List<Diary> = withContext(dispatcher) {
		db.getRepository<Diary> {
			update(diary, true)
		}
		db.commit()
		getDiaries()
	}

	override suspend fun deleteDiary(diaryId: String): List<Diary> = withContext(dispatcher) {
		val repo = db.getRepository<Diary>()
		repo.remove(Diary::id eq diaryId)
		getDiaries()
	}

	override suspend fun getDiaryById(diaryId: String): Diary?
	{
		val repo = db.getRepository<Diary>()
		return repo.find(Diary::id eq diaryId).firstOrNull()
	}

	override suspend fun getDiaries(): List<Diary>
	{
		return db.getRepository<Diary>().find().toList()
	}

	override suspend fun sync(direction: DiariesDataSource.SyncDirection, vararg diary: Diary): List<Diary> = withContext(dispatcher) {
		when (direction)
		{
			DiariesDataSource.SyncDirection.Commit -> {
				db.getRepository<Diary> {
					diary.forEach { update(it, true) }
				}

				db.commit()
			}
		}

		getDiaries()
	}
}
