package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.scripts.AnimationTransfer;
import com.hiveworkshop.rms.ui.application.scripts.ChangeAnimationLengthFrame;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPopupPanel;
import com.hiveworkshop.rms.ui.application.viewer.AnimationViewer;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ImportPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ProgramPreferencesPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.*;
import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.*;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createAndAddMenuItem;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;

public class MenuBar {
    public static final ImageIcon AnimIcon = RMSIcons.AnimIcon;
    static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");

    public static JMenuBar createMenuBar(MainPanel mainPanel) {
        // Create my menu bar
        mainPanel.menuBar = new JMenuBar();

        // Build the file menu
        JMenu fileMenu = createMenu("File", KeyEvent.VK_F, "Allows the user to open, save, close, and manipulate files.");
        mainPanel.menuBar.add(fileMenu);


        mainPanel.recentMenu = createMenu("Open Recent", KeyEvent.VK_R, "Allows you to access recently opened files.");

        JMenu editMenu = createMenu("Edit", KeyEvent.VK_E, "Allows the user to use various tools to edit the currently selected model.");
        mainPanel.menuBar.add(editMenu);

        mainPanel.toolsMenu = createMenu("Tools", KeyEvent.VK_T, "Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");
        mainPanel.toolsMenu.setEnabled(false);
        mainPanel.menuBar.add(mainPanel.toolsMenu);

        JMenu viewMenu = createMenu("View", -1, "Allows the user to control view settings.");
        mainPanel.menuBar.add(viewMenu);

        mainPanel.teamColorMenu = createMenu("Team Color", -1, "Allows the user to control team color settings.");
        mainPanel.menuBar.add(mainPanel.teamColorMenu);

        mainPanel.directoryChangeNotifier.subscribe(() -> {
            GameDataFileSystem.refresh(SaveProfile.get().getDataSources());
            // cache priority order...
            UnitOptionPanel.dropRaceCache();
            DataTable.dropCache();
            ModelOptionPanel.dropCache();
            WEString.dropCache();
            BLPHandler.get().dropCache();
            mainPanel.teamColorMenu.removeAll();
            createTeamColorMenuItems(mainPanel);
            traverseAndReloadData(mainPanel.rootWindow);
        });
        createTeamColorMenuItems(mainPanel);

        JMenu windowMenu = createMenu("Window", KeyEvent.VK_W, "Allows the user to open various windows containing the program features.");
        mainPanel.windowMenu = windowMenu;
        mainPanel.menuBar.add(windowMenu);

        fillWindowsMenu(mainPanel, windowMenu);

        JMenu addMenu = createMenu("Add", KeyEvent.VK_A, "Allows the user to add new components to the model.");
        mainPanel.menuBar.add(addMenu);

        fillAddMenu(mainPanel, addMenu);

        JMenu scriptsMenu = createMenu("Scripts", KeyEvent.VK_A, "Allows the user to execute model edit scripts.");
        mainPanel.menuBar.add(scriptsMenu);

        fillScriptsMenu(mainPanel, scriptsMenu);

        final JMenuItem fixReteraLand = new JMenuItem("Fix Retera Land");
        fixReteraLand.setMnemonic(KeyEvent.VK_A);
        fixReteraLand.addActionListener(e -> {
            final EditableModel currentMDL = mainPanel.currentMDL();
            for (final Geoset geo : currentMDL.getGeosets()) {
                final Animation anim = new Animation(new ExtLog(currentMDL.getExtents()));
                geo.add(anim);
            }
        });
//		scriptsMenu.add(fixReteraLand);

        mainPanel.aboutMenu = createMenu("Help", KeyEvent.VK_H, "");
        mainPanel.menuBar.add(mainPanel.aboutMenu);

        mainPanel.recentMenu.add(new JSeparator());

        createAndAddMenuItem("Clear", mainPanel.recentMenu, KeyEvent.VK_C, e -> clearRecentActionRes(mainPanel));

        updateRecent(mainPanel);

        fillAboutMenu(mainPanel);

        fillToolsMenu(mainPanel);

        fillViewMenu(mainPanel, viewMenu);

        fillFileMenu(mainPanel, fileMenu);


        fillEditMenu(mainPanel, editMenu);

        for (int i = 0; i < mainPanel.menuBar.getMenuCount(); i++) {
            mainPanel.menuBar.getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false);
        }
        return mainPanel.menuBar;
    }

    private static void fillWindowsMenu(MainPanel mainPanel, JMenu windowMenu) {
        final JMenuItem resetViewButton = new JMenuItem("Reset Layout");
        resetViewButton.addActionListener(e -> {
            traverseAndReset(mainPanel.rootWindow);
            final TabWindow startupTabWindow = MainLayoutCreator.createMainLayout(mainPanel);
            mainPanel.rootWindow.setWindow(startupTabWindow);
            MainLayoutCreator.traverseAndFix(mainPanel.rootWindow);
        });
        windowMenu.add(resetViewButton);

        final JMenu viewsMenu = createMenu("Views", KeyEvent.VK_V);
        windowMenu.add(viewsMenu);

        final JMenuItem testItem = new JMenuItem("test");
        testItem.addActionListener(new OpenViewAction(mainPanel.rootWindow, "Animation Preview", () -> {
            final JPanel testPanel = new JPanel();

            for (int i = 0; i < 3; i++) {
//					final ControlledAnimationViewer animationViewer = new ControlledAnimationViewer(
//							currentModelPanel().getModelViewManager(), prefs);
//					animationViewer.setMinimumSize(new Dimension(400, 400));
//					final AnimationController animationController = new AnimationController(
//							currentModelPanel().getModelViewManager(), true, animationViewer);

                final AnimationViewer animationViewer2 = new AnimationViewer(
                        mainPanel.currentModelPanel().getModelViewManager(), mainPanel.prefs, false);
                animationViewer2.setMinimumSize(new Dimension(400, 400));
                testPanel.add(animationViewer2);
//					testPanel.add(animationController);
            }
            testPanel.setLayout(new GridLayout(1, 4));
            return new View("Test", null, testPanel);
        }));

//		viewsMenu.add(testItem);

        createAndAddMenuItem("Animation Preview", viewsMenu, KeyEvent.VK_A, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Animation Preview", mainPanel.previewView));
//        createAndAddMenuItem("Animation Preview", viewsMenu, KeyEvent.VK_A, new OpenViewAction(mainPanel.rootWindow, "Animation Preview", () -> mainPanel.previewView));

        createAndAddMenuItem("Animation Controller", viewsMenu, KeyEvent.VK_C, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Animation Controller", mainPanel.animationControllerView));

        createAndAddMenuItem("Modeling", viewsMenu, KeyEvent.VK_M, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Modeling", mainPanel.creatorView));

        createAndAddMenuItem("Outliner", viewsMenu, KeyEvent.VK_O, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Outliner", mainPanel.viewportControllerWindowView));

        createAndAddMenuItem("Perspective", viewsMenu, KeyEvent.VK_P, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Perspective", mainPanel.perspectiveView));

        createAndAddMenuItem("Front", viewsMenu, KeyEvent.VK_F, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Front", mainPanel.frontView));

        createAndAddMenuItem("Side", viewsMenu, KeyEvent.VK_S, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Side", mainPanel.leftView));

        createAndAddMenuItem("Bottom", viewsMenu, KeyEvent.VK_B, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Bottom", mainPanel.bottomView));

        createAndAddMenuItem("Tools", viewsMenu, KeyEvent.VK_T, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Tools", mainPanel.toolView));

        createAndAddMenuItem("Contents", viewsMenu, KeyEvent.VK_C, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Model", mainPanel.modelDataView));

        createAndAddMenuItem("Footer", viewsMenu, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Footer", mainPanel.timeSliderView));

        createAndAddMenuItem("Matrix Eater Script", viewsMenu, KeyEvent.VK_H, KeyStroke.getKeyStroke("control P"), OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Matrix Eater Script", mainPanel.hackerView));

        final JMenu browsersMenu = createMenu("Browsers", KeyEvent.VK_B);
        windowMenu.add(browsersMenu);

        createAndAddMenuItem("Data Browser", browsersMenu, KeyEvent.VK_A, MPQBrowserView.getOpenMPQViewerAction(mainPanel));

        createAndAddMenuItem("Unit Browser", browsersMenu, KeyEvent.VK_U, getOpenUnitViewerAction(mainPanel));

//        createAndAddMenuItem("Doodad Browser", browsersMenu, KeyEvent.VK_D, getOpenDoodadViewerAction(mainPanel));
        createAndAddMenuItem("Doodad Browser", browsersMenu, KeyEvent.VK_D, e -> OpenDoodadViewerActionRes(mainPanel));

        JMenuItem hiveViewer = new JMenuItem("Hive Browser");
        hiveViewer.setMnemonic(KeyEvent.VK_H);
        hiveViewer.addActionListener(getOpenHiveViewerAction(mainPanel));
//		browsersMenu.add(hiveViewer);

        windowMenu.addSeparator();
    }

    private static void fillAddMenu(final MainPanel mainPanel, JMenu addMenu) {
        mainPanel.addParticle = new JMenu("Particle");
        mainPanel.addParticle.setMnemonic(KeyEvent.VK_P);
        addMenu.add(mainPanel.addParticle);

        final File stockFolder = new File("stock/particles");
        final File[] stockFiles = stockFolder.listFiles((dir, name) -> name.endsWith(".mdx"));
        if (stockFiles != null) {
            for (final File file : stockFiles) {
                final String basicName = file.getName().split("\\.")[0];
                final File pngImage = new File(file.getParent() + File.separatorChar + basicName + ".png");
                if (pngImage.exists()) {
                    try {
                        final Image image = ImageIO.read(pngImage);
                        final JMenuItem particleItem = new JMenuItem(basicName,
                                new ImageIcon(image.getScaledInstance(28, 28, Image.SCALE_DEFAULT)));
                        particleItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                final ParticleEmitter2 particle;
                                try {
                                    particle = MdxUtils.loadEditable(file).sortedIdObjects(ParticleEmitter2.class).get(0);
                                } catch (final IOException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                    return;
                                }

                                final JPanel particlePanel = new JPanel();
                                final List<IdObject> idObjects = new ArrayList<>(mainPanel.currentMDL().getIdObjects());
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
                        });
                        mainPanel.addParticle.add(particleItem);
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        mainPanel.animationMenu = new JMenu("Animation");
        mainPanel.animationMenu.setMnemonic(KeyEvent.VK_A);
        addMenu.add(mainPanel.animationMenu);

        createAndAddMenuItem("Rising/Falling Birth/Death", mainPanel.animationMenu, KeyEvent.VK_R, e -> riseFallBirthActionRes(mainPanel));

        mainPanel.singleAnimationMenu = new JMenu("Single");
        mainPanel.singleAnimationMenu.setMnemonic(KeyEvent.VK_S);
        mainPanel.animationMenu.add(mainPanel.singleAnimationMenu);

        JMenuItem animFromFile = new JMenuItem("From File");
        animFromFile.setMnemonic(KeyEvent.VK_F);
        animFromFile.addActionListener(e -> {
            animFromFileActionRes(mainPanel);
        });
        mainPanel.singleAnimationMenu.add(animFromFile);

        JMenuItem animFromUnit = new JMenuItem("From Unit");
        animFromUnit.setMnemonic(KeyEvent.VK_U);
        animFromUnit.addActionListener(e -> animFromUnitActionRes(mainPanel));
        mainPanel.singleAnimationMenu.add(animFromUnit);

        JMenuItem animFromModel = new JMenuItem("From Model");
        animFromModel.setMnemonic(KeyEvent.VK_M);
        animFromModel.addActionListener(e -> animFromModelActionRes(mainPanel));
        mainPanel.singleAnimationMenu.add(animFromModel);

        JMenuItem animFromObject = new JMenuItem("From Object");
        animFromObject.setMnemonic(KeyEvent.VK_O);
        animFromObject.addActionListener(e -> animFromObjectActionRes(mainPanel));
        mainPanel.singleAnimationMenu.add(animFromObject);
    }

    private static void fillAboutMenu(MainPanel mainPanel) {
        createAndAddMenuItem("Changelog", mainPanel.aboutMenu, KeyEvent.VK_A, e -> creditsButtonActionRes("docs/changelist.rtf", "Changelog"));

        createAndAddMenuItem("About", mainPanel.aboutMenu, KeyEvent.VK_A, e -> creditsButtonActionRes("docs/credits.rtf", "About"));

        JMenuItem jokeButton = new JMenuItem("HTML Magic");
        jokeButton.setMnemonic(KeyEvent.VK_H);
        jokeButton.addActionListener(e -> {
            final JEditorPane jEditorPane;
            try {
                jEditorPane = new JEditorPane(new URL("http://79.179.129.227:8080/clients/editor/"));
                final JFrame testFrame = new JFrame("Test");
                testFrame.setContentPane(jEditorPane);
                testFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                testFrame.pack();
                testFrame.setLocationRelativeTo(mainPanel.nullmodelButton);
                testFrame.setVisible(true);
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        });
        mainPanel.aboutMenu.add(jokeButton);
    }

    private static void fillToolsMenu(MainPanel mainPanel) {
        JMenuItem showMatrices = new JMenuItem("View Selected \"Matrices\"");
        // showMatrices.setMnemonic(KeyEvent.VK_V);
        showMatrices.addActionListener(getViewMatricesAction(mainPanel));
        mainPanel.toolsMenu.add(showMatrices);

        JMenuItem insideOut = new JMenuItem("Flip all selected faces");
        insideOut.setMnemonic(KeyEvent.VK_I);
        insideOut.addActionListener(getInsideOutAction(mainPanel));
        insideOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
        mainPanel.toolsMenu.add(insideOut);

        JMenuItem insideOutNormals = new JMenuItem("Flip all selected normals");
        insideOutNormals.addActionListener(getInsideOutNormalsAction(mainPanel));
        mainPanel.toolsMenu.add(insideOutNormals);

        mainPanel.toolsMenu.add(new JSeparator());

        createAndAddMenuItem("Edit UV Mapping", mainPanel.toolsMenu, KeyEvent.VK_U, e -> editUVsActionRes(mainPanel));

        JMenuItem editTextures = new JMenuItem("Edit Textures");
        editTextures.setMnemonic(KeyEvent.VK_T);
        editTextures.addActionListener(e -> {
            final EditTexturesPopupPanel textureManager = new EditTexturesPopupPanel(mainPanel.currentModelPanel().getModelViewManager(),
                    mainPanel.modelStructureChangeListener, mainPanel.textureExporter);
            final JFrame frame = new JFrame("Edit Textures");
            textureManager.setSize(new Dimension(800, 650));
            frame.setContentPane(textureManager);
            frame.setSize(textureManager.getSize());
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        });
        mainPanel.toolsMenu.add(editTextures);

        createAndAddMenuItem("Rig Selection", mainPanel.toolsMenu, KeyEvent.VK_R, KeyStroke.getKeyStroke("control W"), mainPanel.rigAction);

        mainPanel.tweaksSubmenu = new JMenu("Tweaks");
        mainPanel.tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
        mainPanel.tweaksSubmenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to tweak conversion mistakes.");
        mainPanel.toolsMenu.add(mainPanel.tweaksSubmenu);

        createAndAddMenuItem("Flip All UVs U", mainPanel.tweaksSubmenu, KeyEvent.VK_U, getFlipAllUVsUAction(mainPanel));

        JMenuItem flipAllUVsV = new JMenuItem("Flip All UVs V");
        // flipAllUVsV.setMnemonic(KeyEvent.VK_V);
        flipAllUVsV.addActionListener(getFlipAllUVsVAction(mainPanel));
        mainPanel.tweaksSubmenu.add(flipAllUVsV);

        createAndAddMenuItem("Swap All UVs U for V", mainPanel.tweaksSubmenu, KeyEvent.VK_S, getInverseAllUVsAction(mainPanel));

        mainPanel.mirrorSubmenu = new JMenu("Mirror");
        mainPanel.mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
        mainPanel.mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
        mainPanel.toolsMenu.add(mainPanel.mirrorSubmenu);

        createAndAddMenuItem("Mirror X", mainPanel.mirrorSubmenu, KeyEvent.VK_X, getMirrorAxisAction(mainPanel, "Mirror X", (byte) 0));

        createAndAddMenuItem("Mirror Y", mainPanel.mirrorSubmenu, KeyEvent.VK_Y, getMirrorAxisAction(mainPanel, "Mirror Y", (byte) 1));

        createAndAddMenuItem("Mirror Z", mainPanel.mirrorSubmenu, KeyEvent.VK_Z, getMirrorAxisAction(mainPanel, "Mirror Z", (byte) 2));

        mainPanel.mirrorSubmenu.add(new JSeparator());

        mainPanel.mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
        mainPanel.mirrorFlip.setMnemonic(KeyEvent.VK_A);
        mainPanel.mirrorSubmenu.add(mainPanel.mirrorFlip);
    }

    private static void fillViewMenu(MainPanel mainPanel, JMenu viewMenu) {
        mainPanel.textureModels = new JCheckBoxMenuItem("Texture Models", true);
        mainPanel.textureModels.setMnemonic(KeyEvent.VK_T);
        mainPanel.textureModels.setSelected(true);
        mainPanel.textureModels.addActionListener(e -> mainPanel.prefs.setTextureModels(mainPanel.textureModels.isSelected()));
        viewMenu.add(mainPanel.textureModels);

        JMenuItem newDirectory = new JMenuItem("Change Game Directory");
        newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
        newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
        newDirectory.setMnemonic(KeyEvent.VK_D);
        newDirectory.addActionListener(mainPanel);
//		viewMenu.add(newDirectory);

        viewMenu.add(new JSeparator());

        mainPanel.showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
        // showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
        mainPanel.showVertexModifyControls.addActionListener(e -> showVertexModifyControlsActionRes(mainPanel.modelPanels, mainPanel.prefs, mainPanel.showVertexModifyControls));
        viewMenu.add(mainPanel.showVertexModifyControls);

        viewMenu.add(new JSeparator());

        mainPanel.showNormals = new JCheckBoxMenuItem("Show Normals", true);
        mainPanel.showNormals.setMnemonic(KeyEvent.VK_N);
        mainPanel.showNormals.setSelected(false);
        mainPanel.showNormals.addActionListener(e -> mainPanel.prefs.setShowNormals(mainPanel.showNormals.isSelected()));
        viewMenu.add(mainPanel.showNormals);

        mainPanel.viewMode = new JMenu("3D View Mode");
        viewMenu.add(mainPanel.viewMode);

        mainPanel.viewModes = new ButtonGroup();

        final ActionListener repainter = e -> {
            if (mainPanel.wireframe.isSelected()) {
                mainPanel.prefs.setViewMode(0);
            } else if (mainPanel.solid.isSelected()) {
                mainPanel.prefs.setViewMode(1);
            } else {
                mainPanel.prefs.setViewMode(-1);
            }
            mainPanel.repaint();
        };

        mainPanel.wireframe = new JRadioButtonMenuItem("Wireframe");
        mainPanel.wireframe.addActionListener(repainter);
        mainPanel.viewMode.add(mainPanel.wireframe);
        mainPanel.viewModes.add(mainPanel.wireframe);

        mainPanel.solid = new JRadioButtonMenuItem("Solid");
        mainPanel.solid.addActionListener(repainter);
        mainPanel.viewMode.add(mainPanel.solid);
        mainPanel.viewModes.add(mainPanel.solid);

        mainPanel.viewModes.setSelected(mainPanel.solid.getModel(), true);
    }

    private static void fillFileMenu(MainPanel mainPanel, JMenu fileMenu) {
        createAndAddMenuItem("New", fileMenu, KeyEvent.VK_N, KeyStroke.getKeyStroke("control N"), e -> ToolBar.newModel(mainPanel));

        createAndAddMenuItem("Open", fileMenu, KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> ToolBar.onClickOpen(mainPanel));

        fileMenu.add(mainPanel.recentMenu);

        mainPanel.fetch = new JMenu("Open Internal");
        mainPanel.fetch.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(mainPanel.fetch);

        createAndAddMenuItem("Unit", mainPanel.fetch, KeyEvent.VK_U, KeyStroke.getKeyStroke("control U"), e -> fetchUnitActionRes(mainPanel));

        createAndAddMenuItem("Model", mainPanel.fetch, KeyEvent.VK_M, KeyStroke.getKeyStroke("control M"), e -> fetchModelActionRes(mainPanel));

        createAndAddMenuItem("Object Editor", mainPanel.fetch, KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> fetchObjectActionRes(mainPanel));

        mainPanel.fetch.add(new JSeparator());

        JCheckBoxMenuItem fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
        fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
        fetchPortraitsToo.addActionListener(e -> mainPanel.prefs.setLoadPortraits(fetchPortraitsToo.isSelected()));
        mainPanel.fetchPortraitsToo = fetchPortraitsToo;
        mainPanel.fetch.add(fetchPortraitsToo);
        fetchPortraitsToo.setSelected(true);

        fileMenu.add(new JSeparator());

        JMenu importMenu = createMenu("Import", KeyEvent.VK_I);
        fileMenu.add(importMenu);

        JMenuItem importButton = new JMenuItem("From File");
        importButton.setMnemonic(KeyEvent.VK_I);
        importButton.setAccelerator(KeyStroke.getKeyStroke("control shift I"));
        importButton.addActionListener(e -> {
            ImportFileActions.importButtonActionRes(mainPanel);
        });
        importMenu.add(importButton);

        JMenuItem importUnit = new JMenuItem("From Unit");
        importUnit.setMnemonic(KeyEvent.VK_U);
        importUnit.setAccelerator(KeyStroke.getKeyStroke("control shift U"));
        importUnit.addActionListener(e -> {
            ImportFileActions.importUnitActionRes(mainPanel);
        });
        importMenu.add(importUnit);

        JMenuItem importGameModel = new JMenuItem("From WC3 Model");
        importGameModel.setMnemonic(KeyEvent.VK_M);
        importGameModel.addActionListener(e -> {
            ImportFileActions.importGameModelActionRes(mainPanel);
        });
        importMenu.add(importGameModel);

        JMenuItem importGameObject = new JMenuItem("From Object Editor");
        importGameObject.setMnemonic(KeyEvent.VK_O);
        importGameObject.addActionListener(e -> {
            ImportFileActions.importGameObjectActionRes(mainPanel);
        });
        importMenu.add(importGameObject);

        createAndAddMenuItem("From Workspace", importMenu, KeyEvent.VK_O, e -> ImportFileActions.importFromWorkspaceActionRes(mainPanel));

        createAndAddMenuItem("Save", fileMenu, KeyEvent.VK_S, KeyStroke.getKeyStroke("control S"), e -> {
            if ((mainPanel.currentMDL() != null) && (mainPanel.currentMDL().getFile() != null)) {
                ToolBar.onClickSave(mainPanel);}
        });

        createAndAddMenuItem("Save as", fileMenu, KeyEvent.VK_A, KeyStroke.getKeyStroke("control Q"), e -> onClickSaveAs(mainPanel));

        fileMenu.add(new JSeparator());

        createAndAddMenuItem("Export Texture", fileMenu, KeyEvent.VK_E, e -> exportTexturesActionRes(mainPanel));

        fileMenu.add(new JSeparator());

        JMenuItem revert = new JMenuItem("Revert");
        revert.addActionListener(e -> MPQBrowserView.revertActionRes(mainPanel));
        fileMenu.add(revert);

        createAndAddMenuItem("Close", fileMenu, KeyEvent.VK_E, KeyStroke.getKeyStroke("control E"), e -> closePanelActionRes(mainPanel));

        fileMenu.add(new JSeparator());

        createAndAddMenuItem("Exit", fileMenu, KeyEvent.VK_E, e -> {
            if (closeAll(mainPanel)) {
                MainFrame.frame.dispose();
            }
        });
    }

    private static void fillEditMenu(final MainPanel mainPanel, JMenu editMenu) {
        mainPanel.undo = new UndoMenuItem(mainPanel, "Undo");
        mainPanel.undo.addActionListener(mainPanel.undoAction);
        mainPanel.undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        mainPanel.undo.setEnabled(mainPanel.undo.funcEnabled());
        // undo.addMouseListener(this);
        editMenu.add(mainPanel.undo);

        mainPanel.redo = new RedoMenuItem(mainPanel, "Redo");
        mainPanel.redo.addActionListener(mainPanel.redoAction);
        mainPanel.redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        mainPanel.redo.setEnabled(mainPanel.redo.funcEnabled());
        // redo.addMouseListener(this);
        editMenu.add(mainPanel.redo);


        editMenu.add(new JSeparator());
        final JMenu optimizeMenu = createMenu("Optimize", KeyEvent.VK_O);
        editMenu.add(optimizeMenu);
        createAndAddMenuItem("Linearize Animations", optimizeMenu, KeyEvent.VK_L, e -> linearizeAnimationsActionRes(mainPanel));

        createAndAddMenuItem("Simplify Keyframes (Experimental)", optimizeMenu, KeyEvent.VK_K, e -> simplifyKeyframesActionRes(mainPanel));

        final JMenuItem minimizeGeoset = new JMenuItem("Minimize Geosets");
        minimizeGeoset.setMnemonic(KeyEvent.VK_K);
        minimizeGeoset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int confirm = JOptionPane.showConfirmDialog(mainPanel,
                        "This is experimental and I did not code the Undo option for it yet. Continue?\nMy advice is to click cancel and save once first.",
                        "Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.OK_OPTION) {
                    return;
                }

                mainPanel.currentMDL().doSavePreps();

                final Map<Geoset, Geoset> sourceToDestination = new HashMap<>();
                final List<Geoset> retainedGeosets = new ArrayList<>();
                for (final Geoset geoset : mainPanel.currentMDL().getGeosets()) {
                    boolean alreadyRetained = false;
                    for (final Geoset retainedGeoset : retainedGeosets) {
                        if (retainedGeoset.getMaterial().equals(geoset.getMaterial())
                                && (retainedGeoset.getSelectionGroup() == geoset.getSelectionGroup())
                                && (retainedGeoset.getUnselectable() == geoset.getUnselectable())
                                && mergableGeosetAnims(retainedGeoset.getGeosetAnim(), geoset.getGeosetAnim())) {
                            alreadyRetained = true;
                            for (final GeosetVertex gv : geoset.getVertices()) {
                                retainedGeoset.add(gv);
                            }
                            for (final Triangle t : geoset.getTriangles()) {
                                retainedGeoset.add(t);
                            }
                            break;
                        }
                    }
                    if (!alreadyRetained) {
                        retainedGeosets.add(geoset);
                    }
                }
                final EditableModel currentMDL = mainPanel.currentMDL();
                final List<Geoset> geosets = currentMDL.getGeosets();
                final List<Geoset> geosetsRemoved = new ArrayList<>();
                final Iterator<Geoset> iterator = geosets.iterator();
                while (iterator.hasNext()) {
                    final Geoset geoset = iterator.next();
                    if (!retainedGeosets.contains(geoset)) {
                        iterator.remove();
                        final GeosetAnim geosetAnim = geoset.getGeosetAnim();
                        if (geosetAnim != null) {
                            currentMDL.remove(geosetAnim);
                        }
                        geosetsRemoved.add(geoset);
                    }
                }
                mainPanel.modelStructureChangeListener.geosetsRemoved(geosetsRemoved);
            }

            private boolean mergableGeosetAnims(final GeosetAnim first, final GeosetAnim second) {
                if ((first == null) && (second == null)) {
                    return true;
                }
                if ((first == null) || (second == null)) {
                    return false;
                }
                final AnimFlag firstVisibilityFlag = first.getVisibilityFlag();
                final AnimFlag secondVisibilityFlag = second.getVisibilityFlag();
                if ((firstVisibilityFlag == null) != (secondVisibilityFlag == null)) {
                    return false;
                }
                if ((firstVisibilityFlag != null) && !firstVisibilityFlag.equals(secondVisibilityFlag)) {
                    return false;
                }
                if (first.isDropShadow() != second.isDropShadow()) {
                    return false;
                }
                if (Math.abs(first.getStaticAlpha() - second.getStaticAlpha()) > 0.001) {
                    return false;
                }
                if ((first.getStaticColor() == null) != (second.getStaticColor() == null)) {
                    return false;
                }
                if ((first.getStaticColor() != null) && !first.getStaticColor().equalLocs(second.getStaticColor())) {
                    return false;
                }
                final AnimFlag firstAnimatedColor = first.find("Color");
                final AnimFlag secondAnimatedColor = second.find("Color");
                if ((firstAnimatedColor == null) != (secondAnimatedColor == null)) {
                    return false;
                }
                return (firstAnimatedColor == null) || firstAnimatedColor.equals(secondAnimatedColor);
            }
        });
        optimizeMenu.add(minimizeGeoset);

        JMenuItem sortBones = new JMenuItem("Sort Nodes");
        sortBones.setMnemonic(KeyEvent.VK_S);
        sortBones.addActionListener(e -> {
            final EditableModel model = mainPanel.currentMDL();
            final List<IdObject> roots = new ArrayList<>();
            final List<IdObject> modelList = model.getIdObjects();
            for (final IdObject object : modelList) {
                if (object.getParent() == null) {
                    roots.add(object);
                }
            }
            final Queue<IdObject> bfsQueue = new LinkedList<>(roots);
            final List<IdObject> result = new ArrayList<>();
            while (!bfsQueue.isEmpty()) {
                final IdObject nextItem = bfsQueue.poll();
                bfsQueue.addAll(nextItem.getChildrenNodes());
                result.add(nextItem);
            }
            for (final IdObject node : result) {
                model.remove(node);
            }
            mainPanel.modelStructureChangeListener.nodesRemoved(result);
            for (final IdObject node : result) {
                model.add(node);
            }
            mainPanel.modelStructureChangeListener.nodesAdded(result);
        });
        optimizeMenu.add(sortBones);

        final JMenuItem flushUnusedTexture = new JMenuItem("Flush Unused Texture");
        flushUnusedTexture.setEnabled(false);
        flushUnusedTexture.setMnemonic(KeyEvent.VK_F);
        optimizeMenu.add(flushUnusedTexture);

        final JMenuItem recalcNormals = new JMenuItem("Recalculate Normals");
        recalcNormals.setAccelerator(KeyStroke.getKeyStroke("control N"));
        recalcNormals.addActionListener(getRecalculateNormalsAction(mainPanel));
        editMenu.add(recalcNormals);

        final JMenuItem recalcExtents = new JMenuItem("Recalculate Extents");
        recalcExtents.setAccelerator(KeyStroke.getKeyStroke("control shift E"));
        recalcExtents.addActionListener(getRecalculateExtentsAction(mainPanel));
        editMenu.add(recalcExtents);

        editMenu.add(new JSeparator());
        final TransferActionListener transferActionListener = new TransferActionListener();
        final ActionListener copyActionListener = e -> {
            if (!mainPanel.animationModeState) {
                transferActionListener.actionPerformed(e);
            } else {
                if (e.getActionCommand().equals(TransferHandler.getCutAction().getValue(Action.NAME))) {
                    mainPanel.timeSliderPanel.cut();
                } else if (e.getActionCommand().equals(TransferHandler.getCopyAction().getValue(Action.NAME))) {
                    mainPanel.timeSliderPanel.copy();
                } else if (e.getActionCommand().equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
                    mainPanel.timeSliderPanel.paste();
                }
            }
        };

        createAndAddMenuItem("Cut", editMenu, KeyStroke.getKeyStroke("control X"), (String) TransferHandler.getCutAction().getValue(Action.NAME), copyActionListener);

        createAndAddMenuItem("Copy", editMenu, KeyStroke.getKeyStroke("control C"), (String) TransferHandler.getCopyAction().getValue(Action.NAME), copyActionListener);

        createAndAddMenuItem("Paste", editMenu, KeyStroke.getKeyStroke("control V"), (String) TransferHandler.getPasteAction().getValue(Action.NAME), copyActionListener);

        JMenuItem duplicateSelection = new JMenuItem("Duplicate");
        duplicateSelection.setAccelerator(KeyStroke.getKeyStroke("control D"));
        duplicateSelection.addActionListener(mainPanel.cloneAction);
        editMenu.add(duplicateSelection);

        editMenu.add(new JSeparator());

        JMenuItem snapVertices = new JMenuItem("Snap Vertices");
        snapVertices.setAccelerator(KeyStroke.getKeyStroke("control shift W"));
        snapVertices.addActionListener(e -> getSnapVerticiesAction(mainPanel));
        editMenu.add(snapVertices);

        JMenuItem snapNormals = new JMenuItem("Snap Normals");
        snapNormals.setAccelerator(KeyStroke.getKeyStroke("control L"));
        snapNormals.addActionListener(getSnapNormalsAction(mainPanel));
        editMenu.add(snapNormals);

        editMenu.add(new JSeparator());

        JMenuItem selectAll = new JMenuItem("Select All");
        selectAll.setAccelerator(KeyStroke.getKeyStroke("control A"));
        selectAll.addActionListener(mainPanel.selectAllAction);
        editMenu.add(selectAll);

        JMenuItem invertSelect = new JMenuItem("Invert Selection");
        invertSelect.setAccelerator(KeyStroke.getKeyStroke("control I"));
        invertSelect.addActionListener(mainPanel.invertSelectAction);
        editMenu.add(invertSelect);

        JMenuItem expandSelection = new JMenuItem("Expand Selection");
        expandSelection.setAccelerator(KeyStroke.getKeyStroke("control E"));
        expandSelection.addActionListener(mainPanel.expandSelectionAction);
        editMenu.add(expandSelection);

        editMenu.addSeparator();

        createAndAddMenuItem("Delete", editMenu, KeyEvent.VK_D, mainPanel.deleteAction);

        editMenu.addSeparator();
        createAndAddMenuItem("Preferences Window", editMenu, KeyEvent.VK_P, getOpenPreferencesAction(mainPanel));
    }

    private static void fillScriptsMenu(MainPanel mainPanel, JMenu scriptsMenu) {
        createAndAddMenuItem("Oinkerwinkle-Style AnimTransfer", scriptsMenu, KeyEvent.VK_P, KeyStroke.getKeyStroke("control shift S"), e -> importButtonSActionRes());

        JMenuItem mergeGeoset = new JMenuItem("Oinkerwinkle-Style Merge Geoset");
        mergeGeoset.setMnemonic(KeyEvent.VK_M);
        mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
        mergeGeoset.addActionListener(e -> {
            try {
                mergeGeosetActionRes(mainPanel);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        scriptsMenu.add(mergeGeoset);

        JMenuItem nullmodelButton = new JMenuItem("Edit/delete model components");
        nullmodelButton.setMnemonic(KeyEvent.VK_E);
        nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
        nullmodelButton.addActionListener(e -> nullmodelButtonActionRes(mainPanel));
        mainPanel.nullmodelButton = nullmodelButton;
        scriptsMenu.add(nullmodelButton);

        createAndAddMenuItem("Export Animated to Static Mesh", scriptsMenu, KeyEvent.VK_E, e -> {exportAnimatedToStaticMeshActionRes(mainPanel); });

        createAndAddMenuItem("Export Animated Frame PNG", scriptsMenu, KeyEvent.VK_F, e -> {exportAnimatedFramePNGActionRes(mainPanel);});

        createAndAddMenuItem("Create Back2Back Animation", scriptsMenu, KeyEvent.VK_P, e -> {combineAnimsActionRes(mainPanel);});

        createAndAddMenuItem("Change Animation Lengths by Scaling", scriptsMenu, KeyEvent.VK_A, e -> scaleAnimationsActionRes(mainPanel));

        createAndAddMenuItem("Assign FormatVersion 800", scriptsMenu, KeyEvent.VK_A, e -> mainPanel.currentMDL().setFormatVersion(800));

        createAndAddMenuItem("Assign FormatVersion 1000", scriptsMenu, KeyEvent.VK_A, e -> mainPanel.currentMDL().setFormatVersion(1000));

        createAndAddMenuItem("SD -> HD (highly experimental, requires 900 or 1000)", scriptsMenu, KeyEvent.VK_A, e -> EditableModel.makeItHD(mainPanel.currentMDL()));

        createAndAddMenuItem("HD -> SD (highly experimental, becomes 800)", scriptsMenu, KeyEvent.VK_A, e -> EditableModel.convertToV800(1, mainPanel.currentMDL()));

        createAndAddMenuItem("Recalculate Tangents (requires 900 or 1000)", scriptsMenu, KeyEvent.VK_A, e -> EditableModel.recalculateTangents(mainPanel.currentMDL(), mainPanel));

        final JMenuItem jokebutton = new JMenuItem("Load Retera Land");
        jokebutton.setMnemonic(KeyEvent.VK_A);
        jokebutton.addActionListener(e -> {
            jokeButtonActionResponse(mainPanel);
        });
//		scriptsMenu.add(jokebutton);
    }

    private static void combineAnimsActionRes(MainPanel mainPanel) {
        final List<Animation> anims = mainPanel.currentMDL().getAnims();
        final Animation[] array = anims.toArray(new Animation[0]);
        final Object choice = JOptionPane.showInputDialog(mainPanel, "Pick the first animation",
                "Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
        final Animation animation = (Animation) choice;

        final Object choice2 = JOptionPane.showInputDialog(mainPanel, "Pick the second animation",
                "Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
        final Animation animation2 = (Animation) choice2;

        final String nameChoice = JOptionPane.showInputDialog(mainPanel,
                "What should the combined animation be called?");
        if (nameChoice != null) {
            final int anim1Length = animation.getEnd() - animation.getStart();
            final int anim2Length = animation2.getEnd() - animation2.getStart();
            final int totalLength = anim1Length + anim2Length;

            final EditableModel model = mainPanel.currentMDL();
            final int animTrackEnd = model.animTrackEnd();
            final int start = animTrackEnd + 1000;
            animation.copyToInterval(start, start + anim1Length, model.getAllAnimFlags(),
                    model.sortedIdObjects(EventObject.class));
            animation2.copyToInterval(start + anim1Length, start + totalLength, model.getAllAnimFlags(),
                    model.sortedIdObjects(EventObject.class));

            final Animation newAnimation = new Animation(nameChoice, start, start + totalLength);
            model.add(newAnimation);
            newAnimation.setNonLooping(true);
            newAnimation.setExtents(new ExtLog(animation.getExtents()));
            JOptionPane.showMessageDialog(mainPanel,
                    "DONE! Made a combined animation called " + newAnimation.getName(), "Success",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    private static void exportAnimatedFramePNGActionRes(MainPanel mainPanel) {
        final BufferedImage fBufferedImage = mainPanel.currentModelPanel().getAnimationViewer().getBufferedImage();

        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
            final EditableModel current = mainPanel.currentMDL();
            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
            } else if (mainPanel.profile.getPath() != null) {
                mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
            }
        }
        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
            mainPanel.exportTextureDialog.setSelectedFile(
                    new File(mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator));
        }

        final int x = mainPanel.exportTextureDialog.showSaveDialog(mainPanel);
        if (x == JFileChooser.APPROVE_OPTION) {
            final File file = mainPanel.exportTextureDialog.getSelectedFile();
            if (file != null) {
                try {
                    if (file.getName().lastIndexOf('.') >= 0) {
                        BufferedImage bufferedImage = fBufferedImage;
                        String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
                                .toUpperCase();
                        if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
                                || fileExtension.equals("JPEG")) {
                            JOptionPane.showMessageDialog(mainPanel,
                                    "Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
                            bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
                        }
                        if (fileExtension.equals("BLP")) {
                            fileExtension = "blp";
                        }
                        final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
                        if (!write) {
                            JOptionPane.showMessageDialog(mainPanel, "File type unknown or unavailable");
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, "No file type was specified");
                    }
                } catch (final IOException e1) {
                    ExceptionPopup.display(e1);
                    e1.printStackTrace();
                } catch (final Exception e2) {
                    ExceptionPopup.display(e2);
                    e2.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No output file was specified");
            }
        }
    }

    private static void exportAnimatedToStaticMeshActionRes(MainPanel mainPanel) {
        if (!mainPanel.animationModeState) {
            JOptionPane.showMessageDialog(mainPanel, "You must be in the Animation Editor to use that!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final Vec4 vertexHeap = new Vec4();
        final Vec4 appliedVertexHeap = new Vec4();
        final Vec4 vertexSumHeap = new Vec4();
        final Vec4 normalHeap = new Vec4();
        final Vec4 appliedNormalHeap = new Vec4();
        final Vec4 normalSumHeap = new Vec4();
        final ModelPanel modelContext = mainPanel.currentModelPanel();
        final RenderModel editorRenderModel = modelContext.getEditorRenderModel();
        final EditableModel model = modelContext.getModel();
        final ModelViewManager modelViewManager = modelContext.getModelViewManager();
        final EditableModel snapshotModel = EditableModel.deepClone(model, model.getHeaderName() + "At"
                + editorRenderModel.getAnimatedRenderEnvironment().getAnimationTime());
        for (int geosetIndex = 0; geosetIndex < snapshotModel.getGeosets().size(); geosetIndex++) {
            final Geoset geoset = model.getGeoset(geosetIndex);
            final Geoset snapshotGeoset = snapshotModel.getGeoset(geosetIndex);
            for (int vertexIndex = 0; vertexIndex < geoset.getVertices().size(); vertexIndex++) {
                final GeosetVertex vertex = geoset.getVertex(vertexIndex);
                final GeosetVertex snapshotVertex = snapshotGeoset.getVertex(vertexIndex);
                final List<Bone> bones = vertex.getBones();
                vertexHeap.x = (float) vertex.x;
                vertexHeap.y = (float) vertex.y;
                vertexHeap.z = (float) vertex.z;
                vertexHeap.w = 1;
                if (bones.size() > 0) {
                    vertexSumHeap.set(0, 0, 0, 0);
                    for (final Bone bone : bones) {
                        editorRenderModel.getRenderNode(bone).getWorldMatrix().transform(vertexHeap, appliedVertexHeap);
                        vertexSumHeap.add(appliedVertexHeap);
                    }
                    final int boneCount = bones.size();
                    vertexSumHeap.x /= boneCount;
                    vertexSumHeap.y /= boneCount;
                    vertexSumHeap.z /= boneCount;
                    vertexSumHeap.w /= boneCount;
                } else {
                    vertexSumHeap.set(vertexHeap);
                }
                snapshotVertex.x = vertexSumHeap.x;
                snapshotVertex.y = vertexSumHeap.y;
                snapshotVertex.z = vertexSumHeap.z;

                normalHeap.x = (float) vertex.getNormal().x;
                normalHeap.y = (float) vertex.getNormal().y;
                normalHeap.z = (float) vertex.getNormal().z;
                normalHeap.w = 0;
                if (bones.size() > 0) {
                    normalSumHeap.set(0, 0, 0, 0);
                    for (final Bone bone : bones) {
                        editorRenderModel.getRenderNode(bone).getWorldMatrix().transform(normalHeap, appliedNormalHeap);
                        normalSumHeap.add(appliedNormalHeap);
                    }

                    if (normalSumHeap.length() > 0) {
                        normalSumHeap.normalize();
                    } else {
                        normalSumHeap.set(0, 1, 0, 0);
                    }
                } else {
                    normalSumHeap.set(normalHeap);
                }
                snapshotVertex.getNormal().x = normalSumHeap.x;
                snapshotVertex.getNormal().y = normalSumHeap.y;
                snapshotVertex.getNormal().z = normalSumHeap.z;
            }
        }
        snapshotModel.getIdObjects().clear();
        final Bone boneRoot = new Bone("Bone_Root");
        boneRoot.setPivotPoint(new Vec3(0, 0, 0));
        snapshotModel.add(boneRoot);
        for (final Geoset geoset : snapshotModel.getGeosets()) {
            for (final GeosetVertex vertex : geoset.getVertices()) {
                vertex.getBones().clear();
                vertex.getBones().add(boneRoot);
            }
        }
        final Iterator<Geoset> geosetIterator = snapshotModel.getGeosets().iterator();
        while (geosetIterator.hasNext()) {
            final Geoset geoset = geosetIterator.next();
            final GeosetAnim geosetAnim = geoset.getGeosetAnim();
            if (geosetAnim != null) {
                final Object visibilityValue = geosetAnim.getVisibilityFlag()
                        .interpolateAt(editorRenderModel.getAnimatedRenderEnvironment());
                if (visibilityValue instanceof Float) {
                    final Float visibility = (Float) visibilityValue;
                    final double visvalue = visibility;
                    if (visvalue < 0.01) {
                        geosetIterator.remove();
                        snapshotModel.remove(geosetAnim);
                    }
                }

            }
        }
        snapshotModel.getAnims().clear();
        snapshotModel.add(new Animation("Stand", 333, 1333));
        final List<AnimFlag> allAnimFlags = snapshotModel.getAllAnimFlags();
        for (final AnimFlag flag : allAnimFlags) {
            if (!flag.hasGlobalSeq()) {
                if (flag.size() > 0) {
                    final Object value = flag.interpolateAt(mainPanel.animatedRenderEnvironment);
                    flag.setInterpType(InterpolationType.DONT_INTERP);
                    flag.getValues().clear();
                    flag.getTimes().clear();
                    flag.getInTans().clear();
                    flag.getOutTans().clear();
                    flag.addEntry(333, value);
                }
            }
        }
        mainPanel.fc.setDialogTitle("Export Static Snapshot");
        final int result = mainPanel.fc.showSaveDialog(mainPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = mainPanel.fc.getSelectedFile();
            if (selectedFile != null) {
                if (!selectedFile.getPath().toLowerCase().endsWith(".mdx")) {
                    selectedFile = new File(selectedFile.getPath() + ".mdx");
                }
                try {
                    MdxUtils.saveMdx(snapshotModel, selectedFile);
                } catch (final IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
    }

    private static void jokeButtonActionResponse(MainPanel mainPanel) {
        final StringBuilder sb = new StringBuilder();
        for (final File file : new File(
                "C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\CustomMapData\\LuaFpsMap\\Maps\\MultiplayerFun004")
                .listFiles()) {
            if (!file.getName().toLowerCase().endsWith("_init.txt")) {
                sb.setLength(0);
                try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("BlzSetAbilityActivatedIcon")) {
                            final int startIndex = line.indexOf('"') + 1;
                            final int endIndex = line.lastIndexOf('"');
                            final String dataString = line.substring(startIndex, endIndex);
                            sb.append(dataString);
                        }
                    }
                } catch (final IOException e1) {
                    e1.printStackTrace();
                }
                final String dataString = sb.toString();
                for (int i = 0; (i + 23) < dataString.length(); i += 24) {
                    final Geoset geo = new Geoset();
                    mainPanel.currentMDL().addGeoset(geo);
                    geo.setParentModel(mainPanel.currentMDL());
                    geo.setMaterial(new Material(new Layer("Blend", new Bitmap("textures\\white.blp"))));
                    final String data = dataString.substring(i, i + 24);
                    final int x = Integer.parseInt(data.substring(0, 3));
                    final int y = Integer.parseInt(data.substring(3, 6));
                    final int z = Integer.parseInt(data.substring(6, 9));
                    final int sX = Integer.parseInt(data.substring(9, 10));
                    final int sY = Integer.parseInt(data.substring(10, 11));
                    final int sZ = Integer.parseInt(data.substring(11, 12));
                    final int red = Integer.parseInt(data.substring(12, 15));
                    final int green = Integer.parseInt(data.substring(15, 18));
                    final int blue = Integer.parseInt(data.substring(18, 21));
                    final int alpha = Integer.parseInt(data.substring(21, 24));
                    final GeosetAnim forceGetGeosetAnim = geo.forceGetGeosetAnim();
                    forceGetGeosetAnim.setStaticColor(new Vec3(blue / 255.0, green / 255.0, red / 255.0));
                    forceGetGeosetAnim.setStaticAlpha(alpha / 255.0);
                    System.out.println(x + "," + y + "," + z);

                    final ModelUtils.Mesh mesh = ModelUtils.createBox(new Vec3(x * 10, y * 10, z * 10),
                            new Vec3((x * 10) + (sX * 10), (y * 10) + (sY * 10), (z * 10) + (sZ * 10)), 1, 1,
                            1, geo);
                    geo.getVertices().addAll(mesh.getVertices());
                    geo.getTriangles().addAll(mesh.getTriangles());
                }
            }

        }
        mainPanel.modelStructureChangeListener.geosetsAdded(new ArrayList<>(mainPanel.currentMDL().getGeosets()));
    }

    private static void createTeamColorMenuItems(MainPanel mainPanel) {
        for (int i = 0; i < 25; i++) {
            final String colorNumber = String.format("%2s", i).replace(' ', '0');
            try {
                final String colorName = WEString.getString("WESTRING_UNITCOLOR_" + colorNumber);
                final JMenuItem menuItem = new JMenuItem(colorName, new ImageIcon(BLPHandler.get()
                        .getGameTex("ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp")));
                mainPanel.teamColorMenu.add(menuItem);
                final int teamColorValueNumber = i;
                menuItem.addActionListener(e -> {
                    Material.teamColor = teamColorValueNumber;
                    final ModelPanel modelPanel = mainPanel.currentModelPanel();
                    if (modelPanel != null) {
                        modelPanel.getAnimationViewer().reloadAllTextures();
                        modelPanel.getPerspArea().reloadAllTextures();

                        ModelStructureChangeListenerImplementation.reloadComponentBrowser(mainPanel.geoControlModelData, modelPanel);
                    }
                    mainPanel.profile.getPreferences().setTeamColor(teamColorValueNumber);
                });
            } catch (final Exception ex) {
                // load failed
                break;
            }
        }
    }

    static void traverseAndReset(final DockingWindow window) {
        final int childWindowCount = window.getChildWindowCount();
        for (int i = 0; i < childWindowCount; i++) {
            final DockingWindow childWindow = window.getChildWindow(i);
            traverseAndReset(childWindow);
            if (childWindow instanceof View) {
                final View view = (View) childWindow;
                view.getViewProperties().getViewTitleBarProperties().setVisible(true);
            }
        }
    }

    public static void updateRecent(MainPanel mainPanel) {
        final List<String> recent = SaveProfile.get().getRecent();
        for (final RecentItem recentItem : mainPanel.recentItems) {
            mainPanel.recentMenu.remove(recentItem);
        }
        mainPanel.recentItems.clear();
        for (int i = 0; i < recent.size(); i++) {
            final String fp = recent.get(recent.size() - i - 1);
            if ((mainPanel.recentItems.size() <= i) || (mainPanel.recentItems.get(i).filepath != fp)) {
                // String[] bits = recent.get(i).split("/");

                final RecentItem item = new RecentItem(new File(fp).getName());
                item.filepath = fp;
                mainPanel.recentItems.add(item);
                item.addActionListener(e -> {

                    mainPanel.currentFile = new File(item.filepath);
                    mainPanel.profile.setPath(mainPanel.currentFile.getParent());
                    // frontArea.clearGeosets();
                    // sideArea.clearGeosets();
                    // botArea.clearGeosets();
                    mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                            "Allows the user to control which parts of the model are displayed for editing.");
                    mainPanel.toolsMenu.setEnabled(true);
                    SaveProfile.get().addRecent(mainPanel.currentFile.getPath());
                    updateRecent(mainPanel);
                    MPQBrowserView.loadFile(mainPanel, mainPanel.currentFile);
                });
                mainPanel.recentMenu.add(item, mainPanel.recentMenu.getItemCount() - 2);
            }
        }
    }

    public static boolean closeAll(MainPanel mainPanel) {
        boolean success = true;
        final Iterator<ModelPanel> iterator = mainPanel.modelPanels.iterator();
        boolean closedCurrentPanel = false;
        ModelPanel lastUnclosedModelPanel = null;
        while (iterator.hasNext()) {
            final ModelPanel panel = iterator.next();
            if (success = panel.close(mainPanel)) {
                mainPanel.windowMenu.remove(panel.getMenuItem());
                iterator.remove();
                if (panel == mainPanel.currentModelPanel) {
                    closedCurrentPanel = true;
                }
            } else {
                lastUnclosedModelPanel = panel;
                break;
            }
        }
        if (closedCurrentPanel) {
            MPQBrowserView.setCurrentModel(mainPanel, lastUnclosedModelPanel);
        }
        return success;
    }

    static AbstractAction getSnapVerticiesAction(final MainPanel mainPanel) {
        return new AbstractAction("Snap Vertices") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager()
                            .pushAction(mpanel.getModelEditorManager().getModelEditor().snapSelectedVertices());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getSnapNormalsAction(final MainPanel mainPanel) {
        return new AbstractAction("Snap Normals") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().snapNormals());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getRecalculateNormalsAction(final MainPanel mainPanel) {
        return new AbstractAction("RecalculateNormals") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().recalcNormals());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getRecalculateExtentsAction(final MainPanel mainPanel) {
        return new AbstractAction("RecalculateExtents") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    final JPanel messagePanel = new JPanel(new MigLayout());
                    messagePanel.add(new JLabel("This will calculate the extents of all model components. Proceed?"),
                            "wrap");
                    messagePanel.add(new JLabel("(It may destroy existing extents)"), "wrap");
                    final JRadioButton considerAllBtn = new JRadioButton("Consider all geosets for calculation");
                    final JRadioButton considerCurrentBtn = new JRadioButton(
                            "Consider current editable geosets for calculation");
                    final ButtonGroup buttonGroup = new ButtonGroup();
                    buttonGroup.add(considerAllBtn);
                    buttonGroup.add(considerCurrentBtn);
                    considerAllBtn.setSelected(true);
                    messagePanel.add(considerAllBtn, "wrap");
                    messagePanel.add(considerCurrentBtn, "wrap");
                    final int userChoice = JOptionPane.showConfirmDialog(mainPanel, messagePanel, "Message",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (userChoice == JOptionPane.YES_OPTION) {
                        mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor()
                                .recalcExtents(considerCurrentBtn.isSelected()));
                    }
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getMirrorAxisAction(final MainPanel mainPanel, String s, byte i) {
        return new AbstractAction(s) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    final Vec3 selectionCenter = mpanel.getModelEditorManager().getModelEditor().getSelectionCenter();
                    mpanel.getUndoManager()
                            .pushAction(mpanel.getModelEditorManager().getModelEditor().mirror(i,
                                    mainPanel.mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y,
                                    selectionCenter.z));
                }
                mainPanel.repaint();
            }
        };
    }

    static void updateUIFromProgramPreferences(JCheckBoxMenuItem fetchPortraitsToo, List<ModelPanel> modelPanels, ProgramPreferences prefs, JCheckBoxMenuItem showNormals, JCheckBoxMenuItem showVertexModifyControls, JRadioButtonMenuItem solid, JCheckBoxMenuItem textureModels, JRadioButtonMenuItem wireframe) {
        // prefs.setShowVertexModifierControls(showVertexModifyControls.isSelected());
        showVertexModifyControls.setSelected(prefs.isShowVertexModifierControls());
        // prefs.setTextureModels(textureModels.isSelected());
        textureModels.setSelected(prefs.isTextureModels());
        // prefs.setShowNormals(showNormals.isSelected());
        showNormals.setSelected(prefs.isShowNormals());
        // prefs.setLoadPortraits(true);
        fetchPortraitsToo.setSelected(prefs.isLoadPortraits());
        // prefs.setUseNativeMDXParser(useNativeMDXParser.isSelected());
        switch (prefs.getViewMode()) {
            case 0:
                wireframe.setSelected(true);
                break;
            case 1:
                solid.setSelected(true);
                break;
            default:
                break;
        }
        for (final ModelPanel mpanel : modelPanels) {
            mpanel.getEditorRenderModel()
                    .setSpawnParticles((prefs.getRenderParticles() == null) || prefs.getRenderParticles());
            mpanel.getEditorRenderModel().setAllowInanimateParticles(
                    (prefs.getRenderStaticPoseParticles() == null) || prefs.getRenderStaticPoseParticles());
            mpanel.getAnimationViewer()
                    .setSpawnParticles((prefs.getRenderParticles() == null) || prefs.getRenderParticles());
        }
    }

    private static void dataSourcesChanged(WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier directoryChangeNotifier, List<ModelPanel> modelPanels) {
        for (final ModelPanel modelPanel : modelPanels) {
            final PerspDisplayPanel pdp = modelPanel.getPerspArea();
            pdp.reloadAllTextures();
            modelPanel.getAnimationViewer().reloadAllTextures();
        }
        directoryChangeNotifier.dataSourcesChanged();
    }

    public static MutableObjectData getDoodadData() {
        final War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('d');
        try {
            final CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();
            if (gameDataFileSystem.has("war3map.w3d")) {
                editorData.load(new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream("war3map.w3d")),
                        gameDataFileSystem.has("war3map.wts") ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts"))
                                : null,
                        true);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return new MutableObjectData(MutableObjectData.WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(),
                StandardObjectData.getStandardDoodadMeta(), editorData);
    }

    static AbstractAction getOpenUnitViewerAction(MainPanel mainPanel) {
        return new AbstractAction("Open Unit Browser") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final UnitEditorTree unitEditorTree = MainLayoutCreator.createUnitEditorTree(mainPanel);
                mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
                        new View("Unit Browser",
                                new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                                new JScrollPane(unitEditorTree))));
            }
        };
    }

    static AbstractAction getOpenHiveViewerAction(MainPanel mainPanel) {
        return new AbstractAction("Open Hive Browser") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(BorderLayout.BEFORE_FIRST_LINE, new JLabel(POWERED_BY_HIVE));
                // final JPanel resourceFilters = new JPanel();
                // resourceFilters.setBorder(BorderFactory.createTitledBorder("Resource
                // Filters"));
                // panel.add(BorderLayout.BEFORE_LINE_BEGINS, resourceFilters);
                // resourceFilters.add(new JLabel("Resource Type"));
                // resourceFilters.add(new JComboBox<>(new String[] { "Any" }));
                final JList<String> view = new JList<>(
                        new String[]{"Bongo Bongo (Phantom Shadow Beast)", "Other Model", "Other Model"});
                view.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                                  final int index, final boolean isSelected, final boolean cellHasFocus) {
                        final Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index,
                                isSelected, cellHasFocus);
                        final ImageIcon icon = new ImageIcon(MainPanel.class.getResource("ImageBin/deleteme.png"));
                        setIcon(new ImageIcon(icon.getImage().getScaledInstance(48, 32, Image.SCALE_DEFAULT)));
                        return listCellRendererComponent;
                    }
                });
                panel.add(BorderLayout.BEFORE_LINE_BEGINS, new JScrollPane(view));

                final JPanel tags = new JPanel();
                tags.setBorder(BorderFactory.createTitledBorder("Tags"));
                tags.setLayout(new GridLayout(30, 1));
                tags.add(new JCheckBox("Results must include all selected tags"));
                tags.add(new JSeparator());
                tags.add(new JLabel("Types (Models)"));
                tags.add(new JSeparator());
                tags.add(new JCheckBox("Building"));
                tags.add(new JCheckBox("Doodad"));
                tags.add(new JCheckBox("Item"));
                tags.add(new JCheckBox("User Interface"));
                panel.add(BorderLayout.CENTER, tags);
                // final FloatingWindow floatingWindow =
                // rootWindow.createFloatingWindow(rootWindow.getLocation(),
                // mpqBrowser.getPreferredSize(),
                // new View("MPQ Browser",
                // new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16,
                // Image.SCALE_FAST)),
                // mpqBrowser));
                // floatingWindow.getTopLevelAncestor().setVisible(true);
                mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
                        new View("Hive Browser",
                                new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                                panel)));
            }
        };
    }

    private static void OpenDoodadViewerActionRes(MainPanel mainPanel) {
        final UnitEditorTree unitEditorTree = new UnitEditorTree(getDoodadData(), new DoodadTabTreeBrowserBuilder(),
                MainLayoutCreator.getUnitEditorSettings(), MutableObjectData.WorldEditorDataType.DOODADS);
        unitEditorTree.selectFirstUnit();

        unitEditorTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                try {
                    dodadViewerMouseClickActionRes(e, unitEditorTree, mainPanel);
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        });
        mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
                new View("Doodad Browser",
                        new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                        new JScrollPane(unitEditorTree))));
    }

    private static void dodadViewerMouseClickActionRes(MouseEvent e, UnitEditorTree unitEditorTree, MainPanel mainPanel) {
        if (e.getClickCount() >= 2) {
            final TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
            if (currentUnitTreePath != null) {
                final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath .getLastPathComponent();
                if (o.getUserObject() instanceof MutableObjectData.MutableGameObject) {
                    final MutableObjectData.MutableGameObject obj = (MutableObjectData.MutableGameObject) o.getUserObject();
                    final int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
                    if (numberOfVariations > 1) {
                        for (int i = 0; i < numberOfVariations; i++) {
                            String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl";
                            loadMdxStream(obj, prePath, mainPanel, i == 0);
                        }
                    } else {
                        String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0);
                        loadMdxStream(obj, prePath, mainPanel, true);
                    }
                    mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                            "Allows the user to control which parts of the model are displayed for editing.");
                    mainPanel.toolsMenu.setEnabled(true);
                }
            }
        }
    }

    private static void loadMdxStream(MutableObjectData.MutableGameObject obj, String prePath, MainPanel mainPanel, boolean b) {
        final String path = ImportFileActions.convertPathToMDX(prePath);
        final String portrait = ModelUtils.getPortrait(path);
        final ImageIcon icon = new ImageIcon(IconUtils
                .getIcon(obj, MutableObjectData.WorldEditorDataType.DOODADS)
                .getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        System.out.println(path);
        MainLayoutCreator.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(path), true, b, icon);
        if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
            MainLayoutCreator.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
        }
    }

    static AbstractAction getOpenPreferencesAction(MainPanel mainPanel) {
        return new AbstractAction("Open Preferences") {

            @Override
            public void actionPerformed(final ActionEvent e) {

                final ProgramPreferences programPreferences = new ProgramPreferences();
                programPreferences.loadFrom(mainPanel.prefs);
                final List<DataSourceDescriptor> priorDataSources = SaveProfile.get().getDataSources();
                final ProgramPreferencesPanel programPreferencesPanel = new ProgramPreferencesPanel(programPreferences,
                        priorDataSources);
                // final JFrame frame = new JFrame("Preferences");
                // frame.setIconImage(MainFrame.frame.getIconImage());
                // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                // frame.setContentPane(programPreferencesPanel);
                // frame.pack();
                // frame.setLocationRelativeTo(MainPanel.this);
                // frame.setVisible(true);

                final int ret = JOptionPane.showConfirmDialog(mainPanel, programPreferencesPanel, "Preferences",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (ret == JOptionPane.OK_OPTION) {
                    mainPanel.prefs.loadFrom(programPreferences);
                    final List<DataSourceDescriptor> dataSources = programPreferencesPanel.getDataSources();
                    final boolean changedDataSources = (dataSources != null) && !dataSources.equals(priorDataSources);
                    if (changedDataSources) {
                        SaveProfile.get().setDataSources(dataSources);
                    }
                    SaveProfile.save();
                    if (changedDataSources) {
                        dataSourcesChanged(mainPanel.directoryChangeNotifier, mainPanel.modelPanels);
                    }
                    updateUIFromProgramPreferences(mainPanel.fetchPortraitsToo, mainPanel.modelPanels, mainPanel.prefs, mainPanel.showNormals, mainPanel.showVertexModifyControls, mainPanel.solid, mainPanel.textureModels, mainPanel.wireframe);
                }
            }
        };
    }

    static AbstractAction getViewMatricesAction(final MainPanel mainPanel) {
        return new AbstractAction("View Matrices") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.viewMatrices();
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getInsideOutNormalsAction(final MainPanel mainPanel) {
        return new AbstractAction("Inside Out Normals") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager()
                            .pushAction(mpanel.getModelEditorManager().getModelEditor().flipSelectedNormals());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getInsideOutAction(final MainPanel mainPanel) {
        return new AbstractAction("Inside Out") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().flipSelectedFaces());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getInverseAllUVsAction(MainPanel mainPanel) {
        return new AbstractAction("Swap UVs U for V") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
                    for (final GeosetVertex vertex : geo.getVertices()) {
                        for (final Vec2 tvert : vertex.getTverts()) {
                            final float temp = tvert.x;
                            tvert.x = tvert.y;
                            tvert.y = temp;
                        }
                    }
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getFlipAllUVsVAction(MainPanel mainPanel) {
        return new AbstractAction("Flip All UVs V") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
                    for (final GeosetVertex vertex : geo.getVertices()) {
                        for (final Vec2 tvert : vertex.getTverts()) {
                            tvert.y = 1.0f - tvert.y;
                        }
                    }
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getFlipAllUVsUAction(MainPanel mainPanel) {
        return new AbstractAction("Flip All UVs U") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
                    for (final GeosetVertex vertex : geo.getVertices()) {
                        for (final Vec2 tvert : vertex.getTverts()) {
                            tvert.x = 1.0f - tvert.x;
                        }
                    }
                }
                mainPanel.repaint();
            }
        };
    }

    static void traverseAndReloadData(final DockingWindow window) {
        final int childWindowCount = window.getChildWindowCount();
        for (int i = 0; i < childWindowCount; i++) {
            final DockingWindow childWindow = window.getChildWindow(i);
            traverseAndReloadData(childWindow);
            if (childWindow instanceof View) {
                final View view = (View) childWindow;
                final Component component = view.getComponent();
                if (component instanceof JScrollPane) {
                    final JScrollPane pane = (JScrollPane) component;
                    final Component viewportView = pane.getViewport().getView();
                    if (viewportView instanceof UnitEditorTree) {
                        final UnitEditorTree unitEditorTree = (UnitEditorTree) viewportView;
                        final MutableObjectData.WorldEditorDataType dataType = unitEditorTree.getDataType();
                        if (dataType == MutableObjectData.WorldEditorDataType.UNITS) {
                            System.out.println("saw unit tree");
                            unitEditorTree.setUnitDataAndReloadVerySlowly(MainLayoutCreator.getUnitData());
                        } else if (dataType == MutableObjectData.WorldEditorDataType.DOODADS) {
                            System.out.println("saw doodad tree");
                            unitEditorTree.setUnitDataAndReloadVerySlowly(getDoodadData());
                        }
                    }
                } else if (component instanceof MPQBrowser) {
                    System.out.println("saw mpq tree");
                    final MPQBrowser comp = (MPQBrowser) component;
                    comp.refreshTree();
                }
            }
        }
    }

    static void animFromFileActionRes(MainPanel mainPanel){
        mainPanel.fc.setDialogTitle("Animation Source");
        final EditableModel current = mainPanel.currentMDL();
        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
            mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
        } else if (mainPanel.profile.getPath() != null) {
            mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
        }
        final int returnValue = mainPanel.fc.showOpenDialog(mainPanel);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            mainPanel.currentFile = mainPanel.fc.getSelectedFile();
            mainPanel.profile.setPath(mainPanel.currentFile.getParent());
            final EditableModel animationSourceModel;
            try {
                animationSourceModel = MdxUtils.loadEditable(mainPanel.currentFile);
                addSingleAnimation(mainPanel, current, animationSourceModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mainPanel.fc.setSelectedFile(null);

        ToolBar.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    static void addSingleAnimation(MainPanel mainPanel, final EditableModel current, final EditableModel animationSourceModel) {
        Animation choice = null;
        choice = (Animation) JOptionPane.showInputDialog(mainPanel, "Choose an animation!", "Add Animation",
                JOptionPane.QUESTION_MESSAGE, null, animationSourceModel.getAnims().toArray(),
                animationSourceModel.getAnims().get(0));
        if (choice == null) {
            JOptionPane.showMessageDialog(mainPanel, "Bad choice. No animation added.");
            return;
        }
        final Animation visibilitySource = (Animation) JOptionPane.showInputDialog(mainPanel,
                "Which animation from THIS model to copy visiblity from?", "Add Animation",
                JOptionPane.QUESTION_MESSAGE, null, current.getAnims().toArray(), current.getAnims().get(0));
        if (visibilitySource == null) {
            JOptionPane.showMessageDialog(mainPanel, "No visibility will be copied.");
        }
        final List<Animation> animationsAdded = current.addAnimationsFrom(animationSourceModel,
                Collections.singletonList(choice));
        for (final Animation anim : animationsAdded) {
            current.copyVisibility(visibilitySource, anim);
        }
        JOptionPane.showMessageDialog(mainPanel, "Added " + animationSourceModel.getName() + "'s " + choice.getName()
                + " with " + visibilitySource.getName() + "'s visibility  OK!");
        mainPanel.modelStructureChangeListener.animationsAdded(animationsAdded);
    }

    static void creditsButtonActionRes(String s, String about) {
        final DefaultStyledDocument panel = new DefaultStyledDocument();
        final JTextPane epane = new JTextPane();
        epane.setForeground(Color.BLACK);
        epane.setBackground(Color.WHITE);
        final RTFEditorKit rtfk = new RTFEditorKit();
        try {
            rtfk.read(GameDataFileSystem.getDefault().getResourceAsStream(s), panel, 0);
        } catch (final BadLocationException | IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        epane.setDocument(panel);
        final JFrame frame = new JFrame(about);
        frame.setContentPane(new JScrollPane(epane));
        frame.setSize(650, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // JOptionPane.showMessageDialog(this,new JScrollPane(epane));
    }

    static void animFromObjectActionRes(MainPanel mainPanel){
        mainPanel.fc.setDialogTitle("Animation Source");
        final MutableObjectData.MutableGameObject fetchResult = ImportFileActions.fetchObject(mainPanel);
        if (fetchResult != null) {
            String path = fetchResult.getFieldAsString(UnitFields.MODEL_FILE, 0);
            fetchAndAddSingleAnimation(mainPanel, path);
        }
    }

    static void animFromModelActionRes(MainPanel mainPanel){
        mainPanel.fc.setDialogTitle("Animation Source");
        final ModelOptionPane.ModelElement fetchResult = ImportFileActions.fetchModel(mainPanel);
        if (fetchResult != null) {
            String path = fetchResult.getFilepath();
            fetchAndAddSingleAnimation(mainPanel, path);
        }
    }

    static void animFromUnitActionRes(MainPanel mainPanel){
        mainPanel.fc.setDialogTitle("Animation Source");
        final GameObject fetchResult = ImportFileActions.fetchUnit(mainPanel);
        if (fetchResult != null) {
            String path = fetchResult.getField("file");
            fetchAndAddSingleAnimation(mainPanel, path);
        }
    }

    private static void fetchAndAddSingleAnimation(MainPanel mainPanel, String path) {
        final String filepath = ImportFileActions.convertPathToMDX(path);
        final EditableModel current = mainPanel.currentMDL();
        if (filepath != null) {
            final EditableModel animationSource;
            try {
                animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
                addSingleAnimation(mainPanel, current, animationSource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void riseFallBirthActionRes(MainPanel mainPanel) {
        final ModelView disp = mainPanel.currentModelPanel().getModelViewManager();
        final EditableModel model = disp.getModel();
        final Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);

        final Animation oldBirth = model.findAnimByName("birth");
        final Animation oldDeath = model.findAnimByName("death");

        Animation birth = new Animation("Birth", lastAnim.getEnd() + 300, lastAnim.getEnd() + 2300);
        Animation death = new Animation("Death", birth.getEnd() + 300, birth.getEnd() + 2300);
        final Animation stand = model.findAnimByName("stand");

        final int confirmed = JOptionPane.showConfirmDialog(mainPanel,
                "This will permanently alter model. Are you sure?", "Confirmation",
                JOptionPane.OK_CANCEL_OPTION);
        if (confirmed != JOptionPane.OK_OPTION) {
            return;
        }

        boolean wipeoutOldBirth = false;
        if (oldBirth != null) {
            final String[] choices = {"Ignore", "Delete", "Overwrite"};
            final Object x = JOptionPane.showInputDialog(mainPanel,
                    "Existing birth detected. What should be done with it?", "Question",
                    JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
            if (x == choices[1]) {
                wipeoutOldBirth = true;
            } else if (x == choices[2]) {
                birth = oldBirth;
            } else {
                return;
            }
        }
        boolean wipeoutOldDeath = false;
        if (oldDeath != null) {
            final String[] choices = {"Ignore", "Delete", "Overwrite"};
            final Object x = JOptionPane.showInputDialog(mainPanel,
                    "Existing death detected. What should be done with it?", "Question",
                    JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
            if (x == choices[1]) {
                wipeoutOldDeath = true;
            } else if (x == choices[2]) {
                death = oldDeath;
            } else {
                return;
            }
        }
        if (wipeoutOldBirth) {
            model.remove(oldBirth);
        }
        if (wipeoutOldDeath) {
            model.remove(oldDeath);
        }

        final List<IdObject> roots = new ArrayList<>();
        for (final IdObject obj : model.getIdObjects()) {
            if (obj.getParent() == null) {
                roots.add(obj);
            }
        }
        for (final AnimFlag af : model.getAllAnimFlags()) {
            af.deleteAnim(birth);
            af.deleteAnim(death);
        }
        for (final IdObject obj : roots) {
            if (obj instanceof Bone) {
                final Bone b = (Bone) obj;
                AnimFlag trans = null;
                boolean globalSeq = false;
                for (final AnimFlag af : b.getAnimFlags()) {
                    if (af.getTypeId() == AnimFlag.TRANSLATION) {
                        if (af.hasGlobalSeq()) {
                            globalSeq = true;
                        } else {
                            trans = af;
                        }
                    }
                }
                if (globalSeq) {
                    continue;
                }
                if (trans == null) {
                    final List<Integer> times = new ArrayList<>();
                    final List<Integer> values = new ArrayList<>();
                    trans = new AnimFlag("Translation", times, values);
                    trans.setInterpType(InterpolationType.LINEAR);
                    b.getAnimFlags().add(trans);
                }
                trans.addEntry(birth.getStart(), new Vec3(0, 0, -300));
                trans.addEntry(birth.getEnd(), new Vec3(0, 0, 0));
                trans.addEntry(death.getStart(), new Vec3(0, 0, 0));
                trans.addEntry(death.getEnd(), new Vec3(0, 0, -300));
            }
        }

        // visibility
        for (final VisibilitySource source : model.getAllVisibilitySources()) {
            final AnimFlag dummy = new AnimFlag("dummy");
            final AnimFlag af = source.getVisibilityFlag();
            dummy.copyFrom(af);
            af.deleteAnim(birth);
            af.deleteAnim(death);
            af.copyFrom(dummy, stand.getStart(), stand.getEnd(), birth.getStart(), birth.getEnd());
            af.copyFrom(dummy, stand.getStart(), stand.getEnd(), death.getStart(), death.getEnd());
            af.setEntry(death.getEnd(), 0);
        }

        if (!birth.isNonLooping()) {
            birth.setNonLooping(true);
        }
        if (!death.isNonLooping()) {
            death.setNonLooping(true);
        }

        if (!model.contains(birth)) {
            model.add(birth);
        }
        if (!model.contains(death)) {
            model.add(death);
        }

        JOptionPane.showMessageDialog(mainPanel, "Done!");
    }

    static void simplifyKeyframesActionRes(MainPanel mainPanel) {
        final int x = JOptionPane.showConfirmDialog(mainPanel,
                "This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify keyframes?",
                "Warning: Simplify Keyframes", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.OK_OPTION) {
            simplifyKeyframes(mainPanel);
        }
    }

    static void duplicateSelectionActionRes(MainPanel mainPanel) {
        // final int x = JOptionPane.showConfirmDialog(this,
        // "This is an irreversible process that will split selected
        // vertices into many copies of themself, one for each face, so
        // you can wrap textures and normals in a different
        // way.\n\nContinue?",
        // "Warning"/* : Divide Vertices" */,
        // JOptionPane.OK_CANCEL_OPTION);
        // if (x == JOptionPane.OK_OPTION) {
        final ModelPanel currentModelPanel = mainPanel.currentModelPanel();
        if (currentModelPanel != null) {
            currentModelPanel.getUndoManager().pushAction(currentModelPanel.getModelEditorManager()
                    .getModelEditor().cloneSelectedComponents(mainPanel.namePicker));
        }
        // }
    }

    static void linearizeAnimationsActionRes(MainPanel mainPanel) {
        final int x = JOptionPane.showConfirmDialog(mainPanel,
                "This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify animations?",
                "Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.OK_OPTION) {
            final List<AnimFlag> allAnimFlags = mainPanel.currentMDL().getAllAnimFlags();
            for (final AnimFlag flag : allAnimFlags) {
                flag.linearize();
            }
        }
    }

    static void scaleAnimationsActionRes(MainPanel mainPanel) {
        // if( disp.animpanel == null )
        // {
        // AnimationPanel panel = new UVPanel(disp);
        // disp.setUVPanel(panel);
        // panel.showFrame();
        // }
        // else if(!disp.uvpanel.frameVisible() )
        // {
        // disp.uvpanel.showFrame();
        // }
        final ChangeAnimationLengthFrame aFrame = new ChangeAnimationLengthFrame(mainPanel.currentModelPanel(), () -> mainPanel.timeSliderPanel.revalidateKeyframeDisplay());
        aFrame.setVisible(true);
    }

    static void exportTexturesActionRes(MainPanel mainPanel) {
        final DefaultListModel<Material> materials = new DefaultListModel<>();
        for (int i = 0; i < mainPanel.currentMDL().getMaterials().size(); i++) {
            final Material mat = mainPanel.currentMDL().getMaterials().get(i);
            materials.addElement(mat);
        }
        for (final ParticleEmitter2 emitter2 : mainPanel.currentMDL().sortedIdObjects(ParticleEmitter2.class)) {
            final Material dummyMaterial = new Material(
                    new Layer("Blend", mainPanel.currentMDL().getTexture(emitter2.getTextureID())));
        }

        final JList<Material> materialsList = new JList<>(materials);
        materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialsList.setCellRenderer(new MaterialListRenderer(mainPanel.currentMDL()));
        JOptionPane.showMessageDialog(mainPanel, new JScrollPane(materialsList));

        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
            final EditableModel current = mainPanel.currentMDL();
            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
            } else if (mainPanel.profile.getPath() != null) {
                mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
            }
        }
        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
            mainPanel.exportTextureDialog.setSelectedFile(new File(mainPanel.exportTextureDialog.getCurrentDirectory()
                    + File.separator + materialsList.getSelectedValue().getName()));
        }

        final int x = mainPanel.exportTextureDialog.showSaveDialog(mainPanel);
        if (x == JFileChooser.APPROVE_OPTION) {
            final File file = mainPanel.exportTextureDialog.getSelectedFile();
            if (file != null) {
                try {
                    if (file.getName().lastIndexOf('.') >= 0) {
                        BufferedImage bufferedImage = materialsList.getSelectedValue()
                                .getBufferedImage(mainPanel.currentMDL().getWrappedDataSource());
                        String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
                                .toUpperCase();
                        if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
                                || fileExtension.equals("JPEG")) {
                            JOptionPane.showMessageDialog(mainPanel,
                                    "Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
                            bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
                        }
                        if (fileExtension.equals("BLP")) {
                            fileExtension = "blp";
                        }
                        final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
                        if (!write) {
                            JOptionPane.showMessageDialog(mainPanel, "File type unknown or unavailable");
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, "No file type was specified");
                    }
                } catch (final Exception e1) {
                    ExceptionPopup.display(e1);
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No output file was specified");
            }
        }
    }

    static void editUVsActionRes(MainPanel mainPanel) {
        final ModelPanel disp = mainPanel.currentModelPanel();
        if (disp.getEditUVPanel() == null) {
            final UVPanel panel = new UVPanel(disp, mainPanel.prefs, mainPanel.modelStructureChangeListener);
            disp.setEditUVPanel(panel);

            panel.initViewport();
            final FloatingWindow floatingWindow = mainPanel.rootWindow.createFloatingWindow(
                    new Point(mainPanel.getX() + (mainPanel.getWidth() / 2), mainPanel.getY() + (mainPanel.getHeight() / 2)), panel.getSize(),
                    panel.getView());
            panel.init();
            floatingWindow.getTopLevelAncestor().setVisible(true);
            panel.packFrame();
        } else if (!disp.getEditUVPanel().frameVisible()) {
            final FloatingWindow floatingWindow = mainPanel.rootWindow.createFloatingWindow(
                    new Point(mainPanel.getX() + (mainPanel.getWidth() / 2), mainPanel.getY() + (mainPanel.getHeight() / 2)),
                    disp.getEditUVPanel().getSize(), disp.getEditUVPanel().getView());
            floatingWindow.getTopLevelAncestor().setVisible(true);
        }
    }

    static void showVertexModifyControlsActionRes(List<ModelPanel> modelPanels, ProgramPreferences prefs, JCheckBoxMenuItem showVertexModifyControls) {
        final boolean selected = showVertexModifyControls.isSelected();
        prefs.setShowVertexModifierControls(selected);
        // SaveProfile.get().setShowViewportButtons(selected);
        for (final ModelPanel panel : modelPanels) {
            panel.getFrontArea().setControlsVisible(selected);
            panel.getBotArea().setControlsVisible(selected);
            panel.getSideArea().setControlsVisible(selected);
            final UVPanel uvPanel = panel.getEditUVPanel();
            if (uvPanel != null) {
                uvPanel.setControlsVisible(selected);
            }
        }
    }

    static void nullmodelButtonActionRes(MainPanel mainPanel) {
        nullmodelFile(mainPanel);
        ToolBar.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    static void simplifyKeyframes(MainPanel mainPanel) {
        final EditableModel currentMDL = mainPanel.currentMDL();
        currentMDL.simplifyKeyframes();
    }

    public static String incName(final String name) {
        String output = name;

        int depth = 1;
        boolean continueLoop = true;
        while (continueLoop) {
            char c = '0';
            try {
                c = output.charAt(output.length() - depth);
            } catch (final IndexOutOfBoundsException e) {
                // c remains '0'
                continueLoop = false;
            }
            for (char n = '0'; (n < '9') && continueLoop; n++) {
                // JOptionPane.showMessageDialog(null,"checking "+c+" against
                // "+n);
                if (c == n) {
                    char x = c;
                    x++;
                    output = output.substring(0, output.length() - depth) + x
                            + output.substring((output.length() - depth) + 1);
                    continueLoop = false;
                }
            }
            if (c == '9') {
                output = output.substring(0, output.length() - depth) + 0
                        + output.substring((output.length() - depth) + 1);
            } else if (continueLoop) {
                output = output.substring(0, (output.length() - depth) + 1) + 1
                        + output.substring((output.length() - depth) + 1);
                continueLoop = false;
            }
            depth++;
        }
        if (output == null) {
            output = "name error";
        } else if (output.equals(name)) {
            output = output + "_edit";
        }

        return output;
    }

    public static void nullmodelFile(MainPanel mainPanel) {
        final EditableModel currentMDL = mainPanel.currentMDL();
        if (currentMDL != null) {
            final EditableModel newModel = new EditableModel();
            newModel.copyHeaders(currentMDL);
            if (newModel.getFileRef() == null) {
                newModel.setFileRef(
                        new File(System.getProperty("java.io.tmpdir") + "MatrixEaterExtract/matrixeater_anonymousMDL",
                                "" + (int) (Math.random() * Integer.MAX_VALUE) + ".mdl"));
            }
            while (newModel.getFile().exists()) {
                newModel.setFileRef(
                        new File(currentMDL.getFile().getParent() + "/" + incName(newModel.getName()) + ".mdl"));
            }
            mainPanel.importPanel = new ImportPanel(newModel, EditableModel.deepClone(currentMDL, "CurrentModel"));

            final Thread watcher = new Thread(() -> {
                while (mainPanel.importPanel.getParentFrame().isVisible()
                        && (!mainPanel.importPanel.importStarted()
                        || mainPanel.importPanel.importEnded())) {
                    try {
                        Thread.sleep(1);
                    } catch (final Exception e) {
                        ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
                    }
                }
                // if( !importPanel.getParentFrame().isVisible() &&
                // !importPanel.importEnded() )
                // JOptionPane.showMessageDialog(null,"bad voodoo
                // "+importPanel.importSuccessful());
                // else
                // JOptionPane.showMessageDialog(null,"good voodoo
                // "+importPanel.importSuccessful());
                // if( importPanel.importSuccessful() )
                // {
                // newModel.saveFile();
                // loadFile(newModel.getFile());
                // }

                if (mainPanel.importPanel.importStarted()) {
                    while (!mainPanel.importPanel.importEnded()) {
                        try {
                            Thread.sleep(1);
                        } catch (final Exception e) {
                            ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
                        }
                    }

                    if (mainPanel.importPanel.importSuccessful()) {
                        try {
                            MdxUtils.saveMdx(newModel, newModel.getFile());
                        } catch (final IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        MPQBrowserView.loadFile(mainPanel, newModel.getFile());
                    }
                }
            });
            watcher.start();
        }
    }

    static void clearRecentActionRes(MainPanel mainPanel) {
        final int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
                "Are you sure you want to clear the Recent history?", "Confirm Clear",
                JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            SaveProfile.get().clearRecent();
            updateRecent(mainPanel);
        }
    }

    static void mergeGeosetActionRes(MainPanel mainPanel) throws IOException {
        mainPanel.fc.setDialogTitle("Merge Single Geoset (Oinker-based)");
        final EditableModel current = mainPanel.currentMDL();
        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
            mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
        } else if (mainPanel.profile.getPath() != null) {
            mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
        }
        final int returnValue = mainPanel.fc.showOpenDialog(mainPanel);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            mainPanel.currentFile = mainPanel.fc.getSelectedFile();
            final EditableModel geoSource = MdxUtils.loadEditable(mainPanel.currentFile);
            mainPanel.profile.setPath(mainPanel.currentFile.getParent());
            boolean going = true;
            Geoset host = null;
            while (going) {
                final String s = JOptionPane.showInputDialog(mainPanel,
                        "Geoset into which to Import: (1 to " + current.getGeosetsSize() + ")");
                try {
                    final int x = Integer.parseInt(s);
                    if ((x >= 1) && (x <= current.getGeosetsSize())) {
                        host = current.getGeoset(x - 1);
                        going = false;
                    }
                } catch (final NumberFormatException ignored) {

                }
            }
            Geoset newGeoset = null;
            going = true;
            while (going) {
                final String s = JOptionPane.showInputDialog(mainPanel,
                        "Geoset to Import: (1 to " + geoSource.getGeosetsSize() + ")");
                try {
                    final int x = Integer.parseInt(s);
                    if (x <= geoSource.getGeosetsSize()) {
                        newGeoset = geoSource.getGeoset(x - 1);
                        going = false;
                    }
                } catch (final NumberFormatException ignored) {

                }
            }
            newGeoset.updateToObjects(current);
            System.out.println("putting " + newGeoset.numUVLayers() + " into a nice " + host.numUVLayers());
            for (int i = 0; i < newGeoset.numVerteces(); i++) {
                final GeosetVertex ver = newGeoset.getVertex(i);
                host.add(ver);
                ver.setGeoset(host);// geoset = host;
                // for( int z = 0; z < host.n.numUVLayers(); z++ )
                // {
                // host.getUVLayer(z).addTVertex(newGeoset.getVertex(i).getTVertex(z));
                // }
            }
            for (int i = 0; i < newGeoset.numTriangles(); i++) {
                final Triangle tri = newGeoset.getTriangle(i);
                host.add(tri);
                tri.setGeoRef(host);
            }
        }

        mainPanel.fc.setSelectedFile(null);
    }

    static void importButtonSActionRes() {
        final JFrame frame = new JFrame("Animation Transferer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new AnimationTransfer(frame));
        frame.setIconImage(AnimIcon.getImage());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static void fetchObjectActionRes(MainPanel mainPanel) {
        final MutableObjectData.MutableGameObject objectFetched = ImportFileActions.fetchObject(mainPanel);
        if (objectFetched != null) {
            final String filepath = ImportFileActions.convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
            if (filepath != null) {
                MainLayoutCreator.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true,
                        new ImageIcon(BLPHandler.get()
                                .getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
                                .getScaledInstance(16, 16, Image.SCALE_FAST)));
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MainLayoutCreator.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
                            new ImageIcon(BLPHandler.get()
                                    .getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
                                    .getScaledInstance(16, 16, Image.SCALE_FAST)));
                }
                mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                        "Allows the user to control which parts of the model are displayed for editing.");
                mainPanel.toolsMenu.setEnabled(true);
            }
        }
    }

    static void fetchModelActionRes(MainPanel mainPanel) {
        final ModelOptionPane.ModelElement model = ImportFileActions.fetchModel(mainPanel);
        if (model != null) {
            final String filepath = ImportFileActions.convertPathToMDX(model.getFilepath());
            if (filepath != null) {

                final ImageIcon icon = model.hasCachedIconPath() ? new ImageIcon(BLPHandler.get()
                        .getGameTex(model.getCachedIconPath()).getScaledInstance(16, 16, Image.SCALE_FAST))
                        : MPQBrowserView.MDLIcon;
                MainLayoutCreator.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true, icon);
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MainLayoutCreator.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
                }
                mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                        "Allows the user to control which parts of the model are displayed for editing.");
                mainPanel.toolsMenu.setEnabled(true);
            }
        }
    }

    static void fetchUnitActionRes(MainPanel mainPanel) {
        final GameObject unitFetched = ImportFileActions.fetchUnit(mainPanel);
        if (unitFetched != null) {
            final String filepath = ImportFileActions.convertPathToMDX(unitFetched.getField("file"));
            if (filepath != null) {
                MainLayoutCreator.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true,
                        unitFetched.getScaledIcon(0.25f));
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MainLayoutCreator.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
                            unitFetched.getScaledIcon(0.25f));
                }
                mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                        "Allows the user to control which parts of the model are displayed for editing.");
                mainPanel.toolsMenu.setEnabled(true);
            }
        }
    }

    static void closePanelActionRes(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        final int oldIndex = mainPanel.modelPanels.indexOf(modelPanel);
        if (modelPanel != null) {
            if (modelPanel.close(mainPanel)) {
                mainPanel.modelPanels.remove(modelPanel);
                mainPanel.windowMenu.remove(modelPanel.getMenuItem());
                if (mainPanel.modelPanels.size() > 0) {
                    final int newIndex = Math.min(mainPanel.modelPanels.size() - 1, oldIndex);
                    MPQBrowserView.setCurrentModel(mainPanel, mainPanel.modelPanels.get(newIndex));
                } else {
                    // TODO remove from notifiers to fix leaks
                    MPQBrowserView.setCurrentModel(mainPanel, null);
                }
            }
        }
    }

    static void onClickSaveAs(MainPanel mainPanel) {
        final EditableModel current = mainPanel.currentMDL();
        onClickSaveAs(mainPanel, current);
    }

    static void onClickSaveAs(MainPanel mainPanel, final EditableModel current) {
        try {
            mainPanel.fc.setDialogTitle("Save as");
            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
                mainPanel.fc.setSelectedFile(current.getFile());
            } else if (mainPanel.profile.getPath() != null) {
                mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
            }
            final int returnValue = mainPanel.fc.showSaveDialog(mainPanel);
            File temp = mainPanel.fc.getSelectedFile();
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (temp != null) {
                    final FileFilter ff = mainPanel.fc.getFileFilter();
                    final String ext = ff.accept(new File("junk.mdl")) ? ".mdl" : ".mdx";
                    if (ff.accept(new File("junk.obj"))) {
                        throw new UnsupportedOperationException("OBJ saving has not been coded yet.");
                    }
                    final String name = temp.getName();
                    if (name.lastIndexOf('.') != -1) {
                        if (!name.substring(name.lastIndexOf('.')).equals(ext)) {
                            temp = new File(
                                    temp.getAbsolutePath().substring(0, temp.getAbsolutePath().lastIndexOf('.')) + ext);
                        }
                    } else {
                        temp = new File(temp.getAbsolutePath() + ext);
                    }
                    mainPanel.currentFile = temp;
                    if (temp.exists()) {
                        final Object[] options = {"Overwrite", "Cancel"};
                        final int n = JOptionPane.showOptionDialog(MainFrame.frame, "Selected file already exists.",
                                "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
                                options[1]);
                        if (n == 1) {
                            mainPanel.fc.setSelectedFile(null);
                            return;
                        }
                    }
                    mainPanel.profile.setPath(mainPanel.currentFile.getParent());

                    final MdlxModel mdlx = mainPanel.currentMDL().toMdlx();
                    final FileOutputStream stream = new FileOutputStream(mainPanel.currentFile);

                    if (ext.equals(".mdl")) {
                        MdxUtils.saveMdl(mdlx, stream);
                    } else {
                        MdxUtils.saveMdx(mdlx, stream);
                    }
                    mainPanel.currentMDL().setFileRef(mainPanel.currentFile);
                    // currentMDLDisp().resetBeenSaved();
                    // TODO reset been saved
                    mainPanel.currentModelPanel().getMenuItem().setName(mainPanel.currentFile.getName().split("\\.")[0]);
                    mainPanel.currentModelPanel().getMenuItem().setToolTipText(mainPanel.currentFile.getPath());
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "You tried to save, but you somehow didn't select a file.\nThat is bad.");
                }
            }
            mainPanel.fc.setSelectedFile(null);
            return;
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
        }
        ToolBar.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    public static boolean closeOthers(MainPanel mainPanel, final ModelPanel panelToKeepOpen) {
        boolean success = true;
        final Iterator<ModelPanel> iterator = mainPanel.modelPanels.iterator();
        boolean closedCurrentPanel = false;
        ModelPanel lastUnclosedModelPanel = null;
        while (iterator.hasNext()) {
            final ModelPanel panel = iterator.next();
            if (panel == panelToKeepOpen) {
                lastUnclosedModelPanel = panel;
                continue;
            }
            if (success = panel.close(mainPanel)) {
                mainPanel.windowMenu.remove(panel.getMenuItem());
                iterator.remove();
                if (panel == mainPanel.currentModelPanel) {
                    closedCurrentPanel = true;
                }
            } else {
                lastUnclosedModelPanel = panel;
                break;
            }
        }
        if (closedCurrentPanel) {
            MPQBrowserView.setCurrentModel(mainPanel, lastUnclosedModelPanel);
        }
        return success;
    }

    static class RecentItem extends JMenuItem {
        public RecentItem(final String what) {
            super(what);
        }

        String filepath;
    }

    static class UndoMenuItem extends JMenuItem {
        private MainPanel mainPanel;

        public UndoMenuItem(MainPanel mainPanel, final String text) {
            super(text);
            this.mainPanel = mainPanel;
        }

        @Override
        public String getText() {
            if (funcEnabled()) {
                return "Undo " + mainPanel.currentModelPanel().getUndoManager().getUndoText();// +"
                // Ctrl+Z";
            } else {
                return "Can't undo";// +" Ctrl+Z";
            }
        }

        public boolean funcEnabled() {
            try {
                return !mainPanel.currentModelPanel().getUndoManager().isUndoListEmpty();
            } catch (final NullPointerException e) {
                return false;
            }
        }
    }

    static class RedoMenuItem extends JMenuItem {
        private MainPanel mainPanel;

        public RedoMenuItem(MainPanel mainPanel, final String text) {
            super(text);
            this.mainPanel = mainPanel;
        }

        @Override
        public String getText() {
            if (funcEnabled()) {
                return "Redo " + mainPanel.currentModelPanel().getUndoManager().getRedoText();// +"
                // Ctrl+Y";
            } else {
                return "Can't redo";// +" Ctrl+Y";
            }
        }

        public boolean funcEnabled() {
            try {
                return !mainPanel.currentModelPanel().getUndoManager().isRedoListEmpty();
            } catch (final NullPointerException e) {
                return false;
            }
        }
    }

}
