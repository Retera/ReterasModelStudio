package com.hiveworkshop.wc3.pkb.bind;

/** Payload element declarations of kicked events and of the layer's own (root) event. */
public final class EventPayloadDecl {
	public static final class Element {
		public int nameId;
		public int width;
		/** CLayerCompileCacheEventPayload PayloadFlags, kept for diagnostics. */
		public int flags;
		/** CLayerCompileCacheEventPayload PayloadKind (1 position, 2 orientation, 3 count). */
		public int kind;
	}

	/** A kicked event: channel name and payload layout. */
	public static final class Kicked {
		public String channel = "";
		public Element[] elements = new Element[0];
	}

	private EventPayloadDecl() {
	}

	/** FNV-1a-32 of the payload name, the id the VM uses to address payload elements. */
	public static int payloadNameId(final String name) {
		int h = 0x811C9DC5;
		for (int i = 0; i < name.length(); i++) {
			h ^= name.charAt(i) & 0xFF;
			h *= 0x01000193;
		}
		return h;
	}
}
