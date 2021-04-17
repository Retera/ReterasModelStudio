package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.ParticleEmitterPopcorn;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.actions.model.ParentChangeAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.ComponentGeosetMaterialPanel;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.TimelineKeyNamer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ComponentPopcornPanel extends JPanel implements ComponentPanel<ParticleEmitterPopcorn> {

	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	//	private final Map<AnimatedNode, ComponentGeosetMaterialPanel> nodePanels;
	private final boolean listenersEnabled = true;
	//	private final JLabel trisLabel;
//	private final JLabel vertLabel;
	private final ComponentEditorTextField popcornPathField;
	JPanel visGuidPanel;
	private ParticleEmitterPopcorn popcorn;
	//	private final JPanel nodePanelHolder;
	private ComponentGeosetMaterialPanel nodePanel;
	private FloatValuePanel alphaPanel;
	private FloatValuePanel lifeSpanPanel;
	private FloatValuePanel emissionRatePanel;
	private FloatValuePanel speedPanel;
	private FloatValuePanel visPanel;
	//	private FloatValuePanel transPanel;
//	private ColorValuePanel rotPanel;
//	private ColorValuePanel scalePanel;
	private ColorValuePanel colorPanel;
	private ComponentEditorTextField nameField;
	JLabel parentName;
	ParentChooser parentChooser;


	public ComponentPopcornPanel(final ModelViewManager modelViewManager,
	                             final UndoActionListener undoActionListener,
	                             final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
		this.modelStructureChangeListener = modelStructureChangeListener;

		parentChooser = new ParentChooser(modelViewManager);

		setLayout(new MigLayout("fill", "[][][grow]", "[][][grow]"));

		nameField = new ComponentEditorTextField(24);
		nameField.addEditingStoppedListener(this::textureNameField);
		add(nameField, "wrap");
//		nodePanels = new HashMap<>();
		popcornPathField = new ComponentEditorTextField(24);
		popcornPathField.addEditingStoppedListener(this::texturePathField);
		add(popcornPathField, "wrap");

		add(new JLabel("Parent: "), "split, spanx");
		parentName = new JLabel("Parent");
		add(parentName);
		JButton chooseParentButton = new JButton("change");
		chooseParentButton.addActionListener(e -> chooseParent());
		add(chooseParentButton, "wrap");

		visGuidPanel = new JPanel(new MigLayout("gap 0", "[]8[]"));
		add(visGuidPanel, "wrap");
		add(valuePanelsPanel(), "wrap");

//		nodePanelHolder = new JPanel(new MigLayout());
//		add(nodePanelHolder, "wrap, growx, span 3");
//
//		nodePanelHolder.add(new JLabel("Node"), "wrap");

//		nodePanel = new ComponentGeosetMaterialPanel();
//		nodePanelHolder.add(nodePanel);

//		JPanel geosetInfoPanel = new JPanel(new MigLayout());
//		add(geosetInfoPanel, "wrap, growx, span 3");

//		geosetInfoPanel.add(new JLabel("Triangles: "));
//		trisLabel = new JLabel("0");
//		geosetInfoPanel.add(trisLabel, "wrap");
//
//		geosetInfoPanel.add(new JLabel("Vertices: "));
//		vertLabel = new JLabel("0");
//		geosetInfoPanel.add(vertLabel, "wrap");

	}

	private JPanel valuePanelsPanel() {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0, fill"));
		lifeSpanPanel = getFloatValuePanel(panel, "LifeSpan");
		emissionRatePanel = getFloatValuePanel(panel, "EmissionRate");
		speedPanel = getFloatValuePanel(panel, "Speed");
		alphaPanel = getFloatValuePanel(panel, "Alpha");
		visPanel = getFloatValuePanel(panel, "Visibility");
//		transPanel = getFloatValuePanel(panel);
//		rotPanel = getColorValuePanel(panel, "Rotation");
//		scalePanel = getColorValuePanel(panel, "Scaling");

		ColorValuePanel colorPanel1 = getColorValuePanel(panel, "Color");

		colorPanel = colorPanel1;

		return panel;
	}

	private ColorValuePanel getColorValuePanel(JPanel panel, String title) {
		ColorValuePanel colorPanel1 = new ColorValuePanel(title, undoActionListener, modelStructureChangeListener);
		colorPanel1.setKeyframeHelper(new TimelineKeyNamer(modelViewManager.getModel()));
		JScrollPane colorScrollPane = new JScrollPane(colorPanel1);
		colorScrollPane.setMaximumSize(new Dimension(700, 300));
		colorScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		panel.add(colorScrollPane, "wrap, span 2, growx, hidemode 2");
		return colorPanel1;
	}

	private FloatValuePanel getFloatValuePanel(JPanel panel1, String title) {
		FloatValuePanel panel = new FloatValuePanel(title, undoActionListener, modelStructureChangeListener);
		panel.setKeyframeHelper(new TimelineKeyNamer(modelViewManager.getModel()));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setMaximumSize(new Dimension(700, 300));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		panel1.add(scrollPane, "wrap, span 2, growx");
		return panel;
	}

	private void texturePathField() {
		popcorn.setPath(popcornPathField.getText());
	}

	private void textureNameField() {
		popcorn.setName(nameField.getText());
	}

	@Override
	public void setSelectedItem(ParticleEmitterPopcorn itemToSelect) {
		popcorn = itemToSelect;

		IdObject parent = popcorn.getParent();
		if (parent != null) {
			this.parentName.setText(parent.getName());
		} else {
			parentName.setText("no parent");
		}

		nameField.reloadNewValue(popcorn.getName());
		popcornPathField.reloadNewValue(popcorn.getPath());
		popcorn.updateAnimsVisMap(modelViewManager.getModel().getAnims());
		updateAnimVisGuidPanel();
//		nodePanelHolder.revalidate();
//		nodePanelHolder.repaint();

		lifeSpanPanel.reloadNewValue(popcorn.getLifeSpan(), (FloatAnimFlag) popcorn.find("LifeSpan"), popcorn, "LifeSpan", popcorn::setLifeSpan);
		emissionRatePanel.reloadNewValue(popcorn.getEmissionRate(), (FloatAnimFlag) popcorn.find("EmissionRate"), popcorn, "EmissionRate", popcorn::setEmissionRate);
		speedPanel.reloadNewValue(popcorn.getSpeed(), (FloatAnimFlag) popcorn.find("Speed"), popcorn, "Speed", popcorn::setSpeed);
		alphaPanel.reloadNewValue(popcorn.getAlpha(), (FloatAnimFlag) popcorn.find("Alpha"), popcorn, "Alpha", popcorn::setAlpha);
		visPanel.reloadNewValue(1f, (FloatAnimFlag) popcorn.find("Visibility"), popcorn, "Visibility", null);
//		transPanel.reloadNewValue(0f, popcorn.find("Tran"), popcorn, "Trans", popcorn::setPivotPoint);
//		rotPanel.reloadNewValue(new Quat(0,0,0,1), (Vec3AnimFlag)popcorn.find("Rotation"), popcorn, "Rotation", null);
//		scalePanel.reloadNewValue(new Vec3(1,1,1), (Vec3AnimFlag)popcorn.find("Scaling"), popcorn, "Scaling", null);
		colorPanel.reloadNewValue(popcorn.getColor(), (Vec3AnimFlag) popcorn.find("Color"), popcorn, "Color", popcorn::setColor);

		revalidate();
		repaint();
	}

	@Override
	public void save(EditableModel model, UndoActionListener undoListener, ModelStructureChangeListener changeListener) {

	}

	private JPanel updateAnimVisGuidPanel() {
//		JPanel visGuidPanel = new JPanel(new MigLayout("gap 0", "[][]"));
		visGuidPanel.removeAll();
		for (Animation animation : modelViewManager.getModel().getAnims()) {
			visGuidPanel.add(new JLabel(animation.getName()));
			JButton button = new JButton(popcorn.getAnimVisState(animation).name());
			button.addActionListener(e -> setState(animation, button));
			visGuidPanel.add(button, "wrap, growx");
		}
		return visGuidPanel;
	}

	private ParticleEmitterPopcorn.State setState(Animation animation, JButton parent) {
		JPopupMenu popupMenu = new JPopupMenu();
//		ParticleEmitterPopcorn.State returnState = ParticleEmitterPopcorn.State.none;
		for (ParticleEmitterPopcorn.State state : ParticleEmitterPopcorn.State.values()) {
			JMenuItem menuItem = new JMenuItem(state.name());
//			menuItem.addActionListener(e -> returnState = state);
			menuItem.addActionListener(e -> {
				popcorn.setAnimVisState(animation, state);
				parent.setText(state.name());
			});
			popupMenu.add(menuItem);
		}
		popupMenu.show(parent, parent.getWidth(), 0);
		return ParticleEmitterPopcorn.State.none;
	}
//	private JPopupMenu statePopupMenu(){
//		JPopupMenu popupMenu = new JPopupMenu();
//		JMenuItem menuItem = new JMenuItem(ParticleEmitterPopcorn.State.on.name());
//		popupMenu.add(menuItem);
//		return popupMenu;
//	}

	private void chooseParent() {
		System.out.println(this.getRootPane());
		IdObject newParent = parentChooser.chooseParent(popcorn, this.getRootPane());
		ParentChangeAction action = new ParentChangeAction(popcorn, newParent, modelStructureChangeListener);
		action.redo();
		repaint();
		undoActionListener.pushAction(action);
	}
}
