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

	private static final List<ModelGroup> groups = new ArrayList<>();

	private static DataTable unitData = null;
	private static DataTable itemData = null;
	private static DataTable buffData = null;
	private static DataTable destrData = null;
	private static DataTable doodData = null;
	private static DataTable spawnData = null;
	private static DataTable ginterData = null;

	private static boolean preloaded;

	public static void dropCache() {
		preloaded = false;
	}

	private boolean choosingModel;

	private final JComboBox<ModelGroup> groupBox;
	private final JComboBox<Model> modelBox;
	private final JTextField filePathField;
	private String cachedIconPath;
	private DefaultComboBoxModel<ModelGroup> groupsModel = new DefaultComboBoxModel<>();
	private List<DefaultComboBoxModel<Model>> groupModels = new ArrayList<>();

	private final AnimationViewer viewer;

	private final EditableModel blank = new EditableModel();
	private EditableModel toLoad;

	public ModelOptionPanel() {
		preload();

		for (final ModelGroup group : groups) {
			groupsModel.addElement(group);
			final DefaultComboBoxModel<Model> groupModel = new DefaultComboBoxModel<>();

			System.out.println("group: " + group + ", models: " + group.getModels().size());
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
		DefaultComboBoxModel<Model> groupModel = groupModels.get(groupBox.getSelectedIndex());
		System.out.println("groupModel: " + groupModel + ", size: " + groupModel.getSize());
		modelBox.setModel(groupModel);
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

	public EditableModel getSelectedModel() {
		return toLoad;
	}

	public String getCachedIconPath() {
		return cachedIconPath;
	}

	public void setSelection(String path) {
		if (path != null) {
			ItemFinder:
			for (ModelGroup group : groups) {
				for (Model model : group.getModels()) {
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
		try {
			if (filepath.endsWith(".mdl")) {
				filepath = filepath.replace(".mdl", ".mdx");
			} else if (!filepath.endsWith(".mdx")) {
				filepath = filepath.concat(".mdx");
			}
			InputStream modelStream = GameDataFileSystem.getDefault().getResourceAsStream(filepath);
			if (modelStream != null) {
				MdlxModel mdlxModel = MdxUtils.loadMdlx(modelStream);
				toLoad = TempOpenModelStuff.createEditableModel(mdlxModel);
			} else {
				System.err.println("failed to load file: \"" + filepath + "\"");
				toLoad = null;
				viewer.setModel(blank);
				viewer.setTitle("No model loaded");
			}
		} catch (final Exception exc) {
			exc.printStackTrace();
			toLoad = null;
			viewer.setModel(blank);
			viewer.setTitle("No model loaded");
			// bad model!
		}
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
		unitData = DataTableHolder.getDefault();
		itemData = DataTableHolder.getItems();
		buffData = DataTableHolder.getBuffs();
		destrData = DataTableHolder.getDestructables();
		doodData = DataTableHolder.getDoodads();
		spawnData = DataTableHolder.getSpawns();
		ginterData = DataTableHolder.getGinters();

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

		final Map<String, NamedList<String>> buffModelData = getBuffModelData();

		fillItemData(itemsModelData);
		fillUnitList(destModelData, destrData);
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

	private static Map<String, NamedList<String>> getBuffModelData() {
		final Map<String, NamedList<String>> buffModelData = new HashMap<>();
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
		return buffModelData;
	}

	private void doStringThings() {
		String string = "AreaEffectArt";
		String[] split = string.split("(?=[A-Z])");
		String[] split2 = new String[split.length];
		for (int i = 0; i < split.length; i++) {
			split2[i] = split[i].toLowerCase();
		}
		System.out.println(split);
	}

	private static void fillItemData(Map<String, NamedList<String>> itemsModelData) {
		for (String str : itemData.keySet()) {
			// ITEMS
			Element unit = itemData.get(str);
			String filepath = unit.getField("file");
			if (filepath.length() > 0) {
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
			for (String fieldName : race.keySet()) {
				String value = race.getField(fieldName);
				if (value.endsWith(".mdl")) {
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
					if (model.contains("\\")) {
						model = model.substring(model.lastIndexOf("\\") + 1);
					}
					if (model.contains(".")) {
						model = model.substring(0, model.indexOf("."));
					}
					spawnModelData
							.computeIfAbsent(filepath.toLowerCase(), k -> new NamedList<>(filepath, unit.getIconPath()))
							.add(model);
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
//		int emId = 0;
//		while (extraModels.getField(String.format("%2d", emId).replace(" ", "0")).length() > 0) {
//			String fieldName = String.format("%2d", emId).replace(" ", "0");
//			Model nextModel = new Model()
//					.setDisplayName(WEString.getString(extraModels.getField(fieldName, 2)))
//					.setFilepath(extraModels.getField(fieldName, 1))
//					.setCachedIcon(extraModels.getIconPath());
//			extra.addModel(nextModel);
//
//			emId++;
//		}

//		for (int emId = 0; extraModels.getField(String.format("%2d", emId).replace(" ", "0")).length() > 0; emId++) {
//			String fieldName = String.format("%2d", emId).replace(" ", "0");
//			Model nextModel = new Model()
//					.setDisplayName(WEString.getString(extraModels.getField(fieldName, 2)))
//					.setFilepath(extraModels.getField(fieldName, 1))
//					.setCachedIcon(extraModels.getIconPath());
//			extra.addModel(nextModel);
//
//		}
		for (int emId = 0; emId < 30000; emId++) {
			String fieldName = String.format("%2d", emId).replace(" ", "0");
			if (extraModels.getField(fieldName).length() > 0) {
				Model nextModel = new Model()
						.setDisplayName(WEString.getString(extraModels.getField(fieldName, 2)))
						.setFilepath(extraModels.getField(fieldName, 1))
						.setCachedIcon(extraModels.getIconPath());
				extra.addModel(nextModel);
			} else {
				break;
			}
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
//			NamedList<String> unitList = getUnitList(unitsModelData, unit, filepath);
//			unitList.add(unit.getName());
			unitsModelData
					.computeIfAbsent(filepath.toLowerCase(), k -> new NamedList<>(filepath, unit.getIconPath()))
					.add(unit.getName());
		}

		String missileArtField = unit.getField("Missileart");
		if (missileArtField.length() > 0) {
			if (missileArtField.contains(",")) {
				String[] filepaths = missileArtField.split(",");
				for (String fp : filepaths) {
					unitsMissileData
							.computeIfAbsent(fp.toLowerCase(), k -> new NamedList<>(missileArtField, unit.getIconPath()))
							.add(unit.getName());
				}
			} else {
				unitsMissileData
						.computeIfAbsent(missileArtField.toLowerCase(), k -> new NamedList<>(missileArtField, unit.getIconPath()))
						.add(unit.getName());
			}
		}
	}


	private static NamedList<String> getUnitList(Map<String, NamedList<String>> dataMap, Element unit, String filepath) {
//		return getMissileNamedList(dataMap, unit, filepath, filepath);
		return dataMap.computeIfAbsent(filepath.toLowerCase(), k -> new NamedList<>(filepath, unit.getIconPath()));
	}


	private static void fillUnitList(Map<String, NamedList<String>> modelData, DataTable dataTable) {
		for (String str : dataTable.keySet()) {
			// ITEMS
			Element unit = dataTable.get(str);
			String filepath = unit.getField("file");
			if (filepath.length() > 0) {
				modelData.computeIfAbsent(filepath.toLowerCase(), k -> new NamedList<>(filepath, unit.getIconPath())).add(unit.getName() + " <Base>");

				int numVar = unit.getFieldValue("numVar");
				if (numVar > 1) {
					for (int i = 0; i < numVar; i++) {
						String filepath2 = filepath + i + ".mdl";

						modelData
								.computeIfAbsent(filepath2.toLowerCase(), k -> new NamedList<>(filepath2, unit.getIconPath()))
								.add(unit.getName() + " <" + WEString.getString("WESTRING_PREVIEWER_VAR") + " " + (i + 1) + ">");

					}
				}
			}
		}
	}

	private static void addUnitsToList(Map<String, NamedList<String>> unitsData, Element unit, String artName, String weStringTypeSuffix) {
		String filepath = unit.getField(artName);
		if (filepath.length() > 0) {
//			NamedList<String> unitList = getUnitList(unitsData, unit, filepath);
//			unitList.add(unit.getName() + " " + WEString.getString(weStringTypeSuffix));

			unitsData.computeIfAbsent(filepath.toLowerCase(), k -> new NamedList<>(filepath, unit.getIconPath()))
					.add(unit.getName() + " " + WEString.getString(weStringTypeSuffix));
		}
	}

	private static void addModelsToList(Map<String, NamedList<String>> modelsData, Element unit, String artName, String weStringTypeSuffix) {
		String filepath = unit.getField(artName);
		if (filepath.length() > 0) {
//			if (filepath.contains(",")) {
//				filepath = filepath.split(",")[0];
//			}
//			NamedList<String> unitList = getUnitList(modelsData, unit, filepath);
//			unitList.add(unit.getName() + " " + WEString.getString(weStringTypeSuffix));
			String fp = filepath.split(",")[0];
			modelsData.computeIfAbsent(fp.toLowerCase(), k -> new NamedList<>(fp, unit.getIconPath()))
					.add(unit.getName() + " " + WEString.getString(weStringTypeSuffix));
		}
	}
}
