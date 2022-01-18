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

package com.ealva.musicinfo.wiki.data

import com.ealva.musicinfo.lastfm.data.NullPrimitiveAdapter
import com.ealva.musicinfo.moshi.StringJsonAdapter
import com.squareup.moshi.Moshi

internal fun Moshi.Builder.addRequired(): Moshi.Builder {
  add(FallbackOnNull.ADAPTER_FACTORY)
  add(NullPrimitiveAdapter())
  add(StringJsonAdapter())
  return this
}

internal val theWikiMoshi: Moshi = Moshi.Builder().addRequired().build()

internal fun <T : Any> T.toJson(): String {
  return theWikiMoshi
    .adapter<T>(this::class.java)
    .indent("  ")
    .toJson(this)
}
