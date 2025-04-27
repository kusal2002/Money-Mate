package com.example.moneymate

import android.os.Parcel
import android.os.Parcelable

data class Transaction(
    val amount: Double,
    val category: String,
    val date: String,
    val isExpense: Boolean,
    val name: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        amount = parcel.readDouble(),
        category = parcel.readString() ?: "",
        date = parcel.readString() ?: "",
        isExpense = parcel.readByte() != 0.toByte(),
        name = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(amount)
        parcel.writeString(category)
        parcel.writeString(date)
        parcel.writeByte(if (isExpense) 1 else 0)
        parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}