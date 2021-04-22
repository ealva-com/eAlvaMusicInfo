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

import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class SimilarTrack(
  public val name: String = "",
  @Json(name = "playcount") public val playCount: Int = 0,
  public val mbid: String = "",
  /** Number from 0 to 1 indicating how similar - 1 being exact match and 0 no match */
  public val match: Double = 0.0,
  @Json(name = "url") public val url: String = "",
  @Json(name = "streamable") public val streamable: Streamable = Streamable.NullStreamable,
  public val duration: Int = 0,
  public val artist: TrackArtist = TrackArtist.NullTrackArtist,
  @Json(name = "image") public val images: List<Image> = listOf(),
)

public val SimilarTrack.trackMbid: TrackMbid
  get() = TrackMbid(mbid)
