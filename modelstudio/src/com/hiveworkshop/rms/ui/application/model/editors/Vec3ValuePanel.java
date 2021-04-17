package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.util.FilteredTextField;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import javax.swing.event.CaretListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class Vec3ValuePanel extends ValuePanel<Vec3> {

	private static final Vec3 VEC_3 = new Vec3(1, 1, 1);

	private FilteredTextField staticValueField;

	private Vec3 vec3;


	public Vec3ValuePanel(final String title, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		super(title, undoActionListener, modelStructureChangeListener);

		vec3 = new Vec3(VEC_3);
		allowedCharacters += "\\{}, ";
		floatTrackTableModel.setValueClass(String.class);
	}


	@Override
	JComponent getStaticComponent() {
		staticValueField = new FilteredTextField(24);
		FocusAdapter focusAdapter = getFocusAdapter();
		staticValueField.addFocusListener(focusAdapter);
		return staticValueField;
	}

	private FocusAdapter getFocusAdapter() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				Vec3 value = parseValue(staticValueField.getText());
				if (valueSettingFunction != null) {
					valueSettingFunction.accept(value);
				}
			}
		};
	}

	private void applyFilteredText(String newText) {
		CaretListener listener = staticValueField.getCaretListeners()[0];
		staticValueField.removeCaretListener(listener);
		int carPos = staticValueField.getCaretPosition();
		staticValueField.setText(newText);
		int newCarPos = Math.max(0, Math.min(newText.length(), carPos - 1));
		staticValueField.setCaretPosition(newCarPos);
		staticValueField.addCaretListener(listener);
	}

	@Override
	void reloadStaticValue(Vec3 vec3) {
		if (vec3 != null) {
			this.vec3 = vec3;
		}
		staticValueField.setText(this.vec3.toString());
	}


	@Override
	Vec3 getZeroValue() {
		return new Vec3(0, 0, 0);
	}

	@Override
	Vec3 parseValue(String valueString) {
		Vec3 vecValue;
		String polishedString;
		if (valueString != null && valueString.matches("\\{? ?(\\d*\\.?\\d+)( ?, ?(\\d*\\.?\\d+))* ?}?")) {
//			System.out.println("match");
			polishedString = valueString.replaceAll("[\\{} ]", "");
//			System.out.println("polishedString pre: " + polishedString);
			String[] split = polishedString.split(",");
			int vecSize = split.length;
			if (vecSize < 3) {
				String addString = ",0";
				if (!split[vecSize - 1].equals("")) {
					addString = "," + split[vecSize - 1];
				}
				polishedString += addString;
				if (vecSize < 2) {
					polishedString += addString;
				}
			} else {
				if (split[2].equals("")) {
					polishedString += split[1];
				}
			}
		} else {
			polishedString = valueString.replaceAll("[\\{} ]", "");
			String[] split = polishedString.split(",");
			if (split.length < 3) {
				polishedString = polishedString.replaceAll("\\.\\.", ".0,.");
				split = polishedString.split(",");
			}
			StringBuilder newS = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				if (i < split.length) {
					String s = split[i];
					if (s.equals("") || s.equals(".")) {
						split[i] = "0";
					} else if (s.matches("\\d+\\.")) {
						split[i] += "0";
					} else if (s.matches("\\d*\\.\\d+\\..*")) {
						split[i] = s.substring(0, s.indexOf(".", s.indexOf(".") + 1));
					}
					newS.append(split[i]);
				} else if (split.length != 0) {
					newS.append(split[split.length - 1]);
				} else {
					newS.append("0");
				}
				if (i < 2) {
					newS.append(",");
				}
			}
			polishedString = newS.toString();

		}
//		System.out.println("polishedString: " + polishedString);
		vecValue = Vec3.parseVec3(polishedString);
		return vecValue;
	}

}
