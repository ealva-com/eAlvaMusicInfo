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

package com.ealva.musicinfo.service.art

import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseGroupMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.RecordingTitle
import kotlinx.coroutines.flow.Flow

public interface ArtFinder {
  public suspend fun findAlbumArt(
    artist: ArtistName,
    albumTitle: AlbumTitle,
    groupMbid: ReleaseGroupMbid? = null,
    releaseMbid: ReleaseMbid? = null
  ): Flow<RemoteImage>

  public suspend fun findArtistArt(
    artist: ArtistName,
    artistMbid: ArtistMbid? = null
  ): Flow<RemoteImage>

  public suspend fun findTrackArt(
    artist: ArtistName,
    title: RecordingTitle,
    trackMbid: TrackMbid? = null
  ): Flow<RemoteImage>
}
