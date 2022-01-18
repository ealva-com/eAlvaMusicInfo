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

package com.ealva.musicinfo.service.wiki

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ealva.musicinfo.BuildConfig
import com.ealva.musicinfo.service.common.AppName
import com.ealva.musicinfo.service.common.AppVersion
import com.ealva.musicinfo.service.common.ContactEmail
import com.ealva.musicinfo.service.init.EalvaMusicInfo
import com.ealva.musicinfo.test.shared.MainCoroutineRule
import com.ealva.musicinfo.test.shared.runBlockingTest
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
import com.ealva.ealvabrainz.brainz.data.Url as BrainzUrl

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@Suppress("LargeClass")
public class WikiSmokeTest {
  @get:Rule
  public var coroutineRule: MainCoroutineRule = MainCoroutineRule()

  private lateinit var appCtx: Context
  private lateinit var wikipediaService: WikipediaService

  @Before
  public fun setup() {
    appCtx = ApplicationProvider.getApplicationContext()
    wikipediaService = WikipediaService(
      AppName(BuildConfig.MUSICINFO_APP_NAME),
      AppVersion(BuildConfig.MUSICINFO_APP_VERSION),
      ContactEmail(BuildConfig.MUSICINFO_CONTACT_EMAIL),
      EalvaMusicInfo.okHttpClient,
      coroutineRule.testDispatcher,
    )
    // ensure we do not exceed rate limiting as we are creating the services each time and not
    // applying rate limiting between calls in the service itself
    Thread.sleep(500)
  }

//  @Test
//  public fun testGetWikipediaSummaryFromWikipediaUrl(): Unit = wiki {
//    getArticleSummary(wikipediaUrl)
//      .onSuccess { summary ->
//        expect(summary.displayTitle).toBe("Nirvana (band)")
//      }
//      .onFailure { fail("Wikipedia call failed ") { it } }
//  }
//
//  @Test
//  public fun testGetWikipediaSummaryFromWikidataUrl(): Unit = wiki {
//    getArticleSummary(wikidataUrl)
//      .onSuccess { summary ->
//        expect(summary.displayTitle).toBe("Nirvana (band)")
//      }
//      .onFailure { fail("Wikipedia call failed ") { it } }
//  }

  @Test
  public fun testGetWikipediaSummaryFromArticleTitle(): Unit = wiki {
    getArticleSummary(WikipediaService.ArticleTitle("Nirvana (band)"))
      .onSuccess { summary ->
        expect(summary.displayTitle).toBe("Nirvana (band)")
      }
      .onFailure { fail("Wikipedia call failed ") { it } }
  }

  @Test
  public fun testGetWikipediaSummaryFromArticleTitleWithUnderscore(): Unit = wiki {
    getArticleSummary(WikipediaService.ArticleTitle("Nirvana_(band)"))
      .onSuccess { summary ->
        expect(summary.displayTitle).toBe("Nirvana (band)")
      }
      .onFailure { fail("Wikipedia call failed ") { it } }
  }

//  @Test
//  public fun testGetWikipediaSummaryFromMalformedUrl(): Unit = wiki {
//    getArticleSummary(
//      Url(id = "", resource = "http://ealva.com/")
//    ).onSuccess {
//      fail("Wikipedia call failed ")
//    }.onFailure { msg ->
//      expect(msg).toBeInstanceOf<MusicInfoMessage.MusicInfoExceptionMessage> { exMsg ->
//        expect(exMsg.ex).toBeInstanceOf<NotWikipediaUrlException>()
//      }
//    }
//  }

  private fun wiki(block: suspend WikipediaService.() -> Unit) = coroutineRule.runBlockingTest {
    @Suppress("BlockingMethodInNonBlockingContext")
    runBlocking {
      wikipediaService.block()
    }
  }
}

private val wikipediaUrl: BrainzUrl = BrainzUrl(
  id = "",
  resource = "https://en.wikipedia.org/wiki/Nirvana_(band)"
)

private val wikidataUrl: BrainzUrl = BrainzUrl(
  id = "1221730c-3a48-49fa-8001-beaa6e93c892",
  resource = "https://www.wikidata.org/wiki/Q11649"
)
