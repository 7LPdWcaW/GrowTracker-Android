package me.anon.lib.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import me.anon.view.MainApplication2
import me.anon.view.viewmodel.ViewModelFactory

public fun Fragment.viewModelFactory(): ViewModelFactory = ViewModelFactory(requireActivity().application as MainApplication2, this, arguments)
public fun AppCompatActivity.viewModelFactory(): ViewModelFactory = ViewModelFactory(application as MainApplication2, this)
