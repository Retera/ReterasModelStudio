package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.animation.animFlag.ReplaceAnimFlagsAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

public class GeosetAnimCopyPanel extends JPanel {
	private GeosetAnim donGeosetAnim;
	private boolean invertVis = false;
//	private GeosetAnim recGeosetAnim;

	private final UndoManager undoManager;

	/**
	 * Create the panel.
	 */
	public GeosetAnimCopyPanel(EditableModel model, GeosetAnim recGeosetAnim, UndoManager undoManager) {
//		this.recGeosetAnim = recGeosetAnim;
		this.undoManager = undoManager;
		setLayout(new MigLayout("fill", "[grow][grow]"));

		JTextArea info = new JTextArea("Copies all animation data from the chosen GeosetAnim to this GeosetAnim.");
		info.setEditable(false);
		info.setOpaque(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);

		List<GeosetAnim> geosetAnims = model.getGeosetAnims();


		JCheckBox invert_alpha = new JCheckBox("invert visibility");
		invert_alpha.addActionListener(e -> invertVis = invert_alpha.isSelected());

		JButton copyButton = new JButton("Copy Animation Data");
		copyButton.addActionListener(e -> doCopy(recGeosetAnim, donGeosetAnim));

		add(info, "spanx, growx, wrap");
		add(getDonGeoAnimPanel(geosetAnims), "growx, aligny top");
		add(invert_alpha);
		add(copyButton, "spanx, align center, wrap");
	}

	public static void show(JComponent parent, EditableModel model, GeosetAnim geosetAnim, UndoManager undoManager) {
		GeosetAnimCopyPanel animCopyPanel = new GeosetAnimCopyPanel(model, geosetAnim, undoManager);
		FramePopup.show(animCopyPanel, parent, geosetAnim.getName());
	}

	private JPanel getDonGeoAnimPanel(List<GeosetAnim> geosetAnims) {
		JPanel donGeoAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
		donGeoAnimPanel.add(new JLabel("From:"), "wrap");

		String[] geoAnimNames = geosetAnims.stream().map(GeosetAnim::getName).toArray(String[]::new);
		donGeoAnimPanel.add(getCombobox(geoAnimNames, i -> donGeosetAnim = geosetAnims.get(i)), "wrap, growx");
		donGeosetAnim = geosetAnims.get(0);
		return donGeoAnimPanel;
	}


	private JComboBox<String> getCombobox(String[] strings, Consumer<Integer> indexConsumer){
		JComboBox<String> comboBox = new JComboBox<>(strings);
		comboBox.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED){
				indexConsumer.accept(comboBox.getSelectedIndex());
			}
		});
		comboBox.setSelectedIndex(0);
		return comboBox;
	}

	private void doCopy(GeosetAnim recGeosetAnim, GeosetAnim donGeosetAnim) {
		ArrayList<AnimFlag<?>> newAnimFlags = new ArrayList<>();

		for (AnimFlag<?> animFlag : donGeosetAnim.getAnimFlags()){
			AnimFlag<?> newAnimFlag = animFlag.deepCopy();
			if(invertVis && (animFlag.getName().equals(MdlUtils.TOKEN_ALPHA) || animFlag.getName().equals(MdlUtils.TOKEN_VISIBILITY)) && animFlag instanceof FloatAnimFlag){
				FloatAnimFlag floatAnimFlag = (FloatAnimFlag) newAnimFlag;
				for(TreeMap<Integer, Entry<Float>> entryMap : floatAnimFlag.getAnimMap().values()){
					for(Entry<Float> entry : entryMap.values()){
						entry.setValue(Math.min(1f, Math.max(0f, 1f-entry.getValue())));
					}
				}
			}
			newAnimFlags.add(newAnimFlag);
		}

		undoManager.pushAction(new ReplaceAnimFlagsAction(recGeosetAnim, newAnimFlags, ModelStructureChangeListener.changeListener).redo());
	}


//	private <Q> void setAnimFlagKeyframes(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, AnimFlag<Q> animFlag) {
//		for (int j = 0; j < times; j++) {
//			animFlag.removeKeyframe(recKeyframe + j, recAnimation);
//			Entry<Q> entryAt = animFlag.getEntryAt(donAnimation, donKeyframe + j);
//			if (entryAt != null) {
//				Entry<Q> entry = entryAt.deepCopy();
//				animFlag.setOrAddEntry(recKeyframe + j, entry, recAnimation);
//			}
//		}
//	}
}
