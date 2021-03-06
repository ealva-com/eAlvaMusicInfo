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

package com.ealva.musicinfo

import com.ealva.musicinfo.service.art.CompositeFinderIntegrationTest
import com.ealva.musicinfo.service.lastfm.LastFmSmokeTest
import com.ealva.musicinfo.service.spotify.SpotifySmokeTest
import com.ealva.musicinfo.service.wiki.WikiSmokeTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(
  LastFmSmokeTest::class,
  SpotifySmokeTest::class,
  WikiSmokeTest::class,
//  BrainzArtFinderIntegrationTest::class,
//  LastFmArtFinderIntegrationTest::class,
//  SpotifyArtFinderIntegrationTest::class,
  CompositeFinderIntegrationTest::class // Composite tests Brainz, LastFm, and Spotify ArtFinders
)
public class AndroidTestSuite
