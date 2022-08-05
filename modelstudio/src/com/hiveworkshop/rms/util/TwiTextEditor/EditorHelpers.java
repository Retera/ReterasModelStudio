package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TableComboBoxEditor;
import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelTextureThings;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.util.colorchooser.ColorChooserButton;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.TwiTextEditor.table.TwiTableColorEditor;
import com.hiveworkshop.rms.util.TwiTextEditor.table.TwiTableColorRenderer;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.function.Consumer;

public class EditorHelpers {

	public static class AlphaEditor {
		FloatEditorJSpinner alphaSpinner;
		FlagPanel<Float> flagPanel;
		Consumer<Float> staticConsumer;
		String flagToken;

		public AlphaEditor(ModelHandler modelHandler, Consumer<Float> staticConsumer){
			alphaSpinner = new FloatEditorJSpinner(1.0f, (float) Integer.MIN_VALUE, 0.01f, staticConsumer);
			flagPanel = new FlagPanel<>(MdlUtils.TOKEN_ALPHA, EditorHelpers::parseFloat, 1.0f, modelHandler).setStaticComponent(alphaSpinner);
		}
		public AlphaEditor(ModelHandler modelHandler, String flagToken, Consumer<Float> staticConsumer){
			this.flagToken = flagToken;
			this.staticConsumer = staticConsumer;
			alphaSpinner = new FloatEditorJSpinner(1.0f, (float) Integer.MIN_VALUE, 0.01f, staticConsumer);
			flagPanel = new FlagPanel<>(MdlUtils.TOKEN_ALPHA, EditorHelpers::parseFloat, 1.0f, modelHandler).setStaticComponent(alphaSpinner);
		}

		public AlphaEditor update(TimelineContainer node, FloatAnimFlag alphaFlag, float staticAlpha){
			flagPanel.update(node, alphaFlag, 0f);
			alphaSpinner.reloadNewValue(staticAlpha);
			return this;
		}
		public AlphaEditor update(TimelineContainer node, float staticAlpha){
			flagPanel.update(node, (FloatAnimFlag) node.find(MdlUtils.TOKEN_ALPHA), 0f);
			alphaSpinner.reloadNewValue(staticAlpha);
			return this;
		}

		public FlagPanel<Float> getFlagPanel() {
			return flagPanel;
		}
	}
	public static class FloatEditor {
		FloatEditorJSpinner floatSpinner;
		FlagPanel<Float> flagPanel;
		Consumer<Float> staticConsumer;
		String flagToken;

		public FloatEditor(ModelHandler modelHandler, String flagToken, Consumer<Float> staticConsumer){
			this.flagToken = flagToken;
			this.staticConsumer = staticConsumer;
			floatSpinner = new FloatEditorJSpinner(1.0f, (float) Integer.MIN_VALUE, 0.01f, staticConsumer);
			flagPanel = new FlagPanel<>(flagToken, EditorHelpers::parseFloat, 1.0f, modelHandler).setStaticComponent(floatSpinner);
		}

		public FloatEditor update(TimelineContainer node, FloatAnimFlag alphaFlag, float staticAlpha){
			flagPanel.update(node, alphaFlag, 0f);
			floatSpinner.reloadNewValue(staticAlpha);
			return this;
		}
		public FloatEditor update(TimelineContainer node, float staticAlpha){
			flagPanel.update(node, (FloatAnimFlag) node.find(flagToken), 0f);
			floatSpinner.reloadNewValue(staticAlpha);
			return this;
		}

		public FlagPanel<Float> getFlagPanel() {
			return flagPanel;
		}
	}
	public static class IntegerEditor {
		IntEditorJSpinner intSpinner;
		FlagPanel<Integer> flagPanel;
		Consumer<Integer> staticConsumer;
		String flagToken;

		public IntegerEditor(ModelHandler modelHandler, String flagToken, Consumer<Integer> staticConsumer){
			this.flagToken = flagToken;
			this.staticConsumer = staticConsumer;
			intSpinner = new IntEditorJSpinner(0, Integer.MIN_VALUE, staticConsumer);
			flagPanel = new FlagPanel<>(flagToken, EditorHelpers::parseInt, 0, modelHandler).setStaticComponent(intSpinner);
		}

		public IntegerEditor update(TimelineContainer node, IntAnimFlag alphaFlag, int staticValue){
			flagPanel.update(node, alphaFlag, staticValue);
			intSpinner.reloadNewValue(staticValue);
			return this;
		}
		public IntegerEditor update(TimelineContainer node, int staticValue){
			flagPanel.update(node, (IntAnimFlag) node.find(flagToken), staticValue);
			intSpinner.reloadNewValue(staticValue);
			return this;
		}

