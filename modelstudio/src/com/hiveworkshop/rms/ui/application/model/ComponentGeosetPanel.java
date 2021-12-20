package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.mesh.DeleteGeosetAction;
import com.hiveworkshop.rms.editor.actions.model.SetGeosetAnimAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.ChangeMaterialAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.MakeModelHD;
import com.hiveworkshop.rms.ui.application.actionfunctions.MakeModelSD;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.tools.GeosetAnimCopyPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.MaterialListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.*;

public class ComponentGeosetPanel extends ComponentPanel<Geoset> {
	private final JLabel geosetLabel = new JLabel("geoset name");
	private final JLabel trisLabel = new JLabel("0");
	private final JLabel vertLabel = new JLabel("0");
	private JPanel lodPanel;
	private JPanel hdNamePanel;
	private JSpinner lodSpinner;
	private JTextField nameTextField;
	private JButton toggleSdHd;
	private JSpinner selectionGroupSpinner;
	private Geoset geoset;

	private JPanel animVisPanel;
	private JPanel animPanel;

	private final JComboBox<Material> materialChooser = new JComboBox<>();

	public ComponentGeosetPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("hidemode 1", "[][grow][grow]", "[]"));

		add(geosetLabel, "");
		add(getDeleteButton(e -> removeGeoset()), "skip 1, wrap");
		hdNamePanel = getHdNamePanel();
		add(hdNamePanel, "wrap, growx, spanx");

		JPanel topPanel = new JPanel(new MigLayout("hidemode 1, ins 0", "[][grow][grow]", "[]"));
		add(topPanel, "wrap");
		topPanel.add(getGeosetInfoPanel(), "growx");
		JPanel materialPanelHolder = getMaterialPanelHolder(modelHandler);
		topPanel.add(materialPanelHolder, "wrap, growx, spanx");


		JButton editUvButton = new JButton("Edit Geoset UVs");
		toggleSdHd = new JButton("Make Geoset HD");
		toggleSdHd.addActionListener(e -> toggleSdHd());
		add(toggleSdHd, "wrap");
		animVisPanel = new JPanel();
		add(animVisPanel, "wrap");
		animPanel = new JPanel();
		add(animPanel, "wrap");
	}

	private JPanel getMaterialPanelHolder(ModelHandler modelHandler) {
		JPanel materialPanelHolder = new JPanel(new MigLayout("hidemode 1"));
		materialPanelHolder.add(new JLabel("Material:"), "wrap");

		materialChooser.setRenderer(new MaterialListRenderer(modelHandler.getModel()));
		materialChooser.addItemListener(this::changeTexture);
		materialPanelHolder.add(materialChooser, "wrap");

		JButton cloneMaterial = new JButton("Clone This Material");
		cloneMaterial.addActionListener(e -> cloneMaterial());
		materialPanelHolder.add(cloneMaterial);

		return materialPanelHolder;
	}

	private JPanel getGeosetInfoPanel() {
		JPanel geosetInfoPanel = new JPanel(new MigLayout("fill, hidemode 1", "[][][grow][grow]"));

//		createHDPanel(modelHandler.getModelView());
		lodPanel = getLodPanel();
		geosetInfoPanel.add(lodPanel, "growx, spanx, wrap");

		geosetInfoPanel.add(new JLabel("Triangles: "));
		geosetInfoPanel.add(trisLabel, "wrap");

		geosetInfoPanel.add(new JLabel("Vertices: "));
		geosetInfoPanel.add(vertLabel, "wrap");

		geosetInfoPanel.add(new JLabel("SelectionGroup: "));
		selectionGroupSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		selectionGroupSpinner.addChangeListener(e -> setSelectionGroup());
		geosetInfoPanel.add(selectionGroupSpinner, "wrap");
		return geosetInfoPanel;
	}

	private void changeTexture(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Material itemAt = materialChooser.getItemAt(materialChooser.getSelectedIndex());
			undoManager.pushAction(new ChangeMaterialAction(geoset, itemAt, changeListener).redo());
		}
	}

	private void updateTextureChooser() {
		Material[] materials = modelHandler.getModel().getMaterials().toArray(new Material[0]);
		DefaultComboBoxModel<Material> bitmapModel = new DefaultComboBoxModel<>(materials);
		bitmapModel.setSelectedItem(geoset.getMaterial());
		materialChooser.setModel(bitmapModel);
	}

	private void cloneMaterial() {
		AddMaterialAction addMaterialAction = new AddMaterialAction(geoset.getMaterial().deepCopy(), modelHandler.getModel(), changeListener);
		undoManager.pushAction(addMaterialAction.redo());
	}

	private JPanel getHdNamePanel() {
		JPanel hdNamePanel = new JPanel(new MigLayout("fill, ins 0", "[]16[][grow][grow]"));
		hdNamePanel.add(new JLabel("Name: "));
		nameTextField = new JTextField(26);
		nameTextField.addFocusListener(setLoDName());
		hdNamePanel.add(nameTextField, "spanx 2, wrap");
		return hdNamePanel;
	}

	private JPanel getLodPanel() {
		JPanel lodPanel = new JPanel(new MigLayout("fill, ins 0", "[]16[][grow][grow]"));
		lodPanel.add(new JLabel("LevelOfDetail: "));
		lodSpinner = new JSpinner(new SpinnerNumberModel(0, -1, 100, 1));
		lodSpinner.addChangeListener(e -> setLoD());
		lodPanel.add(lodSpinner, "wrap");
		return lodPanel;
	}


	@Override
	public void setSelectedItem(final Geoset geoset) {
		this.geoset = geoset;
		geosetLabel.setText(geoset.getName());

		setToggleButtonText();
		updateTextureChooser();

		trisLabel.setText("" + geoset.getTriangles().size());
		vertLabel.setText("" + geoset.getVertices().size());

		selectionGroupSpinner.setValue(geoset.getSelectionGroup());
		lodSpinner.setValue(geoset.getLevelOfDetail());
		nameTextField.setText(geoset.getLevelOfDetailName());

		hdNamePanel.setVisible(modelHandler.getModel().getFormatVersion() == 1000);
		lodPanel.setVisible(modelHandler.getModel().getFormatVersion() == 1000);

		remove(animVisPanel);
		animVisPanel = getVisButtonPanel();
		add(animVisPanel, "wrap");

		remove(animPanel);
		animPanel = getGeosetAnimPanel();
		add(animPanel, "wrap");

		revalidate();
		repaint();
	}

	private void setSelectionGroup() {
		geoset.setSelectionGroup((Integer) selectionGroupSpinner.getValue());
	}

	private void setLoD() {
		geoset.setLevelOfDetail((Integer) lodSpinner.getValue());
	}

	private void toggleSdHd() {
		if (geoset != null) {
			if (geoset.isHD()) {
				MakeModelSD.makeSd(geoset);
			} else {
				MakeModelHD.makeHd(geoset);
			}
			setToggleButtonText();
		}
	}

	private void setToggleButtonText() {
		toggleSdHd.setVisible(modelHandler.getModel().getFormatVersion() >= 900);
		if (geoset.isHD()) {
			toggleSdHd.setText("Make Geoset SD");
		} else {
			toggleSdHd.setText("Make Geoset HD");
		}
	}

	private FocusAdapter setLoDName() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				geoset.setLevelOfDetailName(nameTextField.getText());
			}
		};
	}

	private void editUVs() {

	}

	private JPanel getGeosetAnimPanel() {
		JPanel panel = new JPanel(new MigLayout("fill", "[]", "[][][grow]"));
		GeosetAnim geosetAnim = geoset.getGeosetAnim();

		if (geosetAnim != null) {
			panel.add(new JLabel("GeosetAnim"), "wrap");

			JButton button = new JButton("copy all geosetAnim-info from other");
			button.addActionListener(e -> copyFromOther());
			panel.add(button, "wrap");

			FloatValuePanel alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA);
			panel.add(alphaPanel, "wrap, span 2");

			ColorValuePanel colorPanel = new ColorValuePanel(modelHandler, MdlUtils.TOKEN_COLOR);
			panel.add(colorPanel, "wrap, span 2");

			alphaPanel.reloadNewValue((float) geosetAnim.getStaticAlpha(), (FloatAnimFlag) geosetAnim.find(MdlUtils.TOKEN_ALPHA), geosetAnim, MdlUtils.TOKEN_ALPHA, geosetAnim::setStaticAlpha);
			colorPanel.reloadNewValue(geosetAnim.getStaticColor(), (Vec3AnimFlag) geosetAnim.find(MdlUtils.TOKEN_COLOR), geosetAnim, MdlUtils.TOKEN_COLOR, geosetAnim::setStaticColor);
		} else {
			JButton addAnim = new JButton("Add GeosetAnim");
			addAnim.addActionListener(e -> undoManager.pushAction(new SetGeosetAnimAction(model, geoset, changeListener).redo()));
			panel.add(addAnim);
		}
		return panel;
	}

	private void copyFromOther() {
		GeosetAnimCopyPanel.show(ProgramGlobals.getMainPanel(), model, geoset.getGeosetAnim(), undoManager);
		repaint();
	}

	private JPanel getVisButtonPanel() {
		JPanel panel = new JPanel(new MigLayout("", "", ""));
		GeosetAnim geosetAnim = geoset.getGeosetAnim();

		if (geosetAnim != null && geosetAnim.getVisibilityFlag() != null) {
			Map<Animation, String> animVisMap = new HashMap<>();
			for (Animation animation : model.getAnims()) {
				TreeMap<Integer, Entry<Float>> entryMap = geosetAnim.getVisibilityFlag().getEntryMap(animation);
				if (entryMap != null && !entryMap.isEmpty()) {
					float firstValue = entryMap.get(entryMap.firstKey()).getValue();
					Collection<Entry<Float>> visEntries = entryMap.values();
					if (visEntries.stream().allMatch(e -> e.getValue() >= 1)) {
						animVisMap.put(animation, "visible");
					} else if (visEntries.stream().allMatch(e -> e.getValue() == 0)) {
						animVisMap.put(animation, "invisible");
					} else if (visEntries.stream().anyMatch(e -> e.getValue() != firstValue)) {
						animVisMap.put(animation, "animated");
					}
				} else {
					animVisMap.put(animation, "visible");
				}
			}
			for (Animation animation : model.getAnims()) {
				if (animVisMap.containsKey(animation)) {
					panel.add(new JLabel(animation.getName()));
					JButton animButton = new JButton(animVisMap.get(animation));
					animButton.addActionListener(e -> toggleVisibility(animation, animVisMap.get(animation)));
					if (animVisMap.get(animation).equals("visible")) {
						animButton.setBackground(new Color(120, 150, 255));
//						animButton.setForeground(Color.WHITE);
					} else if (animVisMap.get(animation).equals("animated")) {
						animButton.setBackground(new Color(200, 150, 255));
						animButton.setEnabled(false);
					}
					panel.add(animButton, "wrap");
				}
			}
		}

		return panel;
	}

	private void toggleVisibility(Animation animation, String currVis) {
		AnimFlag<Float> visibilityFlag = geoset.getGeosetAnim().getVisibilityFlag();
		TreeMap<Integer, Entry<Float>> entryMap = visibilityFlag.getEntryMap(animation);
		if (currVis.equals("visible")) {
			System.out.println("going invis!");
			if (entryMap == null || entryMap.isEmpty()) {
				Entry<Float> entry = new Entry<>(animation.getStart(), 0f);
				UndoAction action = new AddFlagEntryAction<>(visibilityFlag, entry, animation, changeListener);
				undoManager.pushAction(action.redo());
			} else {
				List<UndoAction> actions = new ArrayList<>();
				for (Entry<Float> entry : entryMap.values()) {
					Entry<Float> newEntry = new Entry<>(entry.getTime(), 0f);
					actions.add(new ChangeFlagEntryAction<>(visibilityFlag, newEntry, entry, animation, null));
				}
				UndoAction action = new CompoundAction("Set visible", actions, changeListener::geosetsUpdated);
				undoManager.pushAction(action.redo());
			}
		} else if (currVis.equals("invisible")) {
			System.out.println("going vis!");
			if (!entryMap.isEmpty()) {
				System.out.println("woop!");
				List<UndoAction> actions = new ArrayList<>();
				for (Entry<Float> entry : entryMap.values()) {
					Entry<Float> newEntry = new Entry<>(entry.getTime(), 1f);
					actions.add(new ChangeFlagEntryAction<>(visibilityFlag, newEntry, entry, animation, null));
				}
				UndoAction action = new CompoundAction("Set invisible", actions, changeListener::geosetsUpdated);
				undoManager.pushAction(action.redo());
			}
		}
	}


