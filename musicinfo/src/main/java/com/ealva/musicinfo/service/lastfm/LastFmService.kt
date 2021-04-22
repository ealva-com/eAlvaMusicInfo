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

package com.ealva.musicinfo.service.lastfm

import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.Limit
import com.ealva.ealvabrainz.common.RecordingTitle
import com.ealva.musicinfo.BuildConfig
import com.ealva.musicinfo.lastfm.LastFm
import com.ealva.musicinfo.lastfm.data.Album
import com.ealva.musicinfo.lastfm.data.Artist
import com.ealva.musicinfo.lastfm.data.LastFmReply
import com.ealva.musicinfo.lastfm.data.SimilarArtists
import com.ealva.musicinfo.lastfm.data.SimilarTracks
import com.ealva.musicinfo.lastfm.data.Track
import com.ealva.musicinfo.lastfm.data.theLastFmMoshi
import com.ealva.musicinfo.service.common.MusicInfoMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoExceptionMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoLastFmMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoStatusMessage.MusicInfoErrorCodeMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoStatusMessage.MusicInfoNullReturn
import com.ealva.musicinfo.service.net.CacheControlInterceptor
import com.ealva.musicinfo.service.net.ThrottlingInterceptor
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

/**
 * A LastFmCall is a suspending function which has a [LastFm] receiver and returns a Retrofit
 * Response with a type parameter of the returned LastFm entity.
 */
public typealias LastFmCall<T> = suspend LastFm.() -> Response<T>

/**
 * LastfmResult<T> is a Result<T, MusicInfoMessage>, T being the Ok return type and a specialization
 * of MusicInfoMessage being the Err return type.
 */
public typealias LastFmResult<T> = Result<T, MusicInfoMessage>

private const val LASTFM_BASE_URL = "https://ws.audioscrobbler.com/"

/**
 * LastFmService is a wrapper around a Retrofit LastFm instance that provides higher level
 * functionality such as rate limiting, splitting status into entity or error message, exception
 * handling, etc.
 *
 * Functions return a [LastFmResult] which is a Result<T, MusicInfoMessage>. If the result is
 * [Ok] it will contain an instance of T. If an error occurs an [Err] is returned which contains a
 * MusicInfoMessage.
 *
 * An [Err] will be a [MusicInfoMessage] of type:
 * * [MusicInfoExceptionMessage] if an underlying exception is thrown
 * * [MusicInfoLastFmMessage] if LastFm returns an error status instead of an entity
 * * [MusicInfoNullReturn] if the response is OK but null
 * * [MusicInfoErrorCodeMessage] if the response is not successful - contains the response
 * status code.
 *
 * All suspend functions are main safe, in that they are dispatched on a contained
 * Coroutine dispatcher, typically Dispatchers.IO. Exceptions are not thrown across this boundary
 * and instead a Result monad, indicating success (Ok) or failure (Err), is returned. Look at the
 * implementation of the lastFm() method to see where everything comes together.
 *
 * If a [MusicInfoLastFmMessage] is returned from a "getInfo" call, the possible status codes are:
 * * 2 : Invalid service - This service does not exist
 * * 3 : Invalid Method - No method with that name in this package
 * * 4 : Authentication Failed - You do not have permissions to access the service
 * * 5 : Invalid format - This service doesn't exist in that format
 * * 6 : Invalid parameters - Your request is missing a required parameter
 * * 7 : Invalid resource specified
 * * 8 : Operation failed - Something else went wrong
 * * 9 : Invalid session key - Please re-authenticate
 * * 10 : Invalid API key - You must be granted a valid key by last.fm
 * * 11 : Service Offline - This service is temporarily offline. Try again later.
 * * 13 : Invalid method signature supplied
 * * 16 : There was a temporary error processing your request. Please try again
 * * 26 : Suspended API key - Access for your account has been suspended, please contact Last.fm
 * * 29 : Rate limit exceeded - Your IP has made too many requests in a short period
 *
 * [LastFm API](https://www.last.fm/api)
 *
 * [Result monad](https://github.com/michaelbull/kotlin-result)
 */
