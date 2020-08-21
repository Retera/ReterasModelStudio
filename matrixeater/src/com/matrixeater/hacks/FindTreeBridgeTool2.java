package com.matrixeater.hacks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.ModelOptionPanel;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.UnitOptionPanel;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

import mpq.MPQException;

public class FindTreeBridgeTool2 {
	public static final War3ID UABI = War3ID.fromString("uabi");
	private static final String[] TREE_BRIDGES = { "LTt1", "LTt5", "LTt3", "ATt0", "ATt1", "LTt2", "LTt0", "LTt4" };

	public static void dropCache() {
		UnitOptionPanel.dropRaceCache();
		DataTable.dropCache();
		ModelOptionPanel.dropCache();
		WEString.dropCache();
		BLPHandler.get().dropCache();
//		teamColorMenu.removeAll();
//		createTeamColorMenuItems();
	}

	public static void main(final String[] args) {
		final Set<String> mergedListfile = MpqCodebase.get().getMergedListfile();
		for (String gameFile : mergedListfile) {
			gameFile = gameFile.toLowerCase();
			if ((gameFile.endsWith(".w3m") || gameFile.endsWith(".w3x"))) {
				String gameFilePath = "./" + gameFile;
				if (gameFilePath.contains(":")) {
					gameFilePath = gameFilePath.replace(":", "");
				}
				final Path mapOnHardDrive = Paths.get(gameFilePath);
				System.out.println(mapOnHardDrive);
				mapOnHardDrive.getParent().toFile().mkdirs();
				try {
					Files.copy(MpqCodebase.get().getResourceAsStream(gameFile), mapOnHardDrive,
							StandardCopyOption.REPLACE_EXISTING);
//					Files.copy(
//							Paths.get("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\TreeBridgeMap.w3x"),
//							mapOnHardDrive, StandardCopyOption.REPLACE_EXISTING);
				} catch (final IOException e) {
					e.printStackTrace();
				}
				try {
					final LoadedMPQ mapMPQ = MpqCodebase.get().loadMPQ(mapOnHardDrive);
					InputStream resourceAsStream;
					if (mapMPQ.has("war3map.j")) {
						resourceAsStream = MpqCodebase.get().getResourceAsStream("war3map.j");

					} else if (mapMPQ.has("scripts\\war3map.j")) {
						resourceAsStream = MpqCodebase.get().getResourceAsStream("scripts\\war3map.j");
					} else {
						resourceAsStream = null;
					}
					if (resourceAsStream != null) {
						try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
							String line;
							while ((line = reader.readLine()) != null) {
								for (final String tb : TREE_BRIDGES) {
									if (line.toLowerCase().contains(tb.toLowerCase())
											|| line.toLowerCase().contains("treebridge")
											|| line.toLowerCase().contains("tree_bridge")
											|| line.toLowerCase().contains("tree bridge")) {
										System.err.println(gameFile + ":");
										System.err.println(line);
									}
								}
							}
						}
					} else {
						System.err.println(mapOnHardDrive);
					}

					final InputStream doofile = MpqCodebase.get().getResourceAsStream("war3map.doo");
					if (doofile != null) {
						final int available = doofile.available();
						final byte[] data = new byte[available];
						doofile.read(data);
						for (int i = 0; i < data.length; i++) {
							for (final String tb2 : TREE_BRIDGES) {
								final String tb = tb2.charAt(3) + "" + tb2.charAt(2) + tb2.charAt(1) + tb2.charAt(0);
								checktb(gameFile, data, i, tb);
								checktb(gameFile, data, i, tb2);
							}
						}
					}

					final Warcraft3MapObjectData mapObjectEditorData = Warcraft3MapObjectData.load(true);
					final MutableObjectData destructibles = mapObjectEditorData.getDestructibles();
					for (final War3ID key : destructibles.keySet()) {
						final MutableGameObject mutableGameObject = destructibles.get(key);
						final ObjectData metaData = destructibles.getSourceSLKMetaData();
						for (final String metakey : metaData.keySet()) {
							try {
								final String fieldAsString = mutableGameObject
										.getFieldAsString(War3ID.fromString(metakey), 0);
								if (fieldAsString.toLowerCase().contains("treebridge") && !Arrays.asList(TREE_BRIDGES)
										.contains(mutableGameObject.getAlias().toString())) {
									System.err.println("TREE BRIDGE MAN NNNN " + gameFile);
								}
							} catch (final RuntimeException exc) {
								if (!exc.getMessage().contains("not a string")) {
									throw exc;
								}
							}
						}
					}

					mapMPQ.unload();
					dropCache();
				} catch (final MPQException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private static void checktb(final String gameFile, final byte[] data, final int i, final String tb) {
		boolean match = true;
		for (int cx = 0; cx < tb.length(); cx++) {
			final int cxm = cx + i;
			if (cxm >= data.length) {
				match = false;
				break;
			} else {
				if (tb.charAt(cx) != data[cxm]) {
					match = false;
				}
			}
		}
		if (match) {
			System.err.println(gameFile + " at index  " + i);
		}
	}

}
