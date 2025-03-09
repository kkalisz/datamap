package com.kkalisz.datamap.sample

import com.kkalisz.datamap.MapBuilder
import com.kkalisz.datamap.BuilderProvider
import com.kkalisz.datamap.MapDataBuilder


@MapBuilder
data class User(
    val id: Long,
    val name: String,
    val email: String?,
    val age: Int,
    val isActive: Boolean = true
) : BuilderProvider<User> {
    override fun mapBuilder(): MapDataBuilder<User> = toMapBuilder()
}
