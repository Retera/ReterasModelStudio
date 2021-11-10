package com.hiveworkshop.rms.ui.browsers.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.parsers.slk.DataTableUtils;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.ui.application.viewer.AnimationViewer;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelOptionPanel extends JPanel {

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

	boolean choosingModel;



	JComboBox<ModelGroup> groupBox;
	JComboBox<Model> modelBox;
	JTextField filePathField;
	String cachedIconPath;
	DefaultComboBoxModel<ModelGroup> groupsModel = new DefaultComboBoxModel<>();
	List<DefaultComboBoxModel<Model>> groupModels = new ArrayList<>();

	AnimationViewer viewer;

	final EditableModel blank = new EditableModel();

	private static void addModelsToList(Map<String, NamedList<String>> modelsData, Element unit, String artName, String weStringTypeSuffix) {
		String filepath = unit.getField(artName);
		if (filepath.length() > 0) {
			if (filepath.contains(",")) {
				filepath = filepath.split(",")[0];
			}
			NamedList<String> unitList = getUnitList(modelsData, unit, filepath);
			unitList.add(unit.getName() + " " + WEString.getString(weStringTypeSuffix));
		}
	}

	public ModelOptionPanel() {
		preload();

		for (final ModelGroup group : groups) {
			groupsModel.addElement(group);
			final DefaultComboBoxModel<Model> groupModel = new DefaultComboBoxModel<>();

			for (final Model model : group.getModels()) {
				groupModel.addElement(model);
			}
			groupModels.add(groupModel);
		}
		groupBox = new JComboBox<>(groupsModel);
		modelBox = new JComboBox<>(groupModels.get(0));
		filePathField = new JTextField(18);
		filePathField.setMaximumSize(new Dimension(4000, 25));
		groupBox.addActionListener(e -> groupBoxListener());
		modelBox.addActionListener(e -> modelBoxListener());

		filePathField.getDocument().addDocumentListener(getFilepathDocumentListener(this));

		groupBox.setMaximumRowCount(11);
		modelBox.setMaximumRowCount(36);

		groupBox.setMaximumSize(new Dimension(200, 25));
		modelBox.setMaximumSize(new Dimension(4000, 25));

		viewer = new AnimationViewer(new ProgramPreferences(), false);
		modelBox.setSelectedIndex(0);

		setLayout(new MigLayout("fill", "", "[grow]"));
		JPanel rightPanel = new JPanel(new MigLayout("fill", "[]", "[][][][grow]"));
		rightPanel.add(groupBox, "wrap, growx");
		rightPanel.add(modelBox, "wrap, growx");
		rightPanel.add(filePathField, "wrap, growx");

		add(viewer, "growx");
		add(rightPanel, "growx, growy");
	}

	private static DocumentListener getFilepathDocumentListener(final ModelOptionPanel modelOptionPanel) {
		return new DocumentListener() {
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
				if (!modelOptionPanel.choosingModel) {
					String filepath = modelOptionPanel.filePathField.getText();
					modelOptionPanel.cachedIconPath = null;
					modelOptionPanel.showModel(filepath);
				}
			}
		};
	}

	private void groupBoxListener() {
		modelBox.setModel(groupModels.get(groupBox.getSelectedIndex()));
		modelBox.setSelectedIndex(0);
	}

	private void modelBoxListener() {
		choosingModel = true;
		Model model = (Model) modelBox.getSelectedItem();
		String filepath = model != null ? model.getFilepath() : null;
		filePathField.setText(filepath);
		cachedIconPath = ((Model) modelBox.getSelectedItem()).getCachedIcon();
		showModel(filepath);
		choosingModel = false;
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
				for (final Model model : group.getModels()) {
					if (model.getFilepath().equals(path)) {
						groupBox.setSelectedItem(group);
						modelBox.setSelectedItem(model);
						cachedIconPath = model.getCachedIcon();
						break ItemFinder;
					}
				}
			}
			filePathField.setText(path);
		} else {
			filePathField.setText("");
		}
	}

	private void showModel(String filepath) {
		EditableModel toLoad;
		try {
			if (filepath.endsWith(".mdl")) {
				filepath = filepath.replace(".mdl", ".mdx");
			} else if (!filepath.endsWith(".mdx")) {
				filepath = filepath.concat(".mdx");
			}
			InputStream modelStream = GameDataFileSystem.getDefault().getResourceAsStream(filepath);
			MdlxModel mdlxModel = MdxUtils.loadMdlx(modelStream);
			toLoad = TempOpenModelStuff.createEditableModel(mdlxModel);
		} catch (final Exception exc) {
			exc.printStackTrace();
			toLoad = blank;
			// bad model!
		}
		viewer.setModel(toLoad);
		viewer.setTitle(toLoad.getName());
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
		unitData = DataTableHolder.get();
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
			} else if (str.startsWith("A") || str.startsWith("S")) {
				// ability
				final Element unit = unitData.get(str);
				addModelsToList(abilityModelData, unit, "Areaeffectart", "WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT");
				addModelsToList(abilityModelData, unit, "areaeffectart", "WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT");
				addModelsToList(abilityModelData, unit, "AreaEffectart", "WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT");
				addModelsToList(abilityModelData, unit, "AreaEffectArt", "WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT");
				addModelsToList(abilityModelData, unit, "AreaeffectArt", "WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT");

				addModelsToList(abilityModelData, unit, "CasterArt", "WESTRING_OE_TYPECAT_SUFFIX_CASTER");
				addModelsToList(abilityModelData, unit, "Casterart", "WESTRING_OE_TYPECAT_SUFFIX_CASTER");
				addModelsToList(abilityModelData, unit, "casterart", "WESTRING_OE_TYPECAT_SUFFIX_CASTER");

				addModelsToList(abilityModelData, unit, "EffectArt", "WESTRING_OE_TYPECAT_SUFFIX_EFFECT");
				addModelsToList(abilityModelData, unit, "Effectart", "WESTRING_OE_TYPECAT_SUFFIX_EFFECT");
				addModelsToList(abilityModelData, unit, "effectart", "WESTRING_OE_TYPECAT_SUFFIX_EFFECT");

				addModelsToList(abilityModelData, unit, "Missileart", "WESTRING_OE_TYPECAT_SUFFIX_MISSILE");
				addModelsToList(abilityModelData, unit, "missileart", "WESTRING_OE_TYPECAT_SUFFIX_MISSILE");
				addModelsToList(abilityModelData, unit, "MissileArt", "WESTRING_OE_TYPECAT_SUFFIX_MISSILE");

				addModelsToList(abilityModelData, unit, "SpecialArt", "WESTRING_OE_TYPECAT_SUFFIX_SPECIAL");
				addModelsToList(abilityModelData, unit, "Specialart", "WESTRING_OE_TYPECAT_SUFFIX_SPECIAL");
				addModelsToList(abilityModelData, unit, "specialart", "WESTRING_OE_TYPECAT_SUFFIX_SPECIAL");

				addModelsToList(abilityModelData, unit, "TargetArt", "WESTRING_OE_TYPECAT_SUFFIX_TARGET");
				addModelsToList(abilityModelData, unit, "Targetart", "WESTRING_OE_TYPECAT_SUFFIX_TARGET");
				addModelsToList(abilityModelData, unit, "targetart", "WESTRING_OE_TYPECAT_SUFFIX_TARGET");
			} else if (str.startsWith("B") || str.startsWith("X")) {
				// BUFF
			} else {
				// UNIT
				final Element unit = unitData.get(str);
				addMissileArtUnitToList(unitsModelData, unitsMissileData, unit);

				addUnitsToList(unitsSpecialData, unit, "Specialart", "WESTRING_OE_TYPECAT_SUFFIX_SPECIAL");

				addUnitsToList(unitsSpecialData, unit, "Targetart", "WESTRING_OE_TYPECAT_SUFFIX_TARGET");
			}
		}

		for (final String str : buffData.keySet()) {

			final Element unit = buffData.get(str);
			addModelsToList(buffModelData, unit, "EffectArt", "WESTRING_OE_TYPECAT_SUFFIX_EFFECT");
			addModelsToList(buffModelData, unit, "Effectart", "WESTRING_OE_TYPECAT_SUFFIX_EFFECT");
			addModelsToList(buffModelData, unit, "effectart", "WESTRING_OE_TYPECAT_SUFFIX_EFFECT");

			addModelsToList(buffModelData, unit, "Missileart", "WESTRING_OE_TYPECAT_SUFFIX_MISSILE");
			addModelsToList(buffModelData, unit, "MissileArt", "WESTRING_OE_TYPECAT_SUFFIX_MISSILE");
			addModelsToList(buffModelData, unit, "missileart", "WESTRING_OE_TYPECAT_SUFFIX_MISSILE");

			addModelsToList(buffModelData, unit, "SpecialArt", "WESTRING_OE_TYPECAT_SUFFIX_SPECIAL");
			addModelsToList(buffModelData, unit, "Specialart", "WESTRING_OE_TYPECAT_SUFFIX_SPECIAL");
			addModelsToList(buffModelData, unit, "specialart", "WESTRING_OE_TYPECAT_SUFFIX_SPECIAL");

			addModelsToList(buffModelData, unit, "TargetArt", "WESTRING_OE_TYPECAT_SUFFIX_TARGET");
			addModelsToList(buffModelData, unit, "Targetart", "WESTRING_OE_TYPECAT_SUFFIX_TARGET");
			addModelsToList(buffModelData, unit, "targetart", "WESTRING_OE_TYPECAT_SUFFIX_TARGET");
		}

		fillItemData(itemsModelData);
		fillUnitList(destModelData, destData);
		fillUnitList(doodModelData, doodData);
		fillSpawnData(spawnModelData);
		fillGinterData(ginterModelData);

