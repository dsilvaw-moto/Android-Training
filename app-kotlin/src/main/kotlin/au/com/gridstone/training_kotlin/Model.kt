package au.com.gridstone.training_kotlin

import android.os.Parcel
import android.os.Parcelable

data class Gallery(val status: Int,
                   val success: Boolean,
                   val data: List<Image>)

data class Image(val id: String,
                 val link: String,
                 val title: String,
                 val width: Int,
                 val height: Int,
                 val datetime: Long,
                 val views: Int,
                 val is_album: Boolean) : Parcelable {
  constructor(source: Parcel) : this(
      source.readString(),
      source.readString(),
      source.readString(),
      source.readInt(),
      source.readInt(),
      source.readLong(),
      source.readInt(),
      source.readInt() != 0)

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.apply {
      writeString(id)
      writeString(link)
      writeString(title)
      writeInt(width)
      writeInt(height)
      writeLong(datetime)
      writeInt(views)
      writeInt(if (is_album) 1 else 0)
    }
  }

  override fun describeContents() = 0

  companion object {
    @Suppress("unused") @JvmField val CREATOR = object : Parcelable.Creator<Image> {
      override fun createFromParcel(source: Parcel) = Image(source)
      override fun newArray(size: Int) = arrayOfNulls<Image?>(size)
    }
  }
}
