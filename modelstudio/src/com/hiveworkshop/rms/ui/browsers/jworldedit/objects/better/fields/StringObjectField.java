package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import java.awt.*;

public class StringObjectField extends AbstractObjectField {

	public static final int STRING_FIELD_COLUMNS = 23;

	public StringObjectField(String displayName, String sortName, String rawDataName, boolean showingLevelDisplay,
	                         War3ID metaKey, int level, WorldEditorDataType dataType, GameObject metaDataField) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
	}

	@Override
	protected Object getValue(MutableGameObject gameUnit, War3ID metaKey, int level) {
		return gameUnit.getFieldAsString(metaKey, level);
	}

	@Override
	protected boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData,
	                              boolean disableLimits, War3ID metaKey, int level, String defaultDialogTitle,
	                              GameObject metaDataField) {
		JPanel popupPanel = new JPanel();
		popupPanel.add(new JLabel(getDisplayName(gameUnit)));

		JTextField textField = new JTextField(gameUnit.getFieldAsString(metaKey, level), STRING_FIELD_COLUMNS);
		popupPanel.add(textField);

		String title = String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_STRING"));
		int result = FieldPopupUtils.showPopup(parent, popupPanel, title, textField);

		if (result == JOptionPane.OK_OPTION) {
			gameUnit.setField(metaKey, level, textField.getText());
			return true;
		}
		return false;
	}

}