public interface LastFmService {
  /**
   * Get Last.fm metadata for the [Album] using [albumTitle] and [artistName]. If [autoCorrect] is
   * [LastFm.AutoCorrect.Yes], any corrected name will be included in the result.
   *
   * [LastFm album.getInfo](https://www.last.fm/api/show/album.getInfo)
   */
  public suspend fun getAlbumInfo(
    artistName: ArtistName,
    albumTitle: AlbumTitle,
    autoCorrect: LastFm.AutoCorrect? = null
  ): LastFmResult<Album>

  /**
   * Get the Last.fm metadata for the [Album] whose MusicBrainz Release MBID is [mbid]
   *
   * [LastFm album.getInfo](https://www.last.fm/api/show/album.getInfo)
   */
  public suspend fun getAlbumInfo(mbid: ReleaseMbid): LastFmResult<Album>

  /**
   * Get the metadata for a [Artist] on Last.fm using the [artistName]. If [autoCorrect] is
   * [LastFm.AutoCorrect.Yes], any corrected name will be included in the result.
   *
   * [LastFm artist.getInfo](https://www.last.fm/api/show/artist.getInfo)
   */
  public suspend fun getArtistInfo(
    artistName: ArtistName,
    autoCorrect: LastFm.AutoCorrect? = null
  ): LastFmResult<Artist>

  /**
   * Get the metadata for a [Artist] on Last.fm using the [ArtistMbid] [mbid]
   *
   * [LastFm artist.getInfo](https://www.last.fm/api/show/artist.getInfo)
   */
  public suspend fun getArtistInfo(mbid: ArtistMbid): LastFmResult<Artist>

  public suspend fun getSimilarArtists(
    artistName: ArtistName,
    limit: Limit? = null,
    autoCorrect: LastFm.AutoCorrect? = null
  ): LastFmResult<SimilarArtists>

  public suspend fun getSimilarArtists(
    mbid: ArtistMbid,
    limit: Limit? = null,
  ): LastFmResult<SimilarArtists>

  /**
   * Get the metadata for a [Track] on Last.fm using the [artistName] and [recordingTitle]. If
   * [autoCorrect] is [LastFm.AutoCorrect.Yes], any corrected name will be included in the result.
   *
   * [LastFm track.getInfo](https://www.last.fm/api/show/track.getInfo)
   */
  public suspend fun getTrackInfo(
    artistName: ArtistName,
    recordingTitle: RecordingTitle,
    autoCorrect: LastFm.AutoCorrect? = null
  ): LastFmResult<Track>

  /**
   * Get the metadata for a track on Last.fm using the [TrackMbid] [mbid]
   *
   * [LastFm track.getInfo](https://www.last.fm/api/show/track.getInfo)
   */
  public suspend fun getTrackInfo(mbid: TrackMbid): LastFmResult<Track>

  public suspend fun getSimilarTracks(
    artistName: ArtistName,
    recordingTitle: RecordingTitle,
    limit: Limit? = null,
    autoCorrect: LastFm.AutoCorrect? = null
  ): LastFmResult<SimilarTracks>

  public suspend fun getSimilarTracks(
    mbid: TrackMbid,
    limit: Limit? = null,
  ): LastFmResult<SimilarTracks>

  public companion object {
    public operator fun invoke(
      appName: String,
      appVersion: String,
      contactEmail: String,
      apiKey: String,
      cacheDirectory: File,
      coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): LastFmService = LastFmServiceImpl(
      Retrofit.Builder()
        .client(
          makeOkHttpClient(
            "LastFmService",
            appName,
            appVersion,
            contactEmail,
            apiKey,
            cacheDirectory,
            addLoggingInterceptor = BuildConfig.DEBUG
          )
        )
        .baseUrl(LASTFM_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(theLastFmMoshi))
        .build()
        .create(LastFm::class.java),
      coroutineDispatcher
    ).also { println("$appName $appVersion $contactEmail $apiKey") }
  }
}

