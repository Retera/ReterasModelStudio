package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.Vec3;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddParticlePanel {
    static void fetchIncludedParticles(MainPanel mainPanel) {
//        ../../../res/stock/particles
        final File stockFolder = new File("modelstudio/res/stock/particles");
        final File[] stockFiles = stockFolder.listFiles((dir, name) -> name.endsWith(".mdx"));
        if (stockFiles != null) {
            for (final File file : stockFiles) {
                parseParticle(mainPanel, file);
            }
        }
    }

    private static void parseParticle(MainPanel mainPanel, File file) {
        final String basicName = file.getName().split("\\.")[0];
        final File pngImage = new File(file.getParent() + File.separatorChar + basicName + ".png");
        if (pngImage.exists()) {
            tryFetchParticle(mainPanel, file, basicName, pngImage);
        }
    }

    private static void tryFetchParticle(MainPanel mainPanel, File file, String basicName, File pngImage){
        final Image image;
        try {
            System.out.println(pngImage);
            image = ImageIO.read(pngImage);
            final JMenuItem particleItem = new JMenuItem(basicName, new ImageIcon(image.getScaledInstance(28, 28, Image.SCALE_DEFAULT)));
            particleItem.addActionListener(e -> makeAddParticlePannel(mainPanel, file, basicName, image));
            mainPanel.addParticle.add(particleItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makeAddParticlePannel(MainPanel mainPanel, File file, String basicName, Image image) {
        final ParticleEmitter2 particle;
        try {
            particle = MdxUtils.loadEditable(file).sortedIdObjects(ParticleEmitter2.class).get(0);
        } catch (final IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        if (mainPanel.currentMDL() != null){
            final JPanel particlePanel = new JPanel();
            final java.util.List<IdObject> idObjects = new ArrayList<>(mainPanel.currentMDL().getIdObjects());
            final Bone nullBone = new Bone("No parent");
            idObjects.add(0, nullBone);
            final JComboBox<IdObject> parent = new JComboBox<>(idObjects.toArray(new IdObject[0]));
            parent.setRenderer(new BasicComboBoxRenderer() {
                @Override
                public Component getListCellRendererComponent(final JList list, final Object value,
                                                              final int index, final boolean isSelected, final boolean cellHasFocus) {
                    final IdObject idObject = (IdObject) value;
                    if (idObject == nullBone) {
                        return super.getListCellRendererComponent(list, "No parent", index, isSelected,
                                cellHasFocus);
                    }
                    return super.getListCellRendererComponent(list,
                            value.getClass().getSimpleName() + " \"" + idObject.getName() + "\"", index,
                            isSelected, cellHasFocus);
                }
            });
            final JLabel parentLabel = new JLabel("Parent:");
            final JLabel imageLabel = new JLabel(
                    new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
            final JLabel titleLabel = new JLabel("Add " + basicName);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

            final JLabel nameLabel = new JLabel("Particle Name:");
            final JTextField nameField = new JTextField("MyBlizParticle");

            final JLabel xLabel = new JLabel("Z:");
            final JSpinner xSpinner = new JSpinner(
                    new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));

            final JLabel yLabel = new JLabel("X:");
            final JSpinner ySpinner = new JSpinner(
                    new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));

            final JLabel zLabel = new JLabel("Y:");
            final JSpinner zSpinner = new JSpinner(
                    new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));
            parent.addActionListener(e14 -> {
                final IdObject choice = parent.getItemAt(parent.getSelectedIndex());
                xSpinner.setValue(choice.getPivotPoint().x);
                ySpinner.setValue(choice.getPivotPoint().y);
                zSpinner.setValue(choice.getPivotPoint().z);
            });

            final JPanel animPanel = new JPanel();
            final List<Animation> anims = mainPanel.currentMDL().getAnims();
            animPanel.setLayout(new GridLayout(anims.size() + 1, 1));
            final JCheckBox[] checkBoxes = new JCheckBox[anims.size()];
            int animIndex = 0;
            for (final Animation anim : anims) {
                animPanel.add(checkBoxes[animIndex] = new JCheckBox(anim.getName()));
                checkBoxes[animIndex].setSelected(true);
                animIndex++;
            }
            final JButton chooseAnimations = new JButton("Choose when to show!");
            chooseAnimations.addActionListener(e13 -> JOptionPane.showMessageDialog(particlePanel, animPanel));
            final JButton[] colorButtons = new JButton[3];
            final Color[] colors = new Color[colorButtons.length];
            for (int i = 0; i < colorButtons.length; i++) {
                final Vec3 colorValues = particle.getSegmentColor(i);
                final Color color = new Color((int) (colorValues.z * 255), (int) (colorValues.y * 255),
                        (int) (colorValues.x * 255));

                final JButton button = new JButton("Color " + (i + 1),
                        new ImageIcon(IconUtils.createBlank(color, 32, 32)));
                colors[i] = color;
                final int index = i;
                button.addActionListener(e12 -> {
                    final Color colorChoice = JColorChooser.showDialog(mainPanel,
                            "Chooser Color", colors[index]);
                    if (colorChoice != null) {
                        colors[index] = colorChoice;
                        button.setIcon(new ImageIcon(IconUtils.createBlank(colors[index], 32, 32)));
                    }
                });
                colorButtons[i] = button;
            }

            makeParticlePanelLayout(particlePanel, parent, parentLabel, imageLabel, titleLabel, nameLabel, nameField, xLabel, xSpinner, yLabel, ySpinner, zLabel, zSpinner, chooseAnimations, colorButtons);

            final int x = JOptionPane.showConfirmDialog(mainPanel, particlePanel,
                    "Add " + basicName, JOptionPane.OK_CANCEL_OPTION);
            if (x == JOptionPane.OK_OPTION) {
                // do stuff
                particle.setPivotPoint(new Vec3(((Number) xSpinner.getValue()).doubleValue(),
                        ((Number) ySpinner.getValue()).doubleValue(),
                        ((Number) zSpinner.getValue()).doubleValue()));
                for (int i = 0; i < colors.length; i++) {
                    particle.setSegmentColor(i, new Vec3(colors[i].getBlue() / 255.00,
                            colors[i].getGreen() / 255.00, colors[i].getRed() / 255.00));
                }
                final IdObject parentChoice = parent.getItemAt(parent.getSelectedIndex());
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
                mainPanel.modelStructureChangeListener
                        .nodesAdded(Collections.singletonList(particle));
            }
        }
    }

    private static void makeParticlePanelLayout(JPanel particlePanel, JComboBox<IdObject> parent, JLabel parentLabel, JLabel imageLabel, JLabel titleLabel, JLabel nameLabel, JTextField nameField, JLabel xLabel, JSpinner xSpinner, JLabel yLabel, JSpinner ySpinner, JLabel zLabel, JSpinner zSpinner, JButton chooseAnimations, JButton[] colorButtons) {
        final GroupLayout layout = new GroupLayout(particlePanel);

        layout.setHorizontalGroup(
                layout.createSequentialGroup().addComponent(imageLabel).addGap(8)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(titleLabel)
                                .addGroup(layout.createSequentialGroup().addComponent(nameLabel)
                                        .addGap(4).addComponent(nameField))
                                .addGroup(layout.createSequentialGroup().addComponent(parentLabel)
                                        .addGap(4).addComponent(parent))
                                .addComponent(chooseAnimations)
                                .addGroup(layout.createSequentialGroup().addComponent(xLabel)
                                        .addComponent(xSpinner).addGap(4).addComponent(yLabel)
                                        .addComponent(ySpinner).addGap(4).addComponent(zLabel)
                                        .addComponent(zSpinner))
                                .addGroup(
                                        layout.createSequentialGroup().addComponent(colorButtons[0])
                                                .addGap(4).addComponent(colorButtons[1]).addGap(4)
                                                .addComponent(colorButtons[2]))));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(imageLabel)
                        .addGroup(
                                layout.createSequentialGroup().addComponent(titleLabel)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                .addComponent(nameLabel).addComponent(nameField))
                                        .addGap(4)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                .addComponent(parentLabel).addComponent(parent))
                                        .addGap(4).addComponent(chooseAnimations).addGap(4)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                .addComponent(xLabel).addComponent(xSpinner)
                                                .addComponent(yLabel).addComponent(ySpinner)
                                                .addComponent(zLabel).addComponent(zSpinner))
                                        .addGap(4)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                .addComponent(colorButtons[0])
                                                .addComponent(colorButtons[1])
                                                .addComponent(colorButtons[2]))));
        particlePanel.setLayout(layout);
    }
}
