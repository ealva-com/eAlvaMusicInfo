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

import com.ealva.musicinfo.lastfm.data.Bio.Companion.NullBio
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class Bio(
  public val links: Links = Links.NullLinks,
  public val published: String = "",
  public val summary: String = "",
  public val content: String = ""
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullBio: Bio = Bio()
    public val fallbackMapping: Pair<String, Any> = Bio::class.java.name to NullBio
  }
}

public inline val Bio.isNullObject: Boolean
  get() = this === NullBio
