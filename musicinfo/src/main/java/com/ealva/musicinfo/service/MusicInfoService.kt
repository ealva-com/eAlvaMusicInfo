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

package com.ealva.musicinfo.service

import com.ealva.brainzsvc.service.CredentialsProvider
import com.ealva.brainzsvc.service.MusicBrainzService
import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.musicinfo.BuildConfig
import com.ealva.musicinfo.service.art.ArtFinder
import com.ealva.musicinfo.service.art.BrainzArtFinder
import com.ealva.musicinfo.service.art.CompositeArtFinder
import com.ealva.musicinfo.service.art.LastFmArtFinder
import com.ealva.musicinfo.service.art.SpotifyArtFinder
import com.ealva.musicinfo.service.art.WikipediaArtFinder
import com.ealva.musicinfo.service.common.AppName
import com.ealva.musicinfo.service.common.AppVersion
import com.ealva.musicinfo.service.common.ContactEmail
import com.ealva.musicinfo.service.common.MusicInfoMessage
import com.ealva.musicinfo.service.init.EalvaMusicInfo
import com.ealva.musicinfo.service.lastfm.LastFmService
import com.ealva.musicinfo.service.spotify.SpotifyService
import com.ealva.musicinfo.service.wiki.WikipediaService
import com.ealva.musicinfo.wiki.data.WikiSummary
import com.github.michaelbull.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient

public typealias MusicInfoResult<T> = Result<T, MusicInfoMessage>

/**
 * MusicInfoService is a single point of construction/use/mgt for several services:
 * * [MusicBrainzService]
 * * [LastFmService]
 * * [SpotifyService]
 * * [WikipediaService]
 *
 * The services can be constructed elsewhere as well and passed during construction.
 */
public interface MusicInfoService {
  /**
   * The [artFinder] will look for artwork across the various services and return the results
   * in a flow.
   */
  public val artFinder: ArtFinder

  public val brainz: MusicBrainzService
  public val wiki: WikipediaService
  public val lastFm: LastFmService?
  public val spotify: SpotifyService?

  public suspend fun getArticleSummary(
    artistName: ArtistName,
    artistMbid: ArtistMbid?
  ): MusicInfoResult<WikiSummary>

  public suspend fun <T : Any> exec(
    block: suspend (
      brainz: MusicBrainzService,
      wiki: WikipediaService,
      lastFm: LastFmService?,
      spotify: SpotifyService?,
    ) -> Result<T, MusicInfoMessage>
  ): Result<T, MusicInfoMessage>

  public companion object {
    public suspend fun make(
      appName: AppName,
      appVersion: AppVersion,
      contactEmail: ContactEmail,
      lastFmApiKey: LastFmService.LastFmApiKey? = null,
      spotifyClientId: SpotifyService.SpotifyClientId? = null,
      spotifyClientSecret: SpotifyService.SpotifyClientSecret? = null,
      credentialsProvider: CredentialsProvider? = null,
      addLoggingInterceptor: Boolean = BuildConfig.DEBUG,
      okHttpClient: OkHttpClient = EalvaMusicInfo.okHttpClient,
      serviceDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): MusicInfoService = MusicInfoServiceImpl(
      MusicBrainzService(
        appName.value,
        appVersion.value,
        contactEmail.value,
        addLoggingInterceptor,
        credentialsProvider,
        okHttpClient,
        serviceDispatcher
      ),
      WikipediaService(
        appName,
        appVersion,
        contactEmail,
        okHttpClient,
        serviceDispatcher
      ),
      if (lastFmApiKey != null) LastFmService(
        appName,
        appVersion,
        contactEmail,
        lastFmApiKey,
        okHttpClient,
        serviceDispatcher
      ) else null,
      if (spotifyClientId != null && spotifyClientSecret != null)
        SpotifyService.make(
          spotifyClientId,
          spotifyClientSecret,
          serviceDispatcher
        )
      else null
    )

    public operator fun invoke(
      brainz: MusicBrainzService,
      wiki: WikipediaService,
      lastFm: LastFmService? = null,
      spotify: SpotifyService? = null
    ): MusicInfoService = MusicInfoServiceImpl(brainz, wiki, lastFm, spotify)
  }
}

private class MusicInfoServiceImpl(
  override val brainz: MusicBrainzService,
  override val wiki: WikipediaService,
  override val lastFm: LastFmService?,
  override val spotify: SpotifyService?
) : MusicInfoService {
  private val wikiArtFinder = WikipediaArtFinder(wiki, brainz)

  override val artFinder: ArtFinder = CompositeArtFinder(
    BrainzArtFinder(brainz),
    wikiArtFinder
  ).apply {
    if (lastFm != null) add(LastFmArtFinder(lastFm))
    if (spotify != null) add(SpotifyArtFinder(spotify))
  }

  override suspend fun getArticleSummary(
    artistName: ArtistName,
    artistMbid: ArtistMbid?
  ): MusicInfoResult<WikiSummary> = wikiArtFinder.getArticleSummary(artistName, artistMbid)

  override suspend fun <T : Any> exec(
    block: suspend (
      brainz: MusicBrainzService,
      wiki: WikipediaService,
      lastFm: LastFmService?,
      spotify: SpotifyService?,
    ) -> Result<T, MusicInfoMessage>
  ): Result<T, MusicInfoMessage> = block(brainz, wiki, lastFm, spotify)
}
