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

import com.ealva.musicinfo.lastfm.data.Track.Companion.NullTrack
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class Track(
  public val name: String = "",
  public val mbid: String = "",
  public val url: String = "",
  public val duration: String = "",
  @Json(name = "@attr") public val attr: Attr = Attr.NullAttr,
  public val streamable: Streamable = Streamable.NullStreamable,
  @field:Json(name = "listeners") public val listenerCount: String = "",
  @field:Json(name = "playcount") public val playCount: String = "",
  public val artist: TrackArtist = TrackArtist.NullTrackArtist,
  public val album: Album = Album.NullAlbum,
  @field:Json(name = "toptags") public val topTags: TopTags = TopTags.NullTopTags,
  public val wiki: Wiki = Wiki.NullWiki
) {
  override fun toString(): String = toJson()

  public companion object {
    public val NullTrack: Track = Track()
    public val fallbackMapping: Pair<String, Any> = Track::class.java.name to NullTrack
  }
}

public inline val Track.isNullObject: Boolean
  get() = this === NullTrack
