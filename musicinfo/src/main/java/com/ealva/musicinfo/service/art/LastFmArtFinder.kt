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

import android.content.Intent
import android.net.Uri
import android.util.Size
import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseGroupMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.RecordingTitle
import com.ealva.ealvalog.e
import com.ealva.ealvalog.invoke
import com.ealva.musicinfo.R
import com.ealva.musicinfo.lastfm.data.Album
import com.ealva.musicinfo.lastfm.data.Artist
import com.ealva.musicinfo.lastfm.data.Image
import com.ealva.musicinfo.lastfm.data.Track
import com.ealva.musicinfo.lastfm.data.theSize
import com.ealva.musicinfo.log.libLogger
import com.ealva.musicinfo.service.common.MusicInfoMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoErrorMessage
import com.ealva.musicinfo.service.lastfm.LastFmResult
import com.ealva.musicinfo.service.lastfm.LastFmService
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapOrElse
import com.github.michaelbull.result.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onEmpty

private val LOG by libLogger(LastFmArtFinder::class)
private val LASTFM_INTENT = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.last.fm/"))

public class LastFmArtFinder(private val lastfm: LastFmService) : ArtFinder {

  override suspend fun findAlbumArt(
    artist: ArtistName,
    albumTitle: AlbumTitle,
    groupMbid: ReleaseGroupMbid?,
    releaseMbid: ReleaseMbid?
  ): Flow<RemoteImage> = when {
    releaseMbid != null -> doFindAlbumArt(releaseMbid).onEmpty {
      if (artist.value.isNotBlank() && albumTitle.value.isNotBlank())
        emitAll(doFindAlbumArt(artist, albumTitle))
    }
    else -> doFindAlbumArt(artist, albumTitle)
  }

  private suspend fun doFindAlbumArt(
    artist: ArtistName,
    albumTitle: AlbumTitle
  ): Flow<RemoteImage> = getAlbum("$artist $albumTitle") {
    getAlbumInfo(artist, albumTitle)
  }

  private suspend fun doFindAlbumArt(
    releaseMbid: ReleaseMbid,
  ): Flow<RemoteImage> = getAlbum("$releaseMbid") {
    getAlbumInfo(releaseMbid)
  }

  private suspend fun getAlbum(
    data: String,
    getAlbum: suspend LastFmService.() -> LastFmResult<Album>
  ): Flow<RemoteImage> = lastfm.getAlbum()
    .andThen { if (it.imageList.isEmpty()) makeErr("No images for $data") else Ok(it.imageList) }
    .imageListToFlow()

  private fun Result<List<Image>, MusicInfoMessage>.imageListToFlow(
  ): Flow<RemoteImage> =
    onFailure { msg -> LOG.e { it(msg.toString()) } }
      .mapOrElse({ emptyList() }) { list -> list }
      .distinctBy { it.text }
      .map { image -> image.toRemoteImage() }
      .asFlow()

  override suspend fun findArtistArt(
    artist: ArtistName,
    artistMbid: ArtistMbid?
  ): Flow<RemoteImage> = when {
    artistMbid != null -> doFindArtistArt(artistMbid).onEmpty {
      if (artist.value.isNotBlank()) doFindArtistArt(artist)
    }
    else -> doFindArtistArt(artist)
  }

  private suspend fun doFindArtistArt(
    artist: ArtistName
  ): Flow<RemoteImage> = getArtist("$artist") {
    getArtistInfo(artist)
  }

  private suspend fun doFindArtistArt(
    artistMbid: ArtistMbid,
  ): Flow<RemoteImage> = getArtist("$artistMbid") {
    getArtistInfo(artistMbid)
  }

  private suspend fun getArtist(
    data: String,
    getArtist: suspend LastFmService.() -> LastFmResult<Artist>
  ): Flow<RemoteImage> = lastfm.getArtist()
    .andThen { if (it.imageList.isEmpty()) makeErr("No images for $data") else Ok(it.imageList) }
    .imageListToFlow()

  override suspend fun findTrackArt(
    artist: ArtistName,
    title: RecordingTitle,
    trackMbid: TrackMbid?
  ): Flow<RemoteImage> = when {
    trackMbid != null -> doFindTrackArt(trackMbid).onEmpty {
      if (artist.value.isNotBlank() && title.value.isNotBlank())
        doFindTrackArt(artist, title)
    }
    else -> doFindTrackArt(artist, title)
  }

  private suspend fun doFindTrackArt(
    trackMbid: TrackMbid,
  ): Flow<RemoteImage> = getTrack("$trackMbid") {
    getTrackInfo(trackMbid)
  }

  private suspend fun doFindTrackArt(
    artist: ArtistName,
    title: RecordingTitle,
  ): Flow<RemoteImage> = getTrack("$artist $title") {
    getTrackInfo(artist, title)
  }

  private suspend fun getTrack(
    data: String,
    getTrack: suspend LastFmService.() -> LastFmResult<Track>
  ): Flow<RemoteImage> = lastfm.getTrack()
    .andThen {
      if (it.album.imageList.isEmpty()) makeErr("No images for $data")
      else Ok(it.album.imageList)
    }
    .imageListToFlow()
}

private fun Image.Size.toSizeBucket(): SizeBucket {
  return when (this) {
    Image.Size.Mega -> SizeBucket.ExtraLarge
    Image.Size.Medium -> SizeBucket.Small
    Image.Size.Large -> SizeBucket.Medium
    Image.Size.ExtraLarge -> SizeBucket.Large
    Image.Size.Small -> SizeBucket.Small
    else -> SizeBucket.Unknown
  }
}

private fun makeErr(msg: String): Err<MusicInfoMessage> = Err(MusicInfoErrorMessage(msg))

private fun Image.toRemoteImage(): RemoteImage {
  val uri = text.toSecureUri()
  return RemoteImage(
    uri,
    theSize.toSizeBucket(),
    TYPE_FRONT,
    R.drawable.ic_lastfm_square_logo,
    LASTFM_INTENT,
    uri.getActualSize()
  )
}

private fun Uri.getActualSize(): Size? {
  val segments = pathSegments
  val sizeSegment = if (segments.size > 1) segments[segments.size - 2] else ""
  return sizeSegment
    .takeWhile { char -> char.isDigit() }
    .toIntOrNull()
    ?.let { size -> Size(size, size) }
}

private val TYPE_FRONT = setOf(RemoteImageType.FRONT)
