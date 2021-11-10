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

public final class NewCustomUnitDialogRunner implements Runnable {
	private static final War3ID UNIT_NAME = War3ID.fromString("unam");
	private final Component popupParent;
	private final MutableObjectData unitData;

	NewCustomUnitDialogRunner(Component popupParent, final MutableObjectData unitData) {
		this.popupParent = popupParent;
		this.unitData = unitData;
	}

	@Override
	public void run() {
		JLabel nameLabel = new JLabel(WEString.getString("WESTRING_UE_FIELDNAME") + ":");

		JTextField nameField = new JTextField(30);
		nameField.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		nameField.setPreferredSize(new Dimension(200, 18));
		nameField.setMaximumSize(new Dimension(200, 100));

		JLabel baseUnitLabel = new JLabel(WEString.getString("WESTRING_UE_BASEUNIT").replace("&", "") + ":");

		UnitOptionPanel unitOptionPanel = new UnitOptionPanel(StandardObjectData.getStandardUnits(), StandardObjectData.getStandardAbilities(), true, true);
		unitOptionPanel.setPreferredSize(new Dimension(416, 400));
		unitOptionPanel.setSize(new Dimension(416, 400));
		unitOptionPanel.doLayout();
		unitOptionPanel.relayout();

		JPanel popupPanel = new JPanel(new MigLayout("ins 0"));
		popupPanel.add(nameLabel, "split 2, spanx");
		popupPanel.add(nameField, "wrap");
		popupPanel.add(baseUnitLabel, "spanx, wrap");
		popupPanel.add(unitOptionPanel, "wrap");

		int response = JOptionPane.showConfirmDialog(popupParent, popupPanel,
				WEString.getString("WESTRING_UE_CREATECUSTOMUNIT"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (response == JOptionPane.OK_OPTION) {
			GameObject selection = unitOptionPanel.getSelection();
			War3ID sourceId = War3ID.fromString(selection.getId());
			War3ID objectId = unitData.getNextDefaultEditorId(War3ID.fromString(sourceId.charAt(0) + "000"));
			MutableGameObject newObject = unitData.createNew(objectId, sourceId);
			newObject.setField(UNIT_NAME, 0, nameField.getText());
		}
	}
}
