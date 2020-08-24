package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public class StringObjectField extends AbstractObjectField {

	public static final int STRING_FIELD_COLUMNS = 23;

	public StringObjectField(final String displayName, final String sortName, final String rawDataName,
			final boolean showingLevelDisplay, final War3ID metaKey, final int level,
			final WorldEditorDataType dataType, final GameObject metaDataField) {
		super(displayName, sortName, rawDataName, showingLevelDisplay, metaKey, level, dataType, metaDataField);
	}

	@Override
	protected Object getValue(final MutableGameObject gameUnit, final War3ID metaKey, final int level) {
		return gameUnit.getFieldAsString(metaKey, level);
	}

	@Override
	protected boolean popupEditor(final MutableGameObject gameUnit, final Component parent, final boolean editRawData,
			final boolean disableLimits, final War3ID metaKey, final int level, final String defaultDialogTitle,
			final GameObject metaDataField) {
		final JPanel popupPanel = new JPanel();
		popupPanel.add(new JLabel(getDisplayName(gameUnit)));
		final JTextField textField = new JTextField(gameUnit.getFieldAsString(metaKey, level), STRING_FIELD_COLUMNS);
		popupPanel.add(textField);
		final int result = FieldPopupUtils.showPopup(parent, popupPanel,
				String.format(defaultDialogTitle, WEString.getString("WESTRING_COD_TYPE_STRING")),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, textField);
		if (result == JOptionPane.OK_OPTION) {
			gameUnit.setField(metaKey, level, textField.getText());
			return true;
		}
		return false;
	}

}
