package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.ui.action.view.PhotoLogView
import me.anon.grow3.ui.logs.view.PhotoLogCard

/**
 * See [me.anon.grow3.ui.logs.view.PhotoLogCard]
 */
@JsonClass(generateAdapter = true)
data class Photo(
	public val imagePaths: ArrayList<String> = arrayListOf()
) : Log(action = "Photo")

public fun Photo.logView(diary: Diary, log: Log) = PhotoLogView(diary, log as Photo)
public fun Photo.logCard(diary: Diary, log: Log) = PhotoLogCard(diary, log as Photo)