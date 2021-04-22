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

package com.ealva.musicinfo.lastfm.data

import com.ealva.musicinfo.lastfm.data.Image.Companion.NullImage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class Image(
  public val size: String = "",
  @field:Json(name = "#text") public val text: String = ""
) {
  override fun toString(): String = toJson()

  public sealed class Size(private val name: String) {
    public object Mega : Size("mega")
    public object ExtraLarge : Size("extralarge")
    public object Large : Size("large")
    public object Medium : Size("medium")
    public object Small : Size("small")
    public class Unknown(value: String) : Size(value)

    override fun toString(): String = name

    public companion object {
      public val values: Array<Size> = arrayOf(Mega, ExtraLarge, Large, Medium, Small)
      private val nameMap: Map<String, Size> = values.associateBy { it.name }
      public operator fun get(name: String): Size = nameMap[name] ?: Unknown(name)
    }
  }

  public companion object {
    public val NullImage: Image = Image()
    public val fallbackMapping: Pair<String, Any> = Image::class.java.name to NullImage
  }
}

public inline val Image.isNullObject: Boolean
  get() = this === NullImage

public inline val Image.theSize: Image.Size
  get() = Image.Size[size]
