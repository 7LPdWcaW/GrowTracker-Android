package me.anon.grow3.util

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.dizitart.no2.mapper.JacksonFacade

open class NitriteFacade(modules: Set<Module>? = setOf()) : JacksonFacade(modules)
{
	override fun createObjectMapper(): ObjectMapper
	{
		val objectMapper = super.createObjectMapper()
		objectMapper.registerModule(KotlinModule())
		objectMapper.registerModule(JodaModule())
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		return objectMapper
	}
}
