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
import com.ealva.ealvabrainz.brainz.data.CoverArtImageType
import com.ealva.musicinfo.service.net.toUriOrEmpty

public data class RemoteImage(
  val location: Uri,
  val sizeBucket: SizeBucket,
  val types: Set<RemoteImageType>,
  val sourceLogoDrawableRes: Int,
  val sourceIntent: Intent,
  val actualSize: Size? = null,
) : Comparable<RemoteImage> {
  override fun compareTo(other: RemoteImage): Int {
    return location.compareTo(other.location)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RemoteImage

    if (location != other.location) return false

    return true
  }

  override fun hashCode(): Int {
    return location.hashCode()
  }

  override fun toString(): String = buildString {
    append("RemoteImageData(location:")
    append(location)
    append(", bucket:")
    append(sizeBucket)
    append(", types:")
    append(types.joinToString(prefix = "[", postfix = "]") { it.name })
    append(", actualSize:")
    append(actualSize)
    append(")")
  }

  public companion object {
    public operator fun invoke(
      url: String,
      bucket: SizeBucket,
      types: Set<RemoteImageType>,
      @DrawableRes sourceLogo: Int,
      source: Intent,
      actualSize: Size? = null
    ): RemoteImage = RemoteImage(
      url.toSecureUri(),
      bucket,
      types,
      sourceLogo,
      source,
      actualSize
    )
//
//    public operator fun invoke(
//      uri: Uri,
//      bucket: SizeBucket,
//      types: Set<RemoteImageType>,
//      @DrawableRes sourceLogo: Int,
//      source: Intent,
//      actualSize: Size? = null
//    ): RemoteImage = RemoteImage(uri, bucket, types, sourceLogo, source, actualSize)
  }
}

private const val HTTP_LENGTH = 4
internal fun String?.toSecureUri(): Uri {
  return if (this != null && startsWith("http:"))
    "https${substring(HTTP_LENGTH)}".toUri()
  else
    toUriOrEmpty()
}

/**
 * This can be used to filter images. MusicBrainz supplies a plethora of image types, but most
 * users assigning album art only care about Front and possibly Poster
 */
public sealed class RemoteImageType(public val name: String) {
  // https://musicbrainz.org/doc/Cover_Art/Types
  public object FRONT : RemoteImageType("Front")
  public object BACK : RemoteImageType("Back")
  public object BOOKLET : RemoteImageType("Booklet")
  public object MEDIUM : RemoteImageType("Medium")
  public object TRAY : RemoteImageType("Tray")
  public object OBI : RemoteImageType("Obi")
  public object SPINE : RemoteImageType("Spine")
  public object TRACK : RemoteImageType("Track")
  public object LINER : RemoteImageType("Liner")
  public object STICKER : RemoteImageType("Sticker")
  public object POSTER : RemoteImageType("Poster")
  public object WATERMARK : RemoteImageType("Watermark")
  public object OTHER : RemoteImageType("Other")
  public object UNKNOWN : RemoteImageType("UNKNOWN")
  public class Unrecognized(name: String) : RemoteImageType(name)
}

public val CoverArtImageType.asRemoteImageType: RemoteImageType
  get() = when (this) {
    CoverArtImageType.FRONT -> RemoteImageType.FRONT
    CoverArtImageType.BACK -> RemoteImageType.BACK
    CoverArtImageType.BOOKLET -> RemoteImageType.BOOKLET
    CoverArtImageType.MEDIUM -> RemoteImageType.MEDIUM
    CoverArtImageType.TRAY -> RemoteImageType.TRAY
    CoverArtImageType.OBI -> RemoteImageType.OBI
    CoverArtImageType.SPINE -> RemoteImageType.SPINE
    CoverArtImageType.TRACK -> RemoteImageType.TRACK
    CoverArtImageType.LINER -> RemoteImageType.LINER
    CoverArtImageType.STICKER -> RemoteImageType.STICKER
    CoverArtImageType.POSTER -> RemoteImageType.POSTER
    CoverArtImageType.WATERMARK -> RemoteImageType.WATERMARK
    CoverArtImageType.OTHER -> RemoteImageType.OTHER
    CoverArtImageType.UNKNOWN -> RemoteImageType.UNKNOWN
    is CoverArtImageType.Unrecognized -> RemoteImageType.Unrecognized(name)
  }
