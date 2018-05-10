package au.com.gridstone.trainingkotlin.imgur

import android.os.Parcel
import android.os.Parcelable

data class ImgurRequest(  val data: List<ImgurPost>?, val success: Boolean,
  val status: Int
) : Parcelable {
  constructor(parcel: Parcel) : this(
      parcel.createTypedArrayList(ImgurPost),
      parcel.readByte() != 0.toByte(),
      parcel.readInt()
  ) {
  }

  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    parcel.writeTypedList(data)
    parcel.writeByte(if (success) 1 else 0)
    parcel.writeInt(status)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<ImgurRequest> {
    override fun createFromParcel(parcel: Parcel): ImgurRequest {
      return ImgurRequest(parcel)
    }

    override fun newArray(size: Int): Array<ImgurRequest?> {
      return arrayOfNulls(size)
    }
  }

}
