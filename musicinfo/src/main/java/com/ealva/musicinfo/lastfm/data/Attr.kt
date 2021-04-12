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

package com.ealva.musicinfo.lastfm.data

import com.ealva.musicinfo.lastfm.data.Attr.Companion.NullAttr
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class Attr(
  public val rank: String = ""
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullAttr: Attr = Attr()
    public val fallbackMapping: Pair<String, Any> = Attr::class.java.name to NullAttr
  }
}

public inline val Attr.isNullObject: Boolean
  get() = this === NullAttr
