
package com.arc.util.extension

import com.arc.util.NamedEnum
import com.arc.util.StringUtils.capitalize

val Enum<*>.displayValue
    get() =
        (this as? NamedEnum)?.displayName ?: name.split('_').joinToString(" ") { low ->
            low.capitalize()
        }
