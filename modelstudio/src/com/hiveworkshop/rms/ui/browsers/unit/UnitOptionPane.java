package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;

import javax.swing.*;
import java.awt.*;

public class UnitOptionPane {
	public static GameObject show(final Component what) {
		return show(what, DataTable.get());
	}

	public static GameObject show(final Component what, final ObjectData table) {
		final UnitOptionPanel uop = new UnitOptionPanel(table, StandardObjectData.getStandardAbilities());
		final int x = JOptionPane.showConfirmDialog(what, uop, "Choose Unit Type", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			return uop.getSelection();
		}
		return null;
	}
}
