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

package com.ealva.musicinfo.service.wiki

import com.ealva.musicinfo.service.common.AppName
import com.ealva.musicinfo.service.common.AppVersion
import com.ealva.musicinfo.service.common.ContactEmail
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Intercepts calls to add a user agent, request format as json
 */
public class WikipediaApiInterceptor(
  appName: AppName,
  appVersion: AppVersion,
  contactEmail: ContactEmail
) : Interceptor {
  private val userAgent: String = """${appName.value}/${appVersion.value} (${contactEmail.value})"""

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
//    val originalUrl = originalRequest.url
    return chain.proceed(
      originalRequest
        .newBuilder()
        .header("User-Agent", userAgent)
//        .url(
//          originalUrl
//            .newBuilder()
//            .addQueryParameter("format", "json")
//            .build()
//        )
        .build()
    )
  }
}
