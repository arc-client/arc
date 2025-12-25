
package com.arc.config

interface ISettingGroup {
	val settings: MutableList<Setting<*, *>>
}

abstract class SettingGroup(c: Configurable) : ISettingGroup {
    override val settings = mutableListOf<Setting<*, *>>()

	init {
		c.settingGroups.add(this)
	}

    fun <T : SettingCore<R>, R : Any> Setting<T, R>.index(): Setting<T, R> {
        settings.add(this)
        return this
    }
}