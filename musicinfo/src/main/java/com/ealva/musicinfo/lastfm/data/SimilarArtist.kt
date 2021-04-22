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
import com.ealva.musicinfo.lastfm.data.SimilarArtist.Companion.NullSimilarArtist
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class SimilarArtist(
  /** Name of the artist, person or group */
  public val name: String = "",
  /**
   * The artist mbid. Available via artist.getSimilar but not available via artist.getInfo.
   * Ensure not empty before using
   */
  public val mbid: String = "",
  /**
   * The floating point value representing how similar the artist is to the "requested" artist, 1
   * being the same and 0 is no similarity. Available via artist.getSimilar but not available via
   * artist.getInfo.
   */
  public val match: String = "",
  /** Link to the LastFm artist page */
  public val url: String = "",
  /** List of images of the artist */
  @field:Json(name = "image") public val imageList: List<Image> = emptyList()
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullSimilarArtist: SimilarArtist = SimilarArtist()
    public val fallbackMapping: Pair<String, Any> =
      SimilarArtist::class.java.name to NullSimilarArtist
  }
}

public inline val SimilarArtist.isNullObject: Boolean
  get() = this === NullSimilarArtist

public val SimilarArtist.artistMbid: ArtistMbid
  get() = ArtistMbid(mbid)
