package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import java.awt.*;

public class IntegerObjectField extends AbstractObjectField {

	public IntegerObjectField(String displayName, String sortName, String rawDataName, boolean showingLevelDisplay,
	                          War3ID metaKey, int level, WorldEditorDataType dataType, GameObject metaDataField) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
	}

	@Override
	protected Object getValue(MutableGameObject gameUnit, War3ID metaKey, int level) {
		return gameUnit.getFieldAsInteger(metaKey, level);
	}

	@Override
	protected boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData,
	                              boolean disableLimits, War3ID metaKey, int level, String defaultDialogTitle,
	                              GameObject metaDataField) {
		JPanel popupPanel = new JPanel();
		popupPanel.add(new JLabel(getDisplayName(gameUnit)));
		JSpinner spinner = getSpinner(gameUnit, disableLimits, metaKey, level, metaDataField);
		popupPanel.add(spinner);
		String title = String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_INT"));
		int result = FieldPopupUtils.showPopup(parent, popupPanel, title, spinner);
		if (result == JOptionPane.OK_OPTION) {
			gameUnit.setField(metaKey, level, ((Number) spinner.getValue()).intValue());
			return true;
		}
		return false;
	}

	private JSpinner getSpinner(MutableGameObject gameUnit, boolean disableLimits, War3ID metaKey, int level, GameObject metaDataField) {
		int minValue = metaDataField.getFieldValue("minVal");
		int maxValue = metaDataField.getFieldValue("maxVal");
		if (disableLimits) {
			minValue = -1000000000;
			maxValue = 1000000000;
		}
		int currentValue = gameUnit.getFieldAsInteger(metaKey, level);
		if (minValue > currentValue) {
			currentValue = minValue;
		}
		if (maxValue < currentValue) {
			currentValue = maxValue;
		}
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentValue, minValue, maxValue, 1));
		spinner.setMinimumSize(new Dimension(50, 1));
		spinner.setPreferredSize(new Dimension(75, 20));
		return spinner;
	}

}
