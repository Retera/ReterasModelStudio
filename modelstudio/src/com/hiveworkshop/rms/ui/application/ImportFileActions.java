package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterUnitEditorModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPane;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImportFileActions {
    public static void importFile(MainPanel mainPanel, final File f){
        final EditableModel currentModel = mainPanel.currentMDL();
        if (currentModel != null) {
            try {
                importFile(mainPanel, MdxUtils.loadEditable(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void importFile(final MainPanel mainPanel, final EditableModel model) {
        final EditableModel currentModel = mainPanel.currentMDL();
        if (currentModel != null) {
            mainPanel.importPanel = new ImportPanel(currentModel, model);
            mainPanel.importPanel.setCallback(new ModelStructureChangeListenerImplementation(mainPanel, new ModelStructureChangeListenerImplementation.ModelReference() {
                private final EditableModel model = mainPanel.currentMDL();

                @Override
                public EditableModel getModel() {
                    return model;
                }
            }));

        }
    }

    static void importButtonActionRes(MainPanel mainPanel) {
        FileDialog fileDialog = new FileDialog(mainPanel);

        final EditableModel model = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
        if (model != null) {
            importFile(mainPanel, model);
        }
        MenuBarActions.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    static void importFromWorkspaceActionRes(MainPanel mainPanel) {
        final List<EditableModel> optionNames = new ArrayList<>();
        for (final ModelPanel modelPanel : mainPanel.modelPanels) {
            final EditableModel model = modelPanel.getModel();
            optionNames.add(model);
        }
        final EditableModel choice = (EditableModel) JOptionPane.showInputDialog(mainPanel,
                "Choose a workspace item to import data from:", "Import from Workspace",
                JOptionPane.OK_CANCEL_OPTION, null, optionNames.toArray(), optionNames.get(0));
        if (choice != null) {
            importFile(mainPanel, EditableModel.deepClone(choice, choice.getHeaderName()));
        }
        MenuBarActions.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    public static String convertPathToMDX(String filepath) {
        if (filepath.endsWith(".mdl")) {
            filepath = filepath.replace(".mdl", ".mdx");
        } else if (!filepath.endsWith(".mdx")) {
            filepath = filepath.concat(".mdx");
        }
        return filepath;
    }

    static void importMdxObject(MainPanel mainPanel, String path){
        final String filepath = convertPathToMDX(path);
        final EditableModel current = mainPanel.currentMDL();
        if (filepath != null) {
            final File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
            importFile(mainPanel, animationSource);
        }
        MenuBarActions.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    static MutableObjectData.MutableGameObject fetchObject(MainPanel mainPanel) {
        final BetterUnitEditorModelSelector selector = new BetterUnitEditorModelSelector(MainLayoutCreator.getUnitData(),
                MainLayoutCreator.getUnitEditorSettings());
        final int x = JOptionPane.showConfirmDialog(mainPanel, selector,
                "Object Editor - Select Unit",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        final MutableObjectData.MutableGameObject choice = selector.getSelection();
        if ((choice == null) || (x != JOptionPane.OK_OPTION)) {
            return null;
        }

        String filepath = choice.getFieldAsString(UnitFields.MODEL_FILE, 0);

        if (isValidFilepath(filepath)) return choice;
        return null;
    }

    static void importGameObjectActionRes(MainPanel mainPanel){
        final MutableObjectData.MutableGameObject fetchObjectResult = fetchObject(mainPanel);
        if (fetchObjectResult != null) {
            String path = fetchObjectResult.getFieldAsString(UnitFields.MODEL_FILE, 0);

            importMdxObject(mainPanel, path);
        }
    }

    static ModelOptionPane.ModelElement fetchModel(MainPanel mainPanel) {
        final ModelOptionPane.ModelElement model = ModelOptionPane.showAndLogIcon(mainPanel);
        if (model == null) {
            return null;
        }
        String filepath = model.getFilepath();
        if (isValidFilepath(filepath)) return model;
        return null;
    }

    static void importGameModelActionRes(MainPanel mainPanel){
        final ModelOptionPane.ModelElement fetchModelResult = fetchModel(mainPanel);
        if (fetchModelResult != null) {
            String path = fetchModelResult.getFilepath();
            importMdxObject(mainPanel, path);
        }
    }

    static GameObject fetchUnit(MainPanel mainPanel) {
        final GameObject choice = UnitOptionPane.show(mainPanel);

        if (choice != null) {
            String filepath = choice.getField("file");
            if (isValidFilepath(filepath)) return choice;
        }
        return null;
    }

    private static boolean isValidFilepath(String filepath) {
        try {
            //check model by converting its path
            convertPathToMDX(filepath);
        } catch (final Exception exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.frame,
                    "The chosen model could not be used.",
                    "Program Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    static void importUnitActionRes(MainPanel mainPanel) {
        final GameObject fetchUnitResult = fetchUnit(mainPanel);
        if (fetchUnitResult != null) {
            String path = fetchUnitResult.getField("file");
            importMdxObject(mainPanel, path);
        }
    }
}
