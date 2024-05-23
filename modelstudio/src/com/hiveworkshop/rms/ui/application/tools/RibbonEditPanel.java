package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.ScaleSequencesLengthsAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetFromOtherRibbonAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.editor.model.util.MotionGenerator;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.colorchooser.ColorChooserIconLabel;
import com.hiveworkshop.rms.util.ImageUtils.ImageCreator;
import com.hiveworkshop.rms.util.*;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class RibbonEditPanel extends JPanel {
	private final static String SLIDER_CONSTRAINTS = "wrap, growx, spanx";
	private final static String SPINNER_CONSTRAINTS = "wrap, growx, spanx, align 50% 100%";
	private final ModelHandler tempModelHandler;
	private final PerspectiveViewport perspectiveViewport;
	private final RibbonEmitter ribbon;
	private final RibbonEmitter copy;
	private final List<IdObject> parents = new ArrayList<>();
	private final Map<String, Function<RibbonEmitter, Double>> nameToValueGetter = new HashMap<>();
	{
		nameToValueGetter.put(MdlUtils.TOKEN_WIDTH, RibbonEmitter::getHeightAbove);
		nameToValueGetter.put(MdlUtils.TOKEN_LENGTH, RibbonEmitter::getHeightBelow);
		nameToValueGetter.put(MdlUtils.TOKEN_LATITUDE, RibbonEmitter::getAlpha);
		nameToValueGetter.put(MdlUtils.TOKEN_GRAVITY, RibbonEmitter::getGravity);
		nameToValueGetter.put(MdlUtils.TOKEN_EMISSION_RATE, r -> (double)r.getEmissionRate());
		nameToValueGetter.put(MdlUtils.TOKEN_LIFE_SPAN, RibbonEmitter::getLifeSpan);
	}

	public RibbonEditPanel(RibbonEmitter ribbon) {
		super(new MigLayout("fill", "[grow][grow]", "[grow][][][]"));
		this.ribbon = ribbon;

		copy = getCopyForEditing(ribbon);
		tempModelHandler = getModelHandler(copy);
		perspectiveViewport = getViewport(tempModelHandler);

		TimeEnvironmentImpl renderEnv = tempModelHandler.getPreviewRenderModel().getTimeEnvironment();
		renderEnv.setRelativeAnimationTime(0);
		renderEnv.setLive(true);

		JPanel previewP = new JPanel(new MigLayout("fill, ins 0", "[grow]", "[grow][][]"));
		previewP.setBorder(BorderFactory.createTitledBorder("Preview"));

		JPanel viewportPanel = new JPanel(new MigLayout("fill", "[left, grow]", "[grow]"));
		viewportPanel.add(perspectiveViewport, "pad -5 -5 0 0");
		previewP.add(viewportPanel, "center, wrap");

		previewP.add(new JLabel("Movement:"), "split");
		previewP.add(getParentChooser(), "wrap");
		previewP.add(new JLabel("Speed:"), "split");
		previewP.add(getSeqSpeedSlider(renderEnv.getCurrentAnimation()), "wrap");
		add(previewP, "growx, growy");
		add(getMaterialPanel(), "wrap");

		JPanel subPanel = new JPanel(new MigLayout("ins 0", "[]20[]"));
		subPanel.add(getSliderPanel(), "");
		subPanel.add(new JPanel(new MigLayout("ins 0")), "growx, wrap");
		add(subPanel, "spanx, wrap");

		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> doApply());
		add(apply, "wrap");

		JButton addAsNew = new JButton("Add as new Emitter");
		addAsNew.addActionListener(e -> addAsNew());
		add(addAsNew, "wrap");
	}

	private JPanel getSliderPanel() {
		JPanel sliderPanel = new JPanel(new MigLayout("ins 0"));

		sliderPanel.add(new SmartNumberSlider("EmissionRate", copy.getEmissionRate(), 100, copy::setEmissionRate).addLabelTooltip("Particles emitted per second"), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Gravity", copy.getGravity(), -100, 100, copy::setGravity), SLIDER_CONSTRAINTS);

		sliderPanel.add(new SmartNumberSlider("Height Above", copy.getHeightAbove(), 400, copy::setHeightAbove).addLabelTooltip("Height above spawn point"), SLIDER_CONSTRAINTS + ", gaptop 5");
		sliderPanel.add(new SmartNumberSlider("Height Below", copy.getHeightBelow(), 400, copy::setHeightBelow).addLabelTooltip("Height below spawn point"), SLIDER_CONSTRAINTS);

		sliderPanel.add(new SmartNumberSlider("LifeSpan (~ 0.01 s)", copy.getLifeSpan() * 100, 200, (i) -> copy.setLifeSpan(i / 100d)).setMinLowerLimit(0).setMaxUpperLimit(1000), SLIDER_CONSTRAINTS);

		return sliderPanel;
	}


	private void addAsNew() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			RibbonEmitter emitter2 = this.copy.copy();
			emitter2.setParent(null);

			for (AnimFlag<?> animFlag : ribbon.getAnimFlags()) {
				if (animFlag != null) {
					emitter2.add(animFlag.deepCopy());
				}
			}

			List<String> affectedAnims = getAffectedAnims(emitter2);
			if (checkShouldScaleAnims(affectedAnims)) {
				for (String s : affectedAnims) {
					AnimFlag<?> animFlag = emitter2.find(s);
					if (animFlag instanceof FloatAnimFlag) {
						double maxValue = nameToValueGetter.get(s).apply(emitter2);
						scaleAnimAmplitude(animFlag, (float) maxValue);
					}
				}
			}
			UndoAction action = new AddNodeAction(modelPanel.getModel(), emitter2, ModelStructureChangeListener.changeListener);
			modelPanel.getModelHandler().getUndoManager().pushAction(action.redo());
		}
	}

	private void doApply() {

		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel().contains(ribbon)) {
			UndoAction action1 = new SetFromOtherRibbonAction(ribbon, copy);
			List<String> affectedAnims = getAffectedAnims(ribbon);
			if (checkShouldScaleAnims(affectedAnims)) {
				List<UndoAction> undoActions = new ArrayList<>();
				undoActions.add(action1);
				for (String s : affectedAnims) {
					UndoAction scaleAmplitudeAction = getScaleAmplitudeAction(ribbon, s, nameToValueGetter.get(s).apply(copy));
					if (scaleAmplitudeAction != null) {
						undoActions.add(scaleAmplitudeAction);
					}
				}
				modelPanel.getModelHandler().getUndoManager().pushAction(new CompoundAction(action1.actionName(), undoActions).redo());
			} else {
				modelPanel.getModelHandler().getUndoManager().pushAction(action1.redo());
			}
		}
	}

	private boolean checkShouldScaleAnims(List<String> affectedAnims) {
		if (!affectedAnims.isEmpty()) {
			StringBuilder sb = new StringBuilder("The following is animated:\n");
			for (String affectedAnim : affectedAnims) {
				sb.append("    ").append(affectedAnim).append("\n");

			}
			sb.append("Scale animated values to match edited values?");
			int opt = JOptionPane.showConfirmDialog(this, sb.toString(), "Values Animated", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
			return opt == JOptionPane.YES_OPTION;
		}
		return false;
	}


	private TwiComboBox<Material> getMaterialChooser(JLabel imageLabel) {
		TwiComboBox<Material> materialComboBox = new TwiComboBox<>(tempModelHandler.getModel().getMaterials(), new Material());
		materialComboBox.setRenderer(new MaterialListRenderer(tempModelHandler.getModel()));
		materialComboBox.selectOrFirst(copy.getMaterial());
		materialComboBox.addOnSelectItemListener(bitmap -> changeMaterial(bitmap, imageLabel));
		return materialComboBox;
	}

	private JPanel getMaterialPanel() {
		JPanel panel = new JPanel(new MigLayout("fill"));

		JLabel imageLabel = new JLabel(getImageIcon());
		TwiComboBox<Material> materialChooser = getMaterialChooser(imageLabel);
		panel.add(materialChooser, "spanx, growx, wrap");

		panel.add(imageLabel, "growx");

		panel.add(new JLabel("Rows"), "split");
		panel.add(new IntEditorJSpinner(copy.getRows(), 1, 1024, i -> updateImageParts(i, copy.getCols(), copy.getTextureSlot(), imageLabel)), ", wrap");
		panel.add(new JLabel("Columns"), "split");
		panel.add(new IntEditorJSpinner(copy.getColumns(), 1, 1024, i -> updateImageParts(copy.getRows(), i, copy.getTextureSlot(), imageLabel)), "wrap");
		panel.add(new JLabel("TextureSlot"), "split, spanx");
		panel.add(new IntEditorJSpinner(copy.getTextureSlot(), 0, 1024, i -> updateImageParts(copy.getRows(), copy.getCols(), i, imageLabel)), "wrap");

		panel.add(new JLabel("Color"), "split 2");
		panel.add(getColorPanel(), "");

		panel.add(new JLabel("Alpha"), "split 2");
		panel.add(new FloatEditorJSpinner((float) copy.getAlpha(), 0.0f, 1.0f, 0.1f, copy::setAlpha), "growx, ");

		return panel;
	}

	private void changeMaterial(Material material, JLabel imageLabel) {
		copy.setMaterial(material);
		tempModelHandler.getPreviewRenderModel().refreshFromEditor();
		tempModelHandler.getPreviewRenderModel().updateNodes(true);
		imageLabel.setIcon(getImageIcon());
	}

	private void updateImageParts(int rows, int cols, int textureSlot, JLabel imageLabel) {
		copy.setRows(rows);
		copy.setColumns(cols);
		copy.setTextureSlot(textureSlot);
		imageLabel.setIcon(getImageIcon());
	}

	private ImageIcon getImageIcon() {
		BufferedImage bufferedImage = ImageCreator.getBufferedImage(copy.getMaterial(), GameDataFileSystem.getDefault());
		int cols = Math.max(1, copy.getCols());
		int rows = Math.max(1, copy.getRows());
		int iconWidth = 128;
		int iconHeight = 128 * bufferedImage.getHeight()/bufferedImage.getWidth();

		Image scaledInstance = bufferedImage.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);

		int w = scaledInstance.getWidth(null);
		int h = scaledInstance.getHeight(null);
		BufferedImage iconImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[w*h];
		Arrays.fill(pixels, 0);
		iconImage.setRGB(0,0, w, h, pixels, 0, w);

		Graphics graphics = iconImage.getGraphics();
		graphics.drawImage(scaledInstance, 0,0,null);

		graphics.setColor(new Color(128,128,128,128));
		float colSpace = w/(float)cols;
		float rowSpace = h/(float)rows;
		for (float i = colSpace; i < w; i+= colSpace) {
			graphics.drawLine((int) i, 0, (int) i, h);
		}
		for (float i = rowSpace; i < h; i+= rowSpace) {
			graphics.drawLine(0, (int) i, w, (int) i);
		}
		int textureSlot = copy.getTextureSlot();
		int rowNum = textureSlot/cols;
		int colNum = textureSlot % cols;

		int mW = (int) (colSpace+2);
		int mH = (int) (rowSpace+2);
		int x1 = (int) (colNum * colSpace)-1;
		int x2 = (int) ((colNum+1) * colSpace)+1;
		int y1 = (int) (rowNum * rowSpace)-1;
		int y2 = (int) ((rowNum+1) * rowSpace)+1;
		graphics.setColor(new Color(128,128,128,190));
		graphics.drawLine(x1, y1, x1, y2);
		graphics.drawLine(x2, y1, x2, y2);
		graphics.drawLine(x1, y1, x2, y1);
		graphics.drawLine(x1, y2, x2, y2);

		graphics.dispose();

		return new ImageIcon(iconImage);
	}

	private TwiComboBox<IdObject> getParentChooser() {
		TwiComboBox<IdObject> parentChooser = new TwiComboBox<>(parents, new Helper("temporary Parent"));
		parentChooser.setStringFunctionRender(o -> (o instanceof IdObject p) ? p.getName() : "NULL");
		parentChooser.addMouseWheelListener(e -> parentChooser.incIndex(e.getWheelRotation()));
		parentChooser.setAllowLastToFirst(true);
		parentChooser.addOnSelectItemListener(copy::setParent);
		parentChooser.selectOrFirstWithListener(null);
		return parentChooser;
	}


	private BasicComboBoxRenderer getBoxRenderer() {
		return new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,
			                                              boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, value == null
						? "(Unanimated)" : value, index, isSelected, cellHasFocus);
			}
		};
	}

	private ModelHandler getModelHandler(RibbonEmitter copy) {
		EditableModel tempModel = new EditableModel();
//		List<Material> materials = new ArrayList<>();
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getCurrentModelPanel().getModel().getTextures().forEach(tempModel::add);
			ProgramGlobals.getCurrentModelPanel().getModel().getMaterials().forEach(tempModel::add);
		}

		tempModel.setExtents(new ExtLog(100));
		Animation animation = new Animation("stand", 100, 5100);
		animation.setNonLooping(false);
		tempModel.add(animation);

