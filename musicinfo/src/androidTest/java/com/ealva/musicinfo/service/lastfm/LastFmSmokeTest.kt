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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.TrackTitle
import com.ealva.musicinfo.BuildConfig
import com.ealva.musicinfo.lastfm.LastFm
import com.ealva.musicinfo.service.common.ContextStringFetcher
import com.ealva.musicinfo.service.common.MusicInfoMessage
import com.ealva.musicinfo.service.common.StringFetcher
import com.ealva.musicinfo.test.shared.MainCoroutineRule
import com.ealva.musicinfo.test.shared.runBlockingTest
import com.ealva.musicinfo.test.shared.toHaveAny
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
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@Suppress("LargeClass")
public class LastFmSmokeTest {

  @get:Rule
  public var coroutineRule: MainCoroutineRule = MainCoroutineRule()

  private lateinit var appCtx: Context
  private lateinit var lastFmService: LastFmService
  private lateinit var fetcher: StringFetcher

  @Before
  public fun setup() {
    appCtx = ApplicationProvider.getApplicationContext()
    fetcher = ContextStringFetcher(appCtx)
    lastFmService = LastFmService(
      BuildConfig.MUSICINFO_APP_NAME,
      BuildConfig.MUSICINFO_APP_VERSION,
      BuildConfig.MUSICINFO_CONTACT_EMAIL,
      BuildConfig.LASTFM_API_KEY,
      File(appCtx.cacheDir, "TestLastFmCache"),
      coroutineRule.testDispatcher
    )
    // ensure we do not exceed rate limiting as we are creating the services each time and not
    // applying rate limiting between calls in the service itself
    Thread.sleep(500)
  }

  @Test
  public fun testGetAlbumInfoTheBeatlesRevolver(): Unit = lastFm {
    getAlbumInfo(ArtistName("The Beatles"), AlbumTitle("Revolver"), null)
      .onSuccess { album ->
        expect(album.artist).toBe("The Beatles")
        expect(album.tracks.trackList).toHaveAny { track -> track.name == "Taxman" }
        expect(album.tracks.trackList).toHaveAny { it.name == "Here, There and Everywhere" }
      }
      .onFailure { fail("LastFm call failed") { it.asString(fetcher) } }
  }

  @Test
  public fun testGetAlbumInfoFromMbidJethroTullAqualung(): Unit = lastFm {
    // Jethro Tull - Aqualung
    val mbid = ReleaseMbid("382dc29a-c797-340b-92ef-4794f1665336")
    getAlbumInfo(mbid)
      .onSuccess { album ->
        expect(album.artist).toBe("Jethro Tull")
        expect(album.tracks.trackList).toHaveAny { it.name == "Aqualung" }
        expect(album.tracks.trackList).toHaveAny { it.name == "Locomotive Breath" }
      }
      .onFailure { fail("LastFm call failed") { it.asString(fetcher) } }
  }

  @Test
  public fun testGetArtistInfoPinkFloyd(): Unit = lastFm {
    getArtistInfo(ArtistName("Pink Floyd"))
      .onSuccess { artist ->
        println(artist)
        expect(artist.name).toBe("Pink Floyd")
        expect(artist.mbid).toBe("83d91898-7763-47d7-b03b-b92132375c47")
        expect(artist.bio.content).toContain("Roger Waters")
      }
      .onFailure { fail("LastFm call failed") { it.asString(fetcher) } }
  }

  @Test
  public fun testGetArtistInfoFromMbidEverclear(): Unit = lastFm {
    val mbid = ArtistMbid("3604c99d-c146-4276-aa0c-9376d333aeb8")
    getArtistInfo(mbid)
      .onSuccess { artist ->
        println(artist)
        expect(artist.name).toBe("Everclear")
        expect(artist.mbid).toBe(mbid.value)
        expect(artist.bio.content).toContain("Art Alexakis")
      }
      .onFailure { fail("LastFm call failed") { it.asString(fetcher) } }
  }

  @Test
  public fun testGetTrackInfoBlueOysterCultDontFearTheReaper(): Unit = lastFm {
    getTrackInfo(
      ArtistName("Blue Oyster Cult"),
      TrackTitle("Don't Fear the Reaper"),
      LastFm.AutoCorrect.Yes
    )
      .onSuccess { track ->
        track.toString().split("\n").forEach { println(it) }
        expect(track.name).toBe("(Don't Fear) The Reaper")
        expect(track.artist.name).toBe("Blue Öyster Cult")
      }
      .onFailure { fail("LastFm call failed") { it.asString(fetcher) } }
  }

  @Test
  public fun testGetTrackInfoFromMbidDontFearTheReaper(): Unit = lastFm {
    getTrackInfo(TrackMbid("125c117d-a7a1-4dc4-a609-67f31eb11613"))
      .onSuccess { track ->
        track.toString().split("\n").forEach { println(it) }
        expect(track.name).toBe("(Don't Fear) The Reaper")
        expect(track.artist.name).toBe("Blue Öyster Cult")
      }
      .onFailure { fail("LastFm call failed") { it.asString(fetcher) } }
  }

  @Test
  public fun testGetTrackInfoFromMbidBadMbid(): Unit = lastFm {
    getTrackInfo(TrackMbid("c117d-a7a1-4dc4-a609-67f31e3"))
      .onSuccess { fail("Expected call to fail") }
      .onFailure { msg ->
        expect(msg).toBeInstanceOf<MusicInfoMessage.MusicInfoLastFmMessage> { lastFmMsg ->
          expect(lastFmMsg.statusCode).toBe(6) // because mbid was malformed
        }
      }
  }

  private fun lastFm(block: suspend LastFmService.() -> Unit) = coroutineRule.runBlockingTest {
    @Suppress("BlockingMethodInNonBlockingContext")
    runBlocking {
      lastFmService.block()
    }
  }
}
