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

import com.ealva.musicinfo.lastfm.data.Wiki.Companion.NullWiki
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class Wiki(
  public val content: String = "",
  public val published: String = "",
  public val summary: String = ""
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullWiki: Wiki = Wiki()
    public val fallbackMapping: Pair<String, Any> = Wiki::class.java.name to NullWiki
  }
}

public inline val Wiki.isNullObject: Boolean
  get() = this === NullWiki
