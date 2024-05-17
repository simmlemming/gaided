package com.gaided.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources

@SuppressLint("DiscouragedApi")
internal fun Context.getDrawableByName(name: String): Drawable {
    val id = resources.getIdentifier(name, "drawable", packageName)
    val drawable = AppCompatResources.getDrawable(this, id)

    return checkNotNull(drawable)
}
