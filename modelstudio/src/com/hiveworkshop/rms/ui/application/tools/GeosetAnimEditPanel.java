package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
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

public class GeosetAnimEditPanel extends JPanel {
	private Geoset donGeoset;
	private boolean invertVis = false;
	private boolean copyVis = true;
	private boolean copyColor = true;
	private FlipColor flipColor = FlipColor.DONT_FLIP;

	private final UndoManager undoManager;

	public GeosetAnimEditPanel(EditableModel model, Geoset recGeoset, UndoManager undoManager) {
		this.undoManager = undoManager;
		setLayout(new MigLayout("fill", "[grow][grow]"));

		JTextArea info = new JTextArea("Copies all animation data from the chosen GeosetAnim to this GeosetAnim.");
		info.setEditable(false);
		info.setOpaque(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);

		JCheckBox copy_alpha = new JCheckBox("copy visibility", true);
		copy_alpha.addActionListener(e -> copyVis = copy_alpha.isSelected());
		JCheckBox invert_alpha = new JCheckBox("invert visibility");
		invert_alpha.addActionListener(e -> invertVis = invert_alpha.isSelected());

		JCheckBox copy_color = new JCheckBox("copy color", true);
		copy_color.addActionListener(e -> copyColor = copy_color.isSelected());
		TwiComboBox<FlipColor> flipColorBox = new TwiComboBox<>(FlipColor.values());
		flipColorBox.addOnSelectItemListener(fc -> flipColor = fc);

		JButton copyButton = new JButton("Copy Animation Data");
		copyButton.addActionListener(e -> doCopy(recGeoset, donGeoset, model.getAllSequences()));

		add(info, "spanx, growx, wrap");
		add(getDonGeoAnimPanel(model.getGeosets()), "spanx, growx, aligny top, wrap");
		add(copy_alpha);
		add(invert_alpha, "wrap");
		add(copy_color, "");
		add(flipColorBox, "wrap");
		add(copyButton, "spanx, align center, wrap");
	}

	public static void show(JComponent parent, EditableModel model, Geoset geoset, UndoManager undoManager) {
		GeosetAnimEditPanel animCopyPanel = new GeosetAnimEditPanel(model, geoset, undoManager);
		FramePopup.show(animCopyPanel, parent, geoset.getName() + "'s Anim");
	}

	private JPanel getDonGeoAnimPanel(List<Geoset> geosets) {
		JPanel donGeoAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
		donGeoAnimPanel.add(new JLabel("From:"), "wrap");

		Geoset geoset = new Geoset();
		geoset.setParentModel(new EditableModel());
		geoset.setLevelOfDetailName("Dummy geoset for prototype purposes cuz Swing");
		TwiComboBox<Geoset> comboBox = new TwiComboBox<>(geosets, geoset);
		comboBox.addOnSelectItemListener(geo -> donGeoset = geo);
		comboBox.setStringFunctionRender((geo) -> ((Geoset) geo).getName() + "'s Anim");

		donGeoset = geosets.get(0);
		donGeoAnimPanel.add(comboBox, "wrap, growx");
		return donGeoAnimPanel;
	}

	private void doCopy(Geoset recGeoset, Geoset donGeoset, List<Sequence> allSequences) {
		ArrayList<UndoAction> actions = new ArrayList<>();

		for (AnimFlag<?> animFlag : donGeoset.getAnimFlags()){
			if (copyVis && (animFlag.getName().equals(MdlUtils.TOKEN_ALPHA) || animFlag.getName().equals(MdlUtils.TOKEN_VISIBILITY))){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recGeoset.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recGeoset, recGeoset.find(animFlag.getName()), null));
				}
				if(invertVis && animFlag instanceof FloatAnimFlag){
					FloatAnimFlag floatAnimFlag = (FloatAnimFlag) newAnimFlag;
					for (Sequence sequence : allSequences){
						if(floatAnimFlag.getEntryAt(sequence, 0) == null){
							if(floatAnimFlag.tans()){
								floatAnimFlag.addEntry(new Entry<>(0, 1.0f, 1.0f, 1.0f), sequence);
							} else {
								floatAnimFlag.addEntry(new Entry<>(0, 1.0f), sequence);
							}
						}
					}
					for(TreeMap<Integer, Entry<Float>> entryMap : floatAnimFlag.getAnimMap().values()){
						for(Entry<Float> entry : entryMap.values()){
							entry.setValue(Math.min(1f, Math.max(0f, 1f-entry.getValue())));
						}
					}
				}
				actions.add(new AddAnimFlagAction<>(recGeoset, newAnimFlag, null));
			} else if (copyColor && animFlag.getName().equals(MdlUtils.TOKEN_COLOR)){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recGeoset.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recGeoset, recGeoset.find(animFlag.getName()), null));
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
				actions.add(new AddAnimFlagAction<>(recGeoset, newAnimFlag, null));
			} else if (!animFlag.getName().equals(MdlUtils.TOKEN_COLOR)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_ALPHA)
					&& !animFlag.getName().equals(MdlUtils.TOKEN_VISIBILITY)){
				AnimFlag<?> newAnimFlag = animFlag.deepCopy();
				if(recGeoset.has(animFlag.getName())){
					actions.add(new RemoveAnimFlagAction(recGeoset, recGeoset.find(animFlag.getName()), null));
				}
				actions.add(new AddAnimFlagAction<>(recGeoset, newAnimFlag, null));
			}
		}
		if(!actions.isEmpty()){
			undoManager.pushAction(new CompoundAction("Copy Geoset anim data", actions, ModelStructureChangeListener.changeListener::geosetsUpdated).redo());
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
		final String name;
		FlipColor(String name){
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
