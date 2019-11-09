package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

import de.wc3data.stream.BlizzardDataOutputStream;
import mpq.MPQException;

public class GridHotkeyMaker {
	private static final War3ID UNIT_BUTTON_X = War3ID.fromString("ubpx");
	private static final War3ID UNIT_BUTTON_Y = War3ID.fromString("ubpy");
	private static final War3ID UNIT_TOOLTIP = War3ID.fromString("utip");
	private static final War3ID UNIT_HOTKEY = War3ID.fromString("uhot");
	private static final War3ID UNIT_TRAINS = War3ID.fromString("utra");
	private static final War3ID STRUCTURES_BUILT = War3ID.fromString("ubui");
	private static final War3ID UNITS_SOLD = War3ID.fromString("useu");
	private static final War3ID ITEMS_SOLD = War3ID.fromString("usei");
	private static final War3ID RESEARCHES_AVAILABLE = War3ID.fromString("ures");
	private static final War3ID ITEMS_MADE = War3ID.fromString("umki");
	private static final War3ID UNIT_ABILITIES = War3ID.fromString("uabi");
	private static final War3ID HERO_ABILITIES = War3ID.fromString("uhab");
	private static final UILookupField[] MENU_MAKING_KEYS = {
			new UILookupField(UNIT_TRAINS, MutableObjectData.WorldEditorDataType.UNITS, true),
			new UILookupField(STRUCTURES_BUILT, MutableObjectData.WorldEditorDataType.UNITS, false),
			new UILookupField(ITEMS_SOLD, MutableObjectData.WorldEditorDataType.ITEM, true),
			new UILookupField(RESEARCHES_AVAILABLE, MutableObjectData.WorldEditorDataType.UPGRADES, true),
			new UILookupField(ITEMS_MADE, MutableObjectData.WorldEditorDataType.ITEM, true),
			new UILookupField(UNITS_SOLD, MutableObjectData.WorldEditorDataType.UNITS, true),
			new UILookupField(UNITS_SOLD, MutableObjectData.WorldEditorDataType.UNITS, true),
			new UILookupField(UNIT_ABILITIES, MutableObjectData.WorldEditorDataType.ABILITIES, true),
			new UILookupField(HERO_ABILITIES, MutableObjectData.WorldEditorDataType.ABILITIES, false), };

	private static final War3ID ITEM_BUTTON_X = War3ID.fromString("ubpx");
	private static final War3ID ITEM_BUTTON_Y = War3ID.fromString("ubpy");

	private static final War3ID ABIL_ART = War3ID.fromString("aart");
	private static final War3ID ABIL_BUTTON_X = War3ID.fromString("abpx");
	private static final War3ID ABIL_BUTTON_Y = War3ID.fromString("abpy");
	private static final War3ID ABIL_LEVELS = War3ID.fromString("alev");
	private static final War3ID ABIL_TOOLTIP = War3ID.fromString("atp1");
	private static final War3ID ABIL_UN_TOOLTIP = War3ID.fromString("aut1");
	private static final War3ID ABIL_LEARN_TOOLTIP = War3ID.fromString("aret");
	private static final War3ID ABIL_HOTKEY = War3ID.fromString("ahky");
	private static final War3ID ABIL_UNHOTKEY = War3ID.fromString("auhk");
	private static final War3ID ABIL_LEARN_HOTKEY = War3ID.fromString("arhk");
	private static final War3ID ABIL_LEARN_BUTTON_X = War3ID.fromString("arpx");
	private static final War3ID ABIL_LEARN_BUTTON_Y = War3ID.fromString("arpy");
	private static final War3ID ABIL_OFF_BUTTON_X = War3ID.fromString("aubx");
	private static final War3ID ABIL_OFF_BUTTON_Y = War3ID.fromString("auby");

	private static final War3ID UPGR_BUTTON_X = War3ID.fromString("gbpx");
	private static final War3ID UPGR_BUTTON_Y = War3ID.fromString("gbpy");
	private static final War3ID UPGR_HOTKEY = War3ID.fromString("ghk1");
	private static final War3ID UPGR_TOOLTIP = War3ID.fromString("gtp1");
	private static final War3ID UPGR_LEVELS = War3ID.fromString("glvl");

