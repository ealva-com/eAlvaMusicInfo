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
import com.ealva.brainzsvc.service.MusicBrainzService
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.brainz.data.TrackMbid
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.RecordingTitle
import com.ealva.musicinfo.BuildConfig
import com.ealva.musicinfo.service.common.AppName
import com.ealva.musicinfo.service.common.AppVersion
import com.ealva.musicinfo.service.common.ContactEmail
import com.ealva.musicinfo.service.init.EalvaMusicInfo
import com.ealva.musicinfo.service.wiki.WikipediaService
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

private val THE_BEATLES = ArtistName("The Beatles")
private val REVOLVER = AlbumTitle("Revolver")
private val RUBBER_SOUL = AlbumTitle("Rubber Soul")
private val HAPPINESS_IS = RecordingTitle("Happiness Is a Warm Gun")

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@Suppress("LargeClass")
public class BrainzArtFinderIntegrationTest {

  @get:Rule
  public var coroutineRule: MainCoroutineRule = MainCoroutineRule()

  private lateinit var appCtx: Context
  private lateinit var finder: ArtFinder

  @Before
  public fun setup() {
    appCtx = ApplicationProvider.getApplicationContext()
    finder = BrainzArtFinder(
      MusicBrainzService(
        BuildConfig.MUSICINFO_APP_NAME,
        BuildConfig.MUSICINFO_APP_VERSION,
        BuildConfig.MUSICINFO_CONTACT_EMAIL,
        true,
        clientForBuilder = EalvaMusicInfo.okHttpClient,
        dispatcher = coroutineRule.testDispatcher
      )
    )
    // ensure we do not exceed rate limiting as we are creating the services each time and not
    // applying rate limiting between calls in the service itself
    Thread.sleep(500)
  }

  @Test
  public fun testBeatlesRevolverArt(): Unit = find {
    findAlbumArt(THE_BEATLES, REVOLVER,).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for Beatles Revolver" }
    }
  }

  @Test
  public fun testBeatlesRubberSoulArt(): Unit = find {
    val releaseMbid = ReleaseMbid("d1092e74-6412-4bc6-a91c-bc3588b764f9")
    findAlbumArt(THE_BEATLES, RUBBER_SOUL, releaseMbid = releaseMbid,).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for Beatles Revolver" }
    }
  }

  @Test
  public fun testBeatlesHappinessIsMbidArt(): Unit = find {
    val trackMbid = TrackMbid("f64ec76e-d63a-4842-8877-42d061bddba5")
    findTrackArt(THE_BEATLES, HAPPINESS_IS, trackMbid,).toList().let { list ->
      expect(list).toNotBeEmpty { "No artwork for The Beatles/Happiness mbid" }
    }
  }

  @Test
  public fun testBeatlesHappinessArt(): Unit = find {
    findTrackArt(THE_BEATLES, HAPPINESS_IS,).toList().let { list ->
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
