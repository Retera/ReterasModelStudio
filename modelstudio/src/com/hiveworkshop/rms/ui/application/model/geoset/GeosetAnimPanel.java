package com.hiveworkshop.rms.ui.application.model.geoset;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SetGeosetStaticAlphaAction;
import com.hiveworkshop.rms.editor.actions.mesh.SetGeosetStaticColorAction;
import com.hiveworkshop.rms.editor.actions.util.BoolAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.tools.GeosetAnimEditPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetAnimPanel extends JPanel {
	private final EditableModel model;
	private final UndoManager undoManager;
	private final ModelStructureChangeListener changeListener;
	private final JPanel animatedPanel;
	private final EditorHelpers.AlphaEditor alphaEditor;
	private final EditorHelpers.ColorEditor colorEditor;
	private final JCheckBox dropShadow;
	private Geoset geoset;

	public GeosetAnimPanel(ModelHandler modelHandler){
		super(new MigLayout("fill, hideMode 2", "[]", "[][][grow]"));
		this.model = modelHandler.getModel();
		this.undoManager = modelHandler.getUndoManager();
		this.changeListener = ModelStructureChangeListener.changeListener;

		animatedPanel = new JPanel(new MigLayout("fill, ins 0", "[]", "[grow]"));
		animatedPanel.add(new JLabel("GeosetAnim"), "wrap");

		JButton button = new JButton("copy all geosetAnim-info from other");
		button.addActionListener(e -> copyFromOther());
		animatedPanel.add(button, "wrap");

		dropShadow = new JCheckBox("DropShadow");
		dropShadow.setToolTipText("Treat geoset as drop shadow");
		dropShadow.addActionListener(e -> setDropShadow(dropShadow.isSelected()));
//		animatedPanel.add(dropShadow, "wrap");

		colorEditor = new EditorHelpers.ColorEditor(modelHandler, this::setStaticColor);
		animatedPanel.add(colorEditor.getFlagPanel(), "wrap");

		alphaEditor = new EditorHelpers.AlphaEditor(modelHandler, this::setStaticAlpha);
		animatedPanel.add(alphaEditor.getFlagPanel(), "wrap");


		add(animatedPanel, "span 2, wrap");
	}
	
	public GeosetAnimPanel setGeoset(Geoset geoset){
		this.geoset = geoset;
		dropShadow.setSelected(geoset.isDropShadow());
		alphaEditor.update(geoset, (float) geoset.getStaticAlpha());
		colorEditor.update(geoset, geoset.getStaticColor());
		animatedPanel.setVisible(true);

		return this;
	}
	private void setDropShadow(boolean dropShadow) {
		if(geoset.isDropShadow() != dropShadow){
			undoManager.pushAction(new BoolAction(geoset::setDropShadow, dropShadow, "Set Drop Shadow", null).redo());
		}
	}

	private void copyFromOther() {
		GeosetAnimEditPanel.show(ProgramGlobals.getMainPanel(), model, geoset, undoManager);
		repaint();
	}

	private void setStaticAlpha(float newAlpha) {
		if(geoset.getStaticAlpha() != newAlpha){
			UndoAction action = new SetGeosetStaticAlphaAction(geoset, newAlpha, changeListener);
			undoManager.pushAction(action.redo());
		}
	}

	private void setStaticColor(Vec3 color) {
		if(!geoset.getStaticColor().equalLocs(color)){
			UndoAction action = new SetGeosetStaticColorAction(geoset, color, changeListener);
			undoManager.pushAction(action.redo());
		}
	}
}
