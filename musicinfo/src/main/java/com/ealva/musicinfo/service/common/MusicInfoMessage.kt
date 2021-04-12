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

package com.ealva.musicinfo.service.common

import com.ealva.brainzsvc.service.R
import com.ealva.ealvalog.e
import com.ealva.ealvalog.invoke
import com.ealva.ealvalog.lazyLogger
import com.ealva.musicinfo.service.net.MusicInfoRawResponse
import com.ealva.musicinfo.service.net.toMusicInfoRawResponse
import retrofit2.Response
import java.io.PrintWriter
import java.io.StringWriter

private val LOG by lazyLogger(MusicInfoMessage::class)

public sealed class MusicInfoMessage {
  public abstract fun asString(fetcher: StringFetcher): String

  @Suppress("MemberVisibilityCanBePrivate")
  public class MusicInfoLastFmMessage(
    public val statusCode: Int,
    public val message: String
  ) : MusicInfoMessage() {
    override fun asString(fetcher: StringFetcher): String {
      return "Service error:$statusCode. $message"
    }
  }

  @Suppress("MemberVisibilityCanBePrivate")
  public sealed class MusicInfoStatusMessage(public val statusCode: Int) : MusicInfoMessage() {
    override fun asString(fetcher: StringFetcher): String =
      fetcher.fetch(R.string.ResultStatusCode, statusCode)

    public class MusicInfoNullReturn(statusCode: Int) : MusicInfoStatusMessage(statusCode) {
      init {
        LOG.e { it("Null return status code=%d", statusCode) }
      }
    }

    public class MusicInfoErrorCodeMessage(
      statusCode: Int,
      private val response: Response<*>
    ) : MusicInfoStatusMessage(statusCode) {
      init {
        LOG.e { it("Error code message. Status code=%d", statusCode) }
      }

      @Suppress("unused")
      public val rawResponse: MusicInfoRawResponse
        get() = response.toMusicInfoRawResponse()
    }
  }

  @Suppress("MemberVisibilityCanBePrivate")
  public class MusicInfoExceptionMessage(public val ex: Throwable) : MusicInfoMessage() {
    override fun asString(fetcher: StringFetcher): String = ex.message ?: ex.toString()

    @Suppress("unused")
    public fun stackTraceToString(throwable: Throwable): String {
      return StringWriter().apply {
        PrintWriter(this).also { pw ->
          throwable.printStackTrace(pw)
        }
      }.toString()
    }
  }
}
