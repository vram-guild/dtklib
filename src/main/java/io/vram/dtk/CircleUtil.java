/*
 * This file is part of Don't Tell Knuth (DTKLIB) and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.vram.dtk;

import java.util.Comparator;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class CircleUtil {
	/**
	 * See {@link #getDistanceSortedCircularOffset(int)}. If you simply want to
	 * iterate all, can simply use this directly.
	 */
	public static final Offset[] DISTANCE_SORTED_CIRCULAR_OFFSETS;

	public static final int DISTANCE_SORTED_CIRCULAR_OFFSETS_MAX_RADIUS = 64;

	public static final int DISTANCE_SORTED_CIRCULAR_OFFSETS_COUNT;

	public static record Offset(int x, int y, int dist) { }

	static {
		// need to use a hash bc fill2dCircleInPlaneXZ does not guarantee uniqueness.
		final ObjectOpenHashSet<Offset> offsets = new ObjectOpenHashSet<>();

		for (final long p : fill2dCircleInPlaneXZ(DISTANCE_SORTED_CIRCULAR_OFFSETS_MAX_RADIUS)) {
			final int x = PackedPoint2i.getX(p);
			final int y = PackedPoint2i.getY(p);
			offsets.add(new Offset(x, y, (int) Math.sqrt(x * x + y * y)));
		}

		final ObjectArrayList<Offset> offsetList = new ObjectArrayList<>(offsets);

		offsetList.sort(new Comparator<Offset>() {
			@Override
			public int compare(Offset o1, Offset o2) {
				return Integer.compare(o1.dist, o2.dist);
			}
		});

		DISTANCE_SORTED_CIRCULAR_OFFSETS_COUNT = offsetList.size();
		DISTANCE_SORTED_CIRCULAR_OFFSETS = offsetList.toArray(new Offset[DISTANCE_SORTED_CIRCULAR_OFFSETS_COUNT]);
	}

	/**
	 * Returns values in a sequence of horizontal offsets from X=0, Z=0.<br>
	 * Y value is the euclidian distance from the origin.<br>
	 * Values are sorted by distance from 0,0,0. Value at index 0 is the origin.<br>
	 * Distance is up to 64 blocks from origin. Values outside that range throw
	 * exceptions.<br>
	 */
	public static Offset getDistanceSortedCircularOffset(int index) {
		return DISTANCE_SORTED_CIRCULAR_OFFSETS[index];
	}

	/**
	 * Returns the last (exclusive) offset index of
	 * {@value #DISTANCE_SORTED_CIRCULAR_OFFSETS} (also the index for
	 * {@link #getDistanceSortedCircularOffset(int)} that is at the given radius
	 * from the origin.
	 */
	public static int getLastDistanceSortedOffsetIndex(int radius) {
		if (radius < 0) {
			radius = 0;
		}

		int result = 0;

		while (++result < DISTANCE_SORTED_CIRCULAR_OFFSETS.length) {
			if (DISTANCE_SORTED_CIRCULAR_OFFSETS[result].dist > radius) {
				return result;
			}
		}

		return result;
	}

	/**
	 * Returns a list of packed block position x & z OFFSETS within the given
	 * radius. Origin will be the start position.
	 */
	private static LongArrayList fill2dCircleInPlaneXZ(int radius) {
		final LongArrayList result = new LongArrayList((int) (2 * radius * 3.2));

		// uses midpoint circle algorithm
		if (radius > 0) {
			int x = radius;
			int z = 0;
			int err = 0;

			result.add(PackedPoint2i.pack(0, 0));

			while (x >= z) {
				if (z > 0) {
					result.add(PackedPoint2i.pack(z, z));
					result.add(PackedPoint2i.pack(-z, z));
					result.add(PackedPoint2i.pack(z, -z));
					result.add(PackedPoint2i.pack(-z, -z));
				}

				for (int i = x; i > z; i--) {
					result.add(PackedPoint2i.pack(i, z));
					result.add(PackedPoint2i.pack(z, i));
					result.add(PackedPoint2i.pack(-i, z));
					result.add(PackedPoint2i.pack(-z, i));
					result.add(PackedPoint2i.pack(i, -z));
					result.add(PackedPoint2i.pack(z, -i));
					result.add(PackedPoint2i.pack(-i, -z));
					result.add(PackedPoint2i.pack(-z, -i));
				}

				if (err <= 0) {
					z += 1;
					err += 2 * z + 1;
				}

				if (err > 0) {
					x -= 1;
					err -= 2 * x + 1;
				}
			}
		}

		return result;
	}
}
