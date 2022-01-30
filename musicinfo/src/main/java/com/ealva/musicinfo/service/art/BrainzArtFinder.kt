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
import com.ealva.brainzsvc.service.BrainzResult
import com.ealva.brainzsvc.service.CoverArtImageInfo
import com.ealva.brainzsvc.service.MusicBrainzService
import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.ealvabrainz.brainz.data.BrowseReleaseList
import com.ealva.ealvabrainz.brainz.data.CoverArtImageSize
import com.ealva.ealvabrainz.brainz.data.Release
import com.ealva.ealvabrainz.brainz.data.ReleaseGroupMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.ealva.ealvabrainz.brainz.data.mbid
import com.ealva.ealvabrainz.browse.ReleaseBrowse.BrowseOn
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.RecordingTitle
import com.ealva.ealvalog.e
import com.ealva.ealvalog.invoke
import com.ealva.musicinfo.R
import com.ealva.musicinfo.log.libLogger
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoErrorMessage
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapAll
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.toErrorIf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEmpty

private val LOG by libLogger(BrainzArtFinder::class)
private val BRAINZ_INTENT = Intent(Intent.ACTION_VIEW, Uri.parse("https://musicbrainz.org/"))

public class BrainzArtFinder(private val brainz: MusicBrainzService) : ArtFinder {

  /**
   * Tries to find album art using [groupMbid] if available, else [releaseMbid] if available,
   * else [artist] and [albumTitle]. If [groupMbid] or [releaseMbid] produce empty flows, uses
   * [artist]/[albumTitle] lookup as fallback.
   */
  override suspend fun findAlbumArt(
    artist: ArtistName,
    albumTitle: AlbumTitle,
    groupMbid: ReleaseGroupMbid?,
    releaseMbid: ReleaseMbid?
  ): Flow<RemoteImage> = when {
    groupMbid != null -> albumArt(groupMbid).onEmpty {
      if (artist.value.isNotBlank() && albumTitle.value.isNotBlank())
        emitAll(albumArt(artist, albumTitle))
    }
    releaseMbid != null -> albumArt(releaseMbid).onEmpty {
      if (artist.value.isNotBlank() && albumTitle.value.isNotBlank())
        emitAll(albumArt(artist, albumTitle))
    }
    else -> albumArt(artist, albumTitle)
  }

  private suspend fun albumArt(
    artist: ArtistName,
    albumTitle: AlbumTitle
  ): Flow<RemoteImage> = brainz
    .findReleaseGroup {
      artist(artist) and releaseGroup(albumTitle) and status(Release.Status.Official)
    }
    .mapError { brainzMessage -> MusicInfoErrorMessage(brainzMessage.toString()) }
    .toErrorIf({ groupList -> groupList.releaseGroups.isEmpty() }) {
      MusicInfoErrorMessage("No release groups for $artist $albumTitle")
    }
    .map { releaseGroupList -> releaseGroupList.releaseGroups }
    .mapAll { releaseGroup -> Ok(albumArt(releaseGroup.mbid)) }
    .onFailure { msg -> LOG.e { it(msg.toString()) } }
    .getOrElse { emptyList() }
    .merge()
    .distinctBy { remoteImage -> remoteImage.location }

  /**
   * [MusicBrainzService.releaseGroupArtFlow] does not return duplicates, so no distinctBy is
   * required
   */
  private suspend fun albumArt(groupMbid: ReleaseGroupMbid): Flow<RemoteImage> = brainz
    .releaseGroupArtFlow(groupMbid)
    .transform()

  /**
   * [MusicBrainzService.releaseArtFlow] does not return duplicates, so no distinctBy is
   * required
   */
  private suspend fun albumArt(releaseMbid: ReleaseMbid): Flow<RemoteImage> = brainz
    .releaseArtFlow(releaseMbid)
    .transform()

  /**
   * Produces an empty flow as MusicBrainz/CoverArtArchive don't have artist artwork
   */
  override suspend fun findArtistArt(
    artist: ArtistName,
    artistMbid: ArtistMbid?
  ): Flow<RemoteImage> = emptyFlow()

