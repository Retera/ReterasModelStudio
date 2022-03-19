package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import java.awt.*;

public class FloatObjectField extends AbstractObjectField {

	public FloatObjectField(String displayName, String sortName, String rawDataName, boolean showingLevelDisplay,
	                        War3ID metaKey, int level, WorldEditorDataType dataType, GameObject metaDataField) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object getValue(MutableGameObject gameUnit, War3ID metaKey, int level) {
		return gameUnit.getFieldAsFloat(metaKey, level);
	}

	@Override
	protected boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData,
	                              boolean disableLimits, War3ID metaKey, int level, String defaultDialogTitle,
	                              GameObject metaDataField) {
		JPanel popupPanel = new JPanel();
		popupPanel.add(new JLabel(getDisplayName(gameUnit)));

		JSpinner spinner = getSpinner(gameUnit, disableLimits, metaKey, level, metaDataField);
		popupPanel.add(spinner);

		final String title = String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_REAL"));
		int result = FieldPopupUtils.showPopup(parent, popupPanel, title, spinner);

		if (result == JOptionPane.OK_OPTION) {
			System.out.println(spinner.getValue() + " on the outside");
			gameUnit.setField(metaKey, level, ((Number) spinner.getValue()).floatValue());
			return true;
		}
		return false;
	}

	private JSpinner getSpinner(MutableGameObject gameUnit, boolean disableLimits, War3ID metaKey, int level, GameObject metaDataField) {
		float minFloatValue = Float.parseFloat(metaDataField.getField("minVal"));
		float maxFloatValue = Float.parseFloat(metaDataField.getField("maxVal"));
		if (disableLimits) {
			minFloatValue = -1000000000.0f;
			maxFloatValue = 1000000000.0f;
		}
		float currentValue = gameUnit.getFieldAsFloat(metaKey, level);
		if (minFloatValue > currentValue) {
			currentValue = minFloatValue;
		}
		if (maxFloatValue < currentValue) {
			currentValue = maxFloatValue;
		}

		JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentValue, minFloatValue, maxFloatValue, 0.1f));
		spinner.setMinimumSize(new Dimension(50, 1));
		spinner.setPreferredSize(new Dimension(75, 20));
		return spinner;
	}

}
