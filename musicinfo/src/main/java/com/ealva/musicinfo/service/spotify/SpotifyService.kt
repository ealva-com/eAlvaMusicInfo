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

package com.ealva.musicinfo.service.spotify

import com.adamratzman.spotify.SpotifyAppApi
import com.adamratzman.spotify.endpoints.pub.SearchApi
import com.adamratzman.spotify.models.Artist
import com.adamratzman.spotify.models.PagingObject
import com.adamratzman.spotify.models.SimpleAlbum
import com.adamratzman.spotify.models.Track
import com.adamratzman.spotify.spotifyAppApi
import com.adamratzman.spotify.utils.Market
import com.ealva.ealvabrainz.common.Limit
import com.ealva.ealvabrainz.common.Offset
import com.ealva.musicinfo.service.common.MusicInfoMessage
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.coroutines.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public typealias SpotifyCall<T> = suspend SearchApi.() -> PagingObject<T>

public typealias SpotifyResult<T> = Result<PagingObject<T>, MusicInfoMessage>

public interface SpotifyService {
  @JvmInline
  public value class SpotifyClientId(public val value: String)

  @JvmInline
  public value class SpotifyClientSecret(public val value: String)

  public suspend fun searchArtist(
    limit: Limit? = null,
    offset: Offset? = null,
    market: Market? = null,
    search: ArtistSearch.() -> Unit
  ): SpotifyResult<Artist>

  public suspend fun searchAlbum(
    limit: Limit? = null,
    offset: Offset? = null,
    market: Market? = null,
    search: AlbumSearch.() -> Unit
  ): SpotifyResult<SimpleAlbum>

  public suspend fun searchTrack(
    limit: Limit? = null,
    offset: Offset? = null,
    market: Market? = null,
    search: TrackSearch.() -> Unit
  ): SpotifyResult<Track>

  public companion object {
    public suspend fun make(
      clientId: SpotifyClientId,
      clientSecret: SpotifyClientSecret,
      coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): SpotifyService {
      val api: SpotifyAppApi = spotifyAppApi(clientId.value, clientSecret.value).build()
      return SpotifyServiceImpl(api, coroutineDispatcher)
    }
  }
}

private class SpotifyServiceImpl(
  private val api: SpotifyAppApi,
  private val dispatcher: CoroutineDispatcher,
) : SpotifyService {
  override suspend fun searchArtist(
    limit: Limit?,
    offset: Offset?,
    market: Market?,
    search: ArtistSearch.() -> Unit
  ): SpotifyResult<Artist> = spotify {
    searchArtist(ArtistSearch(search), limit?.value, offset?.value, market)
  }

  override suspend fun searchAlbum(
    limit: Limit?,
    offset: Offset?,
    market: Market?,
    search: AlbumSearch.() -> Unit
  ): SpotifyResult<SimpleAlbum> = spotify {
    searchAlbum(AlbumSearch(search), limit?.value, offset?.value, market)
  }

  override suspend fun searchTrack(
    limit: Limit?,
    offset: Offset?,
    market: Market?,
    search: TrackSearch.() -> Unit
  ): SpotifyResult<Track> = spotify {
    searchTrack(TrackSearch(search), limit?.value, offset?.value, market)
  }

  private suspend fun <T : Any> spotify(
    block: SpotifyCall<T>
  ): SpotifyResult<T> = withContext(dispatcher) {
    runSuspendCatching { api.search.block() }
      .mapError { ex -> MusicInfoMessage.MusicInfoExceptionMessage(ex) }
  }
}
