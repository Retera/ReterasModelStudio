package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.model.bitmap.AddBitmapAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.application.tools.uielement.IdObjectChooserButton;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.colorchooser.ColorChooserButton;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class AddParticlePanel extends JPanel{

	static String[][] sdParticleFilePairs = {
			{"DustEmitter.mdx", "DustEmitter.png"},
			{"FireSmallBurn.mdx", "FireSmallBurn.png"},
			{"InfernalMagicFire.mdx", "InfernalMagicFire.png"},
			{"LargeBrownSmokeEmitter.mdx", "LargeBrownSmokeEmitter.png"},
			{"LargeFireEmitter.mdx", "LargeFireEmitter.png"},
			{"PixieDustEmitter.mdx", "PixieDustEmitter.png"},
			{"ShockEnergyEmitter.mdx", "ShockEnergyEmitter.png"},
			{"TeamcolorWispShimmer.mdx", "TeamcolorWispShimmer.png"},
			{"WeaponMagicEmitter.mdx", "WeaponMagicEmitter.png"},
			{"WeaponMagicFlatEmitter.mdx", "WeaponMagicFlatEmitter.png"}
	};

	static String[] particleStockFiles = {
			"DustEmitter.mdx",
			"DustEmitter.png",
			"FireSmallBurn.mdx",
			"FireSmallBurn.png",
			"InfernalMagicFire.mdx",
			"InfernalMagicFire.png",
			"LargeBrownSmokeEmitter.mdx",
			"LargeBrownSmokeEmitter.png",
			"LargeFireEmitter.mdx",
			"LargeFireEmitter.png",
			"PixieDustEmitter.mdx",
			"PixieDustEmitter.png",
			"ShockEnergyEmitter.mdx",
			"ShockEnergyEmitter.png",
			"TeamcolorWispShimmer.mdx",
			"TeamcolorWispShimmer.png",
			"WeaponMagicEmitter.mdx",
			"WeaponMagicEmitter.png",
			"WeaponMagicFlatEmitter.mdx",
			"WeaponMagicFlatEmitter.png",
			"MagicFireBurn.png",
			"FireSmallOrange.png",
	};
	final ParticleEmitter2 particle;
	public AddParticlePanel(ParticleInformation particleInformation, EditableModel model){
		super(new MigLayout());
		particle = getParticleEmitter2(particleInformation);
		if (particle == null) return;
		particle.setParent(null);
		String name = particleInformation.getName();

		add(new JLabel(new ImageIcon(particleInformation.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH))));

		JPanel optionsPanel = new JPanel(new MigLayout("ins 0, fill"));

		final JLabel titleLabel = new JLabel("Add " + name);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		optionsPanel.add(titleLabel, "growx, spanx, wrap");

		optionsPanel.add(new JLabel("Particle Name:"), "spanx, split 2");
		optionsPanel.add(new TwiTextField( "My" + name + "Particle", 24, particle::setName), "growx, wrap");


		optionsPanel.add(new JLabel("Parent:"), "spanx, split 2");

		Vec3SpinnerArray pivotSpinners = new Vec3SpinnerArray("X:", "Y:", "Z:").setLabelWrap(false).setLabelConstrains("gapx 10");
		pivotSpinners.setVec3Consumer(particle::setPivotPoint);

		IdObjectChooserButton idObjectChooserButton = new IdObjectChooserButton(model, true, this);
		idObjectChooserButton.setIdObjectConsumer(idObject -> {
			particle.setParent(idObject);
			if(idObject != null){
				particle.setPivotPoint(idObject.getPivotPoint());
				pivotSpinners.setValues(particle.getPivotPoint());
			}
		});
		optionsPanel.add(idObjectChooserButton, "growx, wrap");
		optionsPanel.add(pivotSpinners.spinnerPanel(), "spanx, wrap");

		JPanel animVisPanel = animVisPanel2(model.getAnims(), particle);
		final JButton chooseAnimations = new JButton("Choose when to show!");
		chooseAnimations.addActionListener(e -> JOptionPane.showMessageDialog(this, animVisPanel));
		optionsPanel.add(chooseAnimations, "spanx, align center, wrap");

		optionsPanel.add(colorPanel(particle), "spanx, align center, wrap");

		add(optionsPanel);
	}

	public static JMenu getParticleMenu(){
		JMenu addParticleMenu = createMenu("Particle", KeyEvent.VK_P);
		List<ParticleInformation> particleInformationList = fetchIncludedParticles();
		for (ParticleInformation particleInformation : particleInformationList) {
			addParticleMenu.add(getAddParticleButton(particleInformation));
		}

		addParticleMenu.add(createMenuItem("Empty Popcorn", KeyEvent.VK_O, e -> AddParticlePanel.addEmptyPopcorn()));
		return addParticleMenu;
	}

	private static JMenuItem getAddParticleButton(ParticleInformation particleInformation) {
		ImageIcon icon = new ImageIcon(particleInformation.getImage().getScaledInstance(28, 28, Image.SCALE_DEFAULT));
		JMenuItem particleItem = new JMenuItem(particleInformation.getName(), icon);
		particleItem.addActionListener(e -> addParticleEmitter2(particleInformation, ProgramGlobals.getCurrentModelPanel().getModelHandler()));
		return particleItem;
	}

	private ParticleEmitter2 getParticleEmitter2(ParticleInformation particleInformation) {
		try {
			return MdxUtils.loadEditable(particleInformation.filePath, null).getParticleEmitter2s().get(0);
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static JPanel animVisPanel2(List<Animation> animations, ParticleEmitter2 particle) {
		final JPanel animPanel = new JPanel(new MigLayout(""));

		FloatAnimFlag flag = (FloatAnimFlag) particle.getVisibilityFlag();
		if (flag == null) {
			flag = new FloatAnimFlag(MdlUtils.TOKEN_VISIBILITY);
			particle.add(flag);
		}

		FloatAnimFlag visFlag = (FloatAnimFlag) particle.getVisibilityFlag();
		for (Animation animation : animations) {
			JCheckBox checkBox = new JCheckBox(animation.getName());
			checkBox.setSelected(true);
			visFlag.addEntry(0, 1f, animation);
			checkBox.addActionListener(e -> visFlag.addEntry(0, checkBox.isSelected() ? 1f: 0f, animation));
			animPanel.add(checkBox, "wrap");
		}

		return animPanel;
	}
	private JPanel colorPanel(ParticleEmitter2 particle){
		JPanel colorPanel = new JPanel(new MigLayout("ins 0, fill", "[][][]", "[]"));
		Vec3[] segmentColors = particle.getSegmentColors();
		for(int i = 0; i < 3; i++){
			int finalI = i;
			Vec3 color = segmentColors[i];
			colorPanel.add(new ColorChooserButton("Color " + (i + 1), new Color(color.x, color.y, color.z), c -> particle.setSegmentColor(finalI, c.getColorComponents(null))));
		}
		return colorPanel;
	}

	private static List<ParticleInformation> fetchIncludedParticles() {
		List<ParticleInformation> particleInformations = new ArrayList<>();
		for (String[] particleFiles : sdParticleFilePairs) {
			final String name = particleFiles[0].split("\\.")[0];
			Image image2 = loadImage("stock\\particles\\" + particleFiles[1]);
			String mdxPath = "stock\\particles\\" + particleFiles[0];
			particleInformations.add(new ParticleInformation(mdxPath, name, image2));
		}
		return particleInformations;
	}

	public static void addEmptyPopcorn() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel() != null) {
			System.out.println("added popcorn!");
			ParticleEmitterPopcorn new_popcornEmitter = new ParticleEmitterPopcorn("New PopcornEmitter");
			new_popcornEmitter.setPivotPoint(new Vec3(0, 0, 0));
			AddNodeAction action = new AddNodeAction(modelPanel.getModel(), new_popcornEmitter, ModelStructureChangeListener.changeListener);
			modelPanel.getModelHandler().getUndoManager().pushAction(action.redo());
		}
	}

	public static Image loadImage(final String path) {
		try {
			return ImageIO.read(GameDataFileSystem.getDefault().getResourceAsStream(path));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void addParticleEmitter2(ParticleInformation particleInformation, ModelHandler modelHandler) {
		if(modelHandler != null){
			EditableModel model = modelHandler.getModel();
			AddParticlePanel particlePanel = new AddParticlePanel(particleInformation, model);
			final int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), particlePanel, "Add " + particleInformation.getName(), JOptionPane.OK_CANCEL_OPTION);
			if (x == JOptionPane.OK_OPTION) {
				ParticleEmitter2 particle1 = particlePanel.particle;
				Bitmap texture = particle1.getTexture();
				System.out.println("particle texture: " + texture);
				if(texture != null && !model.contains(texture)){
					System.out.println("particle texture: " + texture.getPath());
					modelHandler.getUndoManager().pushAction(new AddBitmapAction(texture, model, ModelStructureChangeListener.changeListener).redo());
				}
				modelHandler.getUndoManager().pushAction(new AddNodeAction(model, particle1, ModelStructureChangeListener.changeListener).redo());

			}
		}
	}

	private static class ParticleInformation {
		private String filePath;
		private final String uggName;
		private final Image image;

		private ParticleInformation(String filePath, String uggName, Image image) {
			this.filePath = filePath;
			this.uggName = uggName;
			this.image = image;
		}

		public String getName() {
			return uggName;
		}

		public String getFilePath() {
			return filePath;
		}

		public Image getImage() {
			return image;
		}
	}
}
