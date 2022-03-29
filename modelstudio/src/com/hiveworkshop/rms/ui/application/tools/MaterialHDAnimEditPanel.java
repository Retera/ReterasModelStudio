package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MaterialHDAnimEditPanel extends JPanel {
	private Material donMaterial;
	private boolean invertVis = false;
	private boolean copyVis = true;
	private boolean copyColor = true;
	private boolean copyFresOp = true;
	private boolean copyEmissive = true;
	private boolean invFresOp = false;
	private boolean invEmissive = false;
	private FlipColor flipColor = FlipColor.DONT_FLIP;
	private static Material prototypeMaterial = new Material(new Layer(new Bitmap("Non-existing image for prototype purposes cuz Swing")));

	private final UndoManager undoManager;

	/**
	 * Create the panel.
	 */
	public MaterialHDAnimEditPanel(EditableModel model, Material recMaterial, UndoManager undoManager) {
		this.undoManager = undoManager;
		setLayout(new MigLayout("fill", "[grow][grow]"));

		JTextArea info = new JTextArea("Copies all animation data from the chosen Material to this Material.");
		info.setEditable(false);
		info.setOpaque(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);

		JCheckBox invert_Vis = new JCheckBox("invert visibility");
		invert_Vis.addActionListener(e -> invertVis = invert_Vis.isSelected());
		JCheckBox copy_Vis = new JCheckBox("copy visibility", true);
		copy_Vis.addActionListener(e -> copyVis = copy_Vis.isSelected());

		JCheckBox copy_fOpacity = new JCheckBox("copy fresnel opacity", true);
		copy_Vis.addActionListener(e -> copyFresOp = copy_fOpacity.isSelected());
		JCheckBox invert_fOpacity = new JCheckBox("invert fresnel opacity");
		invert_Vis.addActionListener(e -> invFresOp = invert_fOpacity.isSelected());

		JCheckBox copy_emissive = new JCheckBox("copy emissive gain", true);
		copy_Vis.addActionListener(e -> copyEmissive = copy_emissive.isSelected());
		JCheckBox invert_emissive = new JCheckBox("invert emissive gain");
		invert_Vis.addActionListener(e -> invEmissive = invert_emissive.isSelected());

		JCheckBox copy_color = new JCheckBox("copy fresnel color", true);
		copy_color.addActionListener(e -> copyColor = copy_color.isSelected());
		TwiComboBox<FlipColor> flipColorBox = new TwiComboBox<>(FlipColor.values());
		flipColorBox.addOnSelectItemListener(fc -> flipColor = fc);

		JButton copyButton = new JButton("Copy Animation Data");
		copyButton.addActionListener(e -> doCopy(recMaterial, donMaterial, model.getAllSequences()));

		add(info, "spanx, growx, wrap");
		add(getDonGeoAnimPanel(model.getMaterials()), "spanx, growx, aligny top, wrap");
		add(copy_Vis);
		add(invert_Vis, "wrap");
		add(copy_emissive, "");
		add(invert_emissive, "wrap");
		add(copy_fOpacity, "");
		add(invert_fOpacity, "wrap");
		add(copy_color, "");
		add(flipColorBox, "wrap");
		add(copyButton, "spanx, align center, wrap");
	}

	public static void show(JComponent parent, EditableModel model, Material material, UndoManager undoManager) {
		MaterialHDAnimEditPanel animCopyPanel = new MaterialHDAnimEditPanel(model, material, undoManager);
		FramePopup.show(animCopyPanel, parent, material.getName());
	}

	private JPanel getDonGeoAnimPanel(List<Material> materials) {
		JPanel donMaterialPanel = new JPanel(new MigLayout("fill, gap 0"));
		donMaterialPanel.add(new JLabel("From:"), "wrap");


		TwiComboBox<Material> comboBox = new TwiComboBox<>(materials, prototypeMaterial);
		comboBox.addOnSelectItemListener(mat -> donMaterial = mat);
		comboBox.setStringFunctionRender((mat) -> ((Material) mat).getName());

		donMaterial = materials.get(0);
		donMaterialPanel.add(comboBox, "wrap, growx");
		return donMaterialPanel;
	}

	private void doCopy(Material recMaterial, Material donMaterial, List<Sequence> allSequences) {
		ArrayList<UndoAction> actions = new ArrayList<>();

		for(int i = 0; i<donMaterial.getLayers().size() && i<recMaterial.getLayers().size(); i++){
			addLayerActions(actions, donMaterial.getLayer(i), recMaterial.getLayer(i), allSequences);
		}

		if(!actions.isEmpty()){
			undoManager.pushAction(new CompoundAction("Copy Material anim data", actions, ModelStructureChangeListener.changeListener::geosetsUpdated).redo());
		}
	}

	private void addLayerActions(ArrayList<UndoAction> actions, Layer donMatLayer, Layer recMatLayer) {
		for (AnimFlag<?> animFlag : donMatLayer.getAnimFlags()){
			if (copyVis && (animFlag.getName().equals(MdlUtils.TOKEN_ALPHA) || animFlag.getName().equals(MdlUtils.TOKEN_VISIBILITY))){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				invertFloatAnimFlag(animFlag, invertVis, (FloatAnimFlag) newAnimFlag);
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			} else if (copyEmissive && (animFlag.getName().equals(MdlUtils.TOKEN_EMISSIVE) || animFlag.getName().equals(MdlUtils.TOKEN_EMISSIVE_GAIN))){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				invertFloatAnimFlag(animFlag, invEmissive, (FloatAnimFlag) newAnimFlag);
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			} else if (copyFresOp && animFlag.getName().equals(MdlUtils.TOKEN_FRESNEL_OPACITY)){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				invertFloatAnimFlag(animFlag, invFresOp, (FloatAnimFlag) newAnimFlag);
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			} else if (copyColor && (animFlag.getName().equals(MdlUtils.TOKEN_FRESNEL_COLOR) || animFlag.getName().equals(MdlUtils.TOKEN_COLOR))){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				if(flipColor != FlipColor.DONT_FLIP && animFlag instanceof Vec3AnimFlag){
					Vec3AnimFlag floatAnimFlag = (Vec3AnimFlag) newAnimFlag;
					for(TreeMap<Integer, Entry<Vec3>> entryMap : floatAnimFlag.getAnimMap().values()){
						for(Entry<Vec3> entry : entryMap.values()){
							Vec3 value = entry.getValue();
							switch (flipColor){
								case DONT_FLIP  -> value.set(value.x, value.y, value.z);
								case RED_GREEN  -> value.set(value.y, value.x, value.z);
								case RED_BLUE   -> value.set(value.z, value.y, value.x);
								case GREEN_BLUE -> value.set(value.x, value.z, value.y);
								case RGB_BRG    -> value.set(value.z, value.x, value.y);
								case RGB_GBR    -> value.set(value.y, value.z, value.x);
							}
						}
					}
				}
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			} else if (!animFlag.getName().equals(MdlUtils.TOKEN_COLOR)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_FRESNEL_COLOR)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_ALPHA)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_VISIBILITY)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_FRESNEL_OPACITY)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_EMISSIVE)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_EMISSIVE_GAIN)){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			}
		}
	}

	private void invertFloatAnimFlag(AnimFlag<?> animFlag, boolean invert, FloatAnimFlag newAnimFlag) {
		if(invert && animFlag instanceof FloatAnimFlag){
			for(TreeMap<Integer, Entry<Float>> entryMap : newAnimFlag.getAnimMap().values()){
				for(Entry<Float> entry : entryMap.values()){
					entry.setValue(Math.min(1f, Math.max(0f, 1f-entry.getValue())));
				}
			}
		}
	}

	private void addLayerActions(ArrayList<UndoAction> actions, Layer donMatLayer, Layer recMatLayer, List<Sequence> allSequences) {
		for (AnimFlag<?> animFlag : donMatLayer.getAnimFlags()){
			if (copyVis && (animFlag.getName().equals(MdlUtils.TOKEN_ALPHA) || animFlag.getName().equals(MdlUtils.TOKEN_VISIBILITY))){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				invertFloatAnimFlag(animFlag, invertVis, (FloatAnimFlag) newAnimFlag, allSequences);
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			} else if (copyEmissive && (animFlag.getName().equals(MdlUtils.TOKEN_EMISSIVE) || animFlag.getName().equals(MdlUtils.TOKEN_EMISSIVE_GAIN))){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				invertFloatAnimFlag(animFlag, invEmissive, (FloatAnimFlag) newAnimFlag, allSequences);
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			} else if (copyFresOp && animFlag.getName().equals(MdlUtils.TOKEN_FRESNEL_OPACITY)){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				invertFloatAnimFlag(animFlag, invFresOp, (FloatAnimFlag) newAnimFlag, allSequences);
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			} else if (copyColor && (animFlag.getName().equals(MdlUtils.TOKEN_FRESNEL_COLOR) || animFlag.getName().equals(MdlUtils.TOKEN_COLOR))){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				if(flipColor != FlipColor.DONT_FLIP && animFlag instanceof Vec3AnimFlag){
					Vec3AnimFlag floatAnimFlag = (Vec3AnimFlag) newAnimFlag;
					for(TreeMap<Integer, Entry<Vec3>> entryMap : floatAnimFlag.getAnimMap().values()){
						for(Entry<Vec3> entry : entryMap.values()){
							Vec3 value = entry.getValue();
							switch (flipColor){
								case DONT_FLIP  -> value.set(value.x, value.y, value.z);
								case RED_GREEN  -> value.set(value.y, value.x, value.z);
								case RED_BLUE   -> value.set(value.z, value.y, value.x);
								case GREEN_BLUE -> value.set(value.x, value.z, value.y);
								case RGB_BRG    -> value.set(value.z, value.x, value.y);
								case RGB_GBR    -> value.set(value.y, value.z, value.x);
							}
						}
					}
				}
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			} else if (!animFlag.getName().equals(MdlUtils.TOKEN_COLOR)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_FRESNEL_COLOR)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_ALPHA)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_VISIBILITY)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_FRESNEL_OPACITY)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_EMISSIVE)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_EMISSIVE_GAIN)){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recMatLayer.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recMatLayer, recMatLayer.find(animFlag.getName()), null));
				}
				actions.add(new AddAnimFlagAction<>(recMatLayer, newAnimFlag, null));
			}
		}
	}

	private void invertFloatAnimFlag(AnimFlag<?> animFlag, boolean invert, FloatAnimFlag newAnimFlag, List<Sequence> allSequences) {
		if(invert && animFlag instanceof FloatAnimFlag){
			for (Sequence sequence : allSequences){
//				if(!newAnimFlag.hasSequence(sequence) || newAnimFlag.getEntryMap(sequence).size() == 0 || newAnimFlag.getEntryAt(sequence, 0) == null){
				if(newAnimFlag.getEntryAt(sequence, 0) == null){
					if(newAnimFlag.tans()){
						newAnimFlag.addEntry(new Entry<>(0, 1.0f, 1.0f, 1.0f), sequence);
					} else {
						newAnimFlag.addEntry(new Entry<>(0, 1.0f), sequence);
					}
				}
			}
			for(TreeMap<Integer, Entry<Float>> entryMap : newAnimFlag.getAnimMap().values()){
				for(Entry<Float> entry : entryMap.values()){
					entry.setValue(Math.min(1f, Math.max(0f, 1f-entry.getValue())));

					if(newAnimFlag.tans()){
						entry.setInTan(Math.min(1f, Math.max(0f, 1f-entry.getInTan())));
						entry.setOutTan(Math.min(1f, Math.max(0f, 1f-entry.getOutTan())));
					}
				}
			}
		}
	}

	//RGB
	private enum FlipColor{
		DONT_FLIP   ("Don't flip"),
		RED_GREEN   ("red - green"),
		RED_BLUE    ("red - blue"),
		GREEN_BLUE  ("green - blue"),
		RGB_BRG     ("RGB - BRG"),
		RGB_GBR     ("RGB - GBR"),
		;
		String name;
		FlipColor(String name){
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
