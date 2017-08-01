package com.hiveworkshop.assetextractor;

import java.io.IOException;
import java.nio.file.Path;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;

import mpq.MPQException;
import net.wc3c.util.CharInt;
import net.wc3c.w3o.W3UFile;
import net.wc3c.w3o.W3UFile.Unit;
import net.wc3c.wts.WTSFile;

public final class MapAssetExtractor implements AutoCloseable {
	private final Path map;
	private final MpqCodebase codebase;
	private final WarcraftData unitData;
	private LoadedMPQ loadedMPQ;

	public MapAssetExtractor(final Path map) {
		this.map = map;
		codebase = MpqCodebase.get();
		if (map.toString().length() > 0) {
			try {
				loadedMPQ = codebase.loadMPQ(map);
			} catch (final MPQException e) {
				throw new IllegalArgumentException(e);
			} catch (final IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
		this.unitData = loadCustomObjectData();
	}

	private WarcraftData loadCustomObjectData() {
		// statically loads from MPQ codebase the hack injected map MPQ, this is
		// a total hack, and it's bad code
		final WarcraftData unitData = StandardObjectData.getStandardUnits();
		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
		if (map.toString().length() > 0) {
			try {
				WTSFile wts = null;
				if (codebase.has("war3map.wts")) {
					wts = new WTSFile(codebase.getFile("war3map.wts").toPath());
				}
				if (codebase.has("war3map.w3u")) {
					final W3UFile unitDataFile = wts == null ? new W3UFile(codebase.getFile("war3map.w3u").toPath())
							: new W3UFile(codebase.getFile("war3map.w3u").toPath(), wts);
					for (final Unit unit : unitDataFile.getEntries()) {
						final String unitIdString = CharInt.toString(unit.getId());
						if (unit.getParentId() != unit.getId()) {
							// custom unit
							unitData.cloneUnit(CharInt.toString(unit.getParentId()), unitIdString);
							unitData.getTable("Profile").get(unitIdString).setField("JWC3_IS_CUSTOM_UNIT", "1");
						}
						for (final Long key : unit.keySet()) {
							final Element unitMetaDataEntry = standardUnitMeta.get(CharInt.toString(key.intValue()));
							if (unitMetaDataEntry != null) {
								final String slkTableName = unitMetaDataEntry.getField("slk");
								final DataTable table = unitData.getTable(slkTableName);
								final Element unitElement = table.get(unitIdString);
								if (unitElement != null) {
									unitElement.setField(unitMetaDataEntry.getField("field"),
											unit.getProperty(key.intValue()).getValue().toString());
								}
							}
						}
					}
				}
			} catch (final IOException e) {
				// throw new IllegalArgumentException(e);
				e.printStackTrace();
			}
		}
		return unitData;
	}

	public Path getMap() {
		return map;
	}

	public MpqCodebase getCodebase() {
		return codebase;
	}

	public WarcraftData getUnitData() {
		return unitData;
	}

	public void extractObject(final String objectId, final Path destinationFolder,
			final AssetExtractorSettings settings) {
		final AssetSourceObject source = new AssetSourceObject(objectId);
		source.extract(codebase, unitData, destinationFolder, settings);
	}

	@Override
	public void close() {
		if (loadedMPQ != null) {
			loadedMPQ.unload();
		}
	}

	public LoadedMPQ getLoadedMPQ() {
		return loadedMPQ;
	}
}
