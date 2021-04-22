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

package com.ealva.musicinfo.log

import com.ealva.ealvalog.LogEntry
import com.ealva.ealvalog.Logger
import com.ealva.ealvalog.Marker
import com.ealva.ealvalog.Markers
import com.ealva.ealvalog.e
import com.ealva.ealvalog.filter.MarkerFilter
import com.ealva.ealvalog.i
import com.ealva.ealvalog.lazyLogger
import com.ealva.ealvalog.w
import com.ealva.musicinfo.BuildConfig
import kotlin.reflect.KClass

public object MusicInfoLog {
  public var logErrors: Boolean = true

  public const val ERROR_TAG: String = "MusicInfo_Err"

  @Suppress("MemberVisibilityCanBePrivate")
  public const val markerName: String = "eAlvaMusicInfo"

  /**
   * Loggers in eAlvaMusicInfo use this [Marker] so all logging can be filtered.
   */
  public val marker: Marker by lazy { Markers[markerName] }

  /**
   * Clients can use this [MarkerFilter] in a logger handler to direct associated logging where
   * desired (file, Android log, ...). See [eAlvaLog](https://github.com/ealva-com/ealvalog) for
   * information on configuring logging.
   */
  @Suppress("unused")
  public val markerFilter: MarkerFilter by lazy { MarkerFilter(marker) }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T : Any> libLogger(forClass: KClass<T>): Lazy<Logger> =
  lazyLogger(forClass, MusicInfoLog.marker)

@Suppress("NOTHING_TO_INLINE")
internal inline fun libLogger(name: String): Lazy<Logger> =
  lazyLogger(name, MusicInfoLog.marker)

@Suppress("FunctionName", "unused")
internal inline fun Logger._i(
  throwable: Throwable? = null,
  marker: Marker? = null,
  block: (LogEntry) -> Unit
) {
  if (BuildConfig.DEBUG) i(throwable, marker, null, block)
}

@Suppress("FunctionName", "unused")
internal inline fun Logger._w(
  throwable: Throwable? = null,
  marker: Marker? = null,
  block: (LogEntry) -> Unit
) {
  if (BuildConfig.DEBUG) w(throwable, marker, null, block)
}

@Suppress("FunctionName", "unused")
internal inline fun Logger._e(
  throwable: Throwable? = null,
  marker: Marker? = null,
  block: (LogEntry) -> Unit
) {
  if (BuildConfig.DEBUG) e(throwable, marker, null, block)
}
