package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableAbilityData;

import javax.swing.*;
import java.awt.*;

public class UnitOptionPane {
	public static GameObject getGameObject(Component component) {
		UnitOptionPanel uop = new UnitOptionPanel(DataTableHolder.getDefault(), MutableAbilityData.getStandardAbilities());
		int x = JOptionPane.showConfirmDialog(component, uop, "Choose Unit Type", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			GameObject choice = uop.getSelection();
			if (choice != null && isValidFilepath(choice.getField("file"))) {
				return choice;
			}
		}
		return null;
	}

	private static boolean isValidFilepath(String filepath) {
		try {
			//check model by converting its path
			ImportFileActions.convertPathToMDX(filepath);
		} catch (final Exception exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.frame,
					"The chosen model could not be used.",
					"Program Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

}