	private static final Map<MutableGameObject, Integer> objectToXCoord = new HashMap<>();
	private static final Map<MutableGameObject, Integer> objectToYCoord = new HashMap<>();
	private static final Map<MutableGameObject, Integer> objectToLearnXCoord = new HashMap<>();
	private static final Map<MutableGameObject, Integer> objectToLearnYCoord = new HashMap<>();

	private static final String hexcodes = "0123456789abcdef";

	private static boolean hasColorCodes(final String xx) {
		final String lowerCase = xx.toLowerCase();
		final int colorIndex = lowerCase.indexOf("|c");
		if (colorIndex == -1) {
			return false;
		}
		if ((lowerCase.length() - colorIndex) < 10) {
			return false;
		}
		for (int i = 0; i < 8; i++) {
			final char c = lowerCase.charAt(i + colorIndex + 2);
			if (hexcodes.indexOf(c) == -1) {
				return false;
			}
		}
		return true;
	}

	public static void main(final String[] args) {
		final LoadedMPQ mpq;
		try {
			mpq = MpqCodebase.get().loadMPQ(Paths
					.get("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/Altered Melee/(2)HFAlteracIsle.w3x"));
		} catch (final MPQException e1) {
			e1.printStackTrace();
			return;
		} catch (final IOException e1) {
			e1.printStackTrace();
			return;
		}
		Warcraft3MapObjectData objectData;
		try {
			objectData = Warcraft3MapObjectData.load(true);
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		}
		final String[][] grid = { { "Q", "W", "E", "R" }, { "A", "S", "D", "F" }, { "Z", "X", "C", "V" } };
//        System.out.println(objectData.getUpgrades().get(War3ID.fromString("Rhss")).getFieldAsInteger(War3ID.fromString("gbpx"),0));
//        System.exit(0);
		for (final War3ID unitId : objectData.getUnits().keySet()) {
			final MutableGameObject unitType = objectData.getUnits().get(unitId);
			final boolean[][] sharedCommandCardButtons = new boolean[3][4];
			for (final UILookupField menuMakingKey : MENU_MAKING_KEYS) {
				final String trainsList = unitType.getFieldAsString(menuMakingKey.getFieldMetaKey(), 0);
				String[] trainsListItems = trainsList.split(",");
				if (trainsList.length() == 4) {
					trainsListItems = new String[] { trainsList };
				}
				final boolean[][] usedButtonPositions = menuMakingKey.isSharedCommandCard() ? sharedCommandCardButtons
						: new boolean[3][4];
				if ((menuMakingKey.getFieldMetaKey() == UNIT_TRAINS) && (trainsListItems.length > 1)) {
					usedButtonPositions[1][3] = true;
				}
				for (String key : trainsListItems) {
					key = key.trim();
					if (key.length() < 4) {
						continue;
					}
					if (key.length() > 4) {
						key = key.substring(0, 4);
					}
					if (key.equals("R00T")) {
						System.err.println("Found Zear Backpack 1!");
					}
					final War3ID trainedUnitId = War3ID.fromString(key);
					final MutableObjectData dataByType = objectData.getDataByType(menuMakingKey.getDataType());
					if (dataByType == null) {
						throw new IllegalStateException(menuMakingKey.getDataType() + " is null");
					}
					final MutableGameObject trainedUnit = dataByType.get(trainedUnitId);
					if (trainedUnit == null) {
						System.err.println(trainedUnitId);
					} else {
						if (unitId.toString().equals("h013")) {
							System.err.println("Assigning " + trainedUnit.getFieldAsString(
									menuMakingKey.getDataType() == MutableObjectData.WorldEditorDataType.UPGRADES
											? War3ID.fromString("gnam")
											: menuMakingKey
													.getDataType() == MutableObjectData.WorldEditorDataType.ABILITIES
															? War3ID.fromString("anam")
															: War3ID.fromString("unam"),
									0));
						}
						if (menuMakingKey.getDataType() == MutableObjectData.WorldEditorDataType.ABILITIES) {
							final String abilArt = trainedUnit.getFieldAsString(ABIL_ART, 0);
							if ((abilArt == null) || (abilArt.length() == 0) || (abilArt.length() == 1)) {
								continue;
							}
						}
						War3ID buttonX;
						War3ID buttonY;
						switch (menuMakingKey.getDataType()) {
						case ABILITIES:
							if (HERO_ABILITIES.equals(menuMakingKey.getFieldMetaKey())) {
								buttonX = ABIL_LEARN_BUTTON_X;
								buttonY = ABIL_LEARN_BUTTON_Y;
							} else {
								buttonX = ABIL_BUTTON_X;
								buttonY = ABIL_BUTTON_Y;
							}
							break;
						case ITEM:
							buttonX = ITEM_BUTTON_X;
							buttonY = ITEM_BUTTON_Y;
							break;
						case UNITS:
							buttonX = UNIT_BUTTON_X;
							buttonY = UNIT_BUTTON_Y;
							break;
						case UPGRADES:
							buttonX = UPGR_BUTTON_X;
							buttonY = UPGR_BUTTON_Y;
							break;
						case DOODADS:
						case DESTRUCTIBLES:
						case BUFFS_EFFECTS:
						default:
							buttonX = ITEM_BUTTON_X;
							buttonY = UNIT_BUTTON_Y;
							break;
						}
						int xPosition = trainedUnit.getFieldAsInteger(buttonX, 0);
						int yPosition = trainedUnit.getFieldAsInteger(buttonY, 0);
						if (key.equals("R00T")) {
							System.err.println("Found Zear Backpack!");
							System.err.println("Lookup to data got " + trainedUnit.getFieldAsString(
									menuMakingKey.getDataType() == MutableObjectData.WorldEditorDataType.UPGRADES
											? War3ID.fromString("gnam")
											: menuMakingKey
													.getDataType() == MutableObjectData.WorldEditorDataType.ABILITIES
															? War3ID.fromString("anam")
															: War3ID.fromString("unam"),
									0) + " to (" + xPosition + "," + yPosition + ")");

						}
						while ((xPosition >= 0) && (xPosition <= 3) && (yPosition >= 0) && (yPosition <= 2)
								&& usedButtonPositions[yPosition][xPosition]) {
							xPosition++;
							if (xPosition > 3) {
								xPosition = 0;
								yPosition++;
							}
						}
						if ((xPosition >= 0) && (xPosition <= 3) && (yPosition >= 0) && (yPosition <= 2)) {
							if (unitId.toString().equals("h013")) {
								System.err.println("Assigning " + trainedUnit.getFieldAsString(
										menuMakingKey.getDataType() == MutableObjectData.WorldEditorDataType.UPGRADES
												? War3ID.fromString("gnam")
												: menuMakingKey
														.getDataType() == MutableObjectData.WorldEditorDataType.ABILITIES
																? War3ID.fromString("anam")
																: War3ID.fromString("unam"),
										0) + " to (" + xPosition + "," + yPosition + ")");
							}
							usedButtonPositions[yPosition][xPosition] = true;
							if (HERO_ABILITIES.equals(menuMakingKey.getFieldMetaKey())) {
								objectToLearnXCoord.put(trainedUnit, xPosition);
								objectToLearnYCoord.put(trainedUnit, yPosition);
							} else {
								objectToXCoord.put(trainedUnit, xPosition);
								objectToYCoord.put(trainedUnit, yPosition);
							}
						}
					}
				}
			}
		}
		for (final War3ID unitId : objectData.getUnits().keySet()) {
			final MutableGameObject unitType = objectData.getUnits().get(unitId);
			final Integer xPositionCached = objectToXCoord.get(unitType);
			final Integer yPositionCached = objectToYCoord.get(unitType);
			final int xPosition = xPositionCached != null ? xPositionCached
					: unitType.getFieldAsInteger(UNIT_BUTTON_X, 0);
			final int yPosition = yPositionCached != null ? yPositionCached
					: unitType.getFieldAsInteger(UNIT_BUTTON_Y, 0);
			String tooltip = unitType.getFieldAsString(UNIT_TOOLTIP, 0).replace("|r", "").replace("|R", "");
			String gridHotkey = "_";
			if ((xPosition >= 0) && (yPosition >= 0) && (yPosition < grid.length)
					&& (xPosition < grid[yPosition].length)) {
				gridHotkey = grid[yPosition][xPosition];
			}
			unitType.setField(UNIT_HOTKEY, 0, gridHotkey);
			tooltip = processTooltip(gridHotkey, tooltip);
			unitType.setField(UNIT_TOOLTIP, 0, tooltip);
		}
		for (final War3ID unitId : objectData.getItems().keySet()) {
			final MutableGameObject unitType = objectData.getItems().get(unitId);
			final Integer xPositionCached = objectToXCoord.get(unitType);
			final Integer yPositionCached = objectToYCoord.get(unitType);
			final int xPosition = xPositionCached != null ? xPositionCached
					: unitType.getFieldAsInteger(ITEM_BUTTON_X, 0);
			final int yPosition = yPositionCached != null ? yPositionCached
					: unitType.getFieldAsInteger(ITEM_BUTTON_Y, 0);
			String tooltip = unitType.getFieldAsString(UNIT_TOOLTIP, 0).replace("|r", "").replace("|R", "");
			String gridHotkey = "_";
			if ((xPosition >= 0) && (yPosition >= 0) && (yPosition < grid.length)
					&& (xPosition < grid[yPosition].length)) {
				gridHotkey = grid[yPosition][xPosition];
			}
			tooltip = processTooltip(gridHotkey, tooltip);
			unitType.setField(UNIT_HOTKEY, 0, gridHotkey);
			unitType.setField(UNIT_TOOLTIP, 0, tooltip);
		}
		for (final War3ID unitId : objectData.getAbilities().keySet()) {
			final MutableGameObject unitType = objectData.getAbilities().get(unitId);
			{
				final Integer xPositionCached = objectToXCoord.get(unitType);
				final Integer yPositionCached = objectToYCoord.get(unitType);
				final int xPosition = xPositionCached != null ? xPositionCached
						: unitType.getFieldAsInteger(ABIL_BUTTON_X, 0);
				final int yPosition = yPositionCached != null ? yPositionCached
						: unitType.getFieldAsInteger(ABIL_BUTTON_Y, 0);
				String gridHotkey = "_";
				if ((xPosition >= 0) && (yPosition >= 0) && (yPosition < grid.length)
						&& (xPosition < grid[yPosition].length)) {
					gridHotkey = grid[yPosition][xPosition];
				}
				unitType.setField(ABIL_HOTKEY, 0, gridHotkey);
				for (int i = 0; i <= unitType.getFieldAsInteger(ABIL_LEVELS, 0); i++) {
					String tooltip = unitType.getFieldAsString(ABIL_TOOLTIP, i).replace("|r", "").replace("|R", "");
					if ((tooltip != null) && (tooltip.length() > 1)) {

						tooltip = processTooltip(gridHotkey, tooltip);
						unitType.setField(ABIL_TOOLTIP, i, tooltip);
					}
				}
				boolean processedUnTooltips = false;
				for (int i = 0; i <= unitType.getFieldAsInteger(ABIL_LEVELS, 0); i++) {
					String tooltip = unitType.getFieldAsString(ABIL_UN_TOOLTIP, i).replace("|r", "").replace("|R", "");
					if ((tooltip != null) && (tooltip.length() > 1)) {
						processedUnTooltips = true;
						tooltip = processTooltip(gridHotkey, tooltip);
						unitType.setField(ABIL_UN_TOOLTIP, i, tooltip);
					}
				}
				if (processedUnTooltips) {
					unitType.setField(ABIL_UNHOTKEY, 0, gridHotkey);
				}
			}
			{
				final Integer xPositionCached = objectToLearnXCoord.get(unitType);
				final Integer yPositionCached = objectToLearnYCoord.get(unitType);
				final int xPosition = xPositionCached != null ? xPositionCached
						: unitType.getFieldAsInteger(ABIL_LEARN_BUTTON_X, 0);
				final int yPosition = yPositionCached != null ? yPositionCached
						: unitType.getFieldAsInteger(ABIL_LEARN_BUTTON_Y, 0);
				String gridHotkey = "_";
				if ((xPosition >= 0) && (yPosition >= 0) && (yPosition < grid.length)
						&& (xPosition < grid[yPosition].length)) {
					gridHotkey = grid[yPosition][xPosition];
				}
				unitType.setField(ABIL_LEARN_HOTKEY, 0, gridHotkey);
				String tooltip = unitType.getFieldAsString(ABIL_LEARN_TOOLTIP, 0).replace("|r", "").replace("|R", "");
				if ((tooltip != null) && (tooltip.length() > 1)) {
					tooltip = processTooltip(gridHotkey, tooltip);
					unitType.setField(ABIL_LEARN_TOOLTIP, 0, tooltip);
				}
			}
		}
		for (final War3ID unitId : objectData.getUpgrades().keySet()) {
			final MutableGameObject unitType = objectData.getUpgrades().get(unitId);
			final Integer xPositionCached = objectToXCoord.get(unitType);
			final Integer yPositionCached = objectToYCoord.get(unitType);
			final int levels = unitType.getFieldAsInteger(UPGR_LEVELS, 0);
			for (int i = 1; i <= levels; i++) {
				final int xPosition = xPositionCached != null ? xPositionCached
						: unitType.getFieldAsInteger(UPGR_BUTTON_X, i);
				final int yPosition = yPositionCached != null ? yPositionCached
						: unitType.getFieldAsInteger(UPGR_BUTTON_Y, i);
				String gridHotkey = "_";
				if ((xPosition >= 0) && (yPosition >= 0) && (yPosition < grid.length)
						&& (xPosition < grid[yPosition].length)) {
					gridHotkey = grid[yPosition][xPosition];
				}
				unitType.setField(UPGR_HOTKEY, i, gridHotkey);
				String tooltip = unitType.getFieldAsString(UPGR_TOOLTIP, i).replace("|r", "").replace("|R", "");
				if ((tooltip != null) && (tooltip.length() > 1)) {

					tooltip = processTooltip(gridHotkey, tooltip);
					unitType.setField(UPGR_TOOLTIP, i, tooltip);
				}
			}
		}
		try {
			try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(
					new File("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/Altered Melee/HFGen/out.w3u"),
					false)) {
				objectData.getUnits().getEditorData().save(outputStream, false);
			}
			try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(
					new File("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/Altered Melee/HFGen/out.w3t"),
					false)) {
				objectData.getItems().getEditorData().save(outputStream, false);
			}
			try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(
					new File("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/Altered Melee/HFGen/out.w3a"),
					false)) {
				objectData.getAbilities().getEditorData().save(outputStream, false);
			}
			try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(
					new File("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/Altered Melee/HFGen/out.w3q"),
					false)) {
				objectData.getUpgrades().getEditorData().save(outputStream, false);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}

		mpq.unload();
	}

	private static String processTooltip(final String gridHotkey, String tooltip) {
		while (hasColorCodes(tooltip)) {
			final int colorIndex = tooltip.toLowerCase().indexOf("|c");
			tooltip = tooltip.substring(0, colorIndex) + tooltip.substring(colorIndex + 10);
		}
		if (tooltip.contains(" (") && tooltip.contains(")")) {
			final int openPar = tooltip.lastIndexOf("(");
			final int closePar = tooltip.lastIndexOf(")");
			if ((closePar - openPar) == 2) {
				// Strip off the " (Q)" suffix from the Tinker
				tooltip = tooltip.substring(0, openPar - 1);
			}
		}
		final int indexOfLevelInfo = tooltip.indexOf(" - [");
		if (indexOfLevelInfo != -1) {
			tooltip = tooltip.substring(0, indexOfLevelInfo) + " (|cffffcc00" + gridHotkey + "|r)"
					+ tooltip.substring(indexOfLevelInfo);
		} else {
			tooltip = tooltip + " (|cffffcc00" + gridHotkey + "|r)";
		}
		return tooltip;
	}

	private static final class UILookupField {
		private final War3ID fieldMetaKey;
		private final MutableObjectData.WorldEditorDataType dataType;
		private final boolean sharedCommandCard;

		public UILookupField(final War3ID fieldMetaKey, final MutableObjectData.WorldEditorDataType dataType,
				final boolean sharedCommandCard) {
			this.fieldMetaKey = fieldMetaKey;
			this.dataType = dataType;
			this.sharedCommandCard = sharedCommandCard;
		}

		public War3ID getFieldMetaKey() {
			return fieldMetaKey;
		}

		public MutableObjectData.WorldEditorDataType getDataType() {
			return dataType;
		}

		public boolean isSharedCommandCard() {
			return sharedCommandCard;
		}
	}
}
