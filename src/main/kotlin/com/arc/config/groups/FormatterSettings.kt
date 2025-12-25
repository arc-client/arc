
package com.arc.config.groups

import com.arc.config.Configurable
import com.arc.config.SettingGroup
import com.arc.util.NamedEnum

class FormatterSettings(
	c: Configurable,
	baseGroup: NamedEnum,
) : FormatterConfig, SettingGroup(c) {
    val localeEnum by c.setting("Locale", FormatterConfig.Locales.US, "The regional formatting used for numbers").group(baseGroup).index()
    override val locale get() = localeEnum.locale

    val sep by c.setting("Separator", FormatterConfig.TupleSeparator.Comma, "Separator for string serialization of tuple data structures").group(baseGroup).index()
    val customSep by c.setting("Custom Separator", "") { sep == FormatterConfig.TupleSeparator.Custom }.group(baseGroup).index()
    override val separator get() = if (sep == FormatterConfig.TupleSeparator.Custom) customSep else sep.separator

    val group by c.setting("Tuple Prefix", FormatterConfig.TupleGrouping.Parentheses).group(baseGroup).index()
    override val prefix get() = group.prefix
    override val postfix get() = group.postfix

    val floatingPrecision by c.setting("Floating Precision", 3, 0..6, 1, "Precision for floating point numbers").group(baseGroup).index()
    override val precision get() = floatingPrecision

    val timeFormat by c.setting("Time Format", FormatterConfig.Time.IsoDateTime).group(baseGroup).index()
    override val format get() = timeFormat.format
}