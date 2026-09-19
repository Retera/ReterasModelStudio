package com.hiveworkshop.wc3.pkb.sim;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hiveworkshop.wc3.pkb.bind.LayerProgram;

/** Collects VM failures once per distinct message so a broken effect does not flood the log. */
public final class SimIssues {
	private final Set<String> seen = new LinkedHashSet<>();
	private boolean verbose = true;

	public void setVerbose(final boolean verbose) {
		this.verbose = verbose;
	}

	public void report(final LayerProgram layer, final String message) {
		final String key = "L" + (layer == null ? "?" : Integer.toString(layer.id)) + ": " + message;
		if (seen.add(key) && verbose) {
			System.err.println("[pkb sim] " + key);
		}
	}

	public Set<String> messages() {
		return seen;
	}
}
