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

import com.ealva.musicinfo.R
import com.ealva.musicinfo.service.init.EalvaMusicInfo

public sealed class SizeBucket(
  public val maybeVeryLarge: Boolean,
  private val stringRes: Int,
  private val size: Int
) : Comparable<SizeBucket> {
  public object Original : SizeBucket(true, R.string.Original, 3)
  public object ExtraLarge : SizeBucket(true, R.string.Extra_Large, 4)
  public object Large : SizeBucket(false, R.string.Large, 3)
  public object Medium : SizeBucket(false, R.string.Medium, 2)
  public object Small : SizeBucket(false, R.string.Small, 1)
  public object Unknown : SizeBucket(false, R.string.Unknown, 0)

  override fun compareTo(other: SizeBucket): Int = size.compareTo(other.size)

  override fun toString(): String = EalvaMusicInfo.fetch(stringRes)
}

