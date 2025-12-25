
package com.arc.config.settings.collections

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.arc.Arc.gson
import com.arc.config.Setting
import com.arc.config.SettingCore
import com.arc.config.SettingEditorDsl
import com.arc.config.SettingGroupEditor
import com.arc.context.SafeContext
import com.arc.gui.dsl.ImGuiBuilder
import com.arc.threading.runSafe
import imgui.ImGuiListClipper
import imgui.callback.ImListClipperCallback
import imgui.flag.ImGuiChildFlags
import imgui.flag.ImGuiSelectableFlags.DontClosePopups
import java.lang.reflect.Type

/**
 * This generic collection settings handles all [Comparable] values (i.e., not classes) and serialize
 * their values by calling [Any.toString] and loads them by comparing what's in the [immutableCollection].
 * This behavior is by design. If you wish to store collections of non-comparable values you must use [ClassCollectionSetting].
 *
 * If you wish to use a different codec or simply display values differently, you must create your own
 * collection setting.
 *
 * @see [com.arc.config.Configurable]
 */
open class CollectionSetting<R : Any>(
	defaultValue: MutableCollection<R>,
	private var immutableCollection: Collection<R>,
	type: Type
) : SettingCore<MutableCollection<R>>(
	defaultValue,
	type
) {
    private var searchFilter = ""
    private val strListType =
        TypeToken.getParameterized(Collection::class.java, String::class.java).type

    val selectListeners = mutableListOf<SafeContext.(R) -> Unit>()
    val deselectListeners = mutableListOf<SafeContext.(R) -> Unit>()

	context(setting: Setting<*, MutableCollection<R>>)
    override fun ImGuiBuilder.buildLayout() = buildComboBox("item") { it.toString() }

	context(setting: Setting<*, MutableCollection<R>>)
	fun ImGuiBuilder.buildComboBox(itemName: String, toString: (R) -> String) {
		val text = if (value.size == 1) itemName else "${itemName}s"

		combo("##${setting.name}", "${setting.name}: ${value.size} $text") {
			inputText("##${setting.name}-SearchBox", ::searchFilter)

			child(
				strId = "##${setting.name}-ComboOptionsChild",
				childFlags = ImGuiChildFlags.AutoResizeY or ImGuiChildFlags.AlwaysAutoResize,
			) {
				val list = immutableCollection
					.filter { item ->
						val q = searchFilter.trim()
						if (q.isEmpty()) true
						else toString(item).contains(q, ignoreCase = true)
					}

				val listClipperCallback = object : ImListClipperCallback() {
					override fun accept(index: Int) {
						val v = list.getOrNull(index) ?: return
						val selected = value.contains(v)

						selectable(
							label = toString(v),
							selected = selected,
							flags = DontClosePopups
						) {
							if (selected) {
								value.remove(v)
								runSafe { deselectListeners.forEach { listener -> listener(v) } }
							} else {
								value.add(v)
								runSafe { selectListeners.forEach { listener -> listener(v) } }
							}
						}
					}
				}
				ImGuiListClipper.forEach(list.size, listClipperCallback)
			}
		}
	}

	context(setting: Setting<*, MutableCollection<R>>)
    override fun toJson(): JsonElement =
        gson.toJsonTree(value.map { it.toString() })

	context(setting: Setting<*, MutableCollection<R>>)
    override fun loadFromJson(serialized: JsonElement) {
        val strList = gson.fromJson<Collection<String>>(serialized, strListType)
            .mapNotNull { str -> immutableCollection.find { it.toString() == str } }
            .toMutableList()

        value = strList
    }

	companion object {
		fun <T : CollectionSetting<R>, R : Any> Setting<T, MutableCollection<R>>.onSelect(block: SafeContext.(R) -> Unit) = apply {
			core.selectListeners.add(block)
		}

		fun <T : CollectionSetting<R>, R : Any> Setting<T, MutableCollection<R>>.onDeselect(block: SafeContext.(R) -> Unit) = apply {
			core.deselectListeners.add(block)
		}

        @SettingEditorDsl
        @Suppress("unchecked_cast")
        fun <T : Any> SettingGroupEditor.TypedEditBuilder<Collection<T>>.immutableCollection(collection: Collection<T>) {
            (settings as Collection<CollectionSetting<T>>).forEach { it.immutableCollection = collection }
        }
    }
}
