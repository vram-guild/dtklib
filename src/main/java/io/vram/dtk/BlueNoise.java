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

import java.util.BitSet;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

import io.vram.dtk.CircleUtil.Offset;

/**
 * Generates tileable 2d blue noise with integer coordinates using Bridson
 * algorithm for Poisson disk sampling. Representation is sparse so can hold a
 * fairly large tile without too much memory usage but generation time will be
 * correspondingly longer. (Relative to size ^ 2)
 */
public class BlueNoise {
	private final IntArraySet points = new IntArraySet();
	public final int size;
	public final int minSpacing;

	public static BlueNoise create(int size, int minSpacing, long seed) {
		return new BlueNoise(size, minSpacing, seed);
	}

	private BlueNoise(int size, int minSpacing, long seed) {
		this.size = size;
		this.minSpacing = minSpacing;
		generate(seed);
	}

	private class Point {
		private final int x;
		private final int y;

		private Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		private Point(int index) {
			x = xFromIndex(index);
			y = yFromIndex(index);
		}

		private int index() {
			return indexOf(x, y);
		}
	}

	private void generate(long seed) {
		final Random r = new Random(seed);

		final IntArrayList active = new IntArrayList();

		final BitSet points = new BitSet(size * size);

		{
			final int first = indexOf(r.nextInt(), r.nextInt());
			active.add(first);
			points.set(first);
		}

		while (!active.isEmpty()) {
			final Point subject = new Point(active.removeInt(r.nextInt(active.size())));

			for (int i = 0; i < 60; i++) {
				final Point trial = generatePointAround(subject, r);

				if (pointIsValid(trial, points)) {
					active.add(subject.index());
					final int trialIndex = trial.index();
					active.add(trialIndex);
					points.set(trialIndex);
					break;
				}
			}
		}

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				final int index = indexOf(x, y);

				if (points.get(index)) {
					this.points.add(index);
				}
			}
		}
	}

	private boolean pointIsValid(Point p, BitSet points) {
		if (minSpacing <= 64) {
			// faster to use pre-computed offsets if feasible
			for (final Offset vec : CircleUtil.DISTANCE_SORTED_CIRCULAR_OFFSETS) {
				if (vec.dist() > minSpacing) {
					break;
				}

				if (points.get(indexOf(p.x + vec.x(), p.y + vec.y()))) {
					return false;
				}
			}
		} else {
			for (int j = -minSpacing; j <= minSpacing; j++) {
				for (int k = -minSpacing; k <= minSpacing; k++) {
					if (points.get(indexOf(p.x + j, p.y + k)) && Math.sqrt(j * j + k * k) <= minSpacing) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private Point generatePointAround(Point p, Random r) {
		final int radius = minSpacing + r.nextInt(minSpacing + 1);
		final double angle = 2 * Math.PI * r.nextDouble();
		final int newX = (int) Math.round(p.x + radius * Math.cos(angle));
		final int newY = (int) Math.round(p.y + radius * Math.sin(angle));
		return new Point(newX, newY);
	}

	/**
	 * Coordinates are wrapped to size of noise.
	 */
	public boolean isSet(int x, int y) {
		return points.contains(indexOf(x, y));
	}

	//    public void set(int x, int y)
	//    {
	//        this.points.add(indexOf(x, y));
	//    }
	//
	//    public void clear(int x, int y)
	//    {
	//        this.points.rem(indexOf(x, y));
	//    }

	private int xFromIndex(int index) {
		return index & 0xFFFF;
	}

	private int yFromIndex(int index) {
		return index >> 16;
	}

	private int indexOf(int x, int y) {
		x = (x % size + size) % size;
		y = (y % size + size) % size;
		return (y << 16) | x;
	}
}
