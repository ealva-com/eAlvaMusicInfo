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

import com.ealva.musicinfo.lastfm.data.AlbumReply.Companion.NullAlbumReply
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class AlbumReply(
  @field:Json(name = "album") public override val entity: Album = Album.NullAlbum,
  public override val error: Int = 0,
  public override val message: String = ""
) : LastFmReply<Album> {
  override fun toString(): String = toJson()

  public companion object {
    public val NullAlbumReply: AlbumReply = AlbumReply()
    public val fallbackMapping: Pair<String, Any> = AlbumReply::class.java.name to NullAlbumReply
  }
}

public inline val AlbumReply.isNullObject: Boolean
  get() = this === NullAlbumReply
