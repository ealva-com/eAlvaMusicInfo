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

package com.ealva.musicinfo.wiki.data

import com.ealva.musicinfo.test.shared.expect
import com.nhaarman.expect.expect
import com.nhaarman.expect.fail
import com.squareup.moshi.Moshi
import org.junit.Before
import org.junit.Test

public class WikiDataEntitiesTest {
  private lateinit var moshi: Moshi

  @Before
  public fun setup() {
    moshi = theWikiMoshi
  }

  /**
   * Obviously non-exhaustive - test that various areas of a Release are parsed as expected.
   */
  @Test
  public fun `test parse WikiDataEntities`() {
    moshi.adapter(WikiDataEntities::class.java).fromJson(entitiesJson)?.run {
      expect(entities).toNotBeEmpty { "entities is empty" }
      expect(entities).toHaveSize(1)
      val dataEntity = entities.asSequence()
        .map { (_, dataEntity) -> dataEntity }
        .firstOrNull()
      val siteLinks = dataEntity?.sitelinks ?: fail("WikiDataEntity is null")
      expect(siteLinks).toNotBeEmpty { "siteLinks is empty" }
      expect(siteLinks).toContainKey("enwiki")
    } ?: fail("WikiDataEntities is null")
  }
}

private const val entitiesJson =
  """
{
  "entities": {
    "Q11649": {
      "type": "item",
      "id": "Q11649",
      "sitelinks": {
        "afwiki": {
          "site": "afwiki",
          "title": "Nirvana (rock-groep)",
          "badges": [],
          "url": "https://af.wikipedia.org/wiki/Nirvana_(rock-groep)"
        },
        "angwiki": {
          "site": "angwiki",
          "title": "Nirvana",
          "badges": [],
          "url": "https://ang.wikipedia.org/wiki/Nirvana"
        },
        "enwiki": {
          "site": "enwiki",
          "title": "Nirvana (band)",
          "badges": [
            "Q17437796"
          ],
          "url": "https://en.wikipedia.org/wiki/Nirvana_(band)"
        },
        "enwikiquote": {
          "site": "enwikiquote",
          "title": "Nirvana (band)",
          "badges": [],
          "url": "https://en.wikiquote.org/wiki/Nirvana_(band)"
        },
        "zhwiki": {
          "site": "zhwiki",
          "title": "涅槃乐队",
          "badges": [],
          "url": "https://zh.wikipedia.org/wiki/%E6%B6%85%E6%A7%83%E4%B9%90%E9%98%9F"
        }
      }
    }
  },
  "success": 1
}"""
