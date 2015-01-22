/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msiproxy.common.repo;

/**
 * Defines the valid icon sizes
 */
public enum IconSize {
    SIZE_32(32),
    SIZE_64(64),
    SIZE_128(128);

    int size;
    IconSize(int size) {
        this.size = size;
    }
    public int getSize() { return size; }

    public static IconSize getIconSize(int size) {
        switch (size) {
            case 32:  return SIZE_32;
            case 128: return SIZE_128;
            default:  return SIZE_64;
        }
    }
}