		public FlagPanel<Integer> getFlagPanel() {
			return flagPanel;
		}
	}


	public static class TranslationEditor {
		FlagPanel<Vec3> flagPanel;
		Consumer<Vec3> staticConsumer;
		String flagToken;

		public TranslationEditor(ModelHandler modelHandler){
			this(modelHandler, MdlUtils.TOKEN_TRANSLATION, null);
		}
		public TranslationEditor(ModelHandler modelHandler, String flagToken, Consumer<Vec3> staticConsumer){
			this.flagToken = flagToken;
			this.staticConsumer = staticConsumer;
			flagPanel = new FlagPanel<>(flagToken, EditorHelpers::parseVec3, new Vec3(0,0,0), modelHandler);
		}

		public TranslationEditor update(TimelineContainer node, Vec3AnimFlag alphaFlag){
			flagPanel.update(node, alphaFlag, new Vec3(0,0,0));
			return this;
		}
		public TranslationEditor update(TimelineContainer node){
			flagPanel.update(node, (Vec3AnimFlag) node.find(flagToken), new Vec3(0,0,0));
			return this;
		}

		public FlagPanel<Vec3> getFlagPanel() {
			return flagPanel;
		}
	}
	public static class ScalingEditor {
		FlagPanel<Vec3> flagPanel;
		Consumer<Vec3> staticConsumer;
		String flagToken;

		public ScalingEditor(ModelHandler modelHandler){
			this(modelHandler, MdlUtils.TOKEN_SCALING, null);
		}
		public ScalingEditor(ModelHandler modelHandler, String flagToken, Consumer<Vec3> staticConsumer){
			this.flagToken = flagToken;
			this.staticConsumer = staticConsumer;
			flagPanel = new FlagPanel<>(flagToken, EditorHelpers::parseVec3, new Vec3(1,1,1), modelHandler);
		}

		public ScalingEditor update(TimelineContainer node, Vec3AnimFlag alphaFlag){
			flagPanel.update(node, alphaFlag, new Vec3(1,1,1));
			return this;
		}
		public ScalingEditor update(TimelineContainer node){
			flagPanel.update(node, (Vec3AnimFlag) node.find(flagToken), new Vec3(1,1,1));
			return this;
		}

		public FlagPanel<Vec3> getFlagPanel() {
			return flagPanel;
		}
	}
	public static class RotationEditor {
		FlagPanel<Quat> flagPanel;
		Consumer<Quat> staticConsumer;
		String flagToken;

		public RotationEditor(ModelHandler modelHandler){
			this(modelHandler, MdlUtils.TOKEN_ROTATION, null);
		}
		public RotationEditor(ModelHandler modelHandler, String flagToken, Consumer<Quat> staticConsumer){
			this.flagToken = flagToken;
			this.staticConsumer = staticConsumer;
			flagPanel = new FlagPanel<>(MdlUtils.TOKEN_ROTATION, EditorHelpers::parseQuat, new Quat(0,0,0,1), modelHandler);
		}

		public RotationEditor update(TimelineContainer node, QuatAnimFlag alphaFlag){
			flagPanel.update(node, alphaFlag, new Quat(0,0,0,1));
			return this;
		}
		public RotationEditor update(TimelineContainer node){
			flagPanel.update(node, (QuatAnimFlag) node.find(MdlUtils.TOKEN_ROTATION), new Quat(0,0,0,1));
			return this;
		}

		public FlagPanel<Quat> getFlagPanel() {
			return flagPanel;
		}
	}

	public static class ColorEditor {
		FlagPanel<Vec3> flagPanel;
		private Vec3 DEFAULT_COLOR = new Vec3(1, 1, 1);
		ColorChooserButton button;
		private Vec3 color;
		private Vec3 selectedColor;
		Consumer<Vec3> staticConsumer;
		String flagToken;

		public ColorEditor(ModelHandler modelHandler){
			this(modelHandler, MdlUtils.TOKEN_COLOR, null);
		}
		public ColorEditor(ModelHandler modelHandler, Consumer<Vec3> consumer){
			this(modelHandler, MdlUtils.TOKEN_COLOR, consumer);
		}
		public ColorEditor(ModelHandler modelHandler, String flagToken, Consumer<Vec3> staticConsumer){
			this.flagToken = flagToken;
			this.staticConsumer = staticConsumer;
			button = new ColorChooserButton(Color.WHITE, this::colorSelected);
			flagPanel = new FlagPanel<>(MdlUtils.TOKEN_COLOR, EditorHelpers::parseVec3, new Vec3(1,1,1), modelHandler);
			flagPanel.setTableRenderer(new TwiTableColorRenderer(new GlobalSeq(1)));
			flagPanel.setTableEditor(new TwiTableColorEditor<>(EditorHelpers::parseVec3, DEFAULT_COLOR));
			flagPanel.setStaticComponent(button);

			color = new Vec3(DEFAULT_COLOR);
			selectedColor = new Vec3(DEFAULT_COLOR);
		}

