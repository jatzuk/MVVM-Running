package dev.jatzuk.mvvmrunning.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun Bitmap.toByteArray(): ByteArray = ByteArrayOutputStream().use {
        compress(Bitmap.CompressFormat.PNG, 100, it)
        it.toByteArray()
    }

    @TypeConverter
    fun ByteArray.toBitmap(): Bitmap = BitmapFactory.decodeByteArray(this, 0, size)
}
