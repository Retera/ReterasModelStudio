package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AddSingleAnimationActions {
    static void addAnimationFromFile(MainPanel mainPanel){
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

    static void addAnimationFromObject(MainPanel mainPanel){
        mainPanel.fc.setDialogTitle("Animation Source");
        final MutableObjectData.MutableGameObject fetchResult = ImportFileActions.fetchObject(mainPanel);
        if (fetchResult != null) {
            String path = fetchResult.getFieldAsString(UnitFields.MODEL_FILE, 0);
            fetchAndAddSingleAnimation(mainPanel, path);
        }
    }

    static void addAnimFromModel(MainPanel mainPanel){
        mainPanel.fc.setDialogTitle("Animation Source");
        final ModelOptionPane.ModelElement fetchResult = ImportFileActions.fetchModel(mainPanel);
        if (fetchResult != null) {
            String path = fetchResult.getFilepath();
            fetchAndAddSingleAnimation(mainPanel, path);
        }
    }

    static void addAnimationFromUnit(MainPanel mainPanel){
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
}
