package com.hiveworkshop.wc3.pkb;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.pkb.bind.EffectBinder;
import com.hiveworkshop.wc3.pkb.bind.EffectPlan;

/**
 * Loads baked effects for popcorn emitters through the model's data source
 * and remembers them by path. An emitter's path names the source
 * {@code .pkfx}; the game ships the baked {@code .pkb} next to it.
 */
public final class PopcornEffectCache {
	private static final Map<String, PkbEffectSummary> CACHE = new HashMap<>();
	private static final Map<String, EffectPlan> PLAN_CACHE = new HashMap<>();
	private static final PkbEffectSummary MISSING = null;

	private PopcornEffectCache() {
	}

	public static synchronized void dropCache() {
		CACHE.clear();
		PLAN_CACHE.clear();
	}

	/** The bound, runnable effect, or null when the effect is missing or unreadable. Plans are shared. */
	public static synchronized EffectPlan loadPlan(final DataSource dataSource, final String emitterPath) {
		final String path = bakedPath(emitterPath);
		if (path.isEmpty() || (dataSource == null)) {
			return null;
		}
		final String key = path.toLowerCase(Locale.US);
		if (PLAN_CACHE.containsKey(key)) {
			return PLAN_CACHE.get(key);
		}
		EffectPlan plan = null;
		try (InputStream stream = dataSource.getResourceAsStream(path)) {
			if (stream != null) {
				plan = EffectBinder.bind(PkbReader.read(stream));
			}
		} catch (final IOException | RuntimeException e) {
			System.err.println("Popcorn effect " + path + " could not be bound: " + e);
		}
		PLAN_CACHE.put(key, plan);
		return plan;
	}

	/** The baked path for an emitter path: {@code .pkfx} becomes {@code .pkb}, slashes become backslashes. */
	public static String bakedPath(final String emitterPath) {
		if ((emitterPath == null) || emitterPath.isEmpty()) {
			return "";
		}
		String path = emitterPath.replace('/', '\\');
		final int dot = path.lastIndexOf('.');
		if ((dot > path.lastIndexOf('\\'))) {
			path = path.substring(0, dot);
		}
		return path + ".pkb";
	}

	/** The parsed summary, or null when the effect is missing or unreadable. */
	public static synchronized PkbEffectSummary load(final DataSource dataSource, final String emitterPath) {
		final String path = bakedPath(emitterPath);
		if (path.isEmpty() || (dataSource == null)) {
			return null;
		}
		final String key = path.toLowerCase(Locale.US);
		if (CACHE.containsKey(key)) {
			return CACHE.get(key);
		}
		PkbEffectSummary summary = MISSING;
		try (InputStream stream = dataSource.getResourceAsStream(path)) {
			if (stream != null) {
				summary = PkbEffectSummary.of(PkbReader.read(stream));
			}
		} catch (final IOException | RuntimeException e) {
			System.err.println("Popcorn effect " + path + " could not be read: " + e);
		}
		CACHE.put(key, summary);
		return summary;
	}
}
