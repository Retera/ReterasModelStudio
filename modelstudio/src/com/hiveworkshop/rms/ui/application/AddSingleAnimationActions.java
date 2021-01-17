package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddSingleAnimationActions {
//    static void addAnimationFromFile(MainPanel mainPanel){
//        mainPanel.fc.setDialogTitle("Animation Source");
//        final EditableModel current = mainPanel.currentMDL();
//        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
//            mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
//        } else if (mainPanel.profile.getPath() != null) {
//            mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
//        }
//        final int returnValue = mainPanel.fc.showOpenDialog(mainPanel);
//
//        if (returnValue == JFileChooser.APPROVE_OPTION) {
//            mainPanel.currentFile = mainPanel.fc.getSelectedFile();
//            mainPanel.profile.setPath(mainPanel.currentFile.getParent());
//            final EditableModel animationSourceModel;
//            try {
//                animationSourceModel = MdxUtils.loadEditable(mainPanel.currentFile);
//                addSingleAnimation(mainPanel, current, animationSourceModel);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        mainPanel.fc.setSelectedFile(null);
//
//        MenuBarActions.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
//    }

    static void addAnimationFromFile(MainPanel mainPanel) {
        FileDialog fileDialog = new FileDialog(mainPanel);

        final EditableModel animationSourceModel = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
        if (animationSourceModel != null) {
            addSingleAnimation(mainPanel, fileDialog.getModel(), animationSourceModel);
        }

        MenuBarActions.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
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

    static void addAnimationFromUnit(MainPanel mainPanel) {
        mainPanel.fc.setDialogTitle("Animation Source");
        final GameObject fetchResult = ImportFileActions.fetchUnit(mainPanel);
        if (fetchResult != null) {
            String path = fetchResult.getField("file");
            fetchAndAddSingleAnimation(mainPanel, path);
        }
    }

    static void addEmptyAnimation(MainPanel mainPanel) {
//        mainPanel.fc.setDialogTitle("Animation Source");
//        final GameObject fetchResult = ImportFileActions.fetchUnit(mainPanel);
//        if (fetchResult != null) {
//            String path = fetchResult.getField("file");
//            fetchAndAddSingleAnimation(mainPanel, path);
//        }
        final EditableModel current = mainPanel.currentMDL();
        if (current != null) {
            addEmptyAnimation(mainPanel, current);
        }
    }

    static void addEmptyAnimation(MainPanel mainPanel, final EditableModel current) {
        JPanel creationPanel = new JPanel(new MigLayout());

        JPanel newAnimationPanel = new JPanel(new MigLayout());
        newAnimationPanel.add(new JLabel("Add new empty animation"), "span 2, wrap");
        newAnimationPanel.add(new JLabel("Name"));
        JTextField nameField = new JTextField();
        nameField.setText("newAnimation");
//        nameField.setText("");
        newAnimationPanel.add(nameField, "wrap, grow");
        newAnimationPanel.add(new JLabel("Start"));
        JSpinner startSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        newAnimationPanel.add(startSpinner, "wrap");
        newAnimationPanel.add(new JLabel("End"));
        JSpinner endSpinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
        newAnimationPanel.add(endSpinner, "wrap");
        creationPanel.add(newAnimationPanel, "cell 0 0");

        JTable existingAnimationTable = new JTable();
        JPanel existingAnimationsPanel = new JPanel(new MigLayout());
        JScrollPane animScrollPane = new JScrollPane(existingAnimationTable);
        animScrollPane.setPreferredSize(new Dimension(250, 300));
        existingAnimationsPanel.add(animScrollPane, "wrap, span 2");
        creationPanel.add(existingAnimationsPanel, "cell 1 0");

        List<Animation> currAnim = current.getAnims();
        List<Integer> startTimes = new ArrayList<>();
        List<Integer> endTimes = new ArrayList<>();
        List<String> animationNames = new ArrayList<>();
        for (Animation a : currAnim) {
            startTimes.add(a.getStart());
            endTimes.add(a.getEnd());
            animationNames.add(a.getName());
        }

        DefaultTableModel animationTableModel = new DefaultTableModel();
        animationTableModel.addColumn("start", startTimes.toArray());
        animationTableModel.addColumn("end", endTimes.toArray());
        animationTableModel.addColumn("name", animationNames.toArray());

        existingAnimationTable.setModel(animationTableModel);

        JButton setStartAfter = new JButton("Start After");
        setStartAfter.addActionListener(e -> {
            int end = (Integer) endSpinner.getValue();
            int duration = end - (Integer) startSpinner.getValue();
            int newStart = ((Integer) existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 1)) + 1;
            if (newStart > end) {
                end = newStart + duration;
            }
            startSpinner.setValue(newStart);
            endSpinner.setValue(end);
        });
        JButton setEndBefore = new JButton("End Before");
//        setEndBefore.addActionListener(e -> endSpinner.setValue(existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 0)));
        setEndBefore.addActionListener(e -> {
            int start = (Integer) startSpinner.getValue();
            int duration = (Integer) endSpinner.getValue() - start;
            int newEnd = ((Integer) existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 0)) - 1;
            if (newEnd < start) {
                start = newEnd - duration;
            }
            startSpinner.setValue(start);
            endSpinner.setValue(newEnd);
        });

        existingAnimationsPanel.add(setStartAfter);
        existingAnimationsPanel.add(setEndBefore);

//        optionPane.setOptions();
        int option = JOptionPane.showConfirmDialog(mainPanel, creationPanel, "Create Empty Animation", JOptionPane.OK_CANCEL_OPTION);
        System.out.println("option \"" + option + "\"");
        int start = (Integer) startSpinner.getValue();
        int end = (Integer) endSpinner.getValue();
        if (option == 0 && start < end) {
            Animation animation = new Animation(nameField.getText(), start, end);
            current.addAnimation(animation);

            mainPanel.modelStructureChangeListener.animationsAdded(Collections.singletonList(animation));
        } else if (option == 0 && start >= end) {
//            JPanel newEndPanel = new JPanel();
//            JSpinner newEndSpinner = new JSpinner(new SpinnerNumberModel(start + 1,start+1,Integer.MAX_VALUE, 1));
//            newEndPanel.add(newEndSpinner);
            JOptionPane.showConfirmDialog(mainPanel, "End needs to be after start", "Choose valid end time", JOptionPane.DEFAULT_OPTION);
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
