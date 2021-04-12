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

package com.ealva.musicinfo.lastfm.data;

import com.squareup.moshi.FromJson;

@SuppressWarnings("unused")
public class NullPrimitiveAdapter {
  @FromJson
  public int intFromJson(@Nullable Integer value) {
    if (value == null) {
      return 0;
    }
    //noinspection AutoUnboxing
    return value;
  }

  @FromJson
  public long longFromJson(@Nullable Long value) {
    if (value == null) {
      return 0;
    }
    //noinspection AutoUnboxing
    return value;
  }

  @FromJson
  public boolean booleanFromJson(@Nullable Boolean value) {
    if (value == null) {
      return false;
    }
    //noinspection AutoUnboxing
    return value;
  }

  @FromJson
  public double doubleFromJson(@Nullable Double value) {
    if (value == null) {
      return 0;
    }
    //noinspection AutoUnboxing
    return value;
  }

  @FromJson
  public float floatFromJson(@Nullable Float value) {
    if (value == null) {
      return 0;
    }
    //noinspection AutoUnboxing
    return value;
  }

}
