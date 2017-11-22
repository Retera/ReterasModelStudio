package com.hiveworkshop.scripts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.etheller.collections.List;
import com.etheller.collections.MapView;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.units.objectdata.Change;
import com.hiveworkshop.wc3.units.objectdata.ObjectDataChangeEntry;
import com.hiveworkshop.wc3.units.objectdata.ObjectMap;
import com.hiveworkshop.wc3.units.objectdata.WTS;
import com.hiveworkshop.wc3.units.objectdata.WTSFile;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;
import mpq.MPQException;

public class ObjDataLoaderTest {

	public static void main(final String[] args) {
		try {
			final MpqCodebase mpqCodebase = MpqCodebase.get();
			final LoadedMPQ loadMPQ = mpqCodebase.loadMPQ(Paths.get("input/Demons and Wizards.w3x"));
			final InputStream resourceAsStream1 = mpqCodebase.getResourceAsStream("war3map.w3u");
			Files.copy(resourceAsStream1, Paths.get("output/war3map_orig.w3a"), StandardCopyOption.REPLACE_EXISTING);
			final InputStream resourceAsStream = mpqCodebase.getResourceAsStream("war3map.w3a");
			final War3ObjectDataChangeset obj = new War3ObjectDataChangeset();
			final WTSFile wts = new WTSFile(mpqCodebase.getResourceAsStream("war3map.wts"));
			if (!obj.load(new BlizzardDataInputStream(resourceAsStream), wts, true)) {
				System.err.println("Failed to parse.");
			}
			System.out.println(
					"Loaded " + obj.getOriginal().size() + " original and " + obj.getCustom().size() + " custom");
			final ObjectMap custom = obj.getCustom();
			for (final MapView.Entry<War3ID, ObjectDataChangeEntry> entry : custom) {
				// System.out.println(entry.getKey() + ": " + entry.getValue().getChanges().size());
				// for each custom ability
				for (final MapView.Entry<War3ID, List<Change>> changeEntry : entry.getValue().getChanges()) {
					// System.out.println(changeEntry.getKey().asStringValue() + ": ");
					for (final Change change : changeEntry.getValue()) {
						if (change.getDataptr() != 0) {
							System.out.println(entry.getValue().getOldId() + " field " + change.getId() + ": "
									+ change.getDataptr());
						}
						if (change.getVartype() == 0) {
							// System.out.println(" - " + change.getLongval());
						} else if (change.getVartype() == 3) {
							// System.out.println(" - " + change.getStrval());
						} else {
							// System.out.println(" - " + change.getRealval());
						}
					}
				}
				final List<Change> name = entry.getValue().getChanges().get(War3ID.fromString("anam"));
				if (name != null && name.size() > 0) {
					name.get(0).setStrval("Custom " + name.get(0).getStrval());
				}
			}
			try (BlizzardDataOutputStream outstream = new BlizzardDataOutputStream(
					new File("output/my_out_test_bla.w3a"))) {
				if (!obj.save(outstream, false)) {
					System.err.println("Failed to save.");
				}
			}

			// final Obj myTable = loadtable("my_out_test.w3a", wts);
			// final Obj cureTable = loadtable("cure6.w3a", wts);
			// for (final MapView.Entry<War3ID, ObjectDataChangeEntry> entry : myTable.getCustom()) {
			// if (!cureTable.getCustom().containsKey(entry.getKey())) {
			// System.out.println("FOUND MISSING: " + entry.getKey());
			// }
			// }
		} catch (final MPQException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static War3ObjectDataChangeset loadtable(final String name, final WTS wts) throws IOException {
		final War3ObjectDataChangeset obj = new War3ObjectDataChangeset();
		obj.load(new File("output/" + name), wts, true);
		return obj;
	}
}
