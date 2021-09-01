package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.animation.animFlag.ReplaceAnimFlagsAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GeosetAnimCopyPanel extends JPanel {
	JComboBox<String> donAnimBox;

	GeosetAnim donGeosetAnim;
	GeosetAnim recGeosetAnim;

	ModelStructureChangeListener listener;
	UndoManager undoManager;

	/**
	 * Create the panel.
	 */
	public GeosetAnimCopyPanel(ModelView modelView, GeosetAnim geosetAnim, ModelStructureChangeListener listener, UndoManager undoManager) {
		recGeosetAnim = geosetAnim;
		this.listener = listener;
		this.undoManager = undoManager;
		setLayout(new MigLayout("fill", "[grow][grow]"));

		JTextArea info = new JTextArea("Copies all animation data from the chosen GeosetAnim to this GeosetAnim.");
		info.setEditable(false);
		info.setOpaque(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		add(info, "spanx, growx, wrap");
		List<GeosetAnim> geosetAnims = modelView.getModel().getGeosetAnims();
		animChoosingStuff(geosetAnims);

		JPanel donAnimPanel = getDonAnimPanel(geosetAnims);
		add(donAnimPanel, "growx, aligny top");

		JButton copyButton = new JButton("Copy Animation Data");
		copyButton.addActionListener(e -> doCopy(geosetAnim, geosetAnims));
		add(copyButton, "spanx, align center, wrap");
	}

	public static void show(JComponent parent, ModelView modelView, GeosetAnim geosetAnim, ModelStructureChangeListener listener, UndoManager undoManager) {
		final GeosetAnimCopyPanel animCopyPanel = new GeosetAnimCopyPanel(modelView, geosetAnim, listener, undoManager);
		FramePopup.show(animCopyPanel, parent, geosetAnim.getName());
	}

	private JPanel getDonAnimPanel(List<GeosetAnim> animations) {
		JPanel donAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
		donAnimPanel.add(new JLabel("From:"), "wrap");
		donAnimPanel.add(donAnimBox, "wrap, growx");
		return donAnimPanel;
	}

	private void animChoosingStuff(List<GeosetAnim> animations) {
		String[] animNames = animations.stream().map(GeosetAnim::getName).toArray(String[]::new);


		donAnimBox = new JComboBox<>(animNames);
		donAnimBox.addActionListener(e -> donAnimChoosen(animations));
		donGeosetAnim = animations.get(0);
		revalidate();
	}

	private void donAnimChoosen(List<GeosetAnim> animations) {
		donGeosetAnim = animations.get(donAnimBox.getSelectedIndex());
	}

	private void doCopy(GeosetAnim geosetAnim, List<GeosetAnim> geosetAnims) {
		ArrayList<AnimFlag<?>> animFlags = donGeosetAnim.getAnimFlags();
		ReplaceAnimFlagsAction replaceAnimFlagsAction = new ReplaceAnimFlagsAction(recGeosetAnim, animFlags, listener);
		undoManager.pushAction(replaceAnimFlagsAction.redo());
	}

	private void setKeyframes(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, ArrayList<AnimFlag<?>> animFlags) {
		for (AnimFlag<?> animFlag : animFlags) {
			setAnimFlagKeyframes(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlag);
		}
	}

	private <Q> void setAnimFlagKeyframes(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, AnimFlag<Q> animFlag) {
		for (int j = 0; j < times; j++) {
			animFlag.removeKeyframe(recKeyframe + j, recAnimation);
			Entry<Q> entryAt = animFlag.getEntryAt(donAnimation, donKeyframe + j);
			if (entryAt != null) {
				Entry<Q> entry = entryAt.deepCopy();
				animFlag.setOrAddEntry(recKeyframe + j, entry, recAnimation);
			}
		}
	}
}
