/*
 * Copyright (c) 2022  Eric A. Snell
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

/**
 * Base class for MusicInfo exceptions and also used to map exceptions from lower layers
 */
public open class MusicInfoException internal constructor(
  message: String,
  cause: Throwable?
) : RuntimeException(message, cause)

public class MusicInfoNotFoundException(
  message: String,
  cause: Throwable?
) : MusicInfoException(message, cause)
