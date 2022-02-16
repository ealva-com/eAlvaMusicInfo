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
import android.util.Size
import androidx.core.net.toUri
import com.adamratzman.spotify.models.SpotifyImage
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
import com.ealva.musicinfo.log.libLogger
import com.ealva.musicinfo.service.common.MusicInfoMessage
import com.ealva.musicinfo.service.spotify.SpotifyService
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapOrElse
import com.github.michaelbull.result.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

private val LOG by libLogger(SpotifyArtFinder::class)
private val SPOTIFY_INTENT = Intent(Intent.ACTION_VIEW, "https://spotify.com/".toUri())

private const val MAX_SMALL_IMAGE_WIDTH = 250
private const val MAX_MEDIUM_IMAGE_WIDTH = 500
private const val MAX_LARGE_IMAGE_WIDTH = 1000

public class SpotifyArtFinder(private val spotify: SpotifyService) : ArtFinder {
  override suspend fun findAlbumArt(
    artist: ArtistName,
    albumTitle: AlbumTitle,
    groupMbid: ReleaseGroupMbid?,
    releaseMbid: ReleaseMbid?
  ): Flow<RemoteImage> = doFindAlbumArt(artist, albumTitle)

  private suspend fun doFindAlbumArt(
    artist: ArtistName,
    albumTitle: AlbumTitle,
  ): Flow<RemoteImage> = spotify.searchAlbum {
    artist(artist)
    album(albumTitle)
  }.andThen { pagingObject ->
    if (pagingObject.items.isEmpty() || pagingObject.items[0].images.isEmpty())
      makeErr { "$artist $albumTitle" }
    else Ok(pagingObject.items[0].images)
  }.imageListToFlow()

  private fun Result<List<SpotifyImage>, MusicInfoMessage>.imageListToFlow(): Flow<RemoteImage> =
    onFailure { msg -> LOG.e { it(msg.toString()) } }
      .mapOrElse({ emptyList() }) { list -> list }
      .distinctBy { it.url }
      .map { spotifyImage -> spotifyImage.toRemoteImage() }
      .asFlow()

  override suspend fun findArtistArt(
    artist: ArtistName,
    artistMbid: ArtistMbid?
  ): Flow<RemoteImage> = spotify.searchArtist {
    default(artist.value)
  }.andThen { pagingObject ->
    if (pagingObject.items.isEmpty() || pagingObject.items[0].images.isEmpty())
      makeErr { "$artist" }
    else Ok(pagingObject.items[0].images)
  }.imageListToFlow()

  override suspend fun findTrackArt(
    artist: ArtistName,
    title: RecordingTitle,
    trackMbid: TrackMbid?
  ): Flow<RemoteImage> = spotify.searchTrack {
    artist(artist)
    track(title)
  }.andThen { pagingObject ->
    if (pagingObject.items.isEmpty() || pagingObject.items[0].album.images.isEmpty())
      makeErr { "$artist $title" }
    else Ok(pagingObject.items[0].album.images)
  }.imageListToFlow()

  private fun Size.toSizeBucket(): SizeBucket {
    if (width <= 0 || height <= 0) return SizeBucket.Unknown
    return when {
      width <= 0 || height <= 0 -> SizeBucket.Unknown
      width < MAX_SMALL_IMAGE_WIDTH -> SizeBucket.Small
      width < MAX_MEDIUM_IMAGE_WIDTH -> SizeBucket.Medium
      width < MAX_LARGE_IMAGE_WIDTH -> SizeBucket.Large
      else -> SizeBucket.ExtraLarge
    }
  }

  private fun makeErr(msg: () -> String): Err<MusicInfoMessage> =
    Err(MusicInfoMessage.MusicInfoErrorMessage(msg()))

  private fun SpotifyImage.toRemoteImage(): RemoteImage {
    val actualSize = Size(width ?: 0, height ?: 0)
    return RemoteImage(
      url,
      actualSize.toSizeBucket(),
      TYPE_FRONT,
      R.drawable.ic_spotify_green_logo,
      SPOTIFY_INTENT,
      actualSize
    )
  }
}

private val TYPE_FRONT = setOf(RemoteImageType.FRONT)
