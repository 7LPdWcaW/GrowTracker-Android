package me.anon.grow.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.export_dialog.view.*
import me.anon.grow.R
import me.anon.lib.export.ExportProcessor
import me.anon.lib.export.HtmlProcessor
import me.anon.lib.export.MarkdownProcessor

class ExportDialogFragment(val callback: (processor: Class<out ExportProcessor>, includeImages: Boolean) -> Unit) : DialogFragment()
{
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		val view = LayoutInflater.from(activity).inflate(R.layout.export_dialog, null)
		val dialog = AlertDialog.Builder(context!!)

		val options = arrayOf("Markdown", "HTML")
		val optionCls = arrayOf(MarkdownProcessor::class.java, HtmlProcessor::class.java)
		view.formatters.adapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_list_item_1, options)

		dialog.setTitle(R.string.menu_export)
		dialog.setPositiveButton(R.string.menu_export) { dialog, which ->
			callback(optionCls[view.formatters.selectedItemPosition], view.include_images.isChecked)
		}

		dialog.setView(view)
		dialog.setNegativeButton(R.string.cancel) { dialogInterface, i -> onCancel(dialogInterface) }

		return dialog.create()
	}
}
