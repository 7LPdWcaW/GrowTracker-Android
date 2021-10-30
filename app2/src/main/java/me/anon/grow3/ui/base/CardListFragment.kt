package me.anon.grow3.ui.base

import android.content.Context
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import me.anon.grow3.databinding.FragmentCardListBinding
import me.anon.grow3.util.component
import me.anon.grow3.util.dp
import me.anon.grow3.util.updateMargin
import me.anon.grow3.view.adapter.CardListAdapter
import javax.inject.Inject

abstract class CardListFragment : BaseFragment(FragmentCardListBinding::class)
{
	private val viewBindings by viewBinding<FragmentCardListBinding>()
	@Inject protected lateinit var viewAdapter: CardListAdapter

	override fun onAttach(context: Context)
	{
		super.onAttach(context)
		component.inject(this)
	}

	override fun bindUi()
	{
		setToolbar(viewBindings.includeToolbar.toolbar)
		viewBindings.includeToolbar.toolbar.title = ""

		insets.observe(viewLifecycleOwner) {
			viewBindings.includeToolbar.toolbar.updateMargin(it.left, it.top, it.right)
			viewBindings.recyclerView.updatePadding(it.left, right = it.right, bottom = it.bottom + 72.dp(requireContext()))
		}

		viewBindings.recyclerView.adapter = viewAdapter
		viewBindings.recyclerView.layoutManager = LinearLayoutManager(requireContext())
		viewAdapter.clearStack()
	}
}
