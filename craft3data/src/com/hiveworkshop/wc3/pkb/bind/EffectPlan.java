package com.hiveworkshop.wc3.pkb.bind;

/** The bound effect: its layers and the kick routing table. */
public final class EffectPlan {
	public int versionMajor;
	public int versionMinor;
	public LayerProgram[] layers = new LayerProgram[0];
	public EventRoute[] routes = new EventRoute[0];

	public boolean isKickTarget(final int layerId) {
		for (final EventRoute r : routes) {
			if ((r.targetLayer == layerId) && !r.channel.isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
