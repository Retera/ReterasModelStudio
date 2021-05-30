package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.ReplaceAnimFlagsAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GeosetAnimCopyPanel extends JPanel {
	private final FileDialog fileDialog;
	JComboBox<String> donAnimBox;
	JSpinner donTimeSpinner;
	JSpinner donTimeEndSpinner;
	JComboBox<String> recAnimBox;
	JSpinner recTimeSpinner;

	JLabel recTimeLabel = new JLabel("0");
	JLabel donTimeLabel = new JLabel("0");

	GeosetAnim donGeosetAnim;
	GeosetAnim recGeosetAnim;

	ModelStructureChangeListener listener;
	UndoActionListener undoActionListener;

	/**
	 * Create the panel.
	 */
	public GeosetAnimCopyPanel(ModelView modelView, GeosetAnim geosetAnim, ModelStructureChangeListener listener, UndoActionListener undoActionListener) {
		fileDialog = new FileDialog(this);
		recGeosetAnim = geosetAnim;
		this.listener = listener;
		this.undoActionListener = undoActionListener;
		setLayout(new MigLayout("fill", "[grow][grow]"));

//		add(new JLabel("Copies all keyframes from source animation within specified interval to destination. \nWARNING: Make sure that the copied interval fits within the destination animation."), "spanx, wrap");
//		JTextArea info = new JTextArea("Copies all keyframes from chosen interval in source animation into destination animation starting at specified frame.");
		JTextArea info = new JTextArea("Copies all animation data from the chosen GeosetAnim to this GeosetAnim.");
		info.setEditable(false);
		info.setOpaque(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		add(info, "spanx, growx, wrap");
//		JTextArea warning = new JTextArea("WARNING: Make sure that the copied interval fits within the destination animation.");
//		warning.setEditable(false);
//		warning.setOpaque(false);
//		warning.setLineWrap(true);
//		warning.setWrapStyleWord(true);
//		add(warning, "spanx, growx, wrap");
		List<GeosetAnim> geosetAnims = modelView.getModel().getGeosetAnims();
		animChoosingStuff(geosetAnims);

		JPanel donAnimPanel = getDonAnimPanel(geosetAnims);
		add(donAnimPanel, "growx, aligny top");

//		donAnimPanel.add(new JButton("\u23E9"));

//		JPanel recAnimPanel = getRecAnimPanel(geosetAnims);
//		add(recAnimPanel, "growx, wrap, aligny top");

		JButton copyButton = new JButton("Copy Animation Data");
		copyButton.addActionListener(e -> doCopy(geosetAnim, geosetAnims));
		add(copyButton, "spanx, align center, wrap");
	}

	public static void show(Component parent, ModelView modelView, GeosetAnim geosetAnim, ModelStructureChangeListener listener, UndoActionListener undoActionListener) {
		final GeosetAnimCopyPanel textureManager = new GeosetAnimCopyPanel(modelView, geosetAnim, listener, undoActionListener);
		final JFrame frame = new JFrame(geosetAnim.getName());
//			textureManager.setSize(new Dimension(600, 450));
		frame.setContentPane(textureManager);
		frame.pack();
//			frame.setSize(textureManager.getSize());
		frame.setLocationRelativeTo(parent);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	private static void fetchAndAddSingleAnimation(MainPanel mainPanel, String path) {
		final String filepath = ImportFileActions.convertPathToMDX(path);
		final EditableModel current = mainPanel.currentMDL();
		if (filepath != null) {
			final EditableModel animationSource;
//			try {
//				animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
//				addSingleAnimation(mainPanel, current, animationSource);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
	}

//	private JPanel getRecAnimPanel(List<GeosetAnim> animations) {
//		JPanel recAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
//		recAnimPanel.add(new JLabel("To:"), "wrap");
//		recAnimPanel.add(recAnimBox, "wrap, growx");
////		recAnimPanel.add(recTimeLabel, "wrap");
////		recAnimPanel.add(recTimeSpinner, "wrap");
////		JPanel startP = new JPanel(new MigLayout("fill, gap 0, ins 0", "[5%:10%:10%]10[grow][][]"));
////		startP.add(new JLabel("Start"));
////		startP.add(recTimeSpinner, "growx");
////		JButton rwButton1 = new JButton("\u23EE");
////		rwButton1.addActionListener(e -> recTimeSpinner.setValue(recGeosetAnim.getStart()));
////		startP.add(rwButton1);
////		JButton ffButton1 = new JButton("\u23ED");
////		ffButton1.addActionListener(e -> recTimeSpinner.setValue(recGeosetAnim.getEnd()));
////		startP.add(ffButton1);
////		recAnimPanel.add(startP, "growx, wrap");
//		return recAnimPanel;
//	}

	private JPanel getDonAnimPanel(List<GeosetAnim> animations) {
		JPanel donAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
		donAnimPanel.add(new JLabel("From:"), "wrap");
		donAnimPanel.add(donAnimBox, "wrap, growx");
//		donAnimPanel.add(donTimeLabel, "wrap");
//		JPanel startP = new JPanel(new MigLayout("fill, gap 0, ins 0", "[5%:10%:10%]10[grow][][]"));
//		startP.add(new JLabel("Start"));
//		startP.add(donTimeSpinner, "growx");
//		JButton dRwButton1 = new JButton("\u23EE");
//		dRwButton1.addActionListener(e -> donTimeSpinner.setValue(donGeosetAnim.getStart()));
//		startP.add(dRwButton1);
//		JButton dFfButton1 = new JButton("\u23ED");
//		dFfButton1.addActionListener(e -> donTimeSpinner.setValue(donGeosetAnim.getEnd()));
//		startP.add(dFfButton1);
//		donAnimPanel.add(startP, "growx, wrap");

//		JPanel endP = new JPanel(new MigLayout("fill, gap 0, ins 0", "[5%:10%:10%]10[grow][][]"));
//		endP.add(new JLabel("End"));
//		endP.add(donTimeEndSpinner, "growx");
//		JButton dRwButton2 = new JButton("\u23EE");
//		dRwButton2.addActionListener(e -> donTimeEndSpinner.setValue(donGeosetAnim.getStart()));
//		endP.add(dRwButton2);
//		JButton dFfButton2 = new JButton("\u23ED");
//		dFfButton2.addActionListener(e -> donTimeEndSpinner.setValue(donGeosetAnim.getEnd()));
//		endP.add(dFfButton2);
//		donAnimPanel.add(endP, "growx, wrap");
		return donAnimPanel;
	}

	private void animChoosingStuff(List<GeosetAnim> animations) {
		String[] animNames = animations.stream().map(GeosetAnim::getName).toArray(String[]::new);


		donAnimBox = new JComboBox<>(animNames);
		donAnimBox.addActionListener(e -> donAnimChoosen(animations));
		donGeosetAnim = animations.get(0);
//		donTimeLabel.setText(donAnim.getStart() + "  to  " + donAnim.getEnd() + "  (" + (donAnim.getEnd() - donAnim.getStart()) + ")");
//		donTimeSpinner = new JSpinner(getAnimModel(donAnim));
//		donTimeEndSpinner = new JSpinner(getAnimModel(donAnim));
//
//		recAnimBox = new JComboBox<>(animNames);
//		recAnimBox.addActionListener(e -> recAnimChosen(animations));
//		recGeosetAnim = animations.get(0);
//		recTimeLabel.setText(recGeosetAnim.getStart() + "  to  " + recGeosetAnim.getEnd() + "  (" + (recGeosetAnim.getEnd() - recGeosetAnim.getStart()) + ")");
//		recTimeSpinner = new JSpinner(getAnimModel(recGeosetAnim));
		revalidate();
	}

	private void recAnimChosen(List<GeosetAnim> animations) {
		recGeosetAnim = animations.get(recAnimBox.getSelectedIndex());
//		recTimeSpinner.setModel(getAnimModel(recGeosetAnim));
//		recTimeLabel.setText(recGeosetAnim.getStart() + "  to  " + recGeosetAnim.getEnd() + "  (" + (recGeosetAnim.getEnd() - recGeosetAnim.getStart()) + ")");
	}

	private void donAnimChoosen(List<GeosetAnim> animations) {
		donGeosetAnim = animations.get(donAnimBox.getSelectedIndex());
//		donTimeSpinner.setModel(getAnimModel(donGeosetAnim));
//		donTimeEndSpinner.setModel(getAnimModel(donGeosetAnim));
//		donTimeLabel.setText(donGeosetAnim.getStart() + "  to  " + donGeosetAnim.getEnd() + "  (" + (donGeosetAnim.getEnd() - donGeosetAnim.getStart()) + ")");
	}

//	private SpinnerNumberModel getAnimModel(GeosetAnim animation) {
////		int animStartValue = animation.getStart();
////		int animEndValue = animation.getEnd();
////		return new SpinnerNumberModel(animStartValue, animStartValue, animEndValue, 1);
//	}

	private void doCopy(GeosetAnim geosetAnim, List<GeosetAnim> geosetAnims) {
		ArrayList<AnimFlag<?>> animFlags = donGeosetAnim.getAnimFlags();
		ReplaceAnimFlagsAction replaceAnimFlagsAction = new ReplaceAnimFlagsAction(recGeosetAnim, animFlags, listener);
		replaceAnimFlagsAction.redo();
		undoActionListener.pushAction(replaceAnimFlagsAction);
//		Integer donStart = (Integer) donTimeSpinner.getValue();
//		Animation recAnimation = geosetAnims.get(recAnimBox.getSelectedIndex());
//		Integer recStart = (Integer) recTimeSpinner.getValue();
//		int times = (Integer) donTimeEndSpinner.getValue() - donStart + 1;
//		copyKeyframe(geosetAnim, donGeosetAnim, donStart, recAnimation, recStart, times);
	}

	private void copyKeyframe(EditableModel model, Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times) {
		List<Bone> bones = model.getBones();
		List<Helper> helpers = model.getHelpers();

		for (Bone bone : bones) {
			ArrayList<AnimFlag<?>> animFlags = bone.getAnimFlags();
			setKeyframes(donKeyframe, recKeyframe, times, animFlags);
		}
		for (Helper helper : helpers) {
			ArrayList<AnimFlag<?>> animFlags = helper.getAnimFlags();
			setKeyframes(donKeyframe, recKeyframe, times, animFlags);
		}
	}

	private void setKeyframes(int donKeyframe, int recKeyframe, int times, ArrayList<AnimFlag<?>> animFlags) {
		for (int i = 0; i < animFlags.size(); i++) {
			for (int j = 0; j < times; j++) {
				animFlags.get(i).removeKeyframe(recKeyframe + j);
				AnimFlag.Entry<?> entryAt = animFlags.get(i).getEntryAt(donKeyframe + j);
				if (entryAt != null) {
					AnimFlag.Entry<?> entry = new AnimFlag.Entry<>(entryAt);
					animFlags.get(i).setOrAddEntryT(recKeyframe + j, entry);
				}
			}
		}
	}

	private void openModel(MainPanel mainPanel) {
		FileDialog fileDialog = new FileDialog(mainPanel);

		final EditableModel model = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
	}
}
