package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.util.War3ID;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

final class NewCustomUnitDialogRunner implements Runnable {
	private static final War3ID UNIT_NAME = War3ID.fromString("unam");
	private final ObjectEditorPanel objectEditorPanel;
	private final MutableObjectData unitData;

	NewCustomUnitDialogRunner(ObjectEditorPanel objectEditorPanel, final MutableObjectData unitData) {
		this.objectEditorPanel = objectEditorPanel;
		this.unitData = unitData;
	}

	@Override
	public void run() {
		final JLabel nameLabel = new JLabel(WEString.getString("WESTRING_UE_FIELDNAME") + ":");

		final JTextField nameField = new JTextField(30);
		nameField.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		nameField.setPreferredSize(new Dimension(200, 18));
		nameField.setMaximumSize(new Dimension(200, 100));

		final JLabel baseUnitLabel = new JLabel(WEString.getString("WESTRING_UE_BASEUNIT").replace("&", "") + ":");

		final UnitOptionPanel unitOptionPanel = new UnitOptionPanel(StandardObjectData.getStandardUnits(), StandardObjectData.getStandardAbilities(), true, true);
		unitOptionPanel.setPreferredSize(new Dimension(416, 400));
		unitOptionPanel.setSize(new Dimension(416, 400));
		unitOptionPanel.doLayout();
		unitOptionPanel.relayout();

		final JPanel popupPanel = new JPanel(new MigLayout("ins 0"));
		popupPanel.add(nameLabel, "split 2, spanx");
		popupPanel.add(nameField, "wrap");
		popupPanel.add(baseUnitLabel, "spanx, wrap");
		popupPanel.add(unitOptionPanel, "wrap");

		final int response = JOptionPane.showConfirmDialog(objectEditorPanel, popupPanel,
				WEString.getString("WESTRING_UE_CREATECUSTOMUNIT"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (response == JOptionPane.OK_OPTION) {
			final GameObject selection = unitOptionPanel.getSelection();
			final War3ID sourceId = War3ID.fromString(selection.getId());
			final War3ID objectId = unitData.getNextDefaultEditorId(War3ID.fromString(sourceId.charAt(0) + "000"));
			final MutableGameObject newObject = unitData.createNew(objectId, sourceId);
			newObject.setField(UNIT_NAME, 0, nameField.getText());
		}
	}
}
