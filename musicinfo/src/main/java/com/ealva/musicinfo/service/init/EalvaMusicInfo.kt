/*
 * Copyright (c) 2021  Eric A. Snell
 *
 * This file is part of eAlvaMusicInfo
 *
 * eAlvaMusicInfo is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version
 * 3 of  the License, or (at your option) any later version.
 *
 * eAlvaMusicInfo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * eAlvaMusicInfo. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ealva.musicinfo.service.init

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

private const val FIFTY_MEG = 50L * 1024L * 1024L

@SuppressLint("StaticFieldLeak")
public object EalvaMusicInfo {
  internal lateinit var appCtx: Context

  internal fun fetch(@StringRes stringRes: Int, vararg formatArgs: Any): String {
    return appCtx.getString(stringRes, *formatArgs)
  }

  public fun init(context: Context): EalvaMusicInfo = apply {
    appCtx = context.applicationContext
  }

  @Suppress("MemberVisibilityCanBePrivate")
  public val cacheDir: File
    get() = File(appCtx.cacheDir, "MusicInfoCache")

  public val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder().apply {
      cache(Cache(cacheDir, FIFTY_MEG))
    }.build()
  }
}

internal inline fun <reified T : Any> Context.requireSystemService(): T {
  return requireNotNull(getSystemService()) {
    "Failed to get system service ${T::class.java.simpleName}"
  }
}

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Any> requireSystemService(): T =
  EalvaMusicInfo.appCtx.requireSystemService()
