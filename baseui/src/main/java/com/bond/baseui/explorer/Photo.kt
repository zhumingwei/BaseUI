package com.bond.baseui.explorer

import android.os.Parcel
import android.os.Parcelable

/**
 *
 *   @author zhumingwei
 *   @date 2018/7/5 下午6:17
 *   @email zdf312192599@163.com
 */
data class Photo(var id: Int = 0, var path: String = "", var isOriginal: Boolean = false) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        } else if (this === other) {
            return true
        } else {
            if (other !is Photo) {
                return false
            } else {
                return id == other.id
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(path)
        parcel.writeByte(if (isOriginal) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Photo> {
        override fun createFromParcel(parcel: Parcel): Photo {
            return Photo(parcel)
        }

        override fun newArray(size: Int): Array<Photo?> {
            return arrayOfNulls(size)
        }
    }
}