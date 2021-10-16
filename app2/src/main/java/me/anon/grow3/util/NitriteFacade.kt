package me.anon.grow3.util

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import me.anon.grow3.data.model.LogConstants
import org.dizitart.no2.mapper.JacksonFacade

open class NitriteFacade(modules: Set<Module>? = setOf()) : JacksonFacade(modules)
{
	override fun createObjectMapper(): ObjectMapper
	{
		val objectMapper = super.createObjectMapper()
		objectMapper.registerModule(KotlinModule.Builder()
			.withReflectionCacheSize(512)
			.configure(KotlinFeature.NullToEmptyCollection, false)
			.configure(KotlinFeature.NullToEmptyMap, false)
			.configure(KotlinFeature.NullIsSameAsDefault, false)
			.configure(KotlinFeature.SingletonSupport, false)
			.configure(KotlinFeature.StrictNullChecks, false)
			.build())
		objectMapper.registerModule(JodaModule())

		LogConstants.types.forEach { type ->
			objectMapper.registerSubtypes(NamedType(type.type))
		}

		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		return objectMapper
	}
}
