package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.nodepanels.*;

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

	public ComponentsPanel(ModelViewManager modelViewManager,
	                       UndoActionListener undoActionListener,
	                       ModelStructureChangeListener modelStructureChangeListener) {
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

		ComponentAnimationPanel animationPanel = new ComponentAnimationPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		add(animationPanel, Animation.class.getName());
		panelMap.put(Animation.class, animationPanel);


		globalSeqPanel = new ComponentGlobalSequencePanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		add(globalSeqPanel, Integer.class.getName());
		panelMap.put(Integer.class, globalSeqPanel);
//		add(globalSeqPanel, GLOBALSEQ);
////		panelMap.put(EditableModel.class, globalSeqPanel);

		ComponentBitmapPanel bitmapPanel = new ComponentBitmapPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
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

		ComponentGeosetAnimPanel geosetAnimPanel = new ComponentGeosetAnimPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane geosetAnimScrollPane = new JScrollPane(geosetAnimPanel);
		geosetAnimScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(geosetAnimScrollPane, GeosetAnim.class.getName());
		panelMap.put(GeosetAnim.class, geosetAnimPanel);

		ComponentNodePanel nodePanel = new ComponentNodePanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		add(nodePanel, AnimatedNode.class.getName());
		panelMap.put(AnimatedNode.class, nodePanel);

		ComponentPopcornPanel popcornPanel = new ComponentPopcornPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane popcornScrollPane = new JScrollPane(popcornPanel);
		popcornScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(popcornScrollPane, ParticleEmitterPopcorn.class.getName());
		panelMap.put(ParticleEmitterPopcorn.class, popcornPanel);

		ComponentBonePanel bonePanel = new ComponentBonePanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane boneScrollPane = new JScrollPane(bonePanel);
		boneScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(boneScrollPane, Bone.class.getName());
		panelMap.put(Bone.class, bonePanel);

		ComponentHelperPanel helperPanel = new ComponentHelperPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane helperScrollPane = new JScrollPane(helperPanel);
		helperScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(helperScrollPane, Helper.class.getName());
		panelMap.put(Helper.class, helperPanel);

		ComponentParticle2Panel particle2Panel = new ComponentParticle2Panel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane particle2ScrollPane = new JScrollPane(particle2Panel);
		particle2ScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(particle2ScrollPane, ParticleEmitter2.class.getName());
		panelMap.put(ParticleEmitter2.class, particle2Panel);

		ComponentCollisionPanel collisionPanel = new ComponentCollisionPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane collosionScrollPane = new JScrollPane(collisionPanel);
		collosionScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(collosionScrollPane, CollisionShape.class.getName());
		panelMap.put(CollisionShape.class, collisionPanel);

		ComponentEventPanel eventPanel = new ComponentEventPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane eventScrollPane = new JScrollPane(eventPanel);
		eventScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(eventScrollPane, EventObject.class.getName());
		panelMap.put(EventObject.class, eventPanel);

		ComponentRibbonPanel ribbonPanel = new ComponentRibbonPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane ribbonScrollPane = new JScrollPane(ribbonPanel);
		ribbonScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(ribbonScrollPane, RibbonEmitter.class.getName());
		panelMap.put(RibbonEmitter.class, ribbonPanel);

		ComponentAttatchmentPanel attachmentPanel = new ComponentAttatchmentPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		JScrollPane attachmentScrollPane = new JScrollPane(attachmentPanel);
		attachmentScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(attachmentScrollPane, Attachment.class.getName());
		panelMap.put(Attachment.class, attachmentPanel);

//		ComponentPopcornPanel popcornPanel = new ComponentPopcornPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
//		JScrollPane popcornScrollPane = new JScrollPane(popcornPanel);
//		popcornScrollPane.getVerticalScrollBar().setUnitIncrement(16);
//		add(popcornScrollPane, ParticleEmitterPopcorn.class.getName());
//		panelMap.put(ParticleEmitterPopcorn.class, popcornPanel);

		ComponentFaceEffectPanel faceEffectPanel = new ComponentFaceEffectPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		add(faceEffectPanel, FaceEffect.class.getName());
		panelMap.put(FaceEffect.class, faceEffectPanel);

		ComponentCameraPanel cameraPanel = new ComponentCameraPanel(modelViewManager, undoActionListener, modelStructureChangeListener);
		add(cameraPanel, Camera.class.getName());
		panelMap.put(Camera.class, cameraPanel);

		cardLayout.show(this, BLANK);
	}


	public void selected(EditableModel model, Integer globalSequence, int globalSequenceId,
	                     UndoActionListener undoListener,
	                     ModelStructureChangeListener changeListener) {
//		globalSeqPanel.setGlobalSequence(model, globalSequence, globalSequenceId, undoListener, changeListener);
		globalSeqPanel.setSelectedItem(globalSequence);
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
