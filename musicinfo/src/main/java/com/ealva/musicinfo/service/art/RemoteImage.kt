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

package com.ealva.musicinfo.service.art

import android.content.Intent
import android.net.Uri
import android.util.Size
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import com.ealva.musicinfo.service.net.toUriOrEmpty

public interface RemoteImage : Comparable<RemoteImage> {
  public val location: Uri
  public val sizeBucket: SizeBucket
  public val sourceLogoDrawableRes: Int
  public val sourceIntent: Intent
  public val actualSize: Size?

  public companion object {
    public operator fun invoke(
      url: String,
      bucket: SizeBucket,
      @DrawableRes sourceLogo: Int,
      source: Intent,
      actualSize: Size? = null
    ): RemoteImage = RemoteImageData(url.toSecureUri(), bucket, sourceLogo, source, actualSize)

    public operator fun invoke(
      url: Uri,
      bucket: SizeBucket,
      @DrawableRes sourceLogo: Int,
      source: Intent,
      actualSize: Size? = null
    ): RemoteImage = RemoteImageData(url, bucket, sourceLogo, source, actualSize)
  }
}

private const val HTTP_LENGTH = 4
private fun String?.toSecureUri(): Uri {
  return if (this != null && startsWith("http:"))
    "https${substring(HTTP_LENGTH)}".toUri()
  else
    toUriOrEmpty()
}

/**
 * Equality/hashCode/compareTo is determined solely by [location]
 */
private class RemoteImageData(
  override val location: Uri,
  override val sizeBucket: SizeBucket,
  override val sourceLogoDrawableRes: Int,
  override val sourceIntent: Intent,
  override val actualSize: Size? = null
) : RemoteImage {
  override fun compareTo(other: RemoteImage): Int {
    return location.compareTo(other.location)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RemoteImageData

    if (location != other.location) return false

    return true
  }

  override fun hashCode(): Int {
    return location.hashCode()
  }
}
