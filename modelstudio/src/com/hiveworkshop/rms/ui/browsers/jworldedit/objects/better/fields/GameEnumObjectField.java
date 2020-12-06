package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameEnumObjectField extends AbstractObjectField {

	private final String metaMetaKeyName;
	private final String metaMetaWestringName;
	private final DataTable worldEditorData;

	public GameEnumObjectField(final String displayName, final String sortName, final String rawDataName,
							   final boolean showingLevelDisplay, final War3ID metaKey, final int level,
							   final WorldEditorDataType dataType, final GameObject metaDataField, final String metaMetaKeyName,
							   final String metaMetaWestringName, final DataTable worldEditorData) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
		this.metaMetaKeyName = metaMetaKeyName;
		this.metaMetaWestringName = metaMetaWestringName;
		this.worldEditorData = worldEditorData;
	}

	@Override
	protected Object getValue(final MutableGameObject gameUnit, final War3ID metaKey, final int level) {
		return gameUnit.getFieldAsString(metaKey, level);
	}

	@Override
	protected boolean popupEditor(final MutableGameObject gameUnit, final Component parent, final boolean editRawData,
								  final boolean disableLimits, final War3ID metaKey, final int level, final String defaultDialogTitle,
								  final GameObject metaDataField) {

		final Element itemClasses = worldEditorData.get(metaMetaKeyName);
		final List<GameEnumChoice> itemClassesList = new ArrayList<>();
		final int numValues = itemClasses.getFieldValue("NumValues");
		GameEnumChoice selectedItemClass = null;
		for (int i = 0; i < numValues; i++) {
			final String categoryKey = String.format("%2d", i).replace(' ', '0');
			final String categoryData = itemClasses.getField(categoryKey);
			final String[] categoryFields = categoryData.split(",");
			final String value = categoryFields[0];
			String displayName = categoryFields[1];
			if (displayName.startsWith("WESTRING")) {
				displayName = WEString.getString(displayName);
			}
			final GameEnumChoice itemClass = new GameEnumChoice(value, editRawData ? value : displayName);
			itemClassesList.add(itemClass);
			if (value.equals(gameUnit.getFieldAsString(metaKey, level))) {
				selectedItemClass = itemClass;
			}
		}
		itemClassesList.sort(Comparator.comparing(GameEnumChoice::getCategoryDisplay));

		final JPanel popupPanel = new JPanel();
		popupPanel.add(new JLabel(getDisplayName(gameUnit)));
		if (disableLimits) {
			final JTextField textField = new JTextField(gameUnit.getFieldAsString(metaKey, level),
					StringObjectField.STRING_FIELD_COLUMNS);
			popupPanel.add(textField);
			final int result = FieldPopupUtils.showPopup(parent, popupPanel,
					String.format(defaultDialogTitle, WEString.getString(metaMetaWestringName)),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, textField);
			if (result == JOptionPane.OK_OPTION) {
				gameUnit.setField(metaKey, level, textField.getText());
				return true;
			}
		} else {
			final JComboBox<GameEnumChoice> itemClassCombo = new JComboBox<>(
					itemClassesList.toArray(new GameEnumChoice[0]));
			itemClassCombo.setEditable(false);
			if (selectedItemClass != null) {
				itemClassCombo.setSelectedItem(selectedItemClass);
			}
			popupPanel.add(itemClassCombo);
			final int result = FieldPopupUtils.showPopup(parent, popupPanel,
					String.format(defaultDialogTitle, WEString.getString(metaMetaWestringName)),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, itemClassCombo);
			if (result == JOptionPane.OK_OPTION) {
				gameUnit.setField(metaKey, level, ((GameEnumChoice) itemClassCombo.getSelectedItem()).getCategoryKey());
				return true;
			}
		}
		return false;
	}

	private static final class GameEnumChoice {
		private final String categoryKey;
		private final String categoryDisplay;

		public GameEnumChoice(final String categoryKey, final String categoryDisplay) {
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
