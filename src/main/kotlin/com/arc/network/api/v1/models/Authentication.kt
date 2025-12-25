
package com.arc.network.api.v1.models

import com.google.gson.annotations.SerializedName

data class Authentication(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("token_type")
    val tokenType: String,
) {
    data class Data(
        @SerializedName("nbf")
        val notBefore: Long,

        @SerializedName("iat")
        val issuedAt: Long,

        @SerializedName("exp")
        val expirationDate: Long,

        @SerializedName("data")
        val data: Player,
    )
}
