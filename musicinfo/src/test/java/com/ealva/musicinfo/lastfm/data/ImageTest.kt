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

package com.ealva.musicinfo.lastfm.data

import com.nhaarman.expect.expect
import org.junit.Test

public class ImageTest {
  @Test
  public fun testImageSizeMap() {
    expect(Image.Size["mega"]).toBe(Image.Size.Mega)
    expect(Image.Size["extralarge"]).toBe(Image.Size.ExtraLarge)
    expect(Image.Size["large"]).toBe(Image.Size.Large)
    expect(Image.Size["medium"]).toBe(Image.Size.Medium)
    expect(Image.Size["small"]).toBe(Image.Size.Small)
    expect(Image.Size["special"]).toBeInstanceOf<Image.Size.Unknown> { size ->
      expect(size.toString()).toBe("special")
    }
  }
}
