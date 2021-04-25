package me.anon.grow3.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.size
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import me.anon.grow3.R
import me.anon.grow3.util.afterMeasured
import me.anon.grow3.util.drawable
import me.anon.grow3.util.inflate
import me.anon.grow3.util.parentView

class DropDownEditText : MaterialAutoCompleteTextView
{
	public val items = arrayListOf<MenuItem>()
	public var itemSelectListener: (MenuItem) -> Unit = {}

	class DropDownMenuItem(
		var itemId: Int,
		var isChecked: Boolean = false,
		var isCheckable: Boolean = false,
		var iconRes: Int = R.drawable.ic_empty,
		var titleRes: Int = -1
	)

	public var singleSelection = false
	public var requiredSelection = false
	private var defaultText = ""
	private var defaultIcon: Drawable? = null
	private var menuRes = NO_ID
	private val popup by lazy { ListPopupWindow(context) }
	private val adapter = SelectableMenuAdapter()

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, androidx.appcompat.R.attr.autoCompleteTextViewStyle)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		attrs?.let {
			val typedArray = context.obtainStyledAttributes(it, R.styleable.DropDownEditText, 0, 0)
			menuRes = typedArray.getResourceId(R.styleable.DropDownEditText_menu, NO_ID)
			defaultText = typedArray.getString(R.styleable.DropDownEditText_android_text) ?: ""
			defaultIcon = typedArray.getDrawable(R.styleable.DropDownEditText_android_icon)
			defaultIcon?.setTintList(typedArray.getColorStateList(R.styleable.DropDownEditText_android_iconTint))
			singleSelection = typedArray.getBoolean(R.styleable.DropDownEditText_singleSelection, false)
			requiredSelection = typedArray.getBoolean(R.styleable.DropDownEditText_required, false)
			typedArray.recycle()
		}

		if (menuRes != NO_ID)
		{
			val menu = PopupMenu(context, View(context))
			menu.inflate(menuRes)
			with (menu.menu) {
				for (index in 0 until this.size)
				{
					items += getItem(index)
				}
			}
		}

		init()
	}

	public fun checkItems(vararg ids: Int)
	{
		items.filter { it.itemId in ids }.forEach { it.isChecked = true }
		populateText()
	}

	public fun setMenu(items: List<DropDownMenuItem>)
	{
		this.items.clear()

		val menu = PopupMenu(context, View(context))
		this.items.addAll(items.map {
			menu.menu
				.add(0, it.itemId, 0, it.titleRes)
				.setIcon(it.iconRes)
				.setCheckable(it.isCheckable)
				.setChecked(it.isChecked)
		})

		popup.height = items.size * 180
		adapter.notifyDataSetChanged()
		populateText()
	}

	private fun init()
	{
		popup.height = items.size * 180
		popup.anchorView = this
		popup.setDropDownGravity(Gravity.END)
		popup.isModal = true
		popup.setAdapter(adapter)
		popup.setOnDismissListener {
			val current: View = rootView.findFocus()
			current.clearFocus()
		}
		doOnLayout {
			populateText()
		}
	}

	public fun getSelectedItems(): List<MenuItem> = items.filter { it.isChecked }

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		afterMeasured {
			defaultIcon ?: return@afterMeasured
			(parentView.parentView as? TextInputLayout)?.apply {
				post { startIconDrawable = defaultIcon }
				postInvalidate()
			}
		}
	}

	override fun showDropDown()
	{
		popup.show()
	}

	override fun isPopupShowing(): Boolean = popup.isShowing

	private fun populateText()
	{
		with (items.filter { it.isChecked }) {
			if (size > 0)
			{
				this@DropDownEditText.setText(joinToString { it.title })
				(parentView.parentView as? TextInputLayout)?.startIconDrawable = when(size) {
					1 -> first().icon
					else -> drawable(R.drawable.ic_more_horiz)
				}
			}
			else
			{
				this@DropDownEditText.setText(defaultText)
				(parentView.parentView as? TextInputLayout)?.startIconDrawable = defaultIcon
			}
		}
	}

	private inner class SelectableMenuAdapter : BaseAdapter()
	{
		override fun getItem(position: Int): MenuItem = items[position]
		override fun getItemId(position: Int): Long = items[position].itemId.toLong()
		override fun getCount(): Int = items.size

		override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
		{
			val item = getItem(position)
			val view = convertView ?: parent.inflate(R.layout.list_selectable_menu_item)
			val checkbox = view.findViewById<CheckBox>(R.id.checkbox)
			val icon = view.findViewById<ImageView>(R.id.icon)
			checkbox.isChecked = item.isChecked
			checkbox.isVisible = item.isChecked && item.isCheckable
			icon.isVisible = item.icon != null
			icon.setImageDrawable(item.icon)

			view.setOnClickListener {
				with (it.findViewById<CheckBox>(R.id.checkbox)) {
					if (!requiredSelection || getSelectedItems().size != 1 || !isChecked) isChecked = !isChecked

					isVisible = isChecked
					item.isChecked = isChecked
					itemSelectListener(item)

					if (singleSelection)
					{
						items.forEach { it.isChecked = false }
						if (isChecked) item.isChecked = true
						notifyDataSetChanged()
						popup.dismiss()
					}
				}

				populateText()
			}

			view.findViewById<TextView>(R.id.title).text = item.title

			return view
		}
	}
}
