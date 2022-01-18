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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class WikiSummary(
  @field:Json(name = "displaytitle") public val displayTitle: String = "",
  public val title: String = "",
  public val pageId: Long = 0,
  @field:FallbackOnNull @field:Json(name = "originalimage")
  public val originalImage: WikiImage = WikiImage.NullWikiImage,
  public val timestamp: String = "",
  public val description: String = "",
  public val extract: String = "",
  @field:Json(name = "extract_html") public val extractHtml: String = ""
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullWikiSummary: WikiSummary = WikiSummary()
    public val fallbackMapping: Pair<String, Any> = WikiSummary::class.java.name to NullWikiSummary
  }
}

public inline val WikiSummary.isNullObject: Boolean
  get() = this === WikiSummary.NullWikiSummary
