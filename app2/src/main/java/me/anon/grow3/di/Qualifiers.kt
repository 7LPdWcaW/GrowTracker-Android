package me.anon.grow3.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DiariesSource

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CorePrefs

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Cards

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Logs