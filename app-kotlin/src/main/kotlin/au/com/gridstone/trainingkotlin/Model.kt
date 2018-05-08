package au.com.gridstone.trainingkotlin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Gallery(val status: Int,
                   val success: Boolean,
                   val data: List<Image>)

@Parcelize
data class Image(val id: String,
                 val link: String,
                 val title: String,
                 val width: Int,
                 val height: Int,
                 val datetime: Long,
                 val views: Int,
                 val is_album: Boolean) : Parcelable
