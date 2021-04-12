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

package com.ealva.musicinfo.service.lastfm

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Intercepts calls to add a user agent, request format as json, and use the [apiKey] for the
 * request.
 */
public class LastFmApiInterceptor(
  appName: String,
  appVersion: String,
  emailContact: String,
  private val apiKey: String
) : Interceptor {
  private val userAgent: String = """$appName/$appVersion ($emailContact)"""

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val originalUrl = originalRequest.url
    return chain.proceed(
      originalRequest
        .newBuilder()
        .header("User-Agent", userAgent)
        .url(
          originalUrl
            .newBuilder()
            .addQueryParameter("api_key", apiKey)
            .addQueryParameter("format", "json")
            .build()
        )
        .build()
    )
  }
}
