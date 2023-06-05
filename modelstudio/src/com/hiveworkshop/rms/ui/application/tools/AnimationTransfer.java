package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.application.actionfunctions.FileActions;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ModelHolderThing;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.VisibilityShell;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.util.TwiPopup;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AnimationTransfer extends JPanel {
	private Animation animToTransfer;
	private Animation visAnim;

	private JFrame parentFrame;

	private boolean singleAnimation;
	private boolean clearExistingAnimations;

	private final ModelTracker recModelTracker;
	private final ModelTracker donModelTracker;

	public AnimationTransfer(final JFrame parentFrame) {
		this();
		this.parentFrame = parentFrame;
	}

	public AnimationTransfer() {
		setLayout(new MigLayout("gap 0"));
		recModelTracker = new ModelTracker(this);
		donModelTracker = new ModelTracker(this);

		add(getFileFieldsPanel(), "growx, spanx, wrap");

		add(getAnimTransferPanel(), "growx, spanx, wrap");

		add(getButtonPanel(), "spanx, align center, wrap");
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout());
		buttonPanel.add(getSpecialButton("Transfer", e -> doTransferSimple(), new Dimension(200, 35), KeyEvent.VK_T));
		buttonPanel.add(getSpecialButton("Close", e -> close(), new Dimension(80, 35), KeyEvent.VK_D), "wrap");

		JButton goAdvanced = getSpecialButton("Go Advanced", e -> goAdvanced(), null, KeyEvent.VK_G);
		goAdvanced.setToolTipText(
				"Opens the traditional MatrixEater Import window responsible for this Simple Import, " +
						"so that you can micro-manage particular settings before finishing the operation.");

		buttonPanel.add(goAdvanced, "spanx, align center");
		return buttonPanel;
	}
	private JPanel getFileFieldsPanel() {
		JPanel fileFieldsPanel = new JPanel(new MigLayout("gap 0, wrap 3", "[grow]8[align right]8[align right]"));

		fileFieldsPanel.add(new JLabel("Base file:"), "growx");
		fileFieldsPanel.add(recModelTracker.getTextField());
		fileFieldsPanel.add(recModelTracker.getBrowseButton());


		fileFieldsPanel.add(new JLabel("Animation file:"), "growx");
		fileFieldsPanel.add(donModelTracker.getTextField());
		fileFieldsPanel.add(donModelTracker.getBrowseButton());

		return fileFieldsPanel;
	}

	private JPanel getAnimTransferPanel() {
		JPanel animTransferPanel = new JPanel(new MigLayout("gap 0, wrap 2", "20[]8[grow,align right]"));

		JLabel animLabel = new JLabel("Animation to transfer:");
		animLabel.setEnabled(false);
		TwiComboBox<Animation> pickAnimBox = donModelTracker.getAnimBox(a -> animToTransfer = a);
		pickAnimBox.setEnabled(false);

		JLabel visLabel = new JLabel("Get visibility from:");
		visLabel.setEnabled(false);
		TwiComboBox<Animation> visFromBox = recModelTracker.getAnimBox(a -> visAnim = a);
		visFromBox.setEnabled(false);


		JCheckBox clearExistingAnimations = new JCheckBox("Clear Existing Animations", false);
		clearExistingAnimations.addActionListener(e -> this.clearExistingAnimations = clearExistingAnimations.isSelected());
		animTransferPanel.add(clearExistingAnimations, "spanx, wrap");

		JCheckBox transferSingleAnimation = new JCheckBox("Transfer single animation:", false);
		transferSingleAnimation.addActionListener(e -> {
			singleAnimation = transferSingleAnimation.isSelected();
			pickAnimBox.setEnabled(singleAnimation);
			animLabel.setEnabled(singleAnimation);
			visFromBox.setEnabled(singleAnimation);
			visLabel.setEnabled(singleAnimation);
		});
		animTransferPanel.add(transferSingleAnimation, "spanx, wrap");

		animTransferPanel.add(animLabel);
		animTransferPanel.add(pickAnimBox, "growx");

		animTransferPanel.add(visLabel);
		animTransferPanel.add(visFromBox, "growx");
		return animTransferPanel;
	}

	private JButton getSpecialButton(String text, ActionListener actionListener, Dimension minimumSize, int mnem) {
		JButton button = new JButton(text);
		button.setMnemonic(mnem);
		if(minimumSize != null){
			button.setMinimumSize(minimumSize);
		}
		button.addActionListener(actionListener);
		return button;
	}

	public static void main(final String[] args) {
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final UnsupportedLookAndFeelException
				| ClassNotFoundException
				| InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		AnimationTransfer.showWindow();

	}

	public static void showWindow() {
		JFrame parentFrame = new JFrame("Animation Transferer");
		parentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		AnimationTransfer contentPane = new AnimationTransfer(parentFrame);
		parentFrame.setContentPane(contentPane);
		parentFrame.setIconImage(RMSIcons.AnimIcon.getImage());
		parentFrame.pack();
		parentFrame.setLocationRelativeTo(null);
		parentFrame.setVisible(true);
	}

	private void doTransferSimple() {
		EditableModel receivingModel = recModelTracker.model;
		EditableModel donatingModel = donModelTracker.model;
		System.out.println("doTransfer");
		if (receivingModel != null && donatingModel != null) {
			receivingModel.setFilePath(getNewPath(receivingModel.getFile().getPath()));
			new Thread(() -> doImportSimple(receivingModel, donatingModel)).start();
		}
	}

	private void doImportSimple(EditableModel receivingModel, EditableModel donatingModel) {
		AnimTransferFunction importPanel = new AnimTransferFunction(receivingModel, donatingModel);

		List<Sequence> anims = new ArrayList<>();


		if(!clearExistingAnimations) {
			anims.addAll(receivingModel.getAllSequences());
		} else {
			anims.addAll(receivingModel.getGlobalSeqs());
		}

		System.out.println("visAnim: " + visAnim);
		if(singleAnimation) {
			anims.add(animToTransfer);
		} else {
			anims.addAll(donatingModel.getAnims());
			visAnim = null;
		}

		importPanel.animTransfer(anims, visAnim);
		onImportDone(receivingModel, donatingModel, receivingModel);
	}

	private void goAdvanced() {
		EditableModel receivingModel = recModelTracker.model;
		EditableModel donatingModel = donModelTracker.model;
		System.out.println("doTransfer");
		if (receivingModel != null && donatingModel != null) {
			receivingModel.setFilePath(getNewPath(receivingModel.getFile().getPath()));
			new Thread(() -> doImportWithUI(receivingModel, donatingModel)).start();
		}
	}

	private String getNewPath(String orgRecModelPath){
		String[] split = orgRecModelPath.split("(?=\\.\\w\\w\\w$)");
		if (singleAnimation && animToTransfer != null){
			return split[0] + "_" + animToTransfer.getName() + split[1];
		} else {
			return split[0] + "_IMP" + split[1];
		}
	}

	private void doImportWithUI(EditableModel receivingModel, EditableModel donatingModel) {
		ModelHolderThing mht = new ModelHolderThing(receivingModel, donatingModel);
		mht.recModAnims.forEach(g -> g.setDoImport(false));
		mht.donModGeoShells.forEach(g -> g.setDoImport(false));

//		mht.setImportStatusForAllDonBones(IdObjectShell.ImportType.MOTION_FROM);
		mht.donModBoneShells.forEach(shell -> shell.setShouldImport(false));

		mht.donModObjectShells.forEach(shell -> shell.setShouldImport(false));

		mht.visibilityList();
		mht.selectSimilarVisSources();
		matchMotionBones(mht.donModBoneShells, mht.recModBoneShells);

		// Try assuming it's a unit with a corpse; they'll tend to be that way
		// Iterate through new visibility sources, find a geoset with gutz material
		for (VisibilityShell<?> donVis : mht.donModVisibilityShells) {
			if (isGutz(donVis)) {
				for (VisibilityShell<?> impVis : mht.futureVisComponents) {
					if (isGutz(impVis)) {
						impVis.setRecModAnimsVisSource(donVis);
					}
				}
				break;
			}
		}

		new ImportPanelGui(mht, model -> onImportDone(receivingModel, donatingModel, model));
	}

	public void matchMotionBones(List<IdObjectShell<?>> othersBoneShells, List<IdObjectShell<?>> selfBoneShells) {
		Map<String, IdObjectShell<?>> nameMap = new HashMap<>();

		for (IdObjectShell<?> boneShell : othersBoneShells) {
			nameMap.put(boneShell.getName(), boneShell);
		}

		for (IdObjectShell<?> boneShell : selfBoneShells) {
			if (nameMap.containsKey(boneShell.getName()) && (boneShell.getMotionSrcShell() == null)) {
				boneShell.setMotionSrcShell(nameMap.get(boneShell.getName()));
			}
		}
		repaint();
	}
	private boolean isGutz(VisibilityShell<?> donVis) {
		boolean isGeoset = donVis.getSource() instanceof Geoset;
		if(isGeoset){
			boolean hasGeoAnim = ((Geoset) donVis.getSource()).hasAnim();
			if(hasGeoAnim){
				Bitmap bitmap = ((Geoset) donVis.getSource()).getMaterial().firstLayer().firstTexture();
				return bitmap.getPath().equalsIgnoreCase("textures\\gutz.blp");
			}
		}
		return false;
	}


	private void onImportDone(EditableModel receivingModel, EditableModel donatingModel, EditableModel model) {
		String[] open_save = new String[]{"Open Model", "Save Model", "Cancel Import"};
//		int option = JOptionPane.showOptionDialog(this,  "Imported from [" + donatingModel.getName() + "] into [" + receivingModel.getName() + "]", "Import Done", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, open_save, open_save[0]);
		int option = JOptionPane.showOptionDialog(this,  "Imported from [" + donatingModel.getName() + "] into [" + receivingModel.getName() + "]", "Import Done", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, open_save, open_save[0]);
		System.out.println("import done! opt: " + option);
		if(option == 0){
			ModelLoader.loadModel(model);
			parentFrame.setVisible(false);
			parentFrame.dispose();
		} else if (option == 2){
			recModelTracker.reloadModel();
		} else {
			saveModel(receivingModel, donatingModel, model);
		}
	}

	private void saveModel(EditableModel receivingModel, EditableModel donatingModel, EditableModel model) {
		if(FileActions.onClickSaveAs(null, FileDialog.SAVE_MODEL, model)) {
			TwiPopup.quickDismissPopup(this, "Animation transfer done!", "Animation Transfer Done");
			recModelTracker.reloadModel();
		} else {
			onImportDone(receivingModel, donatingModel, model);
		}
	}

	private void close() {
		parentFrame.setVisible(false);
		parentFrame.dispose();
	}

	private static class ModelTracker {
		Component parent;
		EditableModel model;
		String path;
		private final List<Animation> anims = new ArrayList<>();
		TwiTextField modelField;
		Color orgBGColor;
		Color errorBGColor = new Color(200, 180, 180);
		TwiComboBox<Animation> pickAnimBox;

//		private final RMSFileChooser fc = new RMSFileChooser();

		ModelTracker(Component parent){
			this.parent = parent;
			modelField = new TwiTextField(24, fp -> setModel(getModel(path, fp)));
			orgBGColor = modelField.getBackground();
			pickAnimBox = new TwiComboBox<>(anims, new Animation("PrototypePrototypePrototype", 0, 1));
		}

		TwiTextField getTextField(){
			return modelField;
		}

		public JButton getBrowseButton() {
			return Button.forceSize(Button.create("...", e -> openAction()), 28, 18);
		}

		public TwiComboBox<Animation> getAnimBox(Consumer<Animation> animConsumer){
			if(animConsumer != null){
				pickAnimBox.addOnSelectItemListener(animConsumer);
			}
			return pickAnimBox;
		}

		public void reloadModel(){
			setModel(getModel("", path));
		}

		private void openAction() {
			File file = FileActions.onClickOpenGetFile(FileDialog.OPEN_WC_MODEL, parent);
			if(file != null){
				modelField.setTextAndRun(file.getPath());
			}
		}

		private void setModel(EditableModel model){
			if(model != this.model){
				Animation selected = pickAnimBox.getSelected();
				anims.clear();
				this.model = model;
				if (model != null) {
					anims.addAll(model.getAnims());
					path = model.getFile().getPath();
					modelField.setBackground(orgBGColor);
				} else {
					modelField.setBackground(errorBGColor);
				}
				pickAnimBox.selectOrFirstWithListener(selected);
			}
		}

		private EditableModel getModel(String oldPath, String newPath) {
			if (0 < newPath.length() && (!newPath.equals(oldPath))) {
				try {
					File file = new File(newPath);
					if(file.exists()){
						EditableModel model = MdxUtils.loadEditable(file);
						model.setFileRef(file);
						return model;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return null;
		}
	}


	private static class AnimTransfer extends ActionFunction {
		AnimTransfer(){
			super(TextKey.IMPORT_ANIM, AnimationTransfer::showWindow);
			setMenuItemMnemonic(KeyEvent.VK_I);
		}
	}

	public static JMenuItem getMenuItem(){
		return new AnimTransfer().getMenuItem();
	}
}
