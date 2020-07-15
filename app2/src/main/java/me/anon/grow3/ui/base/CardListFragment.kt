package me.anon.grow3.ui.base

import androidx.core.view.updatePadding
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import me.anon.grow3.databinding.FragmentCardListBinding
import me.anon.grow3.util.dp
import me.anon.grow3.util.updateMargin
import me.anon.grow3.view.adapter.CardListAdapter

abstract class CardListFragment : BaseFragment(FragmentCardListBinding::class)
{
	private val viewBindings by viewBinding<FragmentCardListBinding>()
	protected val viewAdapter = CardListAdapter()

	override fun bindUi()
	{
		setToolbar(viewBindings.includeToolbar.toolbar)

		insets.observe(viewLifecycleOwner) {
			viewBindings.includeToolbar.toolbar.updateMargin(it.left, it.top, it.right)
			viewBindings.recyclerView.updatePadding(it.left, right = it.right, bottom = it.bottom + 72.dp(requireContext()))
		}

		viewBindings.recyclerView.adapter = viewAdapter
		viewBindings.recyclerView.layoutManager = LinearLayoutManager(requireContext())
		viewAdapter.clear()
	}
}
