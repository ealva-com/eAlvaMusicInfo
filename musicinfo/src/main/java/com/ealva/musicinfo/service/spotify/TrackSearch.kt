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

import com.ealva.ealvabrainz.brainz.data.EntitySearchField
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.RecordingTitle
import com.ealva.ealvabrainz.common.TrackTitle
import com.ealva.ealvabrainz.common.Year
import com.ealva.ealvabrainz.lucene.InclusiveRange
import com.ealva.ealvabrainz.lucene.Term
import com.ealva.ealvabrainz.search.BaseSearch

public class TrackSearch : BaseSearch<TrackSearch.SearchField>() {
  /** (part of) the album's title */
  public fun album(title: AlbumTitle) {
    add(SearchField.Album, Term(title))
  }

  public fun album(title: String) {
    add(SearchField.Album, Term(title))
  }

  /** (part of) the artist's name */
  public fun artist(name: ArtistName) {
    add(SearchField.Artist, Term(name))
  }

  public fun artist(name: String) {
    add(SearchField.Artist, Term(name))
  }

  /**
   * Searches against track name
   */
  public fun default(name: String) {
    add(SearchField.Default, Term(name))
  }

  public fun genre(genre: String) {
    add(SearchField.Genre, Term(genre))
  }

  public fun track(track: TrackTitle) {
    add(SearchField.Default, Term(track.value))
  }

  public fun track(track: RecordingTitle) {
    add(SearchField.Default, Term(track.value))
  }

  public fun year(date: Year) {
    add(SearchField.Year, Term(date))
  }

  public fun year(from: Year, to: Year) {
    add(
      SearchField.Year,
      InclusiveRange(Term(from), Term(to), prefix = "", separator = "-", suffix = "")
    )
  }

  public enum class SearchField(public override val value: String) : EntitySearchField {
    Album("album"),
    Artist("artist"),

    /** Default matches against album and artist */
    Default(""),
    Genre("genre"),
    Track("track"),
    Year("year")
  }

  public companion object {
    public inline operator fun invoke(search: TrackSearch.() -> Unit): String {
      return TrackSearch().apply(search).toString()
    }
  }
}
