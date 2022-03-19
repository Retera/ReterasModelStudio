package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.RecalculateNormalsAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class RecalculateNormals extends ActionFunction{
	private static double lastNormalMaxAngle = 90;
	private static boolean useTris = false;

	public RecalculateNormals(){
		super(TextKey.RECALCULATE_NORMALS, () -> recalculateNormals());
		setKeyStroke(KeyStroke.getKeyStroke("control shift N"));
	}

	public static void recalculateNormals() {
	    ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
	    if (modelPanel != null) {
	        JPanel panel = new JPanel(new MigLayout());
	        panel.add(new JLabel("Limiting angle"));
	        JSpinner spinner = new JSpinner(new SpinnerNumberModel(lastNormalMaxAngle, -180.0, 180.0, 1));
	        panel.add(spinner, "wrap");
	        panel.add(new JLabel("Use triangles instead of vertices"));
	        JCheckBox useTries = new JCheckBox();
	        useTries.setSelected(useTris);
	        panel.add(useTries);
	        int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), panel, TextKey.RECALCULATE_NORMALS.toString(), JOptionPane.OK_CANCEL_OPTION);
	        if (option == JOptionPane.OK_OPTION) {
	            lastNormalMaxAngle = (double) spinner.getValue();
		        useTris = useTries.isSelected();

		        ModelView modelView = modelPanel.getModelView();

		        Set<GeosetVertex> selectedVertices = new HashSet<>(modelView.getSelectedVertices());
		        if (selectedVertices.isEmpty()) {
			        modelView.getEditableGeosets().forEach(geoset -> selectedVertices.addAll(geoset.getVertices()));
		        }

		        RecalculateNormalsAction recalcNormals = new RecalculateNormalsAction(selectedVertices, lastNormalMaxAngle, useTris);
		        modelPanel.getUndoManager().pushAction(recalcNormals.redo());
	        }
	    }
	    ProgramGlobals.getMainPanel().repaint();
	}
}
