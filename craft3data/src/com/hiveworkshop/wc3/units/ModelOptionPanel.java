package com.hiveworkshop.wc3.units;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modelviewer.AnimationViewer;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.resources.WEString;

import de.wc3data.stream.BlizzardDataInputStream;

public class ModelOptionPanel extends JPanel {

	static class Model {
		String cachedIcon;
		String displayName;
		String filepath;

		@Override
		public String toString() {
			return displayName;
		}
	}

	static class ModelGroup {
		String name;
		List<Model> models = new ArrayList<>();

		public ModelGroup(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static class ModelComparator implements Comparator<Model> {
		@Override
		public int compare(final Model o1, final Model o2) {
			return o1.displayName.compareToIgnoreCase(o2.displayName);
		}

	}

	static class NamedList<E> extends ArrayList<E> {
		String name;
		String cachedIconPath = null; // might be present

		public NamedList(final String name) {
			this.name = name;
		}

		public void setCachedIconPath(final String cachedIconPath) {
			this.cachedIconPath = cachedIconPath;
		}

		public String getCachedIconPath() {
			return cachedIconPath;
		}
	}

	static List<ModelGroup> groups = new ArrayList<>();

	static DataTable unitData = null;
	static DataTable itemData = null;
	static DataTable buffData = null;
	static DataTable destData = null;
	static DataTable doodData = null;
	static DataTable spawnData = null;
	static DataTable ginterData = null;

	static boolean preloaded;

	public static void dropCache() {
		preloaded = false;
	}

	static void preload() {
		if (preloaded) {
			return;
		} else {
			preloaded = true;
			// 11 ModelGroups:
			// - Units
			// - Units - Missiles
			// - Units - Special
			// - Items
			// - Abilities
			// - Buffs
			// - Destructibles
			// - Doodads
			// - Spawned Effects
			// - Game Interface
		}
		groups.clear();
		unitData = DataTable.get();
		itemData = DataTable.getItems();
		buffData = DataTable.getBuffs();
		destData = DataTable.getDestructables();
		doodData = DataTable.getDoodads();
		spawnData = DataTable.getSpawns();
		ginterData = DataTable.getGinters();

		// WESTRING_OE_TYPECAT_UNIT=Units
		// WESTRING_OE_TYPECAT_UNIT_MSSL=Units - Missiles
		// WESTRING_OE_TYPECAT_UNIT_SPEC=Units - Special
		// WESTRING_OE_TYPECAT_ITEM=Items
		// WESTRING_OE_TYPECAT_ABIL=Abilities
		// WESTRING_OE_TYPECAT_BUFF=Buffs
		// WESTRING_OE_TYPECAT_UPGR=Upgrades
		// WESTRING_OE_TYPECAT_DEST=Destructibles
		// WESTRING_OE_TYPECAT_DOOD=Doodads
		// WESTRING_OE_TYPECAT_SPWN=Spawned Effects
		// WESTRING_OE_TYPECAT_SKIN=Game Interface
		// WESTRING_OE_TYPECAT_XTRA=Extra

		// Preload "Units" modelGroup
		final Map<String, NamedList<String>> unitsModelData = new HashMap<>();
		final Map<String, NamedList<String>> unitsMissileData = new HashMap<>();
		final Map<String, NamedList<String>> unitsSpecialData = new HashMap<>();
		final Map<String, NamedList<String>> abilityModelData = new HashMap<>();
		final Map<String, NamedList<String>> buffModelData = new HashMap<>();
		final Map<String, NamedList<String>> itemsModelData = new HashMap<>();
		final Map<String, NamedList<String>> destModelData = new HashMap<>();
		final Map<String, NamedList<String>> doodModelData = new HashMap<>();
		final Map<String, NamedList<String>> spawnModelData = new HashMap<>();
		final Map<String, NamedList<String>> ginterModelData = new HashMap<>();

		// List<Unit> sortedUnitData = new ArrayList<Unit>();
		for (String str : unitData.keySet()) {
			str = str.toUpperCase();
			if (str.startsWith("R")) {
				continue;
			} else if (str.startsWith("A") || str.startsWith("S")) {
				// ability
				final Element unit = unitData.get(str);
				String filepath = unit.getField("Areaeffectart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				filepath = unit.getField("areaeffectart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				filepath = unit.getField("AreaEffectart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				filepath = unit.getField("AreaEffectArt");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				filepath = unit.getField("AreaeffectArt");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase().toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase().toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}

				filepath = unit.getField("CasterArt");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_CASTER"));
				}
				filepath = unit.getField("Casterart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_CASTER"));
				}
				filepath = unit.getField("casterart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_CASTER"));
				}

				filepath = unit.getField("EffectArt");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
				}
				filepath = unit.getField("Effectart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
				}
				filepath = unit.getField("effectart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
				}

