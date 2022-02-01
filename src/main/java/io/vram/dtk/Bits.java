package io.vram.dtk;

public class Bits {
	/**
	 * Bit length needed to contain the given value. Intended for unsigned values.
	 */
	public static int bitLength(long maxValue) {
		return Long.SIZE - Long.numberOfLeadingZeros(maxValue - 1);
	}

	/**
	 * Bit length needed to contain the given value. Intended for unsigned values.
	 */
	public static int bitLength(int maxValue) {
		if (maxValue == 0) {
			return 0;
		}

		return Integer.SIZE - Integer.numberOfLeadingZeros(maxValue - 1);
	}

	/**
	 * Returns bit mask for a value of given bit length.
	 */
	public static long longBitMask(int bitLength) {
		bitLength = bitLength < 0 ? 0 : bitLength > Long.SIZE ? Long.SIZE : bitLength;

		// note: can't use mask = (1L << (bitLength+1)) - 1 here due to overflow &
		// signed values
		long mask = 0L;

		for (int i = 0; i < bitLength; i++) {
			mask |= (1L << i);
		}

		return mask;
	}

	/**
	 * Gives low bits of long value for serialization as two int values<br>
	 * . Use {@link #longFromInts(int, int)} to reconstruct.
	 */
	public static final int longToIntLow(final long input) {
		return (int) input;
	}

	/**
	 * Gives high bits of long value for serialization as two int values<br>
	 * . Use {@link #longFromInts(int, int)} to reconstruct.
	 */
	public static final int longToIntHigh(final long input) {
		return (int) (input >> 32);
	}

	/**
	 * Reconstitutes long value from integer values given by
	 * {@link #longToIntLow(long)} and {@link #longToIntHigh(long)}.
	 */
	public static final long longFromInts(final int high, final int low) {
		return (long) high << 32 | (low & 0xFFFFFFFFL);
	}

	/**
	 * Returns bit mask for a value of given bit length.
	 */
	public static int intBitMask(int bitLength) {
		bitLength = bitLength < 0 ? 0 : bitLength > Integer.SIZE ? Integer.SIZE : bitLength;

		// note: can't use mask = (1L << (bitLength+1)) - 1 here due to overflow &
		// signed values
		int mask = 0;

		for (int i = 0; i < bitLength; i++) {
			mask |= (1L << i);
		}

		return mask;
	}
}
