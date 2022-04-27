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
import com.ealva.brainzsvc.service.MusicBrainzService
import com.ealva.ealvabrainz.brainz.data.Artist
import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.ealvabrainz.brainz.data.Relationships
import com.ealva.ealvabrainz.brainz.data.ReleaseGroupMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.ealva.ealvabrainz.brainz.data.UrlRelation
import com.ealva.ealvabrainz.brainz.data.mbid
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.RecordingTitle
import com.ealva.ealvalog.e
import com.ealva.ealvalog.invoke
import com.ealva.musicinfo.R
import com.ealva.musicinfo.log.libLogger
import com.ealva.musicinfo.service.MusicInfoResult
import com.ealva.musicinfo.service.common.MusicInfoMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoErrorMessage
import com.ealva.musicinfo.service.wiki.WikipediaService
import com.ealva.musicinfo.wiki.data.WikiImage
import com.ealva.musicinfo.wiki.data.WikiSummary
import com.ealva.musicinfo.wiki.data.isNullObject
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.toErrorIf
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

private val LOG by libLogger(WikipediaArtFinder::class)
private val BRAINZ_INTENT = Intent(Intent.ACTION_VIEW, "https://musicbrainz.org/".toUri())

public interface WikipediaArtFinder : ArtFinder {
  public suspend fun getArticleSummary(
    artistName: ArtistName,
    artistMbid: ArtistMbid? = null
  ): MusicInfoResult<WikiSummary>

  public companion object {
    public operator fun invoke(
      wiki: WikipediaService,
      brainz: MusicBrainzService
    ): WikipediaArtFinder = WikipediaArtFinderImpl(wiki, brainz)
  }
}

private class WikipediaArtFinderImpl(
  private val wiki: WikipediaService,
  private val brainz: MusicBrainzService
) : WikipediaArtFinder {

  override suspend fun getArticleSummary(
    artistName: ArtistName,
    artistMbid: ArtistMbid?
  ): MusicInfoResult<WikiSummary> =
    artistMbid?.getArtistArticleSummary() ?: artistName.getArtistArticleSummary()

  private suspend fun ArtistMbid.getArtistArticleSummary(): MusicInfoResult<WikiSummary> = brainz
    .lookupArtist(this) { relationships(Relationships.Url) }
    .onFailure { msg -> LOG.e { it("Lookup artist error. %s", msg) } }
    .getOrElse { Artist.NullArtist }
    .relations
    .filterIsInstance<UrlRelation>()
    .filter { urlRelation -> urlRelation.type == "wikidata" }
    .map { urlRelation -> urlRelation.url }
    .firstOrNull()
    ?.let { url -> wiki.getArticleSummary(url) }
    ?: Err(MusicInfoMessage.MusicInfoNotFoundMessage("Url for $this not found"))

  private suspend fun ArtistName.getArtistArticleSummary(): MusicInfoResult<WikiSummary> = brainz
    .findArtist { artist(this@getArtistArticleSummary) }
    .mapError { brainzMessage -> MusicInfoErrorMessage(brainzMessage.toString()) }
    .map { artistList -> artistList.artists }
    .toErrorIf({ list -> list.isEmpty() }) { MusicInfoErrorMessage("No artists: $this") }
    .map { list -> list[0] }
    .map { artist -> artist.mbid }
    .map { artistMbid -> artistMbid.getArtistArticleSummary() }
    .toErrorIf({ it is Err }) { it.unwrapError() }
    .map { it.unwrap() }


  override suspend fun findAlbumArt(
    artist: ArtistName,
    albumTitle: AlbumTitle,
    groupMbid: ReleaseGroupMbid?,
    releaseMbid: ReleaseMbid?
  ): Flow<RemoteImage> = emptyFlow()

  override suspend fun findArtistArt(
    artist: ArtistName,
    artistMbid: ArtistMbid?
  ): Flow<RemoteImage> = getArticleSummary(artist, artistMbid)
    .toErrorIf({ wikiSummary -> wikiSummary.originalImage.isNullObject }) {
      MusicInfoMessage.MusicInfoNotFoundMessage("No image in WikiSummary")
    }
    .onFailure { msg -> LOG.e { it("%s", msg) } }
    .map { wikiSummary -> wikiSummary.originalImage }
    .map { wikiImage -> wikiImage.toRemoteImage() }
    .map { remoteImage -> flow { emit(remoteImage) } }
    .getOrElse { emptyFlow() }

  private fun WikiImage.toRemoteImage() = RemoteImage(
    url = source,
    bucket = SizeBucket.Original,
    types = setOf(RemoteImageType.FRONT),
    sourceLogo = R.drawable.ic_wikipedia_logo,
    source = BRAINZ_INTENT,
    actualSize = Size(width, height)
  )

  /**
   * Tries to find art browsing Releases for [trackMbid] if not null, else
   */
  override suspend fun findTrackArt(
    artist: ArtistName,
    title: RecordingTitle,
    trackMbid: TrackMbid?
  ): Flow<RemoteImage> = emptyFlow()
}
