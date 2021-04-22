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

@file:Suppress("UnstableApiUsage")

package com.ealva.musicinfo.service.net

import com.ealva.ealvalog.invoke
import com.ealva.musicinfo.log._i
import com.ealva.musicinfo.log.libLogger
import engineering.clientside.throttle.Throttle
import okhttp3.Interceptor
import okhttp3.Response

private val LOG by libLogger(ThrottlingInterceptor::class)

@Suppress("MaxLineLength")
/**
 * Interceptor that throttles requests to the server
 *
 * Be sure to read
 * [MusicBrainz requirements](https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting)
 * for querying their servers.
 */
internal class ThrottlingInterceptor(
  maxCallsPerSecond: Double,
  private val serviceName: String
) : Interceptor {
  private val throttle = Throttle.create(maxCallsPerSecond)

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    // Only throttle GETs
    if ("GET" == request.method) {
      val throttleSlept = throttle.acquire()
      LOG._i { it("intercept throttle service=%s slept=%f", serviceName, throttleSlept) }
    }
    return chain.proceed(request)
  }
}
