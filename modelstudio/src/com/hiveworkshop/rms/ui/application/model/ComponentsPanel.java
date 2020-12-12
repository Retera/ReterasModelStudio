package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;

import javax.swing.*;
import java.awt.*;

public class ComponentsPanel extends JPanel {
	private static final String BLANK = "BLANK";
	private static final String HEADER = "HEADER";
	private static final String COMMENT = "COMMENT";
	private static final String ANIMATION = "ANIMATION";
	private static final String GLOBALSEQ = "GLOBALSEQ";
	private static final String BITMAP = "BITMAP";
	private static final String MATERIAL = "MATERIAL";
	private static final String GEOSET = "GEOSET";
	private final CardLayout cardLayout;
	private final JPanel blankPanel;
	private ComponentHeaderPanel headerPanel;
	private ComponentCommentPanel commentPanel;
	private ComponentAnimationPanel animationPanel;
	private ComponentGlobalSequencePanel globalSeqPanel;
	private ComponentBitmapPanel bitmapPanel;
	private ComponentMaterialPanel materialPanel;
	private ComponentGeosetPanel geosetPanel;

	public ComponentsPanel(final TextureExporter textureExporter) {
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		blankPanel = new JPanel();
		blankPanel.add(new JLabel("Select a model component to get started..."));
		add(blankPanel, BLANK);

		headerPanel = new ComponentHeaderPanel();
		add(headerPanel, HEADER);

		commentPanel = new ComponentCommentPanel();
		add(commentPanel, COMMENT);

		animationPanel = new ComponentAnimationPanel();
		add(animationPanel, ANIMATION);

		globalSeqPanel = new ComponentGlobalSequencePanel();
		add(globalSeqPanel, GLOBALSEQ);

		bitmapPanel = new ComponentBitmapPanel(textureExporter);
		add(bitmapPanel, BITMAP);

		materialPanel = new ComponentMaterialPanel();
		JScrollPane materialScrollPane = new JScrollPane(materialPanel);
		materialScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(materialScrollPane, MATERIAL);

//		geosetPanel = new ComponentGeosetPanel();
//		add(geosetPanel, GEOSET);

		cardLayout.show(this, BLANK);
	}

	public void selected(final EditableModel model) {
	}

	public void selectedHeaderData(final EditableModel model, final ModelViewManager modelViewManager,
	                               final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener) {
		headerPanel.setActiveModel(modelViewManager, undoListener, modelStructureChangeListener);
		cardLayout.show(this, HEADER);
	}

	public void selectedHeaderComment(final Iterable<String> comment) {
		commentPanel.setCommentContents(comment);
		cardLayout.show(this, COMMENT);
	}

//	@Override
//	public void selected(final Animation animation, final UndoActionListener undoListener,
//                         final ModelStructureChangeListener modelStructureChangeListener) {
//		animationPanel.setAnimation(animation, undoListener, modelStructureChangeListener);
//		cardLayout.show(this, ANIMATION);
//		currentPanel = headerPanel;
//	}

	public void selected(final EditableModel model, final Integer globalSequence, final int globalSequenceId,
	                     final UndoActionListener undoActionListener,
	                     final ModelStructureChangeListener modelStructureChangeListener) {
		globalSeqPanel.setGlobalSequence(model, globalSequence, globalSequenceId, undoActionListener,
				modelStructureChangeListener);
		cardLayout.show(this, GLOBALSEQ);
	}

//	public void selected(T itemToSelect, ModelViewManager modelViewManager, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
//
//	}

//	@Override
//	public void selected(final Bitmap texture, final ModelViewManager modelViewManager,
//                         final UndoActionListener undoActionListener,
//                         final ModelStructureChangeListener modelStructureChangeListener) {
//		bitmapPanel.setBitmap(texture, modelViewManager, undoActionListener, modelStructureChangeListener);
//		cardLayout.show(this, BITMAP);
//		currentPanel = headerPanel;
//	}

//	@Override
//	public void selected(final Material material, final ModelViewManager modelViewManager,
//                         final UndoActionListener undoActionListener,
//                         final ModelStructureChangeListener modelStructureChangeListener) {
//		materialPanel.setMaterial(material, modelViewManager, undoActionListener, modelStructureChangeListener);
//		cardLayout.show(this, MATERIAL);
//		currentPanel = materialPanel;
//	}


//	@Override
//	public void selected(final Geoset geoset, final ModelViewManager modelViewManager,
//	                     final UndoActionListener undoActionListener,
//	                     final ModelStructureChangeListener modelStructureChangeListener) {
//		geosetPanel.setSelectedGeoset(geoset, modelViewManager, undoActionListener, modelStructureChangeListener);
//		cardLayout.show(this, GEOSET);
//		currentPanel = geosetPanel;
//	}

	public void selectedBlank() {
		cardLayout.show(this, BLANK);
	}
}
