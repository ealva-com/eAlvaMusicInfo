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

package com.ealva.musicinfo.wiki

import com.ealva.musicinfo.wiki.data.WikiDataEntities
import com.ealva.musicinfo.wiki.data.WikiSummary
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface Wikipedia {
  /**
   * Look up the article summary on Wikipedia using [title], which is the last path segment
   * of a Wikipedia URL. To get the summary of the article on the "Mangrove pitta" bird species,
   * "Mangrove_pitta" would be used as the full URL is https://en.wikipedia.org/wiki/Mangrove_pitta
   *
   * Currently only English (en) is supported.
   *
   * [Nirvana example](https://en.wikipedia.org/api/rest_v1/page/summary/Nirvana_(band))
   */
  @GET("https://en.wikipedia.org/api/rest_v1/page/summary/{title}")
  public suspend fun getWikipediaSummary(@Path("title") title: String): Response<WikiSummary>

  /**
   * Get the Wikidata entities with site links for wikibase item [id]. For our use [id] is typically
   * a single entity ID. For getting the entity for the rock band Nirvana, [id] would be "Q11649".
   * Multiple IDs may be specified and must be separated by the pipe '|' character.
   *
   * Each entity will be in the [WikiDataEntities.entities] map with the key being the
   * wikibase item ID.
   *
   * [Nirvana example](https://www.wikidata.org/w/api.php?action=wbgetentities&format=xml&props=sitelinks/urls&format=json&ids=Q11649)
   */
  @GET("https://www.wikidata.org/w/api.php?action=wbgetentities&format=xml&props=sitelinks/urls&format=json")
  public suspend fun getSiteLinks(@Query("ids") id: String): Response<WikiDataEntities>
}
