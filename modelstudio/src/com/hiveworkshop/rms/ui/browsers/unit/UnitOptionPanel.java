package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableAbilityData;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnitOptionPanel extends JPanel {
	public static final String TILESETS = "ABKYXJDCIFLWNOZGVQ";
	public static final int ICON_SIZE = 32;

	private GameObject selection = null;
	private final String[] raceKeys = {"human", "orc", "undead", "nightelf", "neutrals", "naga"};

	private final TwiComboBox<String> raceBox;
	private final TwiComboBox<String> meleeBox;
	private final TwiComboBox<String> tilesetBox;
	private final TwiComboBox<String> levelBox;

//	private JComboBox<String> playerBox;
	// DefaultComboBoxModel<String> playerBoxModel = new DefaultComboBoxModel<String>();

	private final JLabel unitsLabel;
	private final JLabel heroesLabel;
	private final JLabel buildingsLabel;
	private final JLabel buildingsUprootedLabel;
	private final JLabel specialLabel;

	private final List<UnitButton> unitButtons = new ArrayList<>();
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private final JPanel buttonsPanel;
	private final JScrollPane buttonsScrollPane;

	private final boolean verticalStyle;
	private boolean firstTime = true;

	private final SortedRaces sortedRaces;


	public GameObject getSelection() {
		return selection;
	}

	public UnitOptionPanel(ObjectData dataTable, ObjectData abilityData) {
		this(dataTable, abilityData, false, false);
	}

	public UnitOptionPanel(ObjectData dataTable, ObjectData abilityData, boolean hideBorder, boolean verticalStyle) {
		setLayout(new MigLayout("fillx", "[]", "[fill]"));
		setMaximumSize(ScreenInfo.getBigWindow());
		sortedRaces = new SortedRaces(dataTable, abilityData);
		this.verticalStyle = verticalStyle;
		unitsLabel = new JLabel(WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
		heroesLabel = new JLabel(WEString.getString("WESTRING_UTYPE_HEROES"));
		buildingsLabel = new JLabel(WEString.getString("WESTRING_UTYPE_BUILDINGS"));
		buildingsUprootedLabel = new JLabel(WEString.getString("WESTRING_UTYPE_BUILDINGS_UPROOTED"));
		specialLabel = new JLabel(WEString.getString("WESTRING_UTYPE_SPECIAL"));

		String[] raceStrings = {"WESTRING_RACE_HUMAN", "WESTRING_RACE_ORC", "WESTRING_RACE_UNDEAD", "WESTRING_RACE_NIGHTELF", "WESTRING_RACE_NEUTRAL", "WESTRING_RACE_NEUTRAL_NAGA"};
		List<String> raceList = getWEStringsOf(raceStrings);

		String[] neutralRaceStrings = {"WESTRING_RACE_NEUTRAL", "WESTRING_RACE_NEUTRAL_NAGA"};
		List<String> raceNeutralList = getWEStringsOf(neutralRaceStrings);

		String[] meleeStrings = {"WESTRING_MELEE", "WESTRING_CAMPAIGN", "WESTRING_CUSTOM"};
		List<String> meleeList = getWEStringsOf(meleeStrings);
		meleeList.add(WEString.getString("WESTRING_ITEMSTATUS_HIDDEN").replace("\"", ""));


		List<String> tileSetList = getWEStringsOf(WE_LOC.values());

		List<String> levelList = new ArrayList<>();
		levelList.add(WEString.getString("WESTRING_ANYLEVEL"));
		for (int i = 0; i <= 20; i++) {
			levelList.add(WEString.getString("WESTRING_LEVEL") + String.format(" %d", i));
		}

		buttonsPanel = new JPanel(new MigLayout("ins 0, wrap 7"));
		buttonsScrollPane = getButtonsScrollPane();

		// playerBox = getComboBox(playerBoxModel);

		raceBox = getComboBox(raceList);
		meleeBox = getComboBox(meleeList);
		tilesetBox = getComboBox(tileSetList);
		levelBox = getComboBox(levelList);

		tilesetBox.setSelectedIndex(10);
	}

	public TwiComboBox<String> getComboBox(List<String> strings) {
		TwiComboBox<String> comboBox = new TwiComboBox<>(strings, "Prototype a prototype");
		comboBox.addActionListener(e -> relayout());
		comboBox.setMaximumSize(new Dimension(10000, 25));
		return comboBox;
	}

	private JScrollPane getButtonsScrollPane() {
		JScrollPane buttonsScrollPane = new JScrollPane(buttonsPanel);
		buttonsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		buttonsScrollPane.setFocusable(true);
		buttonsScrollPane.setMaximumSize(ScreenInfo.getBigWindow());
//		if (hideBorder) {
//			buttonsScrollPane.setBorder(null);
//		}
		buttonsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		buttonsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		return buttonsScrollPane;
	}

	protected boolean isShowLevel(String race) {
		return !Arrays.asList(raceKeys).contains(race);
	}

	public static void dropRaceCache() {
		SortedRaces.dropRaceCache();
	}

	private String raceKey() {
		int selectedIndex = raceBox.getSelectedIndex();
		if (0 <= selectedIndex && selectedIndex < raceKeys.length) {
			return raceKeys[selectedIndex];

		} else {
			return raceKeys[0];
		}
//		if (raceBox.getModel() == raceBoxModel) {
//		} else if (selectedIndex == 1) {
//			return "naga";
//		}
//		return "neutrals";
	}

	public void relayout() {
		removeAll();

		String race = raceKey();
		String tileSet = TILESETS.charAt(tilesetBox.getSelectedIndex()) + "";
		boolean isNeutral = race.equals("neutrals");
		boolean checkLevel = levelBox.getSelectedIndex() > 0 && isNeutral;

		clearButtons();

		boolean neutrals = race.equals("neutrals");
		tilesetBox.setVisible(neutrals);
		levelBox.setVisible(neutrals);
		add(raceBox, "growx, gapx 4, split 2, spanx");
		add(meleeBox, "growx, wrap");
		if (verticalStyle) {
			add(tilesetBox, "growx, gapx 4, split 2, spanx");
			add(levelBox, "growx, gapy 4, wrap");
		} else if (neutrals) {
			add(tilesetBox, "growx, gapx 4, split 2, spanx");
			add(levelBox, "growx, gapy 4, wrap");
		}

		add(unitsLabel, "growx, spanx, wrap");
		add(buttonsScrollPane, "growx, growy, spanx, spany");

		setupButtonsPanel();


		RaceData data = sortedRaces.get(race + getMeleeType());
		fillWithButtons(tileSet, isNeutral, checkLevel, data.units);
		addComponents(tileSet, isNeutral, checkLevel, data.heroes, heroesLabel);
		addComponents(tileSet, isNeutral, checkLevel, data.buildings, buildingsLabel);
		addComponents(tileSet, isNeutral, checkLevel, data.buildingsUprooted, buildingsUprootedLabel);
		addComponents(tileSet, isNeutral, checkLevel, data.special, specialLabel);

		revalidate();
	}

	private void clearButtons() {
		buttonsPanel.removeAll();
		buttonGroup.clearSelection();
		for (UnitButton ub : unitButtons) {
			buttonGroup.remove(ub);
		}
		unitButtons.clear();
	}

	private void setupButtonsPanel() {
		int scrollbarWith = buttonsScrollPane.getVerticalScrollBar().getWidth();
		System.out.println("scrollbarWith: " + scrollbarWith);
		int rowLength = Math.max(1, (buttonsScrollPane.getWidth() - scrollbarWith) / 32);
		if (firstTime) {
			rowLength = 8;
			firstTime = false;
		}

		buttonsPanel.setLayout(new MigLayout("wrap " + (rowLength - 1)));
		buttonsScrollPane.setMaximumSize(ScreenInfo.getBigWindow());
	}

	private String getMeleeType() {
		return switch (meleeBox.getSelectedIndex()){
			case 1 -> "campaign";
			case 2 -> "custom";
			case 3 -> "hidden";
			default -> "melee";
		};
	}

	public void addComponents(String tileSet, boolean isNeutral, boolean checkLevel, List<GameObject> gameObjects, JLabel label) {
		for (GameObject unit : gameObjects) {
			if (isValid(tileSet, isNeutral, checkLevel, unit)) {
				buttonsPanel.add(label, "newline 12, span, wrap 4");
				break;
			}
		}
		if (gameObjects.size() > 0) {
			fillWithButtons(tileSet, isNeutral, checkLevel, gameObjects);
		}
	}

	private boolean isValid(String tileSet, boolean isNeutral, boolean checkLevel, GameObject unit) {
		boolean b1 = isNeutral
				&& !unit.getField("tilesets").contains(tileSet)
				&& !unit.getField("tilesets").contains("*")
				&& !unit.getField("tilesets").contains("_");
		boolean level = checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1;
		return !(b1 || level);
	}

	public void fillWithButtons(String tileSet, boolean isNeutral, boolean checkLevel, List<GameObject> gameObjects) {

		for (GameObject unit : gameObjects) {
			if (isValid(tileSet, isNeutral, checkLevel, unit)) {
				UnitButton unitButton = new UnitButton(this::unitChosen, isShowLevel(unit.getField("race")), unit);
				buttonGroup.add(unitButton);
				unitButtons.add(unitButton);
				buttonsPanel.add(unitButton);
			}
		}
	}

	public void unitChosen(UnitButton button) {
		for (UnitButton btn : unitButtons) {
			btn.setEnabled(true);
		}
		if (unitButtons.contains(button)) {
			button.setEnabled(false);
			String name = button.getUnit().getName();

			String race = button.getUnit().getField("race");
			if (isShowLevel(race)) {
				name += " - " + WEString.getString("WESTRING_LEVEL") + " " + button.getUnit().getFieldValue("level");
			}
			unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + name);

			selection = button.getUnit();
		} else {
			selection = null;
			unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
		}
	}

	private DefaultComboBoxModel<String> getBoxModelOf(String[] strings) {
		DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<>();
		for (String string : strings) {
			boxModel.addElement(WEString.getString(string));
		}
		return boxModel;
	}
	private List<String> getWEStringsOf(String[] strings) {
		List<String> boxModel = new ArrayList<>();
		for (String string : strings) {
			boxModel.add(WEString.getString(string));
		}
		return boxModel;
	}
	private DefaultComboBoxModel<String> getBoxModelOf(Enum<?>[] e) {
		DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<>();
		for (Object o : e) {
			boxModel.addElement(WEString.getString(o.toString()));
		}
		return boxModel;
	}
	private List<String> getWEStringsOf(Enum<?>[] e) {
		List<String> boxModel = new ArrayList<>();
		for (Object o : e) {
			boxModel.add(WEString.getString(o.toString()));
		}
		return boxModel;
	}

	public static GameObject getGameObject(Component component) {
		UnitOptionPanel uop = new UnitOptionPanel(DataTableHolder.getDefault(), MutableAbilityData.getStandardAbilities());
		int x = JOptionPane.showConfirmDialog(component, uop, "Choose Unit Type", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			GameObject choice = uop.getSelection();
			if(choice != null && isValidFilepath(choice.getField("file"))){
				return choice;
			}
		}
		return null;
	}

	private static boolean isValidFilepath(String filepath) {
		try {
			//check model by converting its path
			ImportFileActions.convertPathToMDX(filepath);
		} catch (final Exception exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.frame,
					"The chosen model could not be used.",
					"Program Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