		private void colorSelected(Color color){
			selectedColor.set(color.getColorComponents(null));
			if(staticConsumer != null) {
				staticConsumer.accept(selectedColor);
			}
		}

		public ColorEditor update(TimelineContainer node, Vec3AnimFlag alphaFlag){
			flagPanel.update(node, alphaFlag, new Vec3(1,1,1));
			return this;
		}
		public ColorEditor update(TimelineContainer node){
			flagPanel.update(node, (Vec3AnimFlag) node.find(flagToken), new Vec3(1,1,1));
			return this;
		}
		public ColorEditor update(TimelineContainer node, Vec3 color){
			flagPanel.update(node, (Vec3AnimFlag) node.find(flagToken), color);
			button.setCurrentColor(color.asIntColor());
			return this;
		}

		public FlagPanel<Vec3> getFlagPanel() {
			return flagPanel;
		}
	}

	public static class TextureEditor {
		FlagPanel<Bitmap> flagPanel;
		private TwiComboBox<Bitmap> textureChooser;
		private TwiComboBox<Bitmap> staticTextureChooser;
		private Bitmap selectedColor;
		Consumer<Bitmap> staticConsumer;
		String flagToken;

		public TextureEditor(ModelHandler modelHandler){
			this(modelHandler, MdlUtils.TOKEN_TEXTURE_ID, null);
		}
		public TextureEditor(ModelHandler modelHandler, Consumer<Bitmap> consumer){
			this(modelHandler, MdlUtils.TOKEN_TEXTURE_ID, consumer);
		}
		public TextureEditor(ModelHandler modelHandler, String flagToken, Consumer<Bitmap> staticConsumer){
			this.flagToken = flagToken;
			this.staticConsumer = staticConsumer;

			staticTextureChooser = new TwiComboBox<>(modelHandler.getModel().getTextures(), new Bitmap("", 1));
			staticTextureChooser.setRenderer(new TextureListRenderer(modelHandler.getModel()).setImageSize(64));
			staticTextureChooser.addOnSelectItemListener(staticConsumer);


			textureChooser = new TwiComboBox<>(modelHandler.getModel().getTextures(), new Bitmap("", 1));
			textureChooser.setRenderer(ModelTextureThings.getTextureListRenderer());

			flagPanel = new FlagPanel<>(flagToken, "Texture", null, modelHandler.getModel().getTexture(0), modelHandler);
			flagPanel.setTableRenderer(ModelTextureThings.getTextureTableCellRenderer());
			flagPanel.setTableEditor(new TableComboBoxEditor<>(textureChooser));
			flagPanel.setStaticComponent(staticTextureChooser);
		}

		public TextureEditor update(TimelineContainer node, BitmapAnimFlag alphaFlag){
			flagPanel.update(node, alphaFlag, new Bitmap(""));
			return this;
		}
		public TextureEditor update(TimelineContainer node){
			flagPanel.update(node, (BitmapAnimFlag) node.find(flagToken), new Bitmap(""));
			return this;
		}
		public TextureEditor update(TimelineContainer node, Bitmap bitmap){
			flagPanel.update(node, (BitmapAnimFlag) node.find(flagToken), bitmap);
			staticTextureChooser.setSelectedItem(bitmap);
			return this;
		}

		public FlagPanel<Bitmap> getFlagPanel() {
			return flagPanel;
		}
	}


	private static Float parseFloat(String s) {
		s = s.replaceAll("[^-\\.e\\d]", "");

		if (s.matches("(-?\\d+\\.+)")) {
//			System.out.println("5 \"(-?\\d+\\.+)\" - " + s);
			s = s.replace(".", "");
		}
		if (s.matches("(-?\\d+\\.+\\d+)")) {
//			System.out.println("5 \"(-?\\d+\\.+\\d+)\" - " + s);
			s = s.replaceAll("(\\.+)", ".");
		}
		if (s.matches(".*\\d.*") && s.matches("(-?\\d*\\.?\\d+(e\\d+)?)")) {
			return Float.parseFloat(s);
		}
		return 0.0f;
	}


	private static Integer parseInt(String s) {
		return Integer.parseInt(s.replaceAll("[\\D]", ""));
	}

	private static Vec3 parseVec3(String s){
		return Vec3.parseVec3(ValueParserUtil.getString(3,s));
	}
	private static Quat parseQuat(String s){
		return Quat.parseQuat(ValueParserUtil.getString(4,s));
	}
}
