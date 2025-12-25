
package com.arc.network.api.v1.models

import com.google.gson.annotations.SerializedName
import com.arc.network.ArcAPI
import java.util.*

class Cape(
    @SerializedName("uuid")
    val uuid: UUID,

    @SerializedName("type")
    val id: String,
) {
    val url: String
        get() = "${ArcAPI.capes}/$id.png"

    override fun toString() = "Cape(uuid=$uuid, id=$id, url=$url)"
}
