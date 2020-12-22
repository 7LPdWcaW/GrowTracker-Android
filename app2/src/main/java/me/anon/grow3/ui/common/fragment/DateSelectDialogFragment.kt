package me.anon.grow3.ui.common.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import me.anon.grow3.util.asApiString
import me.anon.grow3.util.asDateTime
import org.threeten.bp.ZonedDateTime

/**
 * Headless fragment container for displaying date calendar and time clock dialogs
 */
class DateSelectDialogFragment : Fragment()
{
	companion object
	{
		public const val TAG: String = "date_select_dialog"
		private const val SAVED_DIALOG_STATE_TAG: String = "dialog_state"
		private const val SAVED_DIALOG_SELECTED_DATE: String = "selected_date"
		private const val SAVED_DIALOG_CURRENT_DIALOG: String = "current_dialog"
		private const val SAVED_DIALOG_INCLUDE_TIME: String = "include_time"

		public fun show(selectedDate: String = ZonedDateTime.now().asApiString(), includeTime: Boolean = true, fm: FragmentManager): DateSelectDialogFragment
			= show(selectedDate.asDateTime(), includeTime, fm)

		public fun show(selectedDate: ZonedDateTime = ZonedDateTime.now(), includeTime: Boolean = true, fm: FragmentManager): DateSelectDialogFragment
			= DateSelectDialogFragment().apply {
					this.selectedDate = selectedDate
					this.includeTime = includeTime

					fm.commit {
						add(this@apply, TAG)
					}
				}

		public fun attach(fm: FragmentManager, callback: (ZonedDateTime) -> Unit, dismiss: () -> Unit = {})
		{
			(fm.findFragmentByTag(TAG) as? DateSelectDialogFragment)?.apply {
				onDateTimeSelected = callback
				onDismiss = dismiss
			}
		}
	}

	public lateinit var selectedDate: ZonedDateTime
	public var includeTime: Boolean = true
	public var onDateTimeSelected: (ZonedDateTime) -> Unit = {}
	public var onDismiss: () -> Unit = {}

	private var currentDialog: Dialog? = null

	private val dateCallback = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
		selectedDate = selectedDate
			.withYear(year)
			.withMonth(month + 1)
			.withDayOfMonth(dayOfMonth)
			.withHour(0)
			.withMinute(0)

		if (includeTime) showTimeDialog()
		else
		{
			onDateTimeSelected(selectedDate)
			dismiss()
		}
	}

	private val timeCallback = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
		selectedDate = selectedDate
			.withHour(hourOfDay)
			.withMinute(minute)

		onDateTimeSelected(selectedDate)
		dismiss()
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		includeTime = savedInstanceState?.getBoolean(SAVED_DIALOG_INCLUDE_TIME) ?: includeTime
		savedInstanceState?.getString(SAVED_DIALOG_SELECTED_DATE)?.let { date ->
			selectedDate = date.asDateTime()
		}
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		savedInstanceState?.getBundle(SAVED_DIALOG_STATE_TAG)?.apply {
			when (savedInstanceState.getString(SAVED_DIALOG_CURRENT_DIALOG))
			{
				TimePickerDialog::class.simpleName!! -> currentDialog = showTimeDialog(this)
				else -> currentDialog = showDateDialog(this)
			}
		} ?: showDateDialog()
	}

	override fun onSaveInstanceState(outState: Bundle)
	{
		super.onSaveInstanceState(outState)
		currentDialog?.let {
			val dialogState = it.onSaveInstanceState()
			outState.putBundle(SAVED_DIALOG_STATE_TAG, dialogState);
		}
		outState.putString(SAVED_DIALOG_SELECTED_DATE, selectedDate.asApiString())
		outState.putString(SAVED_DIALOG_CURRENT_DIALOG, currentDialog?.javaClass?.simpleName ?: "")
		outState.putBoolean(SAVED_DIALOG_INCLUDE_TIME, includeTime)
	}

	override fun onStart()
	{
		super.onStart()
		currentDialog?.show()
	}

	override fun onStop()
	{
		super.onStop()
		currentDialog?.hide()
	}

	private fun showDateDialog(savedState: Bundle? = null): Dialog
	{
		currentDialog = DatePickerDialog(
			requireContext(),
			dateCallback,
			selectedDate.year,
			selectedDate.monthValue - 1,
			selectedDate.dayOfMonth
		).apply {
			setOnDismissListener {
				dismiss()
			}

			setOnCancelListener {
				dismiss()
			}

			show()
		}
		savedState?.let { currentDialog?.onRestoreInstanceState(savedState) }
		return currentDialog!!
	}

	private fun showTimeDialog(savedState: Bundle? = null): Dialog
	{
		currentDialog = TimePickerDialog(
			requireContext(),
			timeCallback,
			selectedDate.hour,
			selectedDate.minute,
			true
		).apply {
			setOnDismissListener {
				dismiss()
			}

			setOnCancelListener {
				dismiss()
			}

			show()
		}
		savedState?.let { currentDialog?.onRestoreInstanceState(savedState) }
		return currentDialog!!
	}

	public fun dismiss()
	{
		currentDialog?.cancel()
		currentDialog = null
		onDismiss()
		parentFragmentManager.commit { remove(this@DateSelectDialogFragment) }
	}
}
