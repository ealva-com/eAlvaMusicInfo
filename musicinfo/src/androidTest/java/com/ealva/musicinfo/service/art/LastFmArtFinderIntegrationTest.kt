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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ealva.ealvabrainz.brainz.data.ArtistMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.RecordingTitle
import com.ealva.musicinfo.BuildConfig
import com.ealva.musicinfo.service.lastfm.LastFmService
import com.ealva.musicinfo.test.shared.MainCoroutineRule
import com.ealva.musicinfo.test.shared.runBlockingTest
import com.ealva.musicinfo.test.shared.toNotBeEmpty
import com.nhaarman.expect.expect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

private val THE_BEATLES = ArtistName("The Beatles")
private val REVOLVER = AlbumTitle("Revolver")
private val HAPPINESS_IS = RecordingTitle("Happiness Is a Warm Gun")

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@Suppress("LargeClass")
public class LastFmArtFinderIntegrationTest {
  @get:Rule
  public var coroutineRule: MainCoroutineRule = MainCoroutineRule()

  private lateinit var appCtx: Context
  private lateinit var finder: LastFmArtFinder

  @Before
  public fun setup() {
    appCtx = ApplicationProvider.getApplicationContext()
    runBlocking {
      finder = LastFmArtFinder(
        LastFmService(
          BuildConfig.MUSICINFO_APP_NAME,
          BuildConfig.MUSICINFO_APP_VERSION,
          BuildConfig.MUSICINFO_CONTACT_EMAIL,
          BuildConfig.LASTFM_API_KEY,
          File(appCtx.cacheDir, "TestLastFmCache"),
          coroutineRule.testDispatcher
        )
      )
    }
    // ensure we do not exceed rate limiting as we are creating the services each time and not
    // applying rate limiting between calls in the service itself
    Thread.sleep(500)
  }

  @Test
  public fun testBeatlesRevolverArt(): Unit = find {
    findAlbumArt(THE_BEATLES, REVOLVER).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for Beatles Revolver" }
    }
  }

  @Test
  public fun testBeatlesRubberSoulArt(): Unit = find {
    val releaseMbid = ReleaseMbid("d1092e74-6412-4bc6-a91c-bc3588b764f9")
    findAlbumArt(ArtistName(""), AlbumTitle(""), releaseMbid = releaseMbid).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for Beatles Revolver mbid" }
    }
  }

  @Test
  public fun testTheBeatlesMbidArt(): Unit = find {
    val artistMbid = ArtistMbid("b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d")
    findArtistArt(ArtistName(""), artistMbid).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for The Beatles mbid" }
    }
  }

  @Test
  public fun testTheBeatlesArt(): Unit = find {
    findArtistArt(THE_BEATLES).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for The Beatles" }
    }
  }

  @Test
  public fun testBeatlesHappinessIsMbidArt(): Unit = find {
    val trackMbid = TrackMbid("f64ec76e-d63a-4842-8877-42d061bddba5")
    findTrackArt(ArtistName(""), RecordingTitle(""), trackMbid).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for The Beatles/Happiness mbid" }
    }
  }

  @Test
  public fun testBeatlesHappinessArt(): Unit = find {
    findTrackArt(THE_BEATLES, HAPPINESS_IS).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for The Beatles Happiness Is" }
    }
  }

  private fun find(block: suspend ArtFinder.() -> Unit) = coroutineRule.runBlockingTest {
    @Suppress("BlockingMethodInNonBlockingContext")
    runBlocking {
      finder.block()
    }
  }
}
