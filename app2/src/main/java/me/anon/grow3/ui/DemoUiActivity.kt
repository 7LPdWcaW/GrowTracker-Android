package me.anon.grow3.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.databinding.ActivityDemoUiBinding
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.util.Injector
import me.anon.grow3.util.component
import javax.inject.Inject

class DemoUiActivity : BaseActivity(ActivityDemoUiBinding::class)
{
	override val inject: Injector = { component.inject(this) }
	public val viewBindings by viewBinding<ActivityDemoUiBinding>()
	@Inject internal lateinit var repository: DiariesRepository

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setSupportActionBar(viewBindings.toolbar)
		title = "Demo UI"

		viewBindings.menuFab.setOnClickListener { viewBindings.menuFab.isExpanded = !viewBindings.menuFab.isExpanded }
		viewBindings.sheet.setOnClickListener { viewBindings.menuFab.isExpanded = false }

		lifecycleScope.launchWhenCreated {
			val diary = repository.getDiaryById("0000-000000") ?: return@launchWhenCreated
			viewBindings.stageView.setStages(diary, diary.crop("0002-000004"))
		}
	}
}
