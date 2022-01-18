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

import com.ealva.musicinfo.lastfm.data.AlbumTrack.Companion.NullAlbumTrack
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Track as found in Album from album.getInfo
 */
@JsonClass(generateAdapter = true)
public class AlbumTrack(
  public val name: String = "",
  public val url: String = "",
  public val duration: Int = 0,
  @field:FallbackOnNull @Json(name = "@attr") public val rankAttr: RankAttr = RankAttr.NullRankAttr,
  @field:FallbackOnNull public val streamable: Streamable = Streamable.NullStreamable,
  @field:FallbackOnNull public val artist: TrackArtist = TrackArtist.NullTrackArtist,
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullAlbumTrack: AlbumTrack = AlbumTrack()
    public val fallbackMapping: Pair<String, Any> = Track::class.java.name to NullAlbumTrack
  }
}

public inline val AlbumTrack.isNullObject: Boolean
  get() = this === NullAlbumTrack
