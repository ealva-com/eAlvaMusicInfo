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

package com.ealva.musicinfo.lastfm

import com.ealva.musicinfo.lastfm.data.AlbumStatus
import com.ealva.musicinfo.lastfm.data.ArtistStatus
import com.ealva.musicinfo.lastfm.data.TrackStatus
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

public interface LastFm {
  public enum class AutoCorrect(private val value: Int) {
    No(0),
    Yes(1);

    override fun toString(): String = value.toString()
  }

  /**
   *
   *
   * http://ws.audioscrobbler.com/2.0/?method=album.getinfo&artist=The+Beatles&album=Revolver&api_key=yourapikeygoeshere&format=json
   */
  @GET("2.0/?method=album.getInfo")
  public suspend fun getAlbumInfo(
    @Query("artist") artist: String,
    @Query("album") album: String,
    @Query("autocorrect") correct: AutoCorrect? = null
  ): Response<AlbumStatus>

  @GET("2.0/?method=album.getInfo")
  public suspend fun getAlbumInfo(@Query("mbid") mbid: String): Response<AlbumStatus>

  /**
   *
   *
   * http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=The+Beatles&api_key=youapikeygoeshere&format=json
   */
  @GET("2.0/?method=artist.getInfo")
  public suspend fun getArtistInfo(
    @Query("artist") artist: String,
    @Query("autocorrect") correct: AutoCorrect? = null,
  ): Response<ArtistStatus>

  @GET("2.0/?method=artist.getInfo")
  public suspend fun getArtistInfo(@Query("mbid") mbid: String): Response<ArtistStatus>

  /**
   *
   *
   * http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=youapikeygoeshere&artist=cher&track=believe&format=json
   */
  @GET("2.0/?method=track.getInfo")
  public suspend fun getTrackInfo(
    @Query("artist") artist: String,
    @Query("track") track: String,
    @Query("autocorrect") correct: AutoCorrect? = null,
  ): Response<TrackStatus>

  @GET("2.0/?method=track.getInfo")
  public suspend fun getTrackInfo(@Query("mbid") mbid: String): Response<TrackStatus>
}