private class LastFmServiceImpl(
  private val lastFm: LastFm,
  private val dispatcher: CoroutineDispatcher
) : LastFmService {
  override suspend fun getAlbumInfo(
    artistName: ArtistName,
    albumTitle: AlbumTitle,
    autoCorrect: LastFm.AutoCorrect?
  ): LastFmResult<Album> = lastFm {
    getAlbumInfo(artistName.value, albumTitle.value)
  }

  override suspend fun getAlbumInfo(mbid: ReleaseMbid): LastFmResult<Album> = lastFm {
    getAlbumInfo(mbid.value)
  }

  override suspend fun getArtistInfo(
    artistName: ArtistName,
    autoCorrect: LastFm.AutoCorrect?
  ): LastFmResult<Artist> = lastFm {
    getArtistInfo(artistName.value, autoCorrect)
  }

  override suspend fun getArtistInfo(mbid: ArtistMbid): LastFmResult<Artist> = lastFm {
    getArtistInfo(mbid = mbid.value)
  }

  override suspend fun getSimilarArtists(
    artistName: ArtistName,
    limit: Limit?,
    autoCorrect: LastFm.AutoCorrect?
  ): LastFmResult<SimilarArtists> = lastFm {
    getSimilarArtists(artistName.value, limit?.value, autoCorrect)
  }

  override suspend fun getSimilarArtists(
    mbid: ArtistMbid,
    limit: Limit?
  ): LastFmResult<SimilarArtists> = lastFm {
    getSimilarArtists(mbid = mbid.value, limit = limit?.value)
  }

  override suspend fun getTrackInfo(
    artistName: ArtistName,
    recordingTitle: RecordingTitle,
    autoCorrect: LastFm.AutoCorrect?
  ): LastFmResult<Track> = lastFm {
    getTrackInfo(artistName.value, recordingTitle.value, autoCorrect)
  }

  override suspend fun getTrackInfo(mbid: TrackMbid): LastFmResult<Track> = lastFm {
    getTrackInfo(mbid.value)
  }

  override suspend fun getSimilarTracks(
    artistName: ArtistName,
    recordingTitle: RecordingTitle,
    limit: Limit?,
    autoCorrect: LastFm.AutoCorrect?
  ): LastFmResult<SimilarTracks> = lastFm {
    getSimilarTracks(artistName.value, recordingTitle.value, limit?.value, autoCorrect)
  }

  override suspend fun getSimilarTracks(
    mbid: TrackMbid,
    limit: Limit?
  ): LastFmResult<SimilarTracks> = lastFm {
    getSimilarTracks(mbid.value, limit?.value)
  }

  private suspend fun <U, S : LastFmReply<U>> lastFm(
    block: LastFmCall<S>
  ): LastFmResult<U> = withContext(dispatcher) {
    runCatching { lastFm.block() }
      .mapError { ex -> MusicInfoExceptionMessage(ex) }
      .andThen { response ->
        if (response.isSuccessful) handleResponseBody(response) else makeErrorCodeErr(response)
      }
      .andThen { reply ->
        if (reply.error == 0) Ok(reply.entity) else makeServiceErr(reply)
      }
  }

  private fun <U, S : LastFmReply<U>> handleResponseBody(response: Response<S>) =
    response.body()?.let { Ok(it) } ?: makeNullErr(response)

  private fun <U, S : LastFmReply<U>> makeErrorCodeErr(response: Response<S>) =
    Err(MusicInfoErrorCodeMessage(response.code(), response))

  private fun <U, S : LastFmReply<U>> makeNullErr(response: Response<S>) =
    Err(MusicInfoNullReturn(response.code()))

  private fun <U, S : LastFmReply<U>> makeServiceErr(status: S) =
    Err(MusicInfoLastFmMessage(status.error, status.message))
}

private const val DAYS_MAX_AGE = 14
private const val DAYS_MIN_FRESH = 14
private const val DAYS_MAX_STALE = 365
private const val LASTFM_MAX_CALLS_PER_SECOND = 5.0
private const val TEN_MEG = 10 * 1024 * 1024

internal fun makeOkHttpClient(
  serviceName: String,
  appName: String,
  appVersion: String,
  contactEmail: String,
  apiKey: String,
  cacheDirectory: File,
  addLoggingInterceptor: Boolean = true
): OkHttpClient = OkHttpClient.Builder().apply {
  addInterceptor(CacheControlInterceptor(DAYS_MAX_AGE, DAYS_MIN_FRESH, DAYS_MAX_STALE))
  addInterceptor(ThrottlingInterceptor(LASTFM_MAX_CALLS_PER_SECOND, serviceName))
  addInterceptor(LastFmApiInterceptor(appName, appVersion, contactEmail, apiKey))
  if (addLoggingInterceptor) {
    addInterceptor(
      HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    )
  }
  cache(Cache(cacheDirectory, TEN_MEG.toLong()))
}.build()
