package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.TimelineKeyNamer;
import com.hiveworkshop.rms.ui.application.tools.GeosetAnimCopyPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;


public class ComponentGeosetAnimPanel extends ComponentPanel<GeosetAnim> {
	private final boolean listenersEnabled = true;
	private final JPanel animsPanelHolder;
	private FloatValuePanel alphaPanel;
	private ColorValuePanel colorPanel;
	private ComponentGeosetMaterialPanel geosetAnimPanel;
	GeosetAnim geosetAnim;
	private JLabel geosetLabel;


	public ComponentGeosetAnimPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		super(modelHandler, changeListener);
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

		alphaPanel = new FloatValuePanel("Alpha", modelHandler.getUndoManager(), changeListener);
		alphaPanel.setKeyframeHelper(new TimelineKeyNamer(modelHandler.getModel()));
		animsPanelHolder.add(alphaPanel, "wrap, span 2");

		colorPanel = new ColorValuePanel("Color", modelHandler.getUndoManager(), changeListener);
		colorPanel.setKeyframeHelper(new TimelineKeyNamer(modelHandler.getModel()));
		animsPanelHolder.add(colorPanel, "wrap, span 2");


	}

	@Override
	public void setSelectedItem(final GeosetAnim geosetAnim) {

//		animsPanelHolder.remove(geosetAnimPanel);

//		animPanels.putIfAbsent(geosetAnim, new ComponentGeosetMaterialPanel());
//		geosetAnimPanel = animPanels.get(geosetAnim);

//		geosetAnimPanel.setMaterialChooser(geosetAnim, modelViewManager, undoActionListener, modelStructureChangeListener);
//		animsPanelHolder.add(geosetAnimPanel);
		this.geosetAnim = geosetAnim;
		geosetLabel.setText(geosetAnim.getGeoset().getName());
		animsPanelHolder.revalidate();
		animsPanelHolder.repaint();
		alphaPanel.reloadNewValue((float) geosetAnim.getStaticAlpha(), (FloatAnimFlag) geosetAnim.find("Alpha"), geosetAnim, "Alpha", geosetAnim::setStaticAlpha);
		colorPanel.reloadNewValue(geosetAnim.getStaticColor(), (Vec3AnimFlag) geosetAnim.find("Color"), geosetAnim, "Color", geosetAnim::setStaticColor);

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
