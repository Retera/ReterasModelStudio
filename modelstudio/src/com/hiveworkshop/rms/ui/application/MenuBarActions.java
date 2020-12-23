package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.viewer.AnimationViewer;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ProgramPreferencesPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;
import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class MenuBarActions {
    static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");

    static void updateUIFromProgramPreferences(JCheckBoxMenuItem fetchPortraitsToo, List<ModelPanel> modelPanels, ProgramPreferences prefs, JCheckBoxMenuItem showNormals, JCheckBoxMenuItem showVertexModifyControls, JRadioButtonMenuItem solid, JCheckBoxMenuItem textureModels, JRadioButtonMenuItem wireframe) {
        showVertexModifyControls.setSelected(prefs.isShowVertexModifierControls());
        textureModels.setSelected(prefs.isTextureModels());
        showNormals.setSelected(prefs.isShowNormals());
        fetchPortraitsToo.setSelected(prefs.isLoadPortraits());
        switch (prefs.getViewMode()) {
            case 0 -> wireframe.setSelected(true);
            case 1 -> solid.setSelected(true);
        }
        for (final ModelPanel mpanel : modelPanels) {
            mpanel.getEditorRenderModel().setSpawnParticles((prefs.getRenderParticles() == null) || prefs.getRenderParticles());
            mpanel.getEditorRenderModel().setAllowInanimateParticles((prefs.getRenderStaticPoseParticles() == null) || prefs.getRenderStaticPoseParticles());
            mpanel.getAnimationViewer().setSpawnParticles((prefs.getRenderParticles() == null) || prefs.getRenderParticles());
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
                        gameDataFileSystem.has("war3map.wts")
                                ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts")) : null,
                        true);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return new MutableObjectData(MutableObjectData.WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(),
                StandardObjectData.getStandardDoodadMeta(), editorData);
    }

    static void openUnitViewer(MainPanel mainPanel) {
        final UnitEditorTree unitEditorTree = MainLayoutCreator.createUnitEditorTree(mainPanel);
        mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
                new View("Unit Browser",
                        new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                        new JScrollPane(unitEditorTree))));
    }

    static void openHiveViewer(MainPanel mainPanel) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(BorderLayout.BEFORE_FIRST_LINE, new JLabel(POWERED_BY_HIVE));

        final JList<String> view = new JList<>(new String[] {"Bongo Bongo (Phantom Shadow Beast)", "Other Model", "Other Model"});
        view.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                          final int index, final boolean isSelected, final boolean cellHasFocus) {
                final Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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

        mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
                new View("Hive Browser",
                        new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                        panel)));
    }

    static void openPreferences(MainPanel mainPanel) {
        final ProgramPreferences programPreferences = new ProgramPreferences();
        programPreferences.loadFrom(mainPanel.prefs);
        final List<DataSourceDescriptor> priorDataSources = SaveProfile.get().getDataSources();
        final ProgramPreferencesPanel programPreferencesPanel = new ProgramPreferencesPanel(programPreferences, priorDataSources);

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

    static void createAndShowRtfPanel(String filePath, String title) {
        final DefaultStyledDocument document = new DefaultStyledDocument();
        final JTextPane textPane = new JTextPane();
        textPane.setForeground(Color.BLACK);
        textPane.setBackground(Color.WHITE);
        final RTFEditorKit rtfk = new RTFEditorKit();
        try {
            rtfk.read(GameDataFileSystem.getDefault().getResourceAsStream(filePath), document, 0);
        } catch (final BadLocationException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        textPane.setDocument(document);
        final JFrame frame = new JFrame(title);
        frame.setContentPane(new JScrollPane(textPane));
        frame.setSize(650, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // JOptionPane.showMessageDialog(this,new JScrollPane(textPane));
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

    static void clearRecent(MainPanel mainPanel) {
        final int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
                "Are you sure you want to clear the Recent history?", "Confirm Clear",
                JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            SaveProfile.get().clearRecent();
            MenuBar.updateRecent(mainPanel);
        }
    }

    static void closePanel(MainPanel mainPanel) {
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

    static void newModel(MainPanel mainPanel) {
        final JPanel newModelPanel = new JPanel();
        newModelPanel.setLayout(new MigLayout());
        newModelPanel.add(new JLabel("Model Name: "), "cell 0 0");
        final JTextField newModelNameField = new JTextField("MrNew", 25);
        newModelPanel.add(newModelNameField, "cell 1 0");
        final JRadioButton createEmptyButton = new JRadioButton("Create Empty", true);
        newModelPanel.add(createEmptyButton, "cell 0 1");
        final JRadioButton createPlaneButton = new JRadioButton("Create Plane");
        newModelPanel.add(createPlaneButton, "cell 0 2");
        final JRadioButton createBoxButton = new JRadioButton("Create Box");
        newModelPanel.add(createBoxButton, "cell 0 3");
        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(createBoxButton);
        buttonGroup.add(createPlaneButton);
        buttonGroup.add(createEmptyButton);

        final int userDialogResult = JOptionPane.showConfirmDialog(mainPanel, newModelPanel, "New Model",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (userDialogResult == JOptionPane.OK_OPTION) {
            final EditableModel mdl = new EditableModel(newModelNameField.getText());
            if (createBoxButton.isSelected()) {
                final SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
                final JSpinner spinner = new JSpinner(sModel);
                final int userChoice = JOptionPane.showConfirmDialog(mainPanel, spinner, "Box: Choose Segments",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (userChoice != JOptionPane.OK_OPTION) {
                    return;
                }
                ModelUtils.createBox(mdl, new Vec3(64, 64, 128), new Vec3(-64, -64, 0), ((Number) spinner.getValue()).intValue());
            } else if (createPlaneButton.isSelected()) {
                final SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
                final JSpinner spinner = new JSpinner(sModel);
                final int userChoice = JOptionPane.showConfirmDialog(mainPanel, spinner, "Plane: Choose Segments",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (userChoice != JOptionPane.OK_OPTION) {
                    return;
                }
                ModelUtils.createGroundPlane(mdl, new Vec3(64, 64, 0), new Vec3(-64, -64, 0),
                        ((Number) spinner.getValue()).intValue());
            }
            final ModelPanel temp = new ModelPanel(mainPanel, mdl, mainPanel.prefs, mainPanel, mainPanel.selectionItemTypeGroup,
                    mainPanel.selectionModeGroup, mainPanel.modelStructureChangeListener, mainPanel.coordDisplayListener,
                    mainPanel.viewportTransferHandler, mainPanel.activeViewportWatcher, RMSIcons.MDLIcon, false,
                    mainPanel.textureExporter);
            MPQBrowserView.loadModel(mainPanel, true, true, temp);
        }

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
                    final String name = temp.getName();
                    String ext = ".mdx";
                    if (name.lastIndexOf('.') != -1 &&
                            ff.accept(new File("Junk" + name.substring(name.lastIndexOf('.'))))) {
                        ext = name.substring(name.lastIndexOf('.'));
                    } else {
                        String[] exts = {".mdx", ".mdl", ".obj", ".fbx"};
                        //Don't think texture extensions should be here
                        //, ".blp", ".dds", ".tga", ".png"
                        Supplier<String> a = () -> {
                            for (String e : exts)
                                if (ff.accept(new File("Junk" + e)))
                                    return e;
                            //This should never happen
                            throw new UnsupportedOperationException("Invalid model extension was chosen.");};
                        ext = a.get();
                    }
                    if (ext.equals(".obj") || ext.equals(".fbx"))
                        throw new UnsupportedOperationException(ext + " saving has not been coded yet.");
                    String filepathBase = temp.getAbsolutePath();
                    mainPanel.currentFile = new File(
                            (filepathBase.lastIndexOf('.') == -1
                                    ? filepathBase : filepathBase.substring(0, filepathBase.lastIndexOf('.')))
                                    + ext);

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
        refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    static void onClickSave(MainPanel mainPanel) {
        try {
            if (mainPanel.currentMDL() != null) {
                MdxUtils.saveMdx(mainPanel.currentMDL(), mainPanel.currentMDL().getFile());
                mainPanel.profile.setPath(mainPanel.currentMDL().getFile().getParent());
                // currentMDLDisp().resetBeenSaved();
                // TODO reset been saved
            }
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
        }
        refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    static void onClickOpen(MainPanel mainPanel) {
        mainPanel.fc.setDialogTitle("Open");
        final EditableModel current = mainPanel.currentMDL();
        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
            mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
        } else if (mainPanel.profile.getPath() != null) {
            mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
        }

        final int returnValue = mainPanel.fc.showOpenDialog(mainPanel);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            openFile(mainPanel, mainPanel.fc.getSelectedFile());
        }

        mainPanel.fc.setSelectedFile(null);
    }

    public static void openFile(MainPanel mainPanel, final File f) {
        mainPanel.currentFile = f;
        mainPanel.profile.setPath(mainPanel.currentFile.getParent());
        // frontArea.clearGeosets();
        // sideArea.clearGeosets();
        // botArea.clearGeosets();
        mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                "Allows the user to control which parts of the model are displayed for editing.");
        mainPanel.toolsMenu.setEnabled(true);
        SaveProfile.get().addRecent(mainPanel.currentFile.getPath());
        MenuBar.updateRecent(mainPanel);
        MPQBrowserView.loadFile(mainPanel, mainPanel.currentFile);
    }

    public static void refreshController(JScrollPane geoControl, JScrollPane geoControlModelData) {
        if (geoControl != null) {
            geoControl.repaint();
        }
        if (geoControlModelData != null) {
            geoControlModelData.repaint();
        }
    }

    static View testItemResponse(MainPanel mainPanel) {
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
    }
}
