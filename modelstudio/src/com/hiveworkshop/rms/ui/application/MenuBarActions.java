package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
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
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.scripts.ChangeAnimationLengthFrame;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DoodadTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.gui.modeledit.ImportPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ProgramPreferencesPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import com.hiveworkshop.rms.util.War3ID;
import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MenuBarActions {
    static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");

    static void combineAnimsActionRes(MainPanel mainPanel) {
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

    static void exportAnimatedFramePNGActionRes(MainPanel mainPanel) {
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

    static void exportAnimatedToStaticMeshActionRes(MainPanel mainPanel) {
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

    static void jokeButtonActionResponse(MainPanel mainPanel) {
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

    static void OpenDoodadViewerActionRes(MainPanel mainPanel) {
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
                            MPQBrowserView.loadMdxStream(obj, prePath, mainPanel, i == 0);
                        }
                    } else {
                        String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0);
                        MPQBrowserView.loadMdxStream(obj, prePath, mainPanel, true);
                    }
                    mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                            "Allows the user to control which parts of the model are displayed for editing.");
                    mainPanel.toolsMenu.setEnabled(true);
                }
            }
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
            MenuBar.updateRecent(mainPanel);
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

    static void fetchObjectActionRes(MainPanel mainPanel) {
        final MutableObjectData.MutableGameObject objectFetched = ImportFileActions.fetchObject(mainPanel);
        if (objectFetched != null) {
            final String filepath = ImportFileActions.convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
            if (filepath != null) {
                MPQBrowserView.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true,
                        new ImageIcon(BLPHandler.get()
                                .getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
                                .getScaledInstance(16, 16, Image.SCALE_FAST)));
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MPQBrowserView.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
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
                MPQBrowserView.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true, icon);
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MPQBrowserView.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
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
                MPQBrowserView.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true,
                        unitFetched.getScaledIcon(0.25f));
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MPQBrowserView.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
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
}