//		addNewModelGroup(unitsModelData, "WESTRING_OE_TYPECAT_UNIT");
//		addNewModelGroup(unitsMissileData, "WESTRING_OE_TYPECAT_UNIT_MSSL");
//		addNewModelGroup(unitsSpecialData, "WESTRING_OE_TYPECAT_UNIT_SPEC");
//		addNewModelGroup(itemsModelData, "WESTRING_OE_TYPECAT_ITEM");
//		addNewModelGroup(abilityModelData, "WESTRING_OE_TYPECAT_ABIL");
//		addNewModelGroup(buffModelData, "WESTRING_OE_TYPECAT_BUFF");
//		addNewModelGroup(destModelData, "WESTRING_OE_TYPECAT_DEST");
//		addNewModelGroup(doodModelData, "WESTRING_OE_TYPECAT_DOOD");
//		addNewModelGroup(spawnModelData, "WESTRING_OE_TYPECAT_SPWN");
//		addNewModelGroup(ginterModelData, "WESTRING_OE_TYPECAT_SKIN");

		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT")).fill(unitsModelData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT_MSSL")).fill(unitsMissileData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT_SPEC")).fill(unitsSpecialData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_ITEM")).fill(itemsModelData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_ABIL")).fill(abilityModelData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_BUFF")).fill(buffModelData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_DEST")).fill(destModelData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_DOOD")).fill(doodModelData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_SPWN")).fill(spawnModelData));
		groups.add(new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_SKIN")).fill(ginterModelData));

		addExtraModelGroup();

		// new JFrame().setVisible(true);
	}

	private static void fillItemData(Map<String, NamedList<String>> itemsModelData) {
		for (String str : itemData.keySet()) {
			// ITEMS
			Element unit = itemData.get(str);
			String filepath = unit.getField("file");
			if (filepath.length() > 0) {
//				NamedList<String> unitList = getUnitList(itemsModelData, unit, filepath);
//				unitList.add(unit.getName());
				itemsModelData
						.computeIfAbsent(filepath.toLowerCase(), k -> new NamedList<>(filepath, unit.getIconPath()))
						.add(unit.getName());
//				System.out.println("unit.fieldValue: " + unit.getFieldValue("numVar"));
			}
		}
	}

	private static void fillGinterData(Map<String, NamedList<String>> ginterModelData) {
		for (String str : ginterData.keySet()) {
			Element race = ginterData.get(str);
			// System.err.println("Gintering unit " + str);
			for (String fieldName : race.keySet()) {
				String value = race.getField(fieldName);
				if (value.endsWith(".mdl")) {
//					NamedList<String> unitList = getUnitList(ginterModelData, race, value);
//					unitList.add(fieldName + " (" + race.getUnitId() + ")");
					ginterModelData
							.computeIfAbsent(value.toLowerCase(), k -> new NamedList<>(value, race.getIconPath()))
							.add(fieldName + " (" + race.getUnitId() + ")");
				}
			}
		}
	}

	private static void fillSpawnData(Map<String, NamedList<String>> spawnModelData) {
		for (final String str : spawnData.keySet()) {
			if (!str.equals("init")) {
				// ITEMS
				final Element unit = spawnData.get(str);
				String model = unit.getField("Model");
				if (model.equals("_")) {
					continue;
				}
				String filepath = model;
				if (filepath.length() > 0) {
					NamedList<String> unitList = getUnitList(spawnModelData, unit, filepath);
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
	}

	private static void addExtraModelGroup() {
		ModelGroup extra = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_XTRA"));
		DataTable worldEditData = new DataTable();
		try {
			DataTableUtils.readTXT(worldEditData, GameDataFileSystem.getDefault().getResourceAsStream("UI\\WorldEditData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		Element extraModels = worldEditData.get("ExtraModels");
		int emId = 0;
		while (extraModels.getField(String.format("%2d", emId).replace(" ", "0")).length() > 0) {
			String fieldName = String.format("%2d", emId).replace(" ", "0");
			Model nextModel = new Model()
					.setDisplayName(WEString.getString(extraModels.getField(fieldName, 2)))
					.setFilepath(extraModels.getField(fieldName, 1))
					.setCachedIcon(extraModels.getIconPath());
			extra.addModel(nextModel);

			emId++;
		}
		extra.sortModels();
		groups.add(extra);

		for (Model model : extra.getModels()) {
			System.out.println("Extra model: " + model);// + ": \"" + model.filepath + "\"");
		}
	}

	private static void addMissileArtUnitToList(Map<String, NamedList<String>> unitsModelData, Map<String, NamedList<String>> unitsMissileData, Element unit) {
		String filepath = unit.getField("file");
		if (filepath.length() > 0) {
			NamedList<String> unitList = getUnitList(unitsModelData, unit, filepath);
			unitList.add(unit.getName());
		}

		filepath = unit.getField("Missileart");
		if (filepath.length() > 0) {
			if (filepath.contains(",")) {
				String[] filepaths = filepath.split(",");
				for (String fp : filepaths) {
					NamedList<String> unitList = getMissileNamedList(unitsMissileData, unit, filepath, fp);
					unitList.add(unit.getName());
				}
			} else {
				NamedList<String> unitList = getMissileNamedList(unitsMissileData, unit, filepath, filepath);
				unitList.add(unit.getName());
			}
		}
	}

	private static NamedList<String> getMissileNamedList(Map<String, NamedList<String>> unitsMissileData, Element unit, String filepath, String fp) {
		NamedList<String> unitList = unitsMissileData.get(fp.toLowerCase());
		if (unitList == null) {
			unitList = new NamedList<>(filepath);
			unitList.setCachedIconPath(unit.getIconPath());
			unitsMissileData.put(fp.toLowerCase(), unitList);
		}
		return unitList;
	}

	private static NamedList<String> getUnitList(Map<String, NamedList<String>> spawnModelData, Element unit, String filepath) {
		return getMissileNamedList(spawnModelData, unit, filepath, filepath);
	}

	private static void fillUnitList(Map<String, NamedList<String>> modelData, DataTable dataTable) {
		for (String str : dataTable.keySet()) {
			// ITEMS
			Element unit = dataTable.get(str);
			String filepath = unit.getField("file");
			if (filepath.length() > 0) {
				NamedList<String> unitList = getUnitList(modelData, unit, filepath);
				unitList.add(unit.getName() + " <Base>");

				int numVar = unit.getFieldValue("numVar");
				if (numVar > 1) {
					for (int i = 0; i < numVar; i++) {
						String filepath2 = filepath + i + ".mdl";
						NamedList<String> unitList2 = getUnitList(modelData, unit, filepath2);
						unitList2.add(unit.getName() + " <" + WEString.getString("WESTRING_PREVIEWER_VAR") + " " + (i + 1) + ">");

					}
				}
			}
		}
	}

	private static void addUnitsToList(Map<String, NamedList<String>> unitsData, Element unit, String artName, String weStringTypeSuffix) {
		String filepath = unit.getField(artName);
		if (filepath.length() > 0) {
			NamedList<String> unitList = getUnitList(unitsData, unit, filepath);
			unitList.add(unit.getName() + " " + WEString.getString(weStringTypeSuffix));
		}
	}
}
