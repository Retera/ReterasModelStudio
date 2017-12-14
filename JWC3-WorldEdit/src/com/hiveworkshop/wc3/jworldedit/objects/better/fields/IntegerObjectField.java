package com.hiveworkshop.wc3.jworldedit.objects.better.fields;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class IntegerObjectField extends AbstractObjectField {

	public IntegerObjectField(final String displayName, final String rawDataName, final War3ID metaKey, final int level,
			final WorldEditorDataType dataType, final GameObject metaDataField) {
		super(displayName, rawDataName, metaKey, level, dataType, metaDataField);
	}

	@Override
	protected Object getValue(final MutableGameObject gameUnit, final War3ID metaKey, final int level) {
		return gameUnit.getFieldAsInteger(metaKey, level);
	}

	@Override
	protected boolean popupEditor(final MutableGameObject gameUnit, final Component parent, final boolean editRawData,
			final boolean disableLimits, final War3ID metaKey, final int level, final String defaultDialogTitle,
			final GameObject metaDataField) {
		final JPanel popupPanel = new JPanel();
		popupPanel.add(new JLabel(getDisplayName(gameUnit)));
		int minValue = metaDataField.getFieldValue("minVal");
		int maxValue = metaDataField.getFieldValue("maxVal");
		if (editRawData) {
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
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentValue, minValue, maxValue, 1));
		spinner.setMinimumSize(new Dimension(50, 1));
		spinner.setPreferredSize(new Dimension(75, 20));
		popupPanel.add(spinner);
		final int result = FieldPopupUtils.showPopup(parent, popupPanel,
				String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_INT")),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, spinner);
		if (result == JOptionPane.OK_OPTION) {
			gameUnit.setField(metaKey, level, ((Number) spinner.getValue()).intValue());
			return true;
		}
		return false;
	}

}
