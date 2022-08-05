package com.hiveworkshop.rms.ui.application.model.geoset;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SetGeosetAnimStaticAlphaAction;
import com.hiveworkshop.rms.editor.actions.model.SetGeosetAnimAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.tools.GeosetAnimEditPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetAnimPanel extends JPanel {
	private final EditableModel model;
	private final UndoManager undoManager;
	private final ModelStructureChangeListener changeListener;
	private GeosetAnim geosetAnim;
	private Geoset geoset;
	JPanel animatedPanel;
	EditorHelpers.AlphaEditor alphaEditor;
	EditorHelpers.ColorEditor colorEditor;
	JButton addAnim;
	
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

		colorEditor = new EditorHelpers.ColorEditor(modelHandler);
		animatedPanel.add(colorEditor.getFlagPanel(), "wrap");

		alphaEditor = new EditorHelpers.AlphaEditor(modelHandler, this::setStaticAlpha);
		animatedPanel.add(alphaEditor.getFlagPanel(), "wrap");


		add(animatedPanel, "span 2, wrap");

		addAnim = new JButton("Add GeosetAnim");
		addAnim.addActionListener(e -> setGeosetAnim());
		add(addAnim);
	}
	
	public GeosetAnimPanel setGeoset(Geoset geoset){
		this.geoset = geoset;
		this.geosetAnim = geoset.getGeosetAnim();
		if (geosetAnim != null) {
			alphaEditor.update(geosetAnim, (float) geosetAnim.getStaticAlpha());
			colorEditor.update(geosetAnim, geosetAnim.getStaticColor());
			animatedPanel.setVisible(true);
			addAnim.setVisible(false);
		} else {
			animatedPanel.setVisible(false);
			addAnim.setVisible(true);
		}
		return this;
	}

	private void setGeosetAnim() {
		undoManager.pushAction(new SetGeosetAnimAction(model, geoset, changeListener).redo());
	}

	private void copyFromOther() {
		GeosetAnimEditPanel.show(ProgramGlobals.getMainPanel(), model, geosetAnim, undoManager);
		repaint();
	}

	private void setStaticAlpha(float newAlpha) {
		if(geosetAnim.getStaticAlpha() != newAlpha){
			UndoAction action = new SetGeosetAnimStaticAlphaAction(geosetAnim, newAlpha, changeListener);
			undoManager.pushAction(action.redo());
		}
	}
}
