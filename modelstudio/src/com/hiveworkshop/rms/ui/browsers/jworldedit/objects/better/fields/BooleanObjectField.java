package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public class BooleanObjectField extends AbstractObjectField {
	public BooleanObjectField(final String displayName, final String sortName, final String rawDataName,
			final boolean showingLevelDisplay, final War3ID metaKey, final int level,
			final WorldEditorDataType dataType, final GameObject metaDataField) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
	}

	@Override
	protected Object getValue(final MutableGameObject gameUnit, final War3ID metaKey, final int level) {
		return gameUnit.getFieldAsBoolean(metaKey, level);
	}

	@Override
	protected boolean popupEditor(final MutableGameObject gameUnit, final Component parent, final boolean editRawData,
			final boolean disableLimits, final War3ID metaKey, final int level, final String defaultDialogTitle,
			final GameObject metaDataField) {
		final JPanel checkboxPanel = new JPanel();
		checkboxPanel.add(new JLabel(getDisplayName(gameUnit)));
		final JCheckBox checkBox = new JCheckBox("", gameUnit.getFieldAsBoolean(metaKey, level));
		checkboxPanel.add(checkBox);
		final int result = FieldPopupUtils.showPopup(parent, checkboxPanel,
				String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_BOOL")),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, checkBox);
		if (result == JOptionPane.OK_OPTION) {
			gameUnit.setField(metaKey, level, checkBox.isSelected());
			return true;
		}
		return false;
	}

}
