package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapReplaceableIdAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapWrapModeAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.ui.application.ExportInternal;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ExportTexture;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ComponentBitmapPanel extends ComponentPanel<Bitmap> {

	private Bitmap bitmap;
	private final ComponentEditorTextField texturePathField;
	private final IntEditorJSpinner replaceableIdSpinner;
	private final JLabel sizeLabel;
	private final JCheckBox wrapWidthBox;
	private final JCheckBox wrapHeightBox;
	private final JPanel previewPanel;
	private final ZoomableImagePreviewPanel imagePreviewPanel;
	private ImageUtils.ColorMode colorMode = ImageUtils.ColorMode.RGBA;

	public ComponentBitmapPanel(ModelHandler modelHandler) {
		super(modelHandler);
		texturePathField = new ComponentEditorTextField(24, this::texturePathField);

		replaceableIdSpinner = new IntEditorJSpinner(-1, -1, this::replaceableIdSpinner);

		sizeLabel = new JLabel();

		wrapWidthBox = new JCheckBox("Wrap Width");
		wrapWidthBox.addActionListener(e -> setWrap(Bitmap.WrapFlag.WIDTH, wrapWidthBox.isSelected()));

		wrapHeightBox = new JCheckBox("Wrap Height");
		wrapHeightBox.addActionListener(e -> setWrap(Bitmap.WrapFlag.HEIGHT, wrapHeightBox.isSelected()));

		imagePreviewPanel = new ZoomableImagePreviewPanel(null);
		previewPanel = new JPanel();
		previewPanel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		previewPanel.setLayout(new BorderLayout());
		previewPanel.add(imagePreviewPanel);

		JLabel pathLab = new JLabel("Path: ");
		JLabel replaceableIdLab = new JLabel("ReplaceableId: ");
		JButton exportButton = getButton("Export", e -> exportTextureImageFile());

		setLayout(new MigLayout("fillx", "[][grow][][]", "[][][][][grow]"));
//		add(pathLab,                "cell 0 0");
//		add(texturePathField,       "cell 1 0 3, growx");
//		add(replaceableIdLab,        "cell 0 1");
//		add(replaceableIdSpinner,   "cell 1 1");
//		add(sizeLabel,              "cell 3 1");
//		add(wrapWidthBox,           "cell 0 2");
//		add(wrapHeightBox,          "cell 0 3");
//		add(getColorModeBox(),      "cell 2 3, pushx");
//		add(exportButton,           "cell 3 3");
//
//
//		add(previewPanel, "cell 0 4 4, growx, growy");

		add(pathLab,                "");
		add(texturePathField,       "spanx, growx, wrap");
		add(replaceableIdLab,       "");
		add(replaceableIdSpinner,   "spanx 2");
		add(sizeLabel,              "wrap");
		add(wrapWidthBox,           "wrap");
		add(wrapHeightBox,          "");
		add(getColorModeBox(),      "spanx 2, right");
		add(exportButton,           "right, wrap");


		add(previewPanel, "spanx, growx, growy");
	}

	private void exportTextureImageFile() {
		ImageUtils.ColorMode colorMode1 = getColorMode();
		if(colorMode1 == ImageUtils.ColorMode.RGBA){
			ExportInternal.exportInternalFile(bitmap.getRenderableTexturePath(), "Texture", model.getWrappedDataSource(), this);
		} else if (colorMode1 != null){
			String[] nameParts = bitmap.getRenderableTexturePath().split("\\.(?=.+$)");
			String suggestedName = nameParts[0] + "_" + colorMode.name() + "." + nameParts[1];
			ExportTexture.onClickSaveAs(getImage(bitmap, model.getWrappedDataSource()), suggestedName, FileDialog.SAVE_TEXTURE, ProgramGlobals.getMainPanel());
		}
	}

	private ImageUtils.ColorMode getColorMode(){
		if(colorMode != ImageUtils.ColorMode.RGBA){
			String[] tempStrings = new String[]{"Original", colorMode.name(), "Cancel"};
			JOptionPane optionPane = new JOptionPane("Export Current Filtered Image?", JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, tempStrings, tempStrings[0]);
			JDialog export_option = optionPane.createDialog(this, "Export Option");
			export_option.setVisible(true);
			Object optionPaneValue = optionPane.getValue();

			if(tempStrings[1].equals(optionPaneValue)){
				return colorMode;
			} else if(tempStrings[2].equals(optionPaneValue)){
				return null;
			}
		}
		return ImageUtils.ColorMode.RGBA;
	}

	private void setWrap(Bitmap.WrapFlag flag, boolean set){
		if (bitmap.isFlagSet(flag) != set) {
			undoManager.pushAction(new SetBitmapWrapModeAction(bitmap, flag, set, changeListener).redo());
		}
	}

	private void replaceableIdSpinner(int newValue) {
		if(bitmap.getReplaceableId() != newValue){
			undoManager.pushAction(new SetBitmapReplaceableIdAction(bitmap, newValue, changeListener).redo());
		}
	}

	private void texturePathField(String newPath) {
		if(!bitmap.getPath().equals(newPath)){
			undoManager.pushAction(new SetBitmapPathAction(bitmap, newPath, changeListener).redo());
		}
	}

	@Override
	public ComponentPanel<Bitmap> setSelectedItem(Bitmap bitmap) {
		this.bitmap = bitmap;
		sizeLabel.setText("");
		texturePathField.reloadNewValue(bitmap.getPath());
		replaceableIdSpinner.reloadNewValue(bitmap.getReplaceableId());
		wrapWidthBox.setSelected(bitmap.isWrapWidth());
		wrapHeightBox.setSelected(bitmap.isWrapHeight());

		loadBitmapPreview(bitmap);
		return this;
	}

	private void loadBitmapPreview(Bitmap bitmap) {
		if (bitmap != null) {
			DataSource workingDirectory = model.getWrappedDataSource();
			imagePreviewPanel.setImage(getImage(bitmap, workingDirectory));
			previewPanel.repaint();
		}
	}

	private TwiComboBox<ImageUtils.ColorMode> getColorModeBox() {
		TwiComboBox<ImageUtils.ColorMode> colorModeGroup = new TwiComboBox<>(ImageUtils.ColorMode.values(), ImageUtils.ColorMode.GREEN_GREEN);
		colorModeGroup.addOnSelectItemListener(this::setColorMode);
		colorModeGroup.selectOrFirst(ImageUtils.ColorMode.RGBA);
		return colorModeGroup;
	}

	private void setColorMode(ImageUtils.ColorMode colorMode){
		this.colorMode = colorMode;
		loadBitmapPreview(bitmap);
	}

	private BufferedImage getImage(Bitmap bitmap, DataSource workingDirectory){
		BufferedImage texture = BLPHandler.getImage(bitmap, workingDirectory, colorMode);
		if(texture == null){
			return ImageUtils.getXImage(256, 220, Color.RED);
		}
		return texture;
	}
}
