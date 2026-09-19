package com.hiveworkshop.wc3.pkb.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Uniform-grid spatial hash backing the spatial layer queries. */
public final class ProximityHash {
	public static final int MAX_PAYLOADS = 8;

	public static final class Payload {
		public int nameHash;
		public int components;
		public final float[] value = new float[4];
	}

	public static final class Entry {
		public final float[] position = new float[3];
		public final float[] payload = new float[3];
		public long sourceSelfId;
		public int insertSeq;
		public int payloadCount;
		public final Payload[] payloads = new Payload[MAX_PAYLOADS];

		public Payload findPayload(final int nameHash) {
			for (int i = 0; (i < payloadCount) && (i < MAX_PAYLOADS); i++) {
				if (payloads[i].nameHash == nameHash) {
					return payloads[i];
				}
			}
			return null;
		}
	}

	public interface Visitor {
		void visit(Entry entry, float distanceSquared);
	}

	private final Map<Long, List<Entry>> cells = new HashMap<>();
	private float cellSize = 0.75f;
	private int entryCount;

	public ProximityHash(final float cellSize) {
		setCellSize(cellSize);
	}

	public void setCellSize(final float size) {
		cellSize = size > 0 ? size : 0.75f;
	}

	public int entryCount() {
		return entryCount;
	}

	private static boolean isFinite(final float v) {
		return ((Float.floatToRawIntBits(v) >>> 23) & 0xFF) != 0xFF;
	}

	private static int floorI32(final float v) {
		final int i = (int) v;
		return ((v < 0) && (i != v)) ? (i - 1) : i;
	}

	private static long key(final int x, final int y, final int z) {
		return ((x & 0x1FFFFFL) << 42) | ((y & 0x1FFFFFL) << 21) | (z & 0x1FFFFFL);
	}

	public boolean insert(final float[] position, final float[] payload, final long sourceSelfId,
			final Payload[] named, final int namedCount) {
		if (!isFinite(position[0]) || !isFinite(position[1]) || !isFinite(position[2])) {
			return false;
		}
		final float inv = 1.0f / cellSize;
		final long k = key(floorI32(position[0] * inv), floorI32(position[1] * inv), floorI32(position[2] * inv));
		final Entry e = new Entry();
		System.arraycopy(position, 0, e.position, 0, 3);
		System.arraycopy(payload, 0, e.payload, 0, 3);
		e.sourceSelfId = sourceSelfId;
		final int n = Math.min(namedCount, MAX_PAYLOADS);
		for (int i = 0; i < n; i++) {
			e.payloads[i] = named[i];
		}
		e.payloadCount = n;
		e.insertSeq = entryCount;
		cells.computeIfAbsent(k, kk -> new ArrayList<>()).add(e);
		entryCount++;
		return true;
	}

	public void clear() {
		cells.clear();
		entryCount = 0;
	}

	public void forEachInRadius(final float[] target, final float radius, final Visitor fn) {
		if ((radius <= 0) || cells.isEmpty()) {
			return;
		}
		final float rSq = radius * radius;
		final float inv = 1.0f / cellSize;
		final int cxMin = floorI32((target[0] - radius) * inv);
		final int cxMax = floorI32((target[0] + radius) * inv);
		final int cyMin = floorI32((target[1] - radius) * inv);
		final int cyMax = floorI32((target[1] + radius) * inv);
		final int czMin = floorI32((target[2] - radius) * inv);
		final int czMax = floorI32((target[2] + radius) * inv);
		final long span = (long) (cxMax - cxMin + 1) * (cyMax - cyMin + 1) * (czMax - czMin + 1);
		if (span > (64L * 64L * 64L)) {
			for (final List<Entry> cell : cells.values()) {
				consider(cell, target, rSq, fn);
			}
			return;
		}
		for (int x = cxMin; x <= cxMax; x++) {
			for (int y = cyMin; y <= cyMax; y++) {
				for (int z = czMin; z <= czMax; z++) {
					final List<Entry> cell = cells.get(key(x, y, z));
					if (cell != null) {
						consider(cell, target, rSq, fn);
					}
				}
			}
		}
	}

	private static void consider(final List<Entry> cell, final float[] target, final float rSq, final Visitor fn) {
		for (final Entry e : cell) {
			final float dx = e.position[0] - target[0];
			final float dy = e.position[1] - target[1];
			final float dz = e.position[2] - target[2];
			final float dSq = (dx * dx) + (dy * dy) + (dz * dz);
			if (dSq <= rSq) {
				fn.visit(e, dSq);
			}
		}
	}

	/** The n-th closest entry within the radius (0 = closest), or null. */
	public Entry closestN(final float[] target, final float radius, final int n) {
		final List<Entry> hits = new ArrayList<>();
		final List<Float> dists = new ArrayList<>();
		forEachInRadius(target, radius, (e, dSq) -> {
			hits.add(e);
			dists.add(dSq);
		});
		if (hits.isEmpty() || (n >= hits.size())) {
			return null;
		}
		final Integer[] order = new Integer[hits.size()];
		for (int i = 0; i < order.length; i++) {
			order[i] = i;
		}
		java.util.Arrays.sort(order, (a, b) -> Float.compare(dists.get(a), dists.get(b)));
		return hits.get(order[n]);
	}

	public int neighborCount(final float[] target, final float radius) {
		final int[] count = { 0 };
		forEachInRadius(target, radius, (e, d) -> count[0]++);
		return count[0];
	}
}
