
package com.arc.config

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer

interface Stringifiable<T> { fun stringify(value: T): String }

interface Codec<T> : JsonSerializer<T>, JsonDeserializer<T>