package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.tools.GeosetAnimCopyPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;


public class ComponentGeosetAnimPanel extends ComponentPanel<GeosetAnim> {
	private GeosetAnim geosetAnim;
	private final JPanel animsPanelHolder;
	private final FloatValuePanel alphaPanel;
	private final ColorValuePanel colorPanel;
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

		alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA, modelHandler.getUndoManager());
		animsPanelHolder.add(alphaPanel, "wrap, span 2");

		colorPanel = new ColorValuePanel(modelHandler, MdlUtils.TOKEN_COLOR, modelHandler.getUndoManager());
		animsPanelHolder.add(colorPanel, "wrap, span 2");


	}

	@Override
	public void setSelectedItem(final GeosetAnim geosetAnim) {
		this.geosetAnim = geosetAnim;
		geosetLabel.setText(geosetAnim.getGeoset().getName());
		animsPanelHolder.revalidate();
		animsPanelHolder.repaint();
		alphaPanel.reloadNewValue((float) geosetAnim.getStaticAlpha(), (FloatAnimFlag) geosetAnim.find(MdlUtils.TOKEN_ALPHA), geosetAnim, MdlUtils.TOKEN_ALPHA, geosetAnim::setStaticAlpha);
		colorPanel.reloadNewValue(geosetAnim.getStaticColor(), (Vec3AnimFlag) geosetAnim.find(MdlUtils.TOKEN_COLOR), geosetAnim, MdlUtils.TOKEN_COLOR, geosetAnim::setStaticColor);

		revalidate();
		repaint();
	}


	@Override
	public void save(EditableModel model, UndoManager undoManager,
	                 ModelStructureChangeListener changeListener) {
	}

	private void copyFromOther() {
		GeosetAnimCopyPanel.show(this, modelHandler.getModelView(), geosetAnim, changeListener, modelHandler.getUndoManager());
		repaint();
	}

}
