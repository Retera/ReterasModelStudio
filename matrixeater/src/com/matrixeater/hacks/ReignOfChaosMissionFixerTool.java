package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.etheller.collections.List;
import com.etheller.collections.MapView.Entry;
import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.resources.Resources;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.ModelOptionPanel;
import com.hiveworkshop.wc3.units.UnitOptionPanel;
import com.hiveworkshop.wc3.units.objectdata.Change;
import com.hiveworkshop.wc3.units.objectdata.ChangeMap;
import com.hiveworkshop.wc3.units.objectdata.ObjectDataChangeEntry;
import com.hiveworkshop.wc3.units.objectdata.ObjectMap;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;
import mpq.MPQException;

public class ReignOfChaosMissionFixerTool {
	public static final War3ID UABI = War3ID.fromString("uabi");

	public static void dropCache() {
		UnitOptionPanel.dropRaceCache();
		DataTable.dropCache();
		ModelOptionPanel.dropCache();
		WEString.dropCache();
		Resources.dropCache();
		BLPHandler.get().dropCache();
//		teamColorMenu.removeAll();
//		createTeamColorMenuItems();
	}

	public static void main(final String[] args) {
		final SetView<String> mergedListfile = MpqCodebase.get().getMergedListfile();
		for (String gameFile : mergedListfile) {
			gameFile = gameFile.toLowerCase();
			if (gameFile.contains("campaign") && gameFile.endsWith(".w3m")) {
				System.out.println(gameFile);
				final Path mapOnHardDrive = Paths.get("./" + gameFile);
				mapOnHardDrive.getParent().toFile().mkdirs();
				try {
					Files.copy(MpqCodebase.get().getResourceAsStream(gameFile), mapOnHardDrive,
							StandardCopyOption.REPLACE_EXISTING);
				} catch (final IOException e) {
					e.printStackTrace();
				}
				try {
					final LoadedMPQ mapMPQ = MpqCodebase.get().loadMPQ(mapOnHardDrive);

					final War3ObjectDataChangeset changeset = new War3ObjectDataChangeset('u');
					changeset.load(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream("war3map.w3u")),
							null, false);
					final ObjectMap original = changeset.getOriginal();
					SetView<Entry<War3ID, ObjectDataChangeEntry>> entrySet = original.entrySet();
					for (final Entry<War3ID, ObjectDataChangeEntry> entry : entrySet) {
						if (Character.isUpperCase(entry.getKey().asStringValue().charAt(0))) {
							final ChangeMap changes = entry.getValue().getChanges();
							for (final Entry<War3ID, List<Change>> changeEntry : changes) {
								if (changeEntry.getKey().equals(UABI)) {
									for (final Change change : changeEntry.getValue()) {
										System.out.println("adding to hero: " + entry.getKey() + "   "
												+ change.getStrval() + ",AInv");
										change.setStrval(change.getStrval() + ",AInv");
									}
								}
							}
						}
					}
					entrySet = changeset.getCustom().entrySet();
					for (final Entry<War3ID, ObjectDataChangeEntry> entry : entrySet) {
						if (Character.isUpperCase(entry.getKey().asStringValue().charAt(0))) {
							final ChangeMap changes = entry.getValue().getChanges();
							for (final Entry<War3ID, List<Change>> changeEntry : changes) {
								if (changeEntry.getKey().equals(UABI)) {
									for (final Change change : changeEntry.getValue()) {
										change.setStrval(change.getStrval() + ",AInv");
									}
								}
							}
						}
					}
					try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(
							new File("war3map.w3u"))) {
						changeset.save(outputStream, false);
					}

					mapMPQ.unload();
					dropCache();

					final Process proc = Runtime.getRuntime().exec("dolinjection.bat " + gameFile);
					try {
						proc.waitFor();
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				} catch (final MPQException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
