package com.hiveworkshop.rms.ui.application.model.geoset;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SetGeosetAnimStaticAlphaAction;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.tools.GeosetAnimCopyPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;


public class ComponentGeosetAnimPanel extends ComponentPanel<GeosetAnim> {
	private GeosetAnim geosetAnim;
	private final JPanel animsPanelHolder;
	private final EditorHelpers.AlphaEditor alphaEditor;
	private final EditorHelpers.ColorEditor colorEditor;
	private final JLabel geosetLabel;

	public ComponentGeosetAnimPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill", "[][][grow]", "[][][grow]"));

		JPanel labelPanel = new JPanel(new MigLayout());
		add(labelPanel, "wrap, growx, span 3");
		labelPanel.add(new JLabel("GeosetAnim for Geoset:"));
		geosetLabel = new JLabel("geoset name");
		labelPanel.add(geosetLabel, "wrap");

		animsPanelHolder = new JPanel(new MigLayout());
		add(animsPanelHolder, "wrap, growx, span 3");

		animsPanelHolder.add(new JLabel("GeosetAnim"), "wrap");

		JButton button = new JButton("copy all geosetAnim-info from other");
		button.addActionListener(e -> copyFromOther());
		animsPanelHolder.add(button, "wrap");

		alphaEditor = new EditorHelpers.AlphaEditor(modelHandler, this::setStaticAlpha);
		animsPanelHolder.add(alphaEditor.getFlagPanel(), "wrap, span 2");

		colorEditor = new EditorHelpers.ColorEditor(modelHandler);
		animsPanelHolder.add(colorEditor.getFlagPanel(), "wrap, span 2");

	}

	@Override
	public ComponentPanel<GeosetAnim> setSelectedItem(final GeosetAnim geosetAnim) {
		this.geosetAnim = geosetAnim;
		geosetLabel.setText(geosetAnim.getGeoset().getName());
		animsPanelHolder.revalidate();
		animsPanelHolder.repaint();
		alphaEditor.update(geosetAnim, (float) geosetAnim.getStaticAlpha());
		colorEditor.update(geosetAnim);

		revalidate();
		repaint();
		return this;
	}

	private void copyFromOther() {
		GeosetAnimCopyPanel.show(this, model, geosetAnim, undoManager);
		repaint();
	}

	private void setStaticAlpha(float newAlpha) {
		if(geosetAnim.getStaticAlpha() != newAlpha){
			UndoAction action = new SetGeosetAnimStaticAlphaAction(geosetAnim, newAlpha, changeListener);
			undoManager.pushAction(action.redo());
		}
	}

}
