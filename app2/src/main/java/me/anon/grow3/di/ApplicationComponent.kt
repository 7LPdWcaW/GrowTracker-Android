
package me.anon.grow3.di

import dagger.Component
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.di.module.AppModule
import me.anon.grow3.di.module.DispatcherModule
import me.anon.grow3.ui.BootActivity
import me.anon.grow3.ui.DemoUiActivity
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.action.fragment.LogActionFragment
import me.anon.grow3.ui.crops.fragment.CropListFragment
import me.anon.grow3.ui.crops.fragment.ViewCropFragment
import me.anon.grow3.ui.crud.activity.CropActivity
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.crud.fragment.DiaryCompleteFragment
import me.anon.grow3.ui.crud.fragment.DiaryCropsFragment
import me.anon.grow3.ui.crud.fragment.DiaryDetailsFragment
import me.anon.grow3.ui.crud.fragment.DiaryEnvironmentFragment
import me.anon.grow3.ui.diaries.fragment.DiaryListFragment
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.ui.logs.fragment.LogListFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.handler.ExceptionHandler
import javax.inject.Singleton

@Singleton
@Component(modules = [
	DispatcherModule::class,
	AppModule::class
])
interface ApplicationComponent
{
	public fun exceptionHandler(): ExceptionHandler
	public fun diariesRepo(): DiariesRepository

	public fun inject(fragment: DiaryListFragment)
	public fun inject(fragment: DiaryDetailsFragment)
	public fun inject(fragment: DiaryCropsFragment)
	public fun inject(fragment: DiaryEnvironmentFragment)
	public fun inject(fragment: DiaryCompleteFragment)
	public fun inject(fragment: ViewDiaryFragment)
	public fun inject(fragment: ViewCropFragment)
	public fun inject(fragment: CropListFragment)
	public fun inject(fragment: LogListFragment)
	public fun inject(fragment: LogActionBottomSheetFragment)
	public fun inject(fragment: LogActionFragment)

	public fun inject(activity: BootActivity)
	public fun inject(activity: MainActivity)
	public fun inject(activity: CropActivity)
	public fun inject(activity: DiaryActivity)
	public fun inject(activity: DemoUiActivity)
}
