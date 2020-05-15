package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.textfield.TextInputEditText
import me.anon.grow3.R
import me.anon.lib.ext.inflate

class DropDownEditText : androidx.appcompat.widget.AppCompatAutoCompleteTextView
{
	public var menu: PopupMenu? = null
	private var defaultText = ""
	private var menuRes = NO_ID

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, androidx.appcompat.R.attr.autoCompleteTextViewStyle)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		attrs?.let {
			val typedArray = context.obtainStyledAttributes(it, R.styleable.DropDownEditText, 0, 0)
			menuRes = typedArray.getResourceId(R.styleable.DropDownEditText_menu, NO_ID)
			defaultText = typedArray.getString(R.styleable.DropDownEditText_android_text) ?: ""
			typedArray.recycle()
		}

		if (menuRes != NO_ID)
		{
			menu = PopupMenu(context, this, Gravity.END).apply {
				inflate(menuRes)
			}
		}

		init()
	}

	private fun init()
	{
		menu?.let { menu ->
			setOnClickListener {
				val popup = ListPopupWindow(context)
				popup.anchorView = this
				popup.setDropDownGravity(Gravity.END)
				popup.isModal = true
				popup.setAdapter(SelectableMenuAdapter(context))
				popup.show()
			}
		}
	}

	private class SelectableMenuAdapter(context: Context) : BaseAdapter()
	{
		override fun getItem(position: Int): Any
		{
			return position
		}

		override fun getItemId(position: Int): Long
		{
			return position.toLong()
		}

		override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
		{
			val view = convertView ?: parent.inflate<View>(R.layout.list_selectable_menu_item)
			view.findViewById<TextView>(R.id.title).setText("test $position")

			return view
		}

		override fun getCount(): Int = 15
	}
}
