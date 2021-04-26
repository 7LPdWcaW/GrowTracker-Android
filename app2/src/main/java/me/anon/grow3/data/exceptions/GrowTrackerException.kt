package me.anon.grow3.data.exceptions

import me.anon.grow3.data.model.Log

sealed class GrowTrackerException(msg: String) : java.lang.Exception(msg)
{
	class DiaryLoadFailed(id: String = "") : GrowTrackerException("Failed to load diary $id")
	class InvalidDiaryId : GrowTrackerException("No diary ID set")
	class InvalidCropId : GrowTrackerException("No crop ID set")
	class InvalidLogType : GrowTrackerException("No log type set")
	class InvalidLog(log: Log) : GrowTrackerException("Could not handle log $log")
	class LogLoadFailed(id: String = "") : GrowTrackerException("Failed to load log $id")
	class CropLoadFailed(id: String = "", diaryId: String = "") : GrowTrackerException("Failed to load crop $id in diary $diaryId")

	class InvalidHostActivity : GrowTrackerException("Main host not attached to Main activity")
	class NoRoute : GrowTrackerException("No route set")
	class InvalidRoute(route: String = "") : GrowTrackerException("Could not route to $route")
	class IllegalState(state: String = "") : GrowTrackerException("Application was in an illegal state: $state")
}
