package com.hiveworkshop.wc3.pkb.bind;

/** A kick channel routed to a target layer. */
public final class EventRoute {
	public String channel = "";
	public int targetLayer;
	public int globalEventSlotId;
	public int parentLayerSlot = -1;
}
