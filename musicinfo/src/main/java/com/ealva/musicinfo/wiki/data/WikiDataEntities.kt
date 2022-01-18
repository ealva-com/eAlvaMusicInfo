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

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class WikiDataEntities(
  public val entities: Map<String, WikiDataEntity> = emptyMap(),
  public val success: Int = 0
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullWikiDataEntities: WikiDataEntities = WikiDataEntities()
    public val fallbackMapping: Pair<String, Any> =
      WikiDataEntities::class.java.name to NullWikiDataEntities
  }
}

public inline val WikiDataEntities.isNullObject: Boolean
  get() = this === WikiDataEntities.NullWikiDataEntities
