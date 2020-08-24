package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public class FloatObjectField extends AbstractObjectField {

	public FloatObjectField(final String displayName, final String sortName, final String rawDataName,
			final boolean showingLevelDisplay, final War3ID metaKey, final int level,
			final WorldEditorDataType dataType, final GameObject metaDataField) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object getValue(final MutableGameObject gameUnit, final War3ID metaKey, final int level) {
		return gameUnit.getFieldAsFloat(metaKey, level);
	}

	@Override
	protected boolean popupEditor(final MutableGameObject gameUnit, final Component parent, final boolean editRawData,
			final boolean disableLimits, final War3ID metaKey, final int level, final String defaultDialogTitle,
			final GameObject metaDataField) {
		final JPanel popupPanel = new JPanel();
		popupPanel.add(new JLabel(getDisplayName(gameUnit)));
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
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentValue, minFloatValue, maxFloatValue, 0.1f));
		spinner.setMinimumSize(new Dimension(50, 1));
		spinner.setPreferredSize(new Dimension(75, 20));
		popupPanel.add(spinner);
		final int result = FieldPopupUtils.showPopup(parent, popupPanel,
				String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_REAL")),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, spinner);
		if (result == JOptionPane.OK_OPTION) {
			System.out.println(spinner.getValue() + " on the outside");
			gameUnit.setField(metaKey, level, ((Number) spinner.getValue()).floatValue());
			return true;
		}
		return false;
	}

}
