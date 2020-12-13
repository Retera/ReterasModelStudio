package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComponentsPanel extends JPanel {
	private static final String BLANK = "BLANK";
	private static final String GLOBALSEQ = "GLOBALSEQ";
	private final CardLayout cardLayout;
	private final Map<Class<?>, ComponentPanel<?>> panelMap;
	private final ComponentGlobalSequencePanel globalSeqPanel;

	public ComponentsPanel(final ModelViewManager modelViewManager,
	                       final UndoActionListener undoActionListener,
	                       final ModelStructureChangeListener modelStructureChangeListener,
	                       final TextureExporter textureExporter) {
		panelMap = new HashMap<>();
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		JPanel blankPanel = new JPanel();
		blankPanel.add(new JLabel("Select a model component to get started..."));
		add(blankPanel, BLANK);
//		panelMap.put(null, blankPanel);

		ComponentHeaderPanel headerPanel = new ComponentHeaderPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		add(headerPanel, EditableModel.class.getName());
		panelMap.put(EditableModel.class, headerPanel);

		ComponentCommentPanel commentPanel = new ComponentCommentPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		add(commentPanel, ArrayList.class.getName());
		panelMap.put(ArrayList.class, commentPanel);

		ComponentAnimationPanel animationPanel = new ComponentAnimationPanel(undoActionListener, modelStructureChangeListener);
		add(animationPanel, Animation.class.getName());
		panelMap.put(Animation.class, animationPanel);


		globalSeqPanel = new ComponentGlobalSequencePanel();
		add(globalSeqPanel, GLOBALSEQ);
//		panelMap.put(EditableModel.class, globalSeqPanel);

		ComponentBitmapPanel bitmapPanel = new ComponentBitmapPanel(modelViewManager, undoActionListener, modelStructureChangeListener, textureExporter);
		add(bitmapPanel, Bitmap.class.getName());
		panelMap.put(Bitmap.class, bitmapPanel);

		ComponentMaterialPanel materialPanel = new ComponentMaterialPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane materialScrollPane = new JScrollPane(materialPanel);
		materialScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(materialScrollPane, Material.class.getName());
		panelMap.put(Material.class, materialPanel);

		ComponentGeosetPanel geosetPanel = new ComponentGeosetPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		add(geosetPanel, Geoset.class.getName());
		panelMap.put(Geoset.class, geosetPanel);

		cardLayout.show(this, BLANK);
	}


	public void selected(final EditableModel model, final Integer globalSequence, final int globalSequenceId,
	                     final UndoActionListener undoActionListener,
	                     final ModelStructureChangeListener modelStructureChangeListener) {
		globalSeqPanel.setGlobalSequence(model, globalSequence, globalSequenceId, undoActionListener,
				modelStructureChangeListener);
		cardLayout.show(this, GLOBALSEQ);
	}


	public <T> void setSelectedPanel(T selectedItem) {
		if (selectedItem != null && panelMap.containsKey(selectedItem.getClass())) {
			// Typing of this would be nice, if ever possible...
			ComponentPanel<T> componentPanel = (ComponentPanel<T>) panelMap.get(selectedItem.getClass());
			componentPanel.setSelectedItem(selectedItem);
			cardLayout.show(this, selectedItem.getClass().getName());
		} else {
			selectedBlank();
		}
	}

	public void selectedBlank() {
		cardLayout.show(this, BLANK);
	}
}
