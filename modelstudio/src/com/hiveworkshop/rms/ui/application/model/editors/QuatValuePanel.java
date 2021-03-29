package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.util.FilteredTextField;
import com.hiveworkshop.rms.util.Quat;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class QuatValuePanel extends ValuePanel<Quat> {

	private static final Quat QUAT = new Quat(0, 0, 0, 1);
	private FilteredTextField staticValueField;

	private Quat quat;


	public QuatValuePanel(final String title, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		super(title, undoActionListener, modelStructureChangeListener);

		quat = new Quat(QUAT);
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
				Quat value = parseValue(staticValueField.getText());
				if (valueSettingFunction != null) {
					valueSettingFunction.accept(value);
				}
			}
		};
	}

	@Override
	void reloadStaticValue(Quat vec3) {
		if (vec3 != null) {
			this.quat = vec3;
		}
		staticValueField.setText(this.quat.toString());
	}


	@Override
	Quat getZeroValue() {
		return new Quat(0, 0, 0, 1);
	}

	@Override
	Quat parseValue(String valueString) {
		Quat vecValue;
		String polishedString;
		if (valueString != null && valueString.matches("\\{? ?(\\d*\\.?\\d+)( ?, ?(\\d*\\.?\\d+))* ?}?")) {
//			System.out.println("match");
			polishedString = valueString.replaceAll("[\\{} ]", "");
//			System.out.println("polishedString pre: " + polishedString);
			String[] split = polishedString.split(",");
			int vecSize = split.length;
			if (vecSize < 4) {
				String addString = ",0";
				if (!split[vecSize - 1].equals("")) {
					addString = "," + split[vecSize - 1];
				}
				polishedString += addString;
				if (vecSize < 2) {
					polishedString += addString;
				}
				if (vecSize < 3) {
					polishedString += ",0.99";
				}
			} else {
				if (split[2].equals("")) {
					polishedString += split[1];
				}
			}
		} else {
//			System.out.println("nja");
			polishedString = valueString.replaceAll("[\\{} ]", "");
//			System.out.println("polishedString pre: " + polishedString);
			String[] split = polishedString.split(",");
			if (split.length < 4) {
				polishedString = polishedString.replaceAll("\\.\\.", ".0,.");
				split = polishedString.split(",");
			}
			StringBuilder newS = new StringBuilder();
			for (int i = 0; i < 4; i++) {
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
		vecValue = Quat.parseQuat(polishedString);

		return vecValue;
	}

}
