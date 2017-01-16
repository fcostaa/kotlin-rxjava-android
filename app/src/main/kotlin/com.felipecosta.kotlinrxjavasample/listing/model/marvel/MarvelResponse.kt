package com.felipecosta.kotlinrxjavasample.listing.model.marvel

import com.google.gson.annotations.SerializedName

import java.io.Serializable


class MarvelResponse : Serializable {

    @SerializedName("code")
    var code: Int? = null

    @SerializedName("status")
    var status: String? = null

    @SerializedName("copyright")
    var copyright: String? = null

    @SerializedName("attributionText")
    var attributionText: String? = null

    @SerializedName("attributionHTML")
    var attributionHTML: String? = null

    @SerializedName("data")
    var data: Data? = null

    @SerializedName("etag")
    var etag: String? = null

}