//		parents.add(getSwoosch(animation));
//		parents.add(getAround(animation));
//		parents.add(getRotate(animation));

		parents.add(getHelper("Roll", new Vec3(-20, 0, 0), h -> addRollAnimation(animation, h, new Vec3(0, 200, 0))));
		parents.add(getHelper("Circle", new Vec3(0, -20, -50), h -> addCircleAnimation(animation, h)));
		parents.add(getHelper("Disc", new Vec3(0, -50, 0), h -> addDiscAnimation(animation, h)));

		parents.forEach(tempModel::add);


		copy.setPivotPoint(new Vec3(0, 0, 0));
		if (copy.getVisibilityFlag() != null) {
			FloatAnimFlag flag = new FloatAnimFlag(MdlUtils.TOKEN_VISIBILITY);

			flag.addEntry(0, 1f, animation);
			flag.addEntry(animation.getLength(), 1f, animation);
			copy.setVisibilityFlag(flag);
		}
		System.out.println("copy name: " + copy.getName());
		tempModel.add(copy);

		ModelHandler modelHandler = new ModelHandler(tempModel);
		modelHandler.getPreviewRenderModel().getTimeEnvironment().setSequence(animation);
		modelHandler.getPreviewRenderModel().setVetoOverrideParticles(true);

		return modelHandler;
	}

	private Helper getHelper(String name, Vec3 pivot, Consumer<Helper> animAdder) {
		Helper helper = new Helper(name);
		helper.setPivotPoint(pivot);
		animAdder.accept(helper);
		return helper;
	}

	private Helper getSwoosch(Animation animation) {
//		Vec3 startPos = new Vec3(-20, 0, -30);
		Helper swoosch = new Helper("Roll");
		swoosch.setPivotPoint(new Vec3(-20, 0, 0));
		addRollAnimation(animation, swoosch, new Vec3(0, 200, 0));
		return swoosch;
	}

	private TwiNumberSlider getSeqSpeedSlider(Animation animation) {
		int orgLength = animation.getLength();
//		return new TwiNumberSlider(100, 1, 500, i -> updateAnimSpeed((int) (orgLength / (i * 0.01)), animation), false, true);
		TwiNumberSlider slider = new TwiNumberSlider(100, 1, 500, i -> updateAnimSpeed((int) (orgLength / (i * 0.01)), animation), false, false);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(100);
		slider.setMinorTickSpacing(25);
		return slider;
	}

	private ScaleSequencesLengthsAction sequencesLengthsAction;
	private void updateAnimSpeed(int newLength, Animation animation) {
		if (newLength != animation.getLength()) {
			if (sequencesLengthsAction != null) {
				sequencesLengthsAction.undo();
			}
			sequencesLengthsAction = new ScaleSequencesLengthsAction(tempModelHandler.getModel(), Collections.singletonMap(animation, newLength), null);
			sequencesLengthsAction.redo();
			TimeEnvironmentImpl renderEnv = tempModelHandler.getPreviewRenderModel().getTimeEnvironment();
			renderEnv.setSequence(animation);
		}
	}

	private Helper addRollAnimation(Animation animation, Helper helper, Vec3 moveX) {
		Vec3 startPos = new Vec3(helper.getPivotPoint()).negate();
		Quat rotInit = new Quat().rotX((float) Math.toRadians(90));
		float radius = Math.abs(startPos.dot(rotInit.getAxis()));
		float pathLength = moveX.length();
		double[] travleTimes = getTravleTimes(pathLength, radius);
		int rot90Time = (int) (animation.getLength()*travleTimes[1]/2);
		int quarterTime = animation.getLength() / 4;

		Vec3AnimFlag swooschMove = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
		List<Entry<Vec3>> move1 = MotionGenerator.getMoveAndHold(0, quarterTime + rot90Time, rot90Time * 2, moveX, startPos, true);
		move1.forEach(e -> swooschMove.addEntry(e, animation));
		List<Entry<Vec3>> move2 = MotionGenerator.getMoveAndHold(quarterTime + rot90Time, 2*quarterTime, rot90Time * 2, moveX.negate(), startPos, false);
		move2.forEach(e -> swooschMove.addEntry(e, animation));
		swooschMove.addEntry(animation.getLength(), new Vec3(startPos), animation);
		helper.add(swooschMove);

		QuatAnimFlag swooschRot2 = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
		swooschRot2 .addEntry(animation.getLength(), new Quat(rotInit), animation);
		List<Entry<Quat>> rotation = MotionGenerator.getRotation(quarterTime -rot90Time, rot90Time*2, 180, Vec3.Y_AXIS, rotInit);
		rotation.forEach(e -> swooschRot2.addEntry(e, animation));
		rotInit = rotation.get(rotation.size()-1).value;
		List<Entry<Quat>> rotation2 = MotionGenerator.getRotation(3*quarterTime-rot90Time, rot90Time*2, 180, Vec3.Y_AXIS, rotInit);
		rotation2.forEach(e -> swooschRot2.addEntry(e, animation));
		helper.add(swooschRot2);

		return helper;
	}

	private double[] getTravleTimes(double pathLength, double radius) {

		double endTravel = radius * Math.PI;
		double totTravle = pathLength*2 + endTravel*2;

		double sideFrac = pathLength/totTravle;
		double endFrac = endTravel/totTravle;
		return new double[] {sideFrac, endFrac};
	}

	private Helper getRotate(Animation animation) {
		Helper rotate = new Helper("Disc");
		rotate.setPivotPoint(new Vec3(0, -50, 0));
		addDiscAnimation(animation, rotate);
		return rotate;
	}

	private Helper addDiscAnimation(Animation animation, Helper helper) {
		Vec3 startPos = new Vec3(helper.getPivotPoint()).negate();
		Vec3AnimFlag rotateMove = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
		rotateMove.addEntry(0, new Vec3(startPos), animation);
		rotateMove.addEntry(animation.getLength(), new Vec3(startPos), animation);
		helper.add(rotateMove);

		QuatAnimFlag rotateRot2 = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
		List<Entry<Quat>> rotation = MotionGenerator.getRotation(0, animation.getLength(), 360, Vec3.X_AXIS, new Quat());
		rotation.forEach(e -> rotateRot2.addEntry(e, animation));
		helper.add(rotateRot2);

		return helper;
	}

	private Helper getAround(Animation animation) {
		Helper around = new Helper("Circle");
		around.setPivotPoint(new Vec3(0, -20, -50));
		addCircleAnimation(animation, around);
		return around;
	}

	private Helper addCircleAnimation(Animation animation, Helper helper) {
		Vec3 startPos = new Vec3(helper.getPivotPoint()).negate();
		Vec3AnimFlag aroundMove = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
		aroundMove.addEntry(0, new Vec3(startPos), animation);
		aroundMove.addEntry(animation.getLength(), new Vec3(startPos), animation);
		helper.add(aroundMove);

		Quat rotInit = new Quat().rotX((float) Math.toRadians(90));
		List<Entry<Quat>> rotation = MotionGenerator.getRotation(0, animation.getLength(), 360, Vec3.Y_AXIS, rotInit);
		QuatAnimFlag aroundRot2 = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
		rotation.forEach(e -> aroundRot2.addEntry(e, animation));
		helper.add(aroundRot2);

		return helper;
	}

	private UndoAction getScaleAmplitudeAction(RibbonEmitter emitter, String flagName, double newMaxValue) {
		AnimFlag<?> animFlag = emitter.find(flagName);
		if (animFlag instanceof FloatAnimFlag) {
			AnimFlag<?> newAnimFlag = animFlag.deepCopy();
			scaleAnimAmplitude(newAnimFlag, (float) newMaxValue);
			return new AddAnimFlagAction<>(emitter, newAnimFlag, null);
		}
		return null;
	}

	private void scaleAnimAmplitude(AnimFlag<?> animFlag, float newMaxValue) {
		if (animFlag instanceof FloatAnimFlag floatAnimFlag) {
			float staticValue = getStaticValue(floatAnimFlag, newMaxValue);
			if (staticValue != 0 && staticValue != newMaxValue) {
				float scaleFactor = newMaxValue/staticValue;
				for (TreeMap<Integer, Entry<Float>> seqMap : floatAnimFlag.getAnimMap().values()) {
					for (Entry<Float> entry : seqMap.values()) {
						entry.setValue(entry.getValue() * scaleFactor);
						if (entry.isTangential()) {
							entry.setInTan(entry.getInTan() * scaleFactor);
							entry.setOutTan(entry.getOutTan() * scaleFactor);
						}
					}
				}
			}
		}
	}

	private List<String> getAffectedAnims(RibbonEmitter emitter) {
		List<String> animatedStuff = new ArrayList<>();
		String[] flagNames = new String[] {
				MdlUtils.TOKEN_WIDTH,
				MdlUtils.TOKEN_LENGTH,
				MdlUtils.TOKEN_LATITUDE,
				MdlUtils.TOKEN_GRAVITY,
				MdlUtils.TOKEN_EMISSION_RATE,
				MdlUtils.TOKEN_LIFE_SPAN};
		for (String flagName : flagNames) {
//			System.out.println("Has \"" + flagName + "\": " + emitter.has("flagName"));
			if (emitter.has(flagName)) {
				animatedStuff.add(flagName);
			}
		}
		return animatedStuff;
	}

	private RibbonEmitter getCopyForEditing(RibbonEmitter emitter) {
		RibbonEmitter copy = emitter.copy();

		setStaticValue(copy, MdlUtils.TOKEN_WIDTH, copy::setHeightAbove);
		setStaticValue(copy, MdlUtils.TOKEN_LENGTH, copy::setHeightBelow);
		setStaticValue(copy, MdlUtils.TOKEN_LATITUDE, copy::setAlpha);
		setStaticValue(copy, MdlUtils.TOKEN_GRAVITY, copy::setGravity);
		setStaticValue(copy, MdlUtils.TOKEN_EMISSION_RATE, d -> copy.setEmissionRate(d.intValue()));
		setStaticValue(copy, MdlUtils.TOKEN_LIFE_SPAN, copy::setLifeSpan);

		copy.clearAnimFlags();
		return copy;
	}
	private void setStaticValue(RibbonEmitter emitter, String timelineTitle, Consumer<Float> valueConsumer) {
		AnimFlag<?> animFlag = emitter.find(timelineTitle);
		if (animFlag instanceof FloatAnimFlag floatAnimFlag) {
			Float maxFloat = getStaticValue(floatAnimFlag, null);
			if (maxFloat != null) {
				valueConsumer.accept(maxFloat);
			}
		}
	}

	private Float getStaticValue(FloatAnimFlag animFlag, Float value) {
		Float maxFloat = null;
		for (TreeMap<Integer, Entry<Float>> seqMap : animFlag.getAnimMap().values()) {
			for (Entry<Float> entry : seqMap.values()) {
				maxFloat = maxFloat == null ? entry.getValue() : Math.max(maxFloat, entry.getValue());
			}
		}
		if (maxFloat != null) {
			return maxFloat;
		}
		return value;
	}


	private PerspectiveViewport getViewport(ModelHandler modelHandler) {
		try {
			modelHandler.getPreviewRenderModel().updateNodes(true);
			PerspectiveViewport perspectiveViewport = new PerspectiveViewport().setModel(modelHandler.getModelView(), modelHandler.getPreviewRenderModel(), true);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			return perspectiveViewport;
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setTitle(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		perspectiveViewport.paint(perspectiveViewport.getGraphics());
	}

	public void reloadAllTextures() {
		perspectiveViewport.reloadAllTextures();
	}

	private JPanel getColorPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0"));
		ColorChooserIconLabel button0 = new ColorChooserIconLabel(copy.getStaticColor().asFloatColor(), c -> copy.getStaticColor().set(clampColorVector(c.getComponents(null))));

		panel.add(button0);

		return panel;
	}


	private float[] clampColorVector(float[] rowColor) {
		for (int i = 0; i < rowColor.length; i++) {
			rowColor[i] = Math.max(0, Math.min(1, rowColor[i]));
		}
		return rowColor;
	}
}
