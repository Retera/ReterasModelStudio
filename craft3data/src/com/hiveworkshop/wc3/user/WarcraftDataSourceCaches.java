package com.hiveworkshop.wc3.user;

import java.util.List;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceDescriptor;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.pkb.PopcornEffectCache;
import com.hiveworkshop.wc3.resources.Resources;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.ModelOptionPanel;
import com.hiveworkshop.wc3.units.UnitOptionPanel;

/**
 * The single place that knows every static cache derived from the game data
 * sources.
 * <p>
 * Whenever the configured data sources change (SD/HD/DE switch, a different
 * install, an added mod folder) all of these must be dropped together or the
 * program keeps showing textures, unit data and strings from the previous
 * sources. Call {@link #refresh(List)} instead of touching the caches directly;
 * if you add a new static cache that reads game data, register its drop here.
 */
public final class WarcraftDataSourceCaches {
	private WarcraftDataSourceCaches() {
	}

	/**
	 * Rebuilds the shared data source from the given descriptors and drops every
	 * cache that was populated from the previous sources.
	 * <p>
	 * GL texture handles owned by open viewports are not static and are not
	 * covered here; the caller that owns the viewports must ask them to reload.
	 */
	public static void refresh(final List<DataSourceDescriptor> dataSourceDescriptors) {
		MpqCodebase.get().refresh(dataSourceDescriptors);
		dropDerivedCaches();
	}

	/**
	 * Drops the caches without rebuilding the data source. Useful when the
	 * sources are unchanged but their contents may have (a mod folder edited on
	 * disk).
	 */
	public static void dropDerivedCaches() {
		// order matters only in that the unit browser caches sit on top of
		// DataTable and the string bundles
		UnitOptionPanel.dropRaceCache();
		DataTable.dropCache();
		ModelOptionPanel.dropCache();
		WEString.dropCache();
		Resources.dropCache();
		BLPHandler.get().dropCache();
		PopcornEffectCache.dropCache();
	}
}
