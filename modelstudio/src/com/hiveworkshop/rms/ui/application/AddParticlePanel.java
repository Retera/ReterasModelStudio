package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.Pair;
import com.hiveworkshop.rms.util.Vec3;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class AddParticlePanel {

    static void addParticleButtons(MainPanel mainPanel, JMenu addParticle){
        List<ParticleInformation> particleInformationList = fetchIncludedParticles();
        for (ParticleInformation particleInformation : particleInformationList){
            makeAndAddParticleButtons(mainPanel, addParticle, particleInformation);
        }

    }

    private static void makeAndAddParticleButtons(MainPanel mainPanel, JMenu addParticle, ParticleInformation particleInformation){
        final JMenuItem particleItem = new JMenuItem(particleInformation.getUggName(), new ImageIcon(particleInformation.getImage().getScaledInstance(28, 28, Image.SCALE_DEFAULT)));
        particleItem.addActionListener(e -> makeAddParticlePanel(mainPanel, particleInformation));
        addParticle.add(particleItem);
    }

    private static class ParticleInformation{
        private final File file;
        private final String uggName;
        private final Image image;

        private ParticleInformation(File file, String uggName, Image image) {
            this.file = file;
            this.uggName = uggName;
            this.image = image;
        }

        public File getFile() {
            return file;
        }

        public String getUggName() {
            return uggName;
        }

        public Image getImage() {
            return image;
        }
    }

    static List<ParticleInformation> fetchIncludedParticles() {
//        ../../../res/stock/particles
        final File stockFolder = new File("modelstudio/res/stock/particles");
        final File[] stockFiles = stockFolder.listFiles((dir, name) -> name.endsWith(".mdx"));
        if (stockFiles != null) {
            return Arrays.stream(stockFiles)
                    .map(AddParticlePanel::parseParticleFile)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static ParticleInformation parseParticleFile(File file) {
        final String basicName = file.getName().split("\\.")[0];
        final File pngImage = new File(file.getParent() + File.separatorChar + basicName + ".png");
        if (pngImage.exists()) {
            final Image image;
            try {
                image = ImageIO.read(pngImage);
                return new ParticleInformation(file, basicName, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void makeAddParticlePanel(MainPanel mainPanel, ParticleInformation particleInformation) {
        final ParticleEmitter2 particle;
        try {
            particle = MdxUtils.loadEditable(particleInformation.getFile()).sortedIdObjects(ParticleEmitter2.class).get(0);
        } catch (final IOException e1) {
            e1.printStackTrace();
            return;
        }
        if (mainPanel.currentMDL() != null){
            final JPanel particlePanel = new JPanel();


            final java.util.List<IdObject> idObjects = new ArrayList<>(mainPanel.currentMDL().getIdObjects());
            final Bone nullBone = new Bone("No parent");
            idObjects.add(0, nullBone);

            final JComboBox<IdObject> chooseParticleParentBone = new JComboBox<>(idObjects.toArray(new IdObject[0]));

            chooseParticleParentBone.setRenderer(createParticleParentComboBox(nullBone));
            final JLabel particleParentChooserLabel = new JLabel("Parent:");

            final JLabel imageLabel = new JLabel(new ImageIcon(particleInformation.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
            final JLabel titleLabel = new JLabel("Add " + particleInformation.getUggName());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

            final JLabel nameLabel = new JLabel("Particle Name:");
            final JTextField nameField = new JTextField("My" + particleInformation.getUggName() + "Particle");

            Map<String, Pair<JLabel, JSpinner>> coordinateSpinners = createCoordinateSpinners();

            chooseParticleParentBone.addActionListener(e14 -> setSpinnersToParentBoneCoordinates(chooseParticleParentBone, coordinateSpinners));

            final JPanel animPanel = new JPanel();
            final List<Animation> anims = mainPanel.currentMDL().getAnims();
            final JCheckBox[] checkBoxes = new JCheckBox[anims.size()];

            final JButton chooseAnimations = createAnimationChooserButton(particlePanel, animPanel, anims, checkBoxes);


            final JButton[] colorButtons = new JButton[3];
            final Color[] colors = new Color[colorButtons.length];
            makeColorButtons(particlePanel, particle, colorButtons, colors);

            makeParticlePanelLayout(particlePanel, chooseParticleParentBone, particleParentChooserLabel , imageLabel, titleLabel, nameLabel, nameField, coordinateSpinners, chooseAnimations, colorButtons);

            final int x = JOptionPane.showConfirmDialog(mainPanel, particlePanel,"Add " + particleInformation.getUggName(), JOptionPane.OK_CANCEL_OPTION);
            if (x == JOptionPane.OK_OPTION) {
                addParticleEmitter(mainPanel, particle, nullBone, chooseParticleParentBone, nameField, coordinateSpinners, anims, checkBoxes, colors);
            }
        }
    }

    private static void setSpinnersToParentBoneCoordinates(JComboBox<IdObject> chooseParticleParentBone, Map<String, Pair<JLabel, JSpinner>> coordinateSpinners) {
        final IdObject choice = chooseParticleParentBone.getItemAt(chooseParticleParentBone.getSelectedIndex());
        coordinateSpinners.get("X").getSecond().setValue(choice.getPivotPoint().x);
        coordinateSpinners.get("Y").getSecond().setValue(choice.getPivotPoint().y);
        coordinateSpinners.get("Z").getSecond().setValue(choice.getPivotPoint().z);
    }

    private static void makeColorButtons(JPanel parentPanel, ParticleEmitter2 particle, JButton[] colorButtons, Color[] colors) {
        for (int i = 0; i < colorButtons.length; i++) {
            final Vec3 colorValues = particle.getSegmentColor(i);
            final Color color = new Color((int) (colorValues.z * 255), (int) (colorValues.y * 255), (int) (colorValues.x * 255));

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

    private static JButton createAnimationChooserButton(JPanel parentPanel, JPanel animPanel, List<Animation> anims, JCheckBox[] checkBoxes) {
        animPanel.setLayout(new GridLayout(anims.size() + 1, 1));
        int animIndex = 0;
        for (final Animation anim : anims) {
            animPanel.add(checkBoxes[animIndex] = new JCheckBox(anim.getName()));
            checkBoxes[animIndex].setSelected(true);
            animIndex++;
        }
        final JButton chooseAnimations = new JButton("Choose when to show!");
        chooseAnimations.addActionListener(e13 -> JOptionPane.showMessageDialog(parentPanel, animPanel));
        return chooseAnimations;
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

    private static Map<String, Pair<JLabel, JSpinner>> createCoordinateSpinners() {
        Map<String, Pair<JLabel, JSpinner>> coordinateSpinners = new HashMap<>();
        coordinateSpinners.put("Z", new Pair<>(new JLabel("Z:"), new JSpinner(
                new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001))));
        coordinateSpinners.put("X", new Pair<>(new JLabel("X:"), new JSpinner(
                new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001))));
        coordinateSpinners.put("Y", new Pair<>(new JLabel("Y:"), new JSpinner(
                new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001))));
        return coordinateSpinners;
    }

    private double[] getCoordinates(Map<String, Pair<JLabel, JSpinner>> coordinateSpinners){
        double[] values = new double[3];
        values[0] = ((Number) coordinateSpinners.get("X").getSecond().getValue()).doubleValue();
        values[1] = ((Number) coordinateSpinners.get("Y").getSecond().getValue()).doubleValue();
        values[2] = ((Number) coordinateSpinners.get("Z").getSecond().getValue()).doubleValue();
        return values;
    }

    private static void addParticleEmitter(MainPanel mainPanel, ParticleEmitter2 particle, Bone nullBone, JComboBox<IdObject> chooseParcicleParentBone, JTextField nameField, Map<String, Pair<JLabel, JSpinner>> coordinateSpinners, List<Animation> anims, JCheckBox[] checkBoxes, Color[] colors) {
        int animIndex;
        // do stuff
        particle.setPivotPoint(new Vec3(
                ((Number) coordinateSpinners.get("X").getSecond().getValue()).doubleValue(),
                ((Number) coordinateSpinners.get("Y").getSecond().getValue()).doubleValue(),
                ((Number) coordinateSpinners.get("Z").getSecond().getValue()).doubleValue()));
        for (int i = 0; i < colors.length; i++) {
            particle.setSegmentColor(i, new Vec3(
                    colors[i].getBlue() / 255.00,
                    colors[i].getGreen() / 255.00,
                    colors[i].getRed() / 255.00));
        }
        final IdObject parentChoice = chooseParcicleParentBone.getItemAt(chooseParcicleParentBone.getSelectedIndex());
        if (parentChoice == nullBone) {
            particle.setParent(null);
        } else {
            particle.setParent(parentChoice);
        }
        AnimFlag oldFlag = particle.getVisibilityFlag();
        if (oldFlag == null) {
            oldFlag = new AnimFlag("Visibility");
        }
        final AnimFlag visFlag = AnimFlag.buildEmptyFrom(oldFlag);
        animIndex = 0;
        for (final Animation anim : anims) {
            if (!checkBoxes[animIndex].isSelected()) {
                visFlag.addEntry(anim.getStart(), 0);
            }
            animIndex++;
        }
        particle.setVisibilityFlag(visFlag);
        particle.setName(nameField.getText());
        mainPanel.currentMDL().add(particle);
        mainPanel.modelStructureChangeListener.nodesAdded(Collections.singletonList(particle));
    }

    private static void makeParticlePanelLayout(JPanel particlePanel, JComboBox<IdObject> particleParentBoneChooser, JLabel particleParentChooserLabel, JLabel imageLabel, JLabel titleLabel, JLabel nameLabel, JTextField nameField, Map<String, Pair<JLabel, JSpinner>> coordinateSpinners, JButton chooseAnimations, JButton[] colorButtons) {
        final GroupLayout layout = new GroupLayout(particlePanel);

        setHorizontalLayoutGroup(particleParentBoneChooser, particleParentChooserLabel, imageLabel, titleLabel, nameLabel, nameField, coordinateSpinners, chooseAnimations, colorButtons, layout);
        setVerticalLayoutGroup(particleParentBoneChooser, particleParentChooserLabel, imageLabel, titleLabel, nameLabel, nameField, coordinateSpinners, chooseAnimations, colorButtons, layout);

        particlePanel.setLayout(layout);
    }

    private static void setVerticalLayoutGroup(JComboBox<IdObject> parent, JLabel parentLabel, JLabel imageLabel, JLabel titleLabel, JLabel nameLabel, JTextField nameField, Map<String, Pair<JLabel, JSpinner>> coordinateSpinners, JButton chooseAnimations, JButton[] colorButtons, GroupLayout layout) {
        GroupLayout.ParallelGroup nameGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(nameLabel).addComponent(nameField);
        GroupLayout.ParallelGroup parentGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(parentLabel).addComponent(parent);

        GroupLayout.ParallelGroup axisSpinners = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
        addCoordinateSpinner("X", coordinateSpinners, axisSpinners);
        addCoordinateSpinner("Y", coordinateSpinners, axisSpinners);
        addCoordinateSpinner("Z", coordinateSpinners, axisSpinners);

        GroupLayout.ParallelGroup colorChoosers = layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(colorButtons[0])
                .addComponent(colorButtons[1])
                .addComponent(colorButtons[2]);

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(imageLabel)
                        .addGroup(layout.createSequentialGroup().addComponent(titleLabel)
                                .addGroup(nameGroup).addGap(4)
                                .addGroup(parentGroup).addGap(4)
                                .addComponent(chooseAnimations).addGap(4)
                                .addGroup(axisSpinners).addGap(4)
                                .addGroup(colorChoosers)));
    }

    private static void setHorizontalLayoutGroup(JComboBox<IdObject> parent, JLabel parentLabel, JLabel imageLabel, JLabel titleLabel, JLabel nameLabel, JTextField nameField, Map<String, Pair<JLabel, JSpinner>> coordinateSpinners, JButton chooseAnimations, JButton[] colorButtons, GroupLayout layout) {
        GroupLayout.SequentialGroup nameGroup = layout.createSequentialGroup().addComponent(nameLabel).addGap(4).addComponent(nameField);
        GroupLayout.SequentialGroup parentGroup = layout.createSequentialGroup().addComponent(parentLabel).addGap(4).addComponent(parent);

        GroupLayout.SequentialGroup axisSpinners = layout.createSequentialGroup();
         axisSpinners = addCoordinateSpinner2("X", coordinateSpinners, axisSpinners).addGap(4);
         axisSpinners = addCoordinateSpinner2("Y", coordinateSpinners, axisSpinners).addGap(4);
         axisSpinners = addCoordinateSpinner2("Z", coordinateSpinners, axisSpinners);

        GroupLayout.SequentialGroup colorChoosers = layout.createSequentialGroup()
                .addComponent(colorButtons[0]).addGap(4)
                .addComponent(colorButtons[1]).addGap(4)
                .addComponent(colorButtons[2]);

        layout.setHorizontalGroup(
                layout.createSequentialGroup().addComponent(imageLabel).addGap(8)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(titleLabel)
                                .addGroup(nameGroup)
                                .addGroup(parentGroup)
                                .addComponent(chooseAnimations)
                                .addGroup(axisSpinners)
                                .addGroup(colorChoosers)));
    }

    private static void addCoordinateSpinner(String axis, Map<String, Pair<JLabel, JSpinner>> coordinateSpinners, GroupLayout.Group group){
        group.addComponent(coordinateSpinners.get(axis).getFirst()).addComponent(coordinateSpinners.get(axis).getSecond());
    }

    private static GroupLayout.SequentialGroup addCoordinateSpinner2(String axis, Map<String, Pair<JLabel, JSpinner>> coordinateSpinners, GroupLayout.SequentialGroup group){
        return group.addComponent(coordinateSpinners.get(axis).getFirst()).addComponent(coordinateSpinners.get(axis).getSecond());
    }

}
