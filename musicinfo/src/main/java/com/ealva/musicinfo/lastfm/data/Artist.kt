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

import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.musicinfo.lastfm.data.Artist.Companion.NullArtist
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class Artist(
  public val name: String = "",
  public val mbid: String = "",
  public val url: String = "",
  @field:Json(name = "image") public val imageList: List<Image> = emptyList(),
  public val streamable: String = "",
  @field:Json(name = "ontour") public val onTour: String = "",
  @field:Json(name = "stats") public val stats: Stats = Stats.NullStats,
  @field:Json(name = "similar") public val similar: Similar = Similar.NullSimilar,
  @field:Json(name = "tags") public val tags: Tags = Tags.NullTags,
  @field:Json(name = "bio") public val bio: Bio = Bio.NullBio
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullArtist: Artist = Artist()
    public val fallbackMapping: Pair<String, Any> = Artist::class.java.name to NullArtist
  }
}

public inline val Artist.isNullObject: Boolean
  get() = this === NullArtist

public val Artist.artistMbid: ArtistMbid
  get() = ArtistMbid(mbid)
