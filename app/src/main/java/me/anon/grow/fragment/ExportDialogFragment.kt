package me.anon.grow.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import me.anon.grow.R
import me.anon.grow.databinding.ExportDialogBinding
import me.anon.lib.export.ExportProcessor
import me.anon.lib.export.MarkdownProcessor

class ExportDialogFragment(val callback: (processor: Class<out ExportProcessor>, includeImages: Boolean) -> Unit) : DialogFragment()
{
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		val binding = ExportDialogBinding.inflate(layoutInflater)
		val dialog = AlertDialog.Builder(requireContext())

		val options = arrayOf("Markdown")//, "HTML")
		val optionCls = arrayOf(MarkdownProcessor::class.java)//, HtmlProcessor::class.java)
		binding.formatters.adapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, options)

		dialog.setTitle(R.string.menu_export)
		dialog.setPositiveButton(R.string.menu_export) { dialog, which ->
			callback(optionCls[binding.formatters.selectedItemPosition], binding.includeImages.isChecked)
		}

		dialog.setView(view)
		dialog.setNegativeButton(R.string.cancel) { dialogInterface, i -> onCancel(dialogInterface) }

		return dialog.create()
	}
}
