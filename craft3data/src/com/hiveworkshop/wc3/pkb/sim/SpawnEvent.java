package com.hiveworkshop.wc3.pkb.sim;

import java.util.ArrayList;
import java.util.List;

/** A particle birth request kicked by a parent, routed to the target layer's queue. */
public final class SpawnEvent {
	public static final int MAX_PAYLOAD_FLOAT_SLOTS = 16;

	public static final class PayloadFloatSlot {
		public int nameId;
		public boolean valid;
		public int width;
		public final float[] value = new float[4];

		public void set(final PayloadFloatSlot o) {
			nameId = o.nameId;
			valid = o.valid;
			width = o.width;
			System.arraycopy(o.value, 0, value, 0, 4);
		}

		public void clear() {
			nameId = 0;
			valid = false;
			width = 0;
			value[0] = value[1] = value[2] = value[3] = 0;
		}
	}

	public static PayloadFloatSlot[] newFloatSlots() {
		final PayloadFloatSlot[] slots = new PayloadFloatSlot[MAX_PAYLOAD_FLOAT_SLOTS];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = new PayloadFloatSlot();
		}
		return slots;
	}

	public static void copyFloatSlots(final PayloadFloatSlot[] src, final PayloadFloatSlot[] dst) {
		for (int i = 0; i < MAX_PAYLOAD_FLOAT_SLOTS; i++) {
			dst[i].set(src[i]);
		}
	}

	public int eventId;
	public int sequenceIndex;
	public long parentSelfId;
	public int parentRngState;
	public boolean hasSpawnPosition;
	public final float[] spawnPosition = new float[3];
	public int spawnPositionPayloadId;
	public boolean hasSpawnOrientation;
	public final float[] spawnOrientation = { 0, 0, 0, 1 };
	public int spawnOrientationPayloadId;
	public boolean hasIntPayload;
	public int intPayloadWidth;
	public final int[] intPayload = new int[4];
	public int intPayloadId;
	public boolean hasBoolPayload;
	public int boolPayloadWidth;
	public final int[] boolPayload = new int[4];
	public int boolPayloadId;
	public final PayloadFloatSlot[] floatSlots = newFloatSlots();
	public float subFrameFraction = 1;
	public float lerpedTime;

	public SpawnEvent copy() {
		final SpawnEvent e = new SpawnEvent();
		e.eventId = eventId;
		e.sequenceIndex = sequenceIndex;
		e.parentSelfId = parentSelfId;
		e.parentRngState = parentRngState;
		e.hasSpawnPosition = hasSpawnPosition;
		System.arraycopy(spawnPosition, 0, e.spawnPosition, 0, 3);
		e.spawnPositionPayloadId = spawnPositionPayloadId;
		e.hasSpawnOrientation = hasSpawnOrientation;
		System.arraycopy(spawnOrientation, 0, e.spawnOrientation, 0, 4);
		e.spawnOrientationPayloadId = spawnOrientationPayloadId;
		e.hasIntPayload = hasIntPayload;
		e.intPayloadWidth = intPayloadWidth;
		System.arraycopy(intPayload, 0, e.intPayload, 0, 4);
		e.intPayloadId = intPayloadId;
		e.hasBoolPayload = hasBoolPayload;
		e.boolPayloadWidth = boolPayloadWidth;
		System.arraycopy(boolPayload, 0, e.boolPayload, 0, 4);
		e.boolPayloadId = boolPayloadId;
		copyFloatSlots(floatSlots, e.floatSlots);
		e.subFrameFraction = subFrameFraction;
		e.lerpedTime = lerpedTime;
		return e;
	}

	/** Per-layer queue of pending births. */
	public static final class Queue {
		public final List<SpawnEvent> events = new ArrayList<>();
		public int capacity;
		public int dropped;

		public void clear() {
			events.clear();
			dropped = 0;
		}
	}
}