				filepath = unit.getField("Missileart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
				}
				filepath = unit.getField("missileart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
				}
				filepath = unit.getField("MissileArt");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
				}

				filepath = unit.getField("SpecialArt");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
				}
				filepath = unit.getField("Specialart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
				}
				filepath = unit.getField("specialart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
				}

				filepath = unit.getField("TargetArt");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
				}
				filepath = unit.getField("Targetart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
				}
				filepath = unit.getField("targetart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
				}
			} else if (str.startsWith("B") || str.startsWith("X") || str.startsWith("A")) {
				// BUFF
			} else {
				// UNIT
				final Element unit = unitData.get(str);
				String filepath = unit.getField("file");
				if (filepath.length() > 0) {
					NamedList<String> unitList = unitsModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						unitsModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName());
				}

				filepath = unit.getField("Missileart");
				if (filepath.length() > 0) {
					if (filepath.contains(",")) {
						final String[] filepaths = filepath.split(",");
						for (final String fp : filepaths) {
							NamedList<String> unitList = unitsMissileData.get(fp.toLowerCase());
							if (unitList == null) {
								unitList = new NamedList<>(filepath);
								unitList.setCachedIconPath(unit.getIconPath());
								unitsMissileData.put(fp.toLowerCase(), unitList);
							}
							unitList.add(unit.getName());
						}
					} else {
						NamedList<String> unitList = unitsMissileData.get(filepath.toLowerCase());
						if (unitList == null) {
							unitList = new NamedList<>(filepath);
							unitList.setCachedIconPath(unit.getIconPath());
							unitsMissileData.put(filepath.toLowerCase(), unitList);
						}
						unitList.add(unit.getName());
					}
				}

				filepath = unit.getField("Specialart");
				if (filepath.length() > 0) {
					NamedList<String> unitList = unitsSpecialData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						unitsSpecialData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
				}

				filepath = unit.getField("Targetart");
				if (filepath.length() > 0) {
					NamedList<String> unitList = unitsSpecialData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						unitsSpecialData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
				}
			}
		}

		for (final String str : buffData.keySet()) {

			final Element unit = buffData.get(str);
			String filepath = unit.getField("EffectArt");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
			}
			filepath = unit.getField("Effectart");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
			}
			filepath = unit.getField("effectart");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
			}

			filepath = unit.getField("Missileart");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
			}
			filepath = unit.getField("MissileArt");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
			}
			filepath = unit.getField("missileart");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
			}

			filepath = unit.getField("SpecialArt");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
			}
			filepath = unit.getField("Specialart");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
			}
			filepath = unit.getField("specialart");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
			}

			filepath = unit.getField("TargetArt");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
			}
			filepath = unit.getField("Targetart");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
			}
			filepath = unit.getField("targetart");
			if (filepath.length() > 0) {
				if (filepath.contains(",")) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
			}
		}

		for (final String str : itemData.keySet()) {
			// ITEMS
			final Element unit = itemData.get(str);
			final String filepath = unit.getField("file");
			if (filepath.length() > 0) {
				NamedList<String> unitList = itemsModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					itemsModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName());
			}
		}

		for (final String str : destData.keySet()) {
			// ITEMS
			final Element unit = destData.get(str);
			final String filepath = unit.getField("file");
			if (filepath.length() > 0) {
				NamedList<String> unitList = destModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					destModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " <Base>");

				final int numVar = unit.getFieldValue("numVar");
				if (numVar > 1) {
					for (int i = 0; i < numVar; i++) {

						final String filepath2 = filepath + i + ".mdl";
						if (filepath2.length() > 0) {
							NamedList<String> unitList2 = destModelData.get(filepath2.toLowerCase());
							if (unitList2 == null) {
								unitList2 = new NamedList<>(filepath2);
								unitList2.setCachedIconPath(unit.getIconPath());
								destModelData.put(filepath2.toLowerCase(), unitList2);
							}
							unitList2.add(unit.getName() + " <" + WEString.getString("WESTRING_PREVIEWER_VAR") + " "
									+ (i + 1) + ">");
						}
					}
				}
			}
		}

		for (final String str : doodData.keySet()) {
			// ITEMS
			final Element unit = doodData.get(str);
			final String filepath = unit.getField("file");
			if (filepath.length() > 0) {
				NamedList<String> unitList = doodModelData.get(filepath.toLowerCase());
				if (unitList == null) {
					unitList = new NamedList<>(filepath);
					unitList.setCachedIconPath(unit.getIconPath());
					doodModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " <Base>");

				final int numVar = unit.getFieldValue("numVar");
				if (numVar > 1) {
					for (int i = 0; i < numVar; i++) {

						final String filepath2 = filepath + i + ".mdl";
						if (filepath2.length() > 0) {
							NamedList<String> unitList2 = doodModelData.get(filepath2.toLowerCase());
							if (unitList2 == null) {
								unitList2 = new NamedList<>(filepath2);
								unitList2.setCachedIconPath(unit.getIconPath());
								doodModelData.put(filepath2.toLowerCase(), unitList2);
							}
							unitList2.add(unit.getName() + " <" + WEString.getString("WESTRING_PREVIEWER_VAR") + " "
									+ (i + 1) + ">");
						}
					}
				}
			}
		}

		for (final String str : spawnData.keySet()) {
			if (!str.equals("init")) {
				// ITEMS
				final Element unit = spawnData.get(str);
				String model = unit.getField("Model");
				if (model.equals("_")) {
					continue;
				}
				final String filepath = model;
				if (filepath.length() > 0) {
					NamedList<String> unitList = spawnModelData.get(filepath.toLowerCase());
					if (unitList == null) {
						unitList = new NamedList<>(filepath);
						unitList.setCachedIconPath(unit.getIconPath());
						spawnModelData.put(filepath.toLowerCase(), unitList);
					}
					if (model.contains("\\")) {
						model = model.substring(model.lastIndexOf("\\") + 1);
					}
					if (model.contains(".")) {
						model = model.substring(0, model.indexOf("."));
					}
					unitList.add(model);
				}
			}
		}

		for (final String str : ginterData.keySet()) {
			final Element race = ginterData.get(str);
			// System.err.println("Gintering unit " + str);
			for (final String fieldName : race.keySet()) {
				final String value = race.getField(fieldName);
				if (value.endsWith(".mdl")) {

					final String filepath = value;
					if (filepath.length() > 0) {
						NamedList<String> unitList = ginterModelData.get(filepath.toLowerCase());
						if (unitList == null) {
							unitList = new NamedList<>(filepath);
							unitList.setCachedIconPath(race.getIconPath());
							ginterModelData.put(filepath.toLowerCase(), unitList);
						}
						unitList.add(fieldName + " (" + race.getUnitId() + ")");
					}
				}
			}
		}

		final int lengthCap = 120;
		// Collections.sort(sortedUnitData, new UnitComparator2());
		// for( Unit unit: sortedUnitData ) {
		// }
		final ModelGroup units = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT"));
		for (final String str : unitsModelData.keySet()) {
			final NamedList<String> unitList = unitsModelData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			units.models.add(nextModel);
		}
		Collections.sort(units.models, new ModelComparator());
		groups.add(units);

		final ModelGroup unitsMissiles = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT_MSSL"));
		for (final String str : unitsMissileData.keySet()) {
			final NamedList<String> unitList = unitsMissileData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			unitsMissiles.models.add(nextModel);
		}
		Collections.sort(unitsMissiles.models, new ModelComparator());
		groups.add(unitsMissiles);

		final ModelGroup unitsSpecial = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT_SPEC"));
		for (final String str : unitsSpecialData.keySet()) {
			final NamedList<String> unitList = unitsSpecialData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			unitsSpecial.models.add(nextModel);
		}
		Collections.sort(unitsSpecial.models, new ModelComparator());
		groups.add(unitsSpecial);

		final ModelGroup items = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_ITEM"));
		for (final String str : itemsModelData.keySet()) {
			final NamedList<String> unitList = itemsModelData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			items.models.add(nextModel);
		}
		Collections.sort(items.models, new ModelComparator());
		groups.add(items);

		final ModelGroup abilities = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_ABIL"));
		for (final String str : abilityModelData.keySet()) {
			final NamedList<String> unitList = abilityModelData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			abilities.models.add(nextModel);
		}
		Collections.sort(abilities.models, new ModelComparator());
		groups.add(abilities);

		final ModelGroup buffs = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_BUFF"));
		for (final String str : buffModelData.keySet()) {
			final NamedList<String> unitList = buffModelData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			buffs.models.add(nextModel);
		}
		Collections.sort(buffs.models, new ModelComparator());
		groups.add(buffs);

		final ModelGroup destructibles = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_DEST"));
		for (final String str : destModelData.keySet()) {
			final NamedList<String> unitList = destModelData.get(str);
			Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			destructibles.models.add(nextModel);
		}
		Collections.sort(destructibles.models, new ModelComparator());
		groups.add(destructibles);

		final ModelGroup doodads = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_DOOD"));
		for (final String str : doodModelData.keySet()) {
			final NamedList<String> unitList = doodModelData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			doodads.models.add(nextModel);
		}
		Collections.sort(doodads.models, new ModelComparator());
		groups.add(doodads);

		final ModelGroup spawns = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_SPWN"));
		for (final String str : spawnModelData.keySet()) {
			final NamedList<String> unitList = spawnModelData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			spawns.models.add(nextModel);
		}
		Collections.sort(spawns.models, new ModelComparator());
		groups.add(spawns);

		final ModelGroup ginters = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_SKIN"));
		for (final String str : ginterModelData.keySet()) {
			final NamedList<String> unitList = ginterModelData.get(str);
			// Collections.sort(unitList);
			String nameOutput = "";
			for (final String unitName : unitList) {
				if (nameOutput.length() > 0) {
					nameOutput += ", ";
				}
				if ((nameOutput.length() + unitName.length()) > lengthCap) {
					nameOutput += "...";
					break;
				} else {
					nameOutput += unitName;
				}
			}
			final Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			nextModel.cachedIcon = unitList.getCachedIconPath();
			ginters.models.add(nextModel);
		}
		Collections.sort(ginters.models, new ModelComparator());
		groups.add(ginters);

		final ModelGroup extra = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_XTRA"));
		final DataTable worldEditData = new DataTable();
		try {
			worldEditData.readTXT(MpqCodebase.get().getResourceAsStream("UI\\WorldEditData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		final Element extraModels = worldEditData.get("ExtraModels");
		int emId = 0;
		while (extraModels.getField(String.format("%2d", emId).replace(" ", "0")).length() > 0) {
			final String fieldName = String.format("%2d", emId).replace(" ", "0");
			final Model nextModel = new Model();
			nextModel.displayName = WEString.getString(extraModels.getField(fieldName, 2));
			nextModel.filepath = extraModels.getField(fieldName, 1);
			nextModel.cachedIcon = extraModels.getIconPath();
			extra.models.add(nextModel);

			emId++;
		}
		Collections.sort(extra.models, new ModelComparator());
		groups.add(extra);

		for (final Model model : extra.models) {
			System.out.println(model);// + ": \"" + model.filepath + "\"");
		}

		// new JFrame().setVisible(true);
	}

	JComboBox<ModelGroup> groupBox;
	JComboBox<Model> modelBox;
	JTextField filePathField;
	String cachedIconPath;
	DefaultComboBoxModel<ModelGroup> groupsModel = new DefaultComboBoxModel<>();
	List<DefaultComboBoxModel<Model>> groupModels = new ArrayList<>();

	AnimationViewer viewer;

	final MDL blank = new MDL();
	final ModelView blankDisp = new ModelViewManager(blank);

	public ModelOptionPanel() {
		preload();

		for (final ModelGroup group : groups) {
			groupsModel.addElement(group);
			final DefaultComboBoxModel<Model> groupModel = new DefaultComboBoxModel<>();

			for (final Model model : group.models) {
				groupModel.addElement(model);
			}
			groupModels.add(groupModel);
		}
		groupBox = new JComboBox<>(groupsModel);
		modelBox = new JComboBox<>(groupModels.get(0));
		filePathField = new JTextField();
		filePathField.setMaximumSize(new Dimension(20000, 25));
		groupBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				modelBox.setModel(groupModels.get(groupBox.getSelectedIndex()));
				modelBox.setSelectedIndex(0);
			}
		});
		modelBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				MDL toLoad = blank;
				ModelView modelDisp;
				try {
					String filepath = ((Model) modelBox.getSelectedItem()).filepath;
					filePathField.setText(filepath);
					cachedIconPath = ((Model) modelBox.getSelectedItem()).cachedIcon;
					if (filepath.endsWith(".mdl")) {
						filepath = filepath.replace(".mdl", ".mdx");
					} else if (!filepath.endsWith(".mdx")) {
						filepath = filepath.concat(".mdx");
					}
					final InputStream modelStream = MpqCodebase.get().getResourceAsStream(filepath);
					final MdxModel model = MdxUtils.loadModel(new BlizzardDataInputStream(modelStream));
					toLoad = model.toMDL();// MDL.read(modelStream);
					modelDisp = new ModelViewManager(toLoad);
				} catch (final Exception exc) {
					exc.printStackTrace();
					// bad model!
					modelDisp = blankDisp;
				}

				viewer.setModel(modelDisp);
				viewer.setTitle(toLoad.getName());
			}
		});
		filePathField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(final DocumentEvent e) {
				refresh();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				refresh();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				refresh();
			}

			void refresh() {
				MDL toLoad = blank;
				ModelView modelDisp;
				try {
					String filepath = filePathField.getText();
					if (filepath.endsWith(".mdl")) {
						filepath = filepath.replace(".mdl", ".mdx");
					} else if (!filepath.endsWith(".mdx")) {
						filepath = filepath.concat(".mdx");
					}
					final InputStream modelStream = MpqCodebase.get().getResourceAsStream(filepath);
					final MdxModel model = MdxUtils.loadModel(new BlizzardDataInputStream(modelStream));
					toLoad = model.toMDL();// MDL.read(modelStream);
					modelDisp = new ModelViewManager(toLoad);
					cachedIconPath = null;
				} catch (final Exception exc) {
					exc.printStackTrace();
					// bad model!
					modelDisp = blankDisp;
				}

				viewer.setModel(modelDisp);
				viewer.setTitle(toLoad.getName());
			}
		});

		groupBox.setMaximumRowCount(11);
		modelBox.setMaximumRowCount(36);

		groupBox.setMaximumSize(new Dimension(140, 25));
		modelBox.setMaximumSize(new Dimension(10000, 25));

		// TODO program prefs not be null???
		// viewer = new PerspDisplayPanel("blank", blankDisp, null);
		viewer = new AnimationViewer(blankDisp, new ProgramPreferences(), false);
		modelBox.setSelectedIndex(0);

		add(groupBox);
		add(modelBox);

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(8).addComponent(viewer).addGap(8).addGroup(
				layout.createParallelGroup().addComponent(groupBox).addComponent(modelBox).addComponent(filePathField))
				.addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(8)
				.addGroup(layout.createParallelGroup().addComponent(viewer).addGroup(layout.createSequentialGroup()
						.addComponent(groupBox).addGap(4).addComponent(modelBox).addGap(4).addComponent(filePathField)))
				.addGap(8));

		setLayout(layout);
	}

	public String getSelection() {
		return filePathField.getText();
		// if( modelBox.getSelectedItem() != null ) {
		// return ((Model)modelBox.getSelectedItem()).filepath;
		// } else {
		// return null;
		// }
	}

	public String getCachedIconPath() {
		return cachedIconPath;
	}

	public void setSelection(final String path) {
		if (path != null) {
			ItemFinder: for (final ModelGroup group : groups) {
				for (final Model model : group.models) {
					if (model.filepath.equals(path)) {
						groupBox.setSelectedItem(group);
						modelBox.setSelectedItem(model);
						cachedIconPath = model.cachedIcon;
						break ItemFinder;
					}
				}
			}
			filePathField.setText(path);
		} else {
			filePathField.setText("");
		}
	}

	// public static void main(String[] args) {
	// try {
	// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (InstantiationException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (UnsupportedLookAndFeelException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// preload();
	// JOptionPane.showMessageDialog(null, new ModelOptionPanel(), "Choose
	// Model", JOptionPane.PLAIN_MESSAGE);
	// }
}
