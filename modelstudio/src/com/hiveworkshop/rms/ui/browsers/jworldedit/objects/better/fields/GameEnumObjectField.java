package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameEnumObjectField extends AbstractObjectField {

	private final String metaMetaKeyName;
	private final String metaMetaWestringName;
	private final DataTable worldEditorData;

	public GameEnumObjectField(String displayName, String sortName, String rawDataName,
	                           boolean showingLevelDisplay, War3ID metaKey, int level,
	                           WorldEditorDataType dataType, GameObject metaDataField, String metaMetaKeyName,
	                           String metaMetaWestringName, DataTable worldEditorData) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
		this.metaMetaKeyName = metaMetaKeyName;
		this.metaMetaWestringName = metaMetaWestringName;
		this.worldEditorData = worldEditorData;
	}
	public GameEnumObjectField(String displayName, String sortName, String rawDataName,
	                           boolean showingLevelDisplay, War3ID metaKey, int level,
	                           WorldEditorDataType dataType, GameObject metaDataField) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);

		this.metaMetaKeyName = "unitRace";
		this.metaMetaWestringName = "WESTRING_COD_TYPE_UNITRACE";
		this.worldEditorData = StandardObjectData.getUnitEditorData();
	}

	@Override
	protected Object getValue(MutableGameObject gameUnit, War3ID metaKey, int level) {
		return gameUnit.getFieldAsString(metaKey, level);
	}

	@Override
	protected boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData,
	                              boolean disableLimits, War3ID metaKey, int level, String defaultDialogTitle,
	                              GameObject metaDataField) {

		Element itemClasses = worldEditorData.get("unitRace");
		int numValues = itemClasses.getFieldValue("NumValues");

		String fieldAsString = gameUnit.getFieldAsString(metaKey, level);


		JPanel popupPanel = new JPanel();
		popupPanel.add(new JLabel(getDisplayName(gameUnit)));

		final String title = String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_UNITRACE"));
		if (disableLimits) {
			JTextField textField = new JTextField(fieldAsString, StringObjectField.STRING_FIELD_COLUMNS);
			popupPanel.add(textField);

			int result = FieldPopupUtils.showPopup(parent, popupPanel, title, textField);

			if (result == JOptionPane.OK_OPTION) {
				gameUnit.setField(metaKey, level, textField.getText());
				return true;
			}
		} else {
			JComboBox<GameEnumChoice> itemClassCombo = getComboBox(editRawData, itemClasses, numValues, fieldAsString);
			popupPanel.add(itemClassCombo);

			int result = FieldPopupUtils.showPopup(parent, popupPanel, title, itemClassCombo);

			if (result == JOptionPane.OK_OPTION) {
				gameUnit.setField(metaKey, level, ((GameEnumChoice) itemClassCombo.getSelectedItem()).getCategoryKey());
				return true;
			}
		}
		return false;
	}

	private JComboBox<GameEnumChoice> getComboBox(boolean editRawData, Element itemClasses, int numValues, String fieldAsString) {
		List<GameEnumChoice> itemClassesList = getItemClassesList(editRawData, itemClasses, numValues);

		GameEnumChoice selectedItemClass = itemClassesList.stream()
				.filter(itemClass -> itemClass.getCategoryKey().equals(fieldAsString))
				.findFirst()
				.orElse(null);


		JComboBox<GameEnumChoice> itemClassCombo = new JComboBox<>(itemClassesList.toArray(new GameEnumChoice[0]));
		itemClassCombo.setEditable(false);
		if (selectedItemClass != null) {
			itemClassCombo.setSelectedItem(selectedItemClass);
		}
		return itemClassCombo;
	}

	private List<GameEnumChoice> getItemClassesList(boolean editRawData, Element itemClasses, int numValues) {
		List<GameEnumChoice> itemClassesList = new ArrayList<>();
		for (int i = 0; i < numValues; i++) {
			String categoryKey = String.format("%2d", i).replace(' ', '0');
			String value = itemClasses.getField(categoryKey, 0);
			String displayName = itemClasses.getField(categoryKey, 1);
//			String categoryData = itemClasses.getField(categoryKey);
//			String[] categoryFields = categoryData.split(",");
//			String value = categoryFields[0];
//			String displayName = categoryFields[1];

			if (displayName.startsWith("WESTRING")) {
				displayName = WEString.getString(displayName);
			}
			GameEnumChoice itemClass = new GameEnumChoice(value, editRawData ? value : displayName);
			itemClassesList.add(itemClass);
		}
		itemClassesList.sort(Comparator.comparing(GameEnumChoice::getCategoryDisplay));
		return itemClassesList;
	}

	private static final class GameEnumChoice {
		private final String categoryKey;
		private final String categoryDisplay;

		public GameEnumChoice(String categoryKey, String categoryDisplay) {
			this.categoryKey = categoryKey;
			this.categoryDisplay = categoryDisplay;
		}

		public String getCategoryDisplay() {
			return categoryDisplay;
		}

		public String getCategoryKey() {
			return categoryKey;
		}

		@Override
		public String toString() {
			return categoryDisplay;
		}
	}

}
//package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;
//
//import com.hiveworkshop.rms.parsers.slk.DataTable;
//import com.hiveworkshop.rms.parsers.slk.Element;
//import com.hiveworkshop.rms.parsers.slk.GameObject;
//import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
//import com.hiveworkshop.rms.util.War3ID;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//
//public class GameEnumObjectField extends AbstractObjectField {
//
//	private final String metaMetaKeyName;
//	private final String metaMetaWestringName;
//	private final DataTable worldEditorData;
//
//	public GameEnumObjectField(String displayName, String sortName, String rawDataName,
//	                           boolean showingLevelDisplay, War3ID metaKey, int level,
//	                           WorldEditorDataType dataType, GameObject metaDataField, String metaMetaKeyName,
//	                           String metaMetaWestringName, DataTable worldEditorData) {
//		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
//		this.metaMetaKeyName = metaMetaKeyName;
//		this.metaMetaWestringName = metaMetaWestringName;
//		this.worldEditorData = worldEditorData;
//	}
//	public GameEnumObjectField(String displayName, String sortName, String rawDataName,
//	                           boolean showingLevelDisplay, War3ID metaKey, int level,
//	                           WorldEditorDataType dataType, GameObject metaDataField) {
//		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
//
//		this.metaMetaKeyName = "unitRace";
//		this.metaMetaWestringName = "WESTRING_COD_TYPE_UNITRACE";
//		this.worldEditorData = StandardObjectData.getUnitEditorData();
//	}
//
//	@Override
//	protected Object getValue(MutableGameObject gameUnit, War3ID metaKey, int level) {
//		return gameUnit.getFieldAsString(metaKey, level);
//	}
//
//	@Override
//	protected boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData,
//	                              boolean disableLimits, War3ID metaKey, int level, String defaultDialogTitle,
//	                              GameObject metaDataField) {
//
//		Element itemClasses = worldEditorData.get(metaMetaKeyName);
//		int numValues = itemClasses.getFieldValue("NumValues");
//
//		String fieldAsString = gameUnit.getFieldAsString(metaKey, level);
//
//
//		JPanel popupPanel = new JPanel();
//		popupPanel.add(new JLabel(getDisplayName(gameUnit)));
//
//		final String title = String.format(defaultDialogTitle, WEString.getString(metaMetaWestringName));
//		if (disableLimits) {
//			JTextField textField = new JTextField(fieldAsString, StringObjectField.STRING_FIELD_COLUMNS);
//			popupPanel.add(textField);
//
//			int result = FieldPopupUtils.showPopup(parent, popupPanel, title, textField);
//
//			if (result == JOptionPane.OK_OPTION) {
//				gameUnit.setField(metaKey, level, textField.getText());
//				return true;
//			}
//		} else {
//			JComboBox<GameEnumChoice> itemClassCombo = getComboBox(editRawData, itemClasses, numValues, fieldAsString);
//			popupPanel.add(itemClassCombo);
//
//			int result = FieldPopupUtils.showPopup(parent, popupPanel, title, itemClassCombo);
//
//			if (result == JOptionPane.OK_OPTION) {
//				gameUnit.setField(metaKey, level, ((GameEnumChoice) itemClassCombo.getSelectedItem()).getCategoryKey());
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private JComboBox<GameEnumChoice> getComboBox(boolean editRawData, Element itemClasses, int numValues, String fieldAsString) {
//		List<GameEnumChoice> itemClassesList = getItemClassesList(editRawData, itemClasses, numValues);
//
//		GameEnumChoice selectedItemClass = null;
//		for (GameEnumChoice itemClass : itemClassesList) {
//			if (itemClass.getCategoryKey().equals(fieldAsString)) {
//				selectedItemClass = itemClass;
//				break;
//			}
//		}
//
//		JComboBox<GameEnumChoice> itemClassCombo = new JComboBox<>(itemClassesList.toArray(new GameEnumChoice[0]));
//		itemClassCombo.setEditable(false);
//		if (selectedItemClass != null) {
//			itemClassCombo.setSelectedItem(selectedItemClass);
//		}
//		return itemClassCombo;
//	}
//
//	private List<GameEnumChoice> getItemClassesList(boolean editRawData, Element itemClasses, int numValues) {
//		List<GameEnumChoice> itemClassesList = new ArrayList<>();
//		for (int i = 0; i < numValues; i++) {
//			String categoryKey = String.format("%2d", i).replace(' ', '0');
//			String categoryData = itemClasses.getField(categoryKey);
//			String[] categoryFields = categoryData.split(",");
//			String value = categoryFields[0];
//			String displayName = categoryFields[1];
//
//			if (displayName.startsWith("WESTRING")) {
//				displayName = WEString.getString(displayName);
//			}
//			GameEnumChoice itemClass = new GameEnumChoice(value, editRawData ? value : displayName);
//			itemClassesList.add(itemClass);
//		}
//		itemClassesList.sort(Comparator.comparing(GameEnumChoice::getCategoryDisplay));
//		return itemClassesList;
//	}
//
//	private static final class GameEnumChoice {
//		private final String categoryKey;
//		private final String categoryDisplay;
//
//		public GameEnumChoice(String categoryKey, String categoryDisplay) {
//			this.categoryKey = categoryKey;
//			this.categoryDisplay = categoryDisplay;
//		}
//
//		public String getCategoryDisplay() {
//			return categoryDisplay;
//		}
//
//		public String getCategoryKey() {
//			return categoryKey;
//		}
//
//		@Override
//		public String toString() {
//			return categoryDisplay;
//		}
//	}
//
//}
