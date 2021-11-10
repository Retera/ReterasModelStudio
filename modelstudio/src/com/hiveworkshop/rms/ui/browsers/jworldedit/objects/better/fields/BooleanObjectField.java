package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import java.awt.*;

public class BooleanObjectField extends AbstractObjectField {
	public BooleanObjectField(String displayName, String sortName, String rawDataName, boolean showingLevelDisplay,
	                          War3ID metaKey, int level, WorldEditorDataType dataType, GameObject metaDataField) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
	}

	@Override
	protected Object getValue(MutableGameObject gameUnit, War3ID metaKey, int level) {
		return gameUnit.getFieldAsBoolean(metaKey, level);
	}

	@Override
	protected boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData,
	                              boolean disableLimits, War3ID metaKey, int level, String defaultDialogTitle,
	                              GameObject metaDataField) {
		JPanel checkboxPanel = new JPanel();
		checkboxPanel.add(new JLabel(getDisplayName(gameUnit)));

		JCheckBox checkBox = new JCheckBox("", gameUnit.getFieldAsBoolean(metaKey, level));
		checkboxPanel.add(checkBox);

		String title = String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_BOOL"));
		int result = FieldPopupUtils.showPopup(parent, checkboxPanel, title, checkBox);
		if (result == JOptionPane.OK_OPTION) {
			gameUnit.setField(metaKey, level, checkBox.isSelected());
			return true;
		}
		return false;
	}

}
