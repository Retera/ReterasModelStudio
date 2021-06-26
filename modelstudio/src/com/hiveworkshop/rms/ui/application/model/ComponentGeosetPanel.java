package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.model.SetGeosetAnimAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.ChangeMaterialAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.TimelineKeyNamer;
import com.hiveworkshop.rms.ui.application.tools.GeosetAnimCopyPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.MaterialListCellRenderer;
import com.hiveworkshop.rms.util.BiMap;
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
	private final JLabel trisLabel;
	private final JLabel vertLabel;
	JPanel hdPanel;
	JSpinner lodSpinner;
	JTextField nameTextField;
	JButton toggleSdHd;
	//	private final JLabel selectionGroupLabel;
	private JSpinner selectionGroupSpinner;
	private Geoset geoset;
	private JLabel geosetLabel;

	private final boolean listenersEnabled = true;
	private final JPanel materialPanelHolder;
	private JPanel animVisPanel;
	private JPanel animPanel;

	DefaultListModel<Material> materials = new IterableListModel<>();
	JList<Material> materialList = new JList<>(materials);

	MaterialListCellRenderer renderer;

	public ComponentGeosetPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		super(modelHandler, changeListener);
		setLayout(new MigLayout("", "[][grow][grow]", "[]"));

		geosetLabel = new JLabel("geoset name");
		add(geosetLabel, "wrap");

		materialPanelHolder = new JPanel(new MigLayout("hidemode 1"));
		add(materialPanelHolder, "wrap, growx, spanx");

		materialPanelHolder.add(new JLabel("Material:"), "wrap");

		JPanel geosetInfoPanel = new JPanel(new MigLayout("fill, hidemode 1", "[][][grow][grow]"));
		add(geosetInfoPanel, "wrap, growx, spanx");

		createHDPanel(modelHandler.getModelView());
		geosetInfoPanel.add(hdPanel, "growx, spanx, wrap");

		geosetInfoPanel.add(new JLabel("Triangles: "));
		trisLabel = new JLabel("0");
		geosetInfoPanel.add(trisLabel, "wrap");

		geosetInfoPanel.add(new JLabel("Vertices: "));
		vertLabel = new JLabel("0");
		geosetInfoPanel.add(vertLabel, "wrap");

		geosetInfoPanel.add(new JLabel("SelectionGroup: "));
		selectionGroupSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		selectionGroupSpinner.addChangeListener(e -> setSelectionGroup());
		geosetInfoPanel.add(selectionGroupSpinner, "wrap");

		JButton editUvButton = new JButton("Edit Geoset UVs");
		toggleSdHd = new JButton("Make Geoset HD");
		toggleSdHd.addActionListener(e -> toggleSdHd());
		add(toggleSdHd, "wrap");
		animVisPanel = new JPanel();
		add(animVisPanel, "wrap");
		animPanel = new JPanel();
		add(animPanel, "wrap");

		getMaterialPanel();
	}

	private void updateMaterialPanel() {
		materialPanelHolder.removeAll();
		materialPanelHolder.add(new JLabel("Material:"), "wrap");
		JComboBox<String> materialChooser = getMaterialChooser();
		JButton cloneMaterial = new JButton("Clone This Material");
		cloneMaterial.addActionListener(e -> cloneMaterial());
		materialPanelHolder.add(materialChooser);
		materialPanelHolder.add(cloneMaterial);
	}

	private void cloneMaterial() {
		AddMaterialAction addMaterialAction = new AddMaterialAction(new Material(geoset.getMaterial()), modelHandler.getModel(), changeListener);
		modelHandler.getUndoManager().pushAction(addMaterialAction.redo());
	}

	private JComboBox<String> getMaterialChooser() {
		BiMap<String, Material> map = new BiMap<>();
		List<Material> materials = modelHandler.getModel().getMaterials();
		for (Material material : materials) {
			map.put("# " + map.size() + " " + material.getName(), material);
		}
		JComboBox<String> materialChooser = new JComboBox<>(map.keySet().toArray(new String[0]));
		materialChooser.setSelectedItem(map.getByValue(geoset.getMaterial()));
		materialChooser.addItemListener(e -> chooseMaterial(map, e));
		return materialChooser;
	}

	private void chooseMaterial(Map<String, Material> map, ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof String) {
			Material material = map.get((String) e.getItem());
			if (material != geoset.getMaterial()) {
				ChangeMaterialAction action = new ChangeMaterialAction(geoset, material, changeListener);
				modelHandler.getUndoManager().pushAction(action.redo());
			}
		}
	}

	private void getMaterialPanel() {
		renderer = new MaterialListCellRenderer(modelHandler.getModel());
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

	private void updateMaterialPane() {
		materials.clear();
		materials.addAll(modelHandler.getModel().getMaterials());
		renderer.setMaterial(geoset.getMaterial());
	}

	private void setGeosetMaterial(ListSelectionEvent e) {
		Material material = materialList.getSelectedValue();
		if (!e.getValueIsAdjusting() && material != null) {
			if (material != geoset.getMaterial()) {
				ChangeMaterialAction action = new ChangeMaterialAction(geoset, material, changeListener);
				modelHandler.getUndoManager().pushAction(action.redo());
			}
		}
	}

	private void createHDPanel(ModelView modelViewManager) {
		hdPanel = new JPanel(new MigLayout("fill, ins 0", "[]16[][grow][grow]"));

		hdPanel.add(new JLabel("Name: "));
		nameTextField = new JTextField(26);
		nameTextField.addFocusListener(setLoDName());
		hdPanel.add(nameTextField, "spanx 2, wrap");

		hdPanel.add(new JLabel("LevelOfDetail: "));
		lodSpinner = new JSpinner(new SpinnerNumberModel(0, -1, 10000, 1));
		hdPanel.add(lodSpinner, "wrap");
		lodSpinner.addChangeListener(e -> setLoD());

		hdPanel.setVisible(modelViewManager.getModel().getFormatVersion() == 1000);
	}


	@Override
	public void setSelectedItem(final Geoset geoset) {
		this.geoset = geoset;
		geosetLabel.setText(geoset.getName());

		setToggleButtonText();

		updateMaterialPane();

		updateMaterialPanel();
		materialPanelHolder.revalidate();
		materialPanelHolder.repaint();

		trisLabel.setText("" + geoset.getTriangles().size());
		vertLabel.setText("" + geoset.getVertices().size());

		selectionGroupSpinner.setValue(geoset.getSelectionGroup());
		lodSpinner.setValue(geoset.getLevelOfDetail());
		nameTextField.setText(geoset.getLevelOfDetailName());

		hdPanel.setVisible(modelHandler.getModel().getFormatVersion() == 1000);

		remove(animVisPanel);
		animVisPanel = getVisButtonPanel();
		add(animVisPanel, "wrap");

		remove(animPanel);
		animPanel = getGeosetAnimPanel();
		add(animPanel, "wrap");

		revalidate();
		repaint();
	}


	@Override
	public void save(final EditableModel model, final UndoManager undoManager,
	                 final ModelStructureChangeListener changeListener) {
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
				geoset.makeSd();
			} else {
				geoset.makeHd();
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

			FloatValuePanel alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA, modelHandler.getUndoManager(), changeListener);
			alphaPanel.setKeyframeHelper(new TimelineKeyNamer(modelHandler.getModel()));
			panel.add(alphaPanel, "wrap, span 2");

			ColorValuePanel colorPanel = new ColorValuePanel(modelHandler, MdlUtils.TOKEN_COLOR, modelHandler.getUndoManager(), changeListener);
			colorPanel.setKeyframeHelper(new TimelineKeyNamer(modelHandler.getModel()));
			panel.add(colorPanel, "wrap, span 2");

			alphaPanel.reloadNewValue((float) geosetAnim.getStaticAlpha(), (FloatAnimFlag) geosetAnim.find(MdlUtils.TOKEN_ALPHA), geosetAnim, MdlUtils.TOKEN_ALPHA, geosetAnim::setStaticAlpha);
			colorPanel.reloadNewValue(geosetAnim.getStaticColor(), (Vec3AnimFlag) geosetAnim.find(MdlUtils.TOKEN_COLOR), geosetAnim, MdlUtils.TOKEN_COLOR, geosetAnim::setStaticColor);
		} else {
			JButton addAnim = new JButton("Add GeosetAnim");
			addAnim.addActionListener(e -> modelHandler.getUndoManager().pushAction(new SetGeosetAnimAction(modelHandler.getModel(), geoset, changeListener).redo()));
			panel.add(addAnim);
		}
		return panel;
	}

	private void addAnim() {
		UndoAction action = new SetGeosetAnimAction(modelHandler.getModel(), geoset, changeListener);
		modelHandler.getUndoManager().pushAction(new SetGeosetAnimAction(modelHandler.getModel(), geoset, changeListener).redo());
	}

	private void copyFromOther() {
		GeosetAnimCopyPanel.show(this, modelHandler.getModelView(), geoset.getGeosetAnim(), changeListener, modelHandler.getUndoManager());
		repaint();
	}

	private JPanel getVisButtonPanel() {
		JPanel panel = new JPanel(new MigLayout("", "", ""));
		GeosetAnim geosetAnim = geoset.getGeosetAnim();

		if (geosetAnim != null && geosetAnim.getVisibilityFlag() != null) {
			TreeMap<Integer, Entry<Float>> entryMap = geosetAnim.getVisibilityFlag().getEntryMap();
			Map<Animation, String> animVisMap = new HashMap<>();
			for (Animation animation : modelHandler.getModel().getAnims()) {
				NavigableMap<Integer, Entry<Float>> animSubMap = entryMap.subMap(animation.getStart(), true, animation.getEnd(), true);
				if (animSubMap.isEmpty() || animSubMap.values().stream().allMatch(e -> e.getValue() >= 1)) {
					animVisMap.put(animation, "visible");
				} else if (animSubMap.values().stream().allMatch(e -> e.getValue() == 0)) {
					animVisMap.put(animation, "invisible");
				} else if (animSubMap.values().stream().anyMatch(e -> e.getValue() == 0)
						&& animSubMap.values().stream().anyMatch(e -> e.getValue() > 0)
						|| animSubMap.values().stream().anyMatch(e -> e.getValue() >= 1)
						&& animSubMap.values().stream().anyMatch(e -> e.getValue() < 1)) {
					animVisMap.put(animation, "animated");
				}
			}
			for (Animation animation : modelHandler.getModel().getAnims()) {
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
		NavigableMap<Integer, Entry<Float>> animSubMap = visibilityFlag.getEntryMap().subMap(animation.getStart(), true, animation.getEnd(), true);
		if (currVis.equals("visible")) {
			System.out.println("going invis!");
			if (animSubMap.isEmpty()) {
				Entry<Float> entry = new Entry<>(animation.getStart(), 0f);
				UndoAction action = new AddFlagEntryAction(visibilityFlag, entry, geoset.getGeosetAnim(), changeListener);
				modelHandler.getUndoManager().pushAction(action.redo());
			} else {
				List<UndoAction> actions = new ArrayList<>();
				for (Entry<Float> entry : animSubMap.values()) {
					Entry<Float> newEntry = new Entry<>(entry.getTime(), 0f);
					actions.add(new ChangeFlagEntryAction<>(visibilityFlag, newEntry, entry, null));
				}
				UndoAction action = new CompoundAction("Set visible", actions, changeListener::geosetsUpdated);
				modelHandler.getUndoManager().pushAction(action.redo());
			}
		} else if (currVis.equals("invisible")) {
			System.out.println("going vis!");
			if (!animSubMap.isEmpty()) {
				System.out.println("woop!");
				List<UndoAction> actions = new ArrayList<>();
				for (Entry<Float> entry : animSubMap.values()) {
					Entry<Float> newEntry = new Entry<>(entry.getTime(), 1f);
					actions.add(new ChangeFlagEntryAction<>(visibilityFlag, newEntry, entry, null));
				}
				UndoAction action = new CompoundAction("Set invisible", actions, changeListener::geosetsUpdated);
				modelHandler.getUndoManager().pushAction(action.redo());
			}
		}
	}

}