  /**
   * Tries to find art browsing Releases for [trackMbid] if not null, else
   */
  override suspend fun findTrackArt(
    artist: ArtistName,
    title: RecordingTitle,
    trackMbid: TrackMbid?
  ): Flow<RemoteImage> = when {
    trackMbid != null -> trackArt(trackMbid).onEmpty {
      emitAll(trackArt(artist, title))
    }
    else -> trackArt(artist, title)
  }

  /**
   * In processing the stream of information, convert to a flow before creating other flows so
   * that cancellation works correctly. The list of releases is converted to a flow and then
   * flatMapMerge gets a flow for every release and merges them into a single flow.
   */
  @OptIn(FlowPreview::class)
  private suspend fun trackArt(
    trackMbid: TrackMbid
  ): Flow<RemoteImage> = browseTrackReleases(trackMbid)
    .mapError { brainzMessage -> MusicInfoErrorMessage(brainzMessage.toString()) }
    .toErrorIf({ list -> list.releases.isEmpty() }) {
      MusicInfoErrorMessage("No releases for $trackMbid")
    }
    .map { browseReleaseList -> browseReleaseList.releases }
    .onFailure { msg -> LOG.e { it(msg.toString()) } }
    .getOrElse { emptyList() }
    .asFlow()
    .flatMapMerge { release -> albumArt(release.mbid) }
    .distinctBy { remoteImage -> remoteImage.location }

  private suspend fun browseTrackReleases(
    trackMbid: TrackMbid
  ): BrainzResult<BrowseReleaseList> = brainz.browseReleases(BrowseOn.Track(trackMbid)) {
    status(Release.Status.Official)
    include(Release.Browse.ReleaseGroups)
  }

  /**
   * In processing the stream of information, convert to a flow before creating other flows so
   * that cancellation works correctly. The list of releases is converted to a flow and then
   * map gets a flow for every release. Then flattenConcat merges them into a single flow.
   */
  @OptIn(FlowPreview::class)
  private suspend fun trackArt(
    artist: ArtistName,
    track: RecordingTitle
  ): Flow<RemoteImage> = brainz
    .findRecording { artist(artist) and recording(track) and status(Release.Status.Official) }
    .mapError { brainzMessage -> MusicInfoErrorMessage(brainzMessage.toString()) }
    .toErrorIf({ list -> list.recordings.isEmpty() }) {
      MusicInfoErrorMessage("No recordings for $artist $track")
    }
    .map { recordingList -> recordingList.recordings }
    .getOrElse { emptyList() }
    .flatMap { recording -> recording.releases }
    .asFlow()
    .map { release -> albumArt(release.mbid) }
    .flattenConcat()
    .distinctBy { remoteImage -> remoteImage.location }
}

private fun CoverArtImageSize.toSizeBucket(): SizeBucket {
  return when (this) {
    CoverArtImageSize.Original -> SizeBucket.Original
    CoverArtImageSize.Size250 -> SizeBucket.Medium
    CoverArtImageSize.Size500 -> SizeBucket.Large
    CoverArtImageSize.Size1200 -> SizeBucket.ExtraLarge
    CoverArtImageSize.Unknown -> SizeBucket.Unknown
    else -> SizeBucket.Unknown
  }
}

private fun Flow<CoverArtImageInfo>.transform(): Flow<RemoteImage> = flow {
  collect { info -> emit(info.toRemoteImage()) }
}

private fun CoverArtImageInfo.toRemoteImage() =
  RemoteImage(
    location,
    size.toSizeBucket(),
    types.mapTo(mutableSetOf()) { coverArtImageType -> coverArtImageType.asRemoteImageType },
    R.drawable.ic_musicbrainz_logo,
    BRAINZ_INTENT,
    size.toActual()
  )

private fun CoverArtImageSize.toActual(): Size? {
  return when (this) {
    CoverArtImageSize.Original -> null
    CoverArtImageSize.Size250 -> Size(250, 250)
    CoverArtImageSize.Size500 -> Size(500, 500)
    CoverArtImageSize.Size1200 -> Size(1200, 1200)
    CoverArtImageSize.Unknown -> null
  }
}

private fun <T, K> Flow<T>.distinctBy(selector: (T) -> K): Flow<T> = flow {
  val past = HashSet<K>()
  collect {
    if (past.add(selector(it))) emit(it)
  }
}