//	private void chooseMaterial(Map<String, Material> map, ItemEvent e) {
//		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof String) {
//			Material material = map.get((String) e.getItem());
//			if (material != geoset.getMaterial()) {
//				ChangeMaterialAction action = new ChangeMaterialAction(geoset, material, changeListener);
//				modelHandler.getUndoManager().pushAction(action.redo());
//			}
//		}
//	}

//	private void updateMaterialPanel() {
//		materialPanelHolder.removeAll();
//		materialPanelHolder.add(new JLabel("Material:"), "wrap");
//		JComboBox<String> materialChooser = getMaterialChooser();
//		JButton cloneMaterial = new JButton("Clone This Material");
//		cloneMaterial.addActionListener(e -> cloneMaterial());
//		materialPanelHolder.add(materialChooser);
//		materialPanelHolder.add(cloneMaterial);
//	}


//	private JComboBox<String> getMaterialChooser() {
//		BiMap<String, Material> map = new BiMap<>();
//		List<Material> materials = modelHandler.getModel().getMaterials();
//		for (Material material : materials) {
//			map.put("# " + map.size() + " " + material.getName(), material);
//		}
//		JComboBox<String> materialChooser = new JComboBox<>(map.keySet().toArray(new String[0]));
//		materialChooser.setSelectedItem(map.getByValue(geoset.getMaterial()));
//		materialChooser.addItemListener(e -> chooseMaterial(map, e));
//		return materialChooser;
//	}

	DefaultListModel<Material> materials = new IterableListModel<>();
	JList<Material> materialList = new JList<>(materials);
	MaterialListCellRenderer renderer;

	private void getMaterialPanel() {
		renderer = new MaterialListCellRenderer(model);
//		materials = new IterableListModel<>(modelHandler.getModel().getMaterials());
		JLabel materialText;
		JScrollPane materialListPane;
		materialText = new JLabel("Material:");
		add(materialText, "left, wrap");
		// Header for materials list

		materialList.setCellRenderer(renderer);
		materialList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		materialList.addListSelectionListener(this::setGeosetMaterial);

		materialListPane = new JScrollPane(materialList);
		add(materialListPane, "spanx, wrap");
	}

	private void setGeosetMaterial(ListSelectionEvent e) {
		Material material = materialList.getSelectedValue();
		if (!e.getValueIsAdjusting() && material != null) {
			if (material != geoset.getMaterial()) {
				ChangeMaterialAction action = new ChangeMaterialAction(geoset, material, changeListener);
				undoManager.pushAction(action.redo());
			}
		}
	}

	private void removeGeoset() {
		UndoAction action = new DeleteGeosetAction(model, geoset, changeListener);
		undoManager.pushAction(action.redo());
	}


	// ToDo give GeosetPanel an additional visibillity button panel width the vissibillity of the material
	//  or preferably put this next to the geosetAnimVisButtonPanel
	private JPanel getMatVisButtonPanel() {
		JPanel panel = new JPanel(new MigLayout("", "", ""));
		Material material = geoset.getMaterial();

		if (material != null) {
			Map<Animation, String> animVisMap = new HashMap<>();
//			for (Animation animation : model.getAnims()) {
//				TreeMap<Integer, Entry<Float>> entryMap = geosetAnim.getVisibilityFlag().getEntryMap(animation);
//				if (entryMap != null && !entryMap.isEmpty()) {
//					float firstValue = entryMap.get(entryMap.firstKey()).getValue();
//					Collection<Entry<Float>> visEntries = entryMap.values();
//					if (visEntries.stream().allMatch(e -> e.getValue() >= 1)) {
//						animVisMap.put(animation, "visible");
//					} else if (visEntries.stream().allMatch(e -> e.getValue() == 0)) {
//						animVisMap.put(animation, "invisible");
//					} else if (visEntries.stream().anyMatch(e -> e.getValue() != firstValue)) {
//						animVisMap.put(animation, "animated");
//					}
//				} else {
//					animVisMap.put(animation, "visible");
//				}
//			}
			for (Animation animation : model.getAnims()) {
				if (animVisMap.containsKey(animation)) {
					panel.add(new JLabel(animation.getName()));
					JButton animButton = new JButton(animVisMap.get(animation));
					animButton.addActionListener(e -> toggleVisibility(animation, animVisMap.get(animation)));
					if (animVisMap.get(animation).equals("visible")) {
						animButton.setBackground(new Color(120, 150, 255));
//						animButton.setForeground(Color.WHITE);
					} else if (animVisMap.get(animation).equals("animated")) {
						animButton.setBackground(new Color(200, 150, 255));
						animButton.setEnabled(false);
					}
					panel.add(animButton, "wrap");
				}
			}
		}

		return panel;
	}
}
