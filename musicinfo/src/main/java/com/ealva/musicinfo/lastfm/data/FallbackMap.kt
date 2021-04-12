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

internal object FallbackMap {
  private val map = mapOf(
    Album.fallbackMapping,
    AlbumStatus.fallbackMapping,
    Artist.fallbackMapping,
    ArtistStatus.fallbackMapping,
    Attr.fallbackMapping,
    Bio.fallbackMapping,
    Image.fallbackMapping,
    Link.fallbackMapping,
    Links.fallbackMapping,
    Similar.fallbackMapping,
    SimilarArtist.fallbackMapping,
    Stats.fallbackMapping,
    Streamable.fallbackMapping,
    Tag.fallbackMapping,
    Tags.fallbackMapping,
    TopTags.fallbackMapping,
    Track.fallbackMapping,
    TrackArtist.fallbackMapping,
    Tracks.fallbackMapping,
    TrackStatus.fallbackMapping,
    Wiki.fallbackMapping
  )

  fun get(key: String): Any {
    return map[key] ?: throw IllegalStateException("Fallback map not configured for $key")
  }
}
