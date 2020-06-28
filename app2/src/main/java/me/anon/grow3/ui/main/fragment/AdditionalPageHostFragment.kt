package me.anon.grow3.ui.main.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentHostBinding
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.util.then

class AdditionalPageHostFragment : BaseHostFragment(FragmentHostBinding::class)
{
	private var transactionJob: Deferred<Unit>? = null
	private var attachJob: Deferred<Unit>? = null
	private val scope = CoroutineScope(Job() + Dispatchers.Main)

	public fun launchWhenAttached(block: suspend CoroutineScope.() -> Unit)
	{
		attachJob = scope.async(start = isAdded then CoroutineStart.DEFAULT ?: CoroutineStart.LAZY) {
			block()
		}
	}

	override fun onAttach(context: Context)
	{
		super.onAttach(context)

		lifecycleScope.launch {
			transactionJob?.await()
			attachJob?.await()
		}
	}

	override fun onDestroy()
	{
		super.onDestroy()
		scope.cancel()
	}

	public fun addPage(fragment: Fragment)
	{
		transactionJob = scope.async(start = CoroutineStart.LAZY) {
			childFragmentManager.commitNow {
				setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				replace(R.id.main_content, fragment)
			}
		}

		if (isAdded) lifecycleScope.launch {
			transactionJob?.await()
		}
	}
}
