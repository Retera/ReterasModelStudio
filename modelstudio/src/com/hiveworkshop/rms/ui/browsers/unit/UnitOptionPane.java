package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;

import javax.swing.*;
import java.awt.*;

public class UnitOptionPane {
	public static GameObject show(Component component) {
		UnitOptionPanel uop = new UnitOptionPanel(DataTable.get(), StandardObjectData.getStandardAbilities());
		int x = JOptionPane.showConfirmDialog(component, uop, "Choose Unit Type", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			return uop.getSelection();
		}
		return null;
	}

	public static GameObject fetchUnit(Component component) {
		UnitOptionPanel uop = new UnitOptionPanel(DataTable.get(), StandardObjectData.getStandardAbilities());
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
	public static String fetchUnit1(Component component) {
		UnitOptionPanel uop = new UnitOptionPanel(DataTable.get(), StandardObjectData.getStandardAbilities());
		int x = JOptionPane.showConfirmDialog(component, uop, "Choose Unit Type", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			GameObject choice = uop.getSelection();
			if (choice != null && isValidFilepath(choice.getField("file"))) {
				return choice.getField("file");
			}
		}
		return null;
	}
	public static GameObject fetchUnit2() {
		GameObject choice = show(ProgramGlobals.getMainPanel());

		if (choice != null) {
			String filepath = choice.getField("file");
			if (isValidFilepath(filepath)) return choice;
		}
		return null;
	}

	private static boolean isValidFilepath(String filepath) {
		try {
			//check model by converting its path
			convertPathToMDX(filepath);
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


	public static String convertPathToMDX(String filepath) {
		if (filepath.endsWith(".mdl")) {
			filepath = filepath.replace(".mdl", ".mdx");
		} else if (!filepath.endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}

	public static String convertPathToMDX2(String filepath) {
		if (filepath.endsWith(".mdl") || filepath.endsWith(".mdx")) {
			filepath = filepath.substring(0, filepath.length()-4);
		}
		return filepath.concat(".mdx");
	}
}
