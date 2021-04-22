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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.TrackTitle
import com.ealva.musicinfo.BuildConfig
import com.ealva.musicinfo.test.shared.MainCoroutineRule
import com.ealva.musicinfo.test.shared.runBlockingTest
import com.ealva.musicinfo.test.shared.toHaveAny
import com.ealva.musicinfo.test.shared.toNotBeEmpty
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.nhaarman.expect.expect
import com.nhaarman.expect.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@Suppress("LargeClass")
public class SpotifySmokeTest {

  @get:Rule
  public var coroutineRule: MainCoroutineRule = MainCoroutineRule()

  private lateinit var appCtx: Context
  private lateinit var spotifyService: SpotifyService

  @Before
  public fun setup() {
    appCtx = ApplicationProvider.getApplicationContext()
    runBlocking {
      spotifyService = SpotifyService.make(
        BuildConfig.SPOTIFY_CLIENT_ID,
        BuildConfig.SPOTIFY_CLIENT_SECRET,
        coroutineRule.testDispatcher
      )
    }
    // ensure we do not exceed rate limiting as we are creating the services each time and not
    // applying rate limiting between calls in the service itself
    Thread.sleep(500)
  }

  @Test
  public fun testGetAlbumInfoTheBeatlesRevolver(): Unit = spotify {
    searchAlbum {
      artist(ArtistName("The Beatles"))
      album(AlbumTitle("Revolver"))
    }.onSuccess { paging ->
      expect(paging.items).toNotBeEmpty { "No albums returned" }
      expect(paging.items).toHaveAny { it.name.contains("Revolver") } // "Revolve (Remastered)"
      expect(paging.items[0].artists).toNotBeEmpty { "No artists for first item" }
      expect(paging.items[0].artists).toHaveAny { it.name == "The Beatles" }
    }.onFailure { fail("LastFm call failed") { it } }
  }

  @Test
  public fun testGetArtistInfoPinkFloyd(): Unit = spotify {
    searchArtist {
      artist(ArtistName("Pink Floyd"))
    }.onSuccess { paging ->
      expect(paging.items).toNotBeEmpty { "No Artists returned" }
    }.onFailure { fail("LastFm call failed") { it } }
  }

  @Test
  public fun testGetTrackInfoBlueOysterCultDontFearTheReaper(): Unit = spotify {
    searchTrack {
      artist(ArtistName("Blue Oyster Cult"))
      track(TrackTitle("(Don't Fear) the Reaper"))
    }.onSuccess { paging ->
      expect(paging.items).toNotBeEmpty { "No tracks returned" }
      expect(paging.items).toHaveAny { track ->
        track.artists.any { artist -> artist.name == "Blue Öyster Cult" }
      }
      expect(paging.items).toHaveAny { track ->
        track.album.name.equals("Agents of Fortune", ignoreCase = true)
      }
    }.onFailure { fail("LastFm call failed") { it } }
  }

  private fun spotify(block: suspend SpotifyService.() -> Unit) = coroutineRule.runBlockingTest {
    @Suppress("BlockingMethodInNonBlockingContext")
    runBlocking {
      spotifyService.block()
    }
  }
}
