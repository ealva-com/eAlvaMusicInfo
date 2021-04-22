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

import com.ealva.musicinfo.lastfm.data.ArtistReply.Companion.NullArtistReply
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class ArtistReply(
  @field:Json(name = "artist") public override val entity: Artist = Artist.NullArtist,
  public override val error: Int = 0,
  public override val message: String = ""
) : LastFmReply<Artist> {
  override fun toString(): String = toJson()

  public companion object {
    public val NullArtistReply: ArtistReply = ArtistReply()
    public val fallbackMapping: Pair<String, Any> = ArtistReply::class.java.name to NullArtistReply
  }
}

public inline val ArtistReply.isNullObject: Boolean
  get() = this === NullArtistReply
