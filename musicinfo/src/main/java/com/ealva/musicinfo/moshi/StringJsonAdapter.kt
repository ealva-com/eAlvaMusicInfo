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

package com.ealva.musicinfo.moshi

import com.ealva.ealvalog.e
import com.ealva.ealvalog.invoke
import com.ealva.musicinfo.log.libLogger
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

private val LOG by libLogger(StringJsonAdapter::class)

internal class StringJsonAdapter {
  @FromJson
  fun fromJson(reader: JsonReader): String {
    return when (val token = reader.peek()) {
      JsonReader.Token.NULL -> "".also { reader.nextNull<String>() }
      JsonReader.Token.STRING -> reader.nextString()
      else -> "".also {
        LOG.e { it("Unrecognized token ${token.name}, expecting null or string", token.name) }
      }
    }
  }
}
