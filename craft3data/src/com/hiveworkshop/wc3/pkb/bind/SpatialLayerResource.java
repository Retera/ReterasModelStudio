package com.hiveworkshop.wc3.pkb.bind;

/** A spatial (proximity) layer a layer inserts into or queries. */
public final class SpatialLayerResource {
	public static final class Payload {
		public String name = "";
		public int payloadType;
		public int payloadFlags;
	}

	public String name = "";
	public String fullName = "";
	public float cellSize = 0.75f;
	public int flags = 1;
	public Payload[] payloads = new Payload[0];

	public String identity() {
		return fullName.isEmpty() ? name : fullName;
	}

	public static int payloadNameHash(final String name) {
		int h = (int) 2166136261L;
		for (int i = 0; i < name.length(); i++) {
			h ^= name.charAt(i) & 0xFF;
			h *= 16777619;
		}
		return h;
	}

	public int payloadNameHashById(final int payloadId) {
		if ((payloadId < 0) || (payloadId >= payloads.length)) {
			return 0;
		}
		return payloadNameHash(payloads[payloadId].name);
	}
}
