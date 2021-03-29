package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

public class AddParticlePanel {

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

    static void addParticleButtons(MainPanel mainPanel, JMenu addParticle) {
        List<ParticleInformation> particleInformationList = fetchIncludedParticles();
        for (ParticleInformation particleInformation : particleInformationList) {
            makeAndAddParticleButtons(mainPanel, addParticle, particleInformation);
        }

    }

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

    static void addEmptyPopcorn(MainPanel mainPanel) {
        final EditableModel current = mainPanel.currentMDL();
        if (current != null) {
            System.out.println("added popcorn!");
            ParticleEmitterPopcorn new_popcornEmitter = new ParticleEmitterPopcorn("New PopcornEmitter");
            new_popcornEmitter.setPivotPoint(new Vec3(0, 0, 0));
            current.add(new_popcornEmitter);
            mainPanel.modelStructureChangeListener.nodesAdded(Collections.singletonList(new_popcornEmitter));
        }
    }

    private static void makeAndAddParticleButtons(MainPanel mainPanel, JMenu addParticle, ParticleInformation particleInformation) {
        final JMenuItem particleItem = new JMenuItem(particleInformation.getName(), new ImageIcon(particleInformation.getImage().getScaledInstance(28, 28, Image.SCALE_DEFAULT)));
        particleItem.addActionListener(e -> makeAddParticlePanel(mainPanel, particleInformation));
        addParticle.add(particleItem);
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

    public static Image loadImage(final String path) {
        try {
//            System.out.println(path);
            return ImageIO.read(GameDataFileSystem.getDefault().getResourceAsStream(path));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void makeAddParticlePanel(MainPanel mainPanel, ParticleInformation particleInformation) {
        final ParticleEmitter2 particle;
        try {
            InputStream is = GameDataFileSystem.getDefault().getResourceAsStream(particleInformation.filePath);
            particle = MdxUtils.loadEditable(is).getParticleEmitter2s().get(0);
        } catch (final IOException e1) {
            e1.printStackTrace();
            return;
        }
        if (mainPanel.currentMDL() != null) {
            final JPanel particlePanel = new JPanel(new MigLayout());

            final JLabel imageLabel = new JLabel(new ImageIcon(particleInformation.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
            particlePanel.add(imageLabel);

            JPanel optionsPanel = new JPanel(new MigLayout("ins 0, fill"));

            final JLabel titleLabel = new JLabel("Add " + particleInformation.getName());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
            optionsPanel.add(titleLabel, "spanx, wrap");

            optionsPanel.add(new JLabel("Particle Name:"), "spanx, split 2");
            JTextField nameField = new JTextField("My" + particleInformation.getName() + "Particle");
            optionsPanel.add(nameField, "growx, wrap");


            optionsPanel.add(new JLabel("Parent:"), "spanx, split 2");

            java.util.List<IdObject> idObjects = new ArrayList<>(mainPanel.currentMDL().getIdObjects());
            Bone nullBone = new Bone("No parent");
            idObjects.add(0, nullBone);

            JComboBox<IdObject> parentBone = new JComboBox<>(idObjects.toArray(new IdObject[0]));
            parentBone.setRenderer(createParticleParentComboBox(nullBone));
            optionsPanel.add(parentBone, "growx, wrap");

            Vec3SpinnerArray spinnerArray = new Vec3SpinnerArray("X:", "Y:", "Z:").setLabelWrap(false).setLabelConstrains("gapx 10");
            optionsPanel.add(spinnerArray.spinnerPanel(), "spanx, wrap");

            parentBone.addActionListener(e14 -> spinnerArray.setValues(((IdObject) parentBone.getSelectedItem()).getPivotPoint()));

            Map<Animation, Boolean> animVisStatus = new HashMap<>();
            mainPanel.currentMDL().getAnims().forEach(a -> animVisStatus.put(a, true));

            final JPanel animPanel = animVisPanel(animVisStatus);

            final JButton chooseAnimations = new JButton("Choose when to show!");
            chooseAnimations.addActionListener(e13 -> JOptionPane.showMessageDialog(particlePanel, animPanel));
            optionsPanel.add(chooseAnimations, "spanx, align center, wrap");

            final JButton[] colorButtons = new JButton[3];
            final Color[] colors = new Color[colorButtons.length];
            makeColorButtons(particlePanel, particle, colorButtons, colors);
            JPanel colorPanel = new JPanel(new MigLayout("ins 0, fill", "[][][]", "[]"));
            for (JButton button : colorButtons) {
                colorPanel.add(button);
            }
            optionsPanel.add(colorPanel, "spanx, wrap");

            particlePanel.add(optionsPanel);
            final int x = JOptionPane.showConfirmDialog(mainPanel, particlePanel, "Add " + particleInformation.getName(), JOptionPane.OK_CANCEL_OPTION);
            if (x == JOptionPane.OK_OPTION) {
                IdObject parent = (IdObject) parentBone.getSelectedItem();
                if (parent == nullBone) {
                    parent = null;
                }
                addParticleEmitter2(mainPanel, particle, parent, nameField.getText(), spinnerArray.getValue(), animVisStatus, colors);
            }
        }
    }

    private static void addParticleEmitter2(MainPanel mainPanel, ParticleEmitter2 particle, IdObject parent, String name, Vec3 pivot, Map<Animation, Boolean> animVisMap, Color[] colors) {
        particle.setPivotPoint(pivot);
        for (int i = 0; i < colors.length; i++) {
            particle.setSegmentColor(i, new Vec3(
                    colors[i].getRed() / 255.00,
                    colors[i].getGreen() / 255.00,
                    colors[i].getBlue() / 255.00));
        }

        particle.setParent(parent);

        FloatAnimFlag oldFlag = (FloatAnimFlag) particle.getVisibilityFlag();
        if (oldFlag == null) {
            oldFlag = new FloatAnimFlag("Visibility");
        }

        final FloatAnimFlag visFlag = (FloatAnimFlag) AnimFlag.buildEmptyFrom(oldFlag);

        for (Animation anim : animVisMap.keySet()) {
            if (!animVisMap.get(anim)) {
                visFlag.addEntry(anim.getStart(), 0f);
            }
        }
        particle.setVisibilityFlag(visFlag);
        particle.setName(name);
        mainPanel.currentMDL().add(particle);
        mainPanel.modelStructureChangeListener.nodesAdded(Collections.singletonList(particle));
    }


    private static JPanel animVisPanel(Map<Animation, Boolean> animVisStatus) {
        final JPanel animPanel = new JPanel(new MigLayout(""));

        for (Animation animation : animVisStatus.keySet()) {
            JCheckBox checkBox = new JCheckBox(animation.getName());
            checkBox.setSelected(animVisStatus.get(animation));
            checkBox.addActionListener(e -> animVisStatus.put(animation, checkBox.isSelected()));
            animPanel.add(checkBox, "wrap");
        }

        return animPanel;
    }


    private static void makeColorButtons(JPanel parentPanel, ParticleEmitter2 particle, JButton[] colorButtons, Color[] colors) {
        for (int i = 0; i < colorButtons.length; i++) {
            final Vec3 colorValues = particle.getSegmentColor(i);
            final Color color = new Color((int) (colorValues.x * 255), (int) (colorValues.y * 255), (int) (colorValues.z * 255));

            final JButton button = new JButton("Color " + (i + 1), new ImageIcon(IconUtils.createBlank(color, 32, 32)));
            colors[i] = color;
            final int index = i;
            button.addActionListener(e12 -> {
                final Color colorChoice = JColorChooser.showDialog(parentPanel,"Chooser Color", colors[index]);
                if (colorChoice != null) {
                    colors[index] = colorChoice;
                    button.setIcon(new ImageIcon(IconUtils.createBlank(colors[index], 32, 32)));
                }
            });
            colorButtons[i] = button;
        }
    }

    private static BasicComboBoxRenderer createParticleParentComboBox(Bone nullBone) {
        return new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                final IdObject idObject = (IdObject) value;
                if (idObject == nullBone) {
                    return super.getListCellRendererComponent(list, "No parent", index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value.getClass().getSimpleName() + " \"" + idObject.getName() + "\"", index, isSelected, cellHasFocus);
            }
        };
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
