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

package com.ealva.musicinfo.service.net

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

internal class CacheControlInterceptor(
  private val daysMaxAge: Int,
  private val daysMinFresh: Int,
  private val daysMaxStale: Int
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    var request = chain.request()

    if ("GET" == request.method) {
      request = if (networkIsAvailable()) {
        request.newBuilder()
          .cacheControl(
            CacheControl.Builder()
              .maxAge(daysMaxAge, TimeUnit.DAYS)
              .minFresh(daysMinFresh, TimeUnit.DAYS)
              .build()
          )
          .build()
      } else {
        request.newBuilder()
          .cacheControl(
            CacheControl.Builder()
              .onlyIfCached()
              .maxStale(daysMaxStale, TimeUnit.DAYS)
              .build()
          )
          .build()
      }
    }
    return chain.proceed(request)
  }
}
