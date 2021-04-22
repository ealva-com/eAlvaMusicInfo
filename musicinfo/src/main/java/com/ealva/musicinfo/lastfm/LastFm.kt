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

@file:Suppress("MaxLineLength")

package com.ealva.musicinfo.lastfm

import com.ealva.musicinfo.lastfm.data.AlbumReply
import com.ealva.musicinfo.lastfm.data.ArtistReply
import com.ealva.musicinfo.lastfm.data.SimilarArtistReply
import com.ealva.musicinfo.lastfm.data.SimilarTrackReply
import com.ealva.musicinfo.lastfm.data.TrackReply
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

public interface LastFm {
  /**
   * Use AutoCorrect.Yes with functions that take a name/title if the LastFm server should offer
   * spelling corrections in the returned data
   */
  public enum class AutoCorrect(private val value: Int) {
    /** Don't auto correct misspelled names or titles */
    No(0),
    /** Include LastFm server auto corrected names/titles in the returned data */
    Yes(1);

    override fun toString(): String = value.toString()
  }

  /**
   * Get the metadata and track list for an album on Last.fm using the artist and album name
   *
   * [LastFm album.getInfo](https://www.last.fm/api/show/album.getInfo)
   *
   * [Example](http://ws.audioscrobbler.com/2.0/?method=album.getinfo&artist=The+Beatles&album=Revolver&api_key=YOUR_API_KEY&format=json)
   */
  @GET("2.0/?method=album.getInfo")
  public suspend fun getAlbumInfo(
    @Query("artist") artist: String,
    @Query("album") album: String,
    @Query("autocorrect") correct: AutoCorrect? = null
  ): Response<AlbumReply>

  /**
   * Get the metadata and track list for an album on Last.fm using the MusicBrainz Release MBID
   *
   * [LastFm album.getInfo](https://www.last.fm/api/show/album.getInfo)
   *
   * [Example](http://ws.audioscrobbler.com/2.0/?method=album.getinfo&mbid=63b3a8ca-26f2-4e2b-b867-647a6ec2bebd&api_key=YOUR_API_KEY&format=json)
   */
  @GET("2.0/?method=album.getInfo")
  public suspend fun getAlbumInfo(@Query("mbid") mbid: String): Response<AlbumReply>

  /**
   * Get the metadata for an artist. Includes biography, truncated at 300 characters.
   *
   * [LastFm artist.getInfo](https://www.last.fm/api/show/artist.getInfo)
   *
   * [Example]()http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=The+Beatles&api_key=YOUR_API_KEY&format=json)
   */
  @GET("2.0/?method=artist.getInfo")
  public suspend fun getArtistInfo(
    @Query("artist") artist: String,
    @Query("autocorrect") correct: AutoCorrect? = null,
  ): Response<ArtistReply>

  /**
   * Get the metadata for an artist. Includes biography, truncated at 300 characters.
   *
   * [LastFm artist.getInfo](https://www.last.fm/api/show/artist.getInfo)
   *
   * [Example](http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&mbid=3604c99d-c146-4276-aa0c-9376d333aeb8&api_key=YOUR_API_KEY&format=json)
   */
  @GET("2.0/?method=artist.getInfo")
  public suspend fun getArtistInfo(@Query("mbid") mbid: String): Response<ArtistReply>

  /**
   * Get all the artists similar to this artist
   *
   * [LastFm artist.getSimilar](https://www.last.fm/api/show/artist.getSimilar)
   *
   * [Example]()http://ws.audioscrobbler.com/2.0/?method=artist.getsimilar&artist=cher&api_key=YOUR_API_KEY&format=json)
   */
  @GET("2.0/?method=artist.getSimilar")
  public suspend fun getSimilarArtists(
    @Query("artist") artist: String,
    @Query("limit") limit: Int? = null,
    @Query("autocorrect") correct: AutoCorrect? = null,
  ): Response<SimilarArtistReply>

  /**
   * Get all the artists similar to this artist
   *
   * [LastFm artist.getSimilar](https://www.last.fm/api/show/artist.getSimilar)
   *
   * http://ws.audioscrobbler.com/2.0/?method=artist.getsimilar&mbid=bfcc6d75-a6a5-4bc6-8282-47aec8531818&api_key=YOUR_API_KEY&format=json
   */
  @GET("2.0/?method=artist.getSimilar")
  public suspend fun getSimilarArtists(
    @Query("mbid") mbid: String,
    @Query("limit") limit: Int? = null,
  ): Response<SimilarArtistReply>

  /**
   * Get the metadata for a track on Last.fm using the artist/track name
   *
   * [LastFm track.getInfo](https://www.last.fm/api/show/track.getInfo)
   *
   * http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=YOUR_API_KEY&artist=cher&track=believe&format=json
   */
  @GET("2.0/?method=track.getInfo")
  public suspend fun getTrackInfo(
    @Query("artist") artist: String,
    @Query("track") track: String,
    @Query("autocorrect") correct: AutoCorrect? = null,
  ): Response<TrackReply>

  /**
   * Get the metadata for a track on Last.fm using the MusicBrainz Track MBID
   *
   * [LastFm track.getInfo](https://www.last.fm/api/show/track.getInfo)
   *
   * http://ws.audioscrobbler.com/2.0/?method=track.getInfo&mbid=32ca187e-ee25-4f18-b7d0-3b6713f24635&api_key=YOUR_API_KEY&format=json
   */
  @GET("2.0/?method=track.getInfo")
  public suspend fun getTrackInfo(@Query("mbid") mbid: String): Response<TrackReply>

  /**
   * Get the similar tracks for this track on Last.fm, based on listening data.
   *
   * [LastFm track.getSimilar](https://www.last.fm/api/show/track.getSimilar)
   *
   * http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=cher&track=believe&api_key=YOUR_API_KEY&format=json
   */
  @GET("2.0/?method=track.getSimilar")
  public suspend fun getSimilarTracks(
    @Query("artist") artist: String,
    @Query("track") track: String,
    @Query("limit") limit: Int? = null,
    @Query("autocorrect") correct: AutoCorrect? = null,
  ): Response<SimilarTrackReply>

  /**
   * Get the similar tracks for this track on Last.fm, based on listening data.
   *
   * [LastFm track.getSimilar](https://www.last.fm/api/show/track.getSimilar)
   *
   * http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&mbid=32ca187e-ee25-4f18-b7d0-3b6713f24635&api_key=YOUR_API_KEY&format=json
   */
  @GET("2.0/?method=track.getSimilar")
  public suspend fun getSimilarTracks(
    @Query("mbid") mbid: String,
    @Query("limit") limit: Int? = null
  ): Response<SimilarTrackReply>
}
