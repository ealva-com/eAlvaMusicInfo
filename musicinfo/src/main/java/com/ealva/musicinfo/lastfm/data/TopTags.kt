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

import com.ealva.musicinfo.lastfm.data.TopTags.Companion.NullTopTags
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class TopTags(
  @field:Json(name = "tag") public val tagList: List<Tag> = emptyList()
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullTopTags: TopTags = TopTags()
    public val fallbackMapping: Pair<String, Any> = TopTags::class.java.name to NullTopTags
  }
}

public inline val TopTags.isNullObject: Boolean
  get() = this === NullTopTags
