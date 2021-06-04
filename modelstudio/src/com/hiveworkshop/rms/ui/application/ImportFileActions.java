package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.TempStuffFromEditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
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
    public static void importFile(final File f) {
        try {
            importFile(MdxUtils.loadEditable(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void importFile(final EditableModel model) {
        ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null && modelPanel.getModel() != null) {
            ImportPanel importPanel = new ImportPanel(modelPanel.getModel(), model);
            importPanel.setCallback(new ModelStructureChangeListener());
//	        importPanel.setCallback(new ModelStructureChangeListener(new ModelStructureChangeListener.ModelReference() {
//		        private final EditableModel model = mainPanel.currentMDL();
//
//		        @Override
//		        public EditableModel getModel() {
//			        return model;
//		        }
//	        }));

        }
    }

    public static void repaintModelTrees() {
        if (ProgramGlobals.getCurrentModelPanel() != null) {
            ProgramGlobals.getCurrentModelPanel().repaintModelTrees();
        }
    }

    public static void importButtonActionRes(MainPanel mainPanel) {
        FileDialog fileDialog = new FileDialog(mainPanel);

        final EditableModel model = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
        if (model != null) {
            importFile(model);
        }
        repaintModelTrees();
    }

    public static void importFromWorkspaceActionRes() {
        final List<EditableModel> optionNames = new ArrayList<>();
        for (final ModelPanel modelPanel : ProgramGlobals.getModelPanels()) {
            final EditableModel model = modelPanel.getModel();
            optionNames.add(model);
        }
        final EditableModel choice = (EditableModel) JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
                "Choose a workspace item to import data from:", "Import from Workspace",
                JOptionPane.OK_CANCEL_OPTION, null, optionNames.toArray(), optionNames.get(0));
        if (choice != null) {
            importFile(TempStuffFromEditableModel.deepClone(choice, choice.getHeaderName()));
        }
        repaintModelTrees();
    }

    public static String convertPathToMDX(String filepath) {
        if (filepath.endsWith(".mdl")) {
            filepath = filepath.replace(".mdl", ".mdx");
        } else if (!filepath.endsWith(".mdx")) {
            filepath = filepath.concat(".mdx");
        }
        return filepath;
    }

    static void importMdxObject(String path) {
        final String filepath = convertPathToMDX(path);
        if (filepath != null) {
            final File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
            importFile(animationSource);
        }
        repaintModelTrees();
    }

    public static MutableObjectData.MutableGameObject fetchObject() {
        final BetterUnitEditorModelSelector selector = new BetterUnitEditorModelSelector(MainLayoutCreator.getUnitData(),
                MainLayoutCreator.getUnitEditorSettings());
        int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), selector, "Object Editor - Select Unit",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        final MutableObjectData.MutableGameObject choice = selector.getSelection();
        if ((choice == null) || (x != JOptionPane.OK_OPTION)) {
            return null;
        }

        String filepath = choice.getFieldAsString(UnitFields.MODEL_FILE, 0);

        if (isValidFilepath(filepath)) return choice;
        return null;
    }

    public static void importGameObjectActionRes() {
        final MutableObjectData.MutableGameObject fetchObjectResult = fetchObject();
        if (fetchObjectResult != null) {
            String path = fetchObjectResult.getFieldAsString(UnitFields.MODEL_FILE, 0);

            importMdxObject(path);
        }
    }

    public static ModelOptionPane.ModelElement fetchModel() {
        final ModelOptionPane.ModelElement model = ModelOptionPane.showAndLogIcon(ProgramGlobals.getMainPanel());
        if (model == null) {
            return null;
        }
        String filepath = model.getFilepath();
        if (isValidFilepath(filepath)) return model;
        return null;
    }

    public static void importGameModelActionRes() {
        final ModelOptionPane.ModelElement fetchModelResult = fetchModel();
        if (fetchModelResult != null) {
            String path = fetchModelResult.getFilepath();
            importMdxObject(path);
        }
    }

    public static GameObject fetchUnit() {
        final GameObject choice = UnitOptionPane.show(ProgramGlobals.getMainPanel());

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

    public static void importUnitActionRes() {
        final GameObject fetchUnitResult = fetchUnit();
        if (fetchUnitResult != null) {
            String path = fetchUnitResult.getField("file");
            importMdxObject(path);
        }
    }
}
