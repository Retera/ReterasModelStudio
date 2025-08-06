package com.matrixeater.gltf;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorPanel;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.matrixeater.src.MainPanel;

import de.wc3data.stream.BlizzardDataInputStream;

import java.awt.event.ActionEvent;

public class GLTFExport implements ActionListener {
    private final Logger log = Logger.getLogger(GLTFExport.class.getName());
    // I know it's bad, don't worry.
    private final MainPanel mainframe;

    public GLTFExport(MainPanel mainframe) {
        this.mainframe = mainframe;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // var model = mainframe.currentMDL();
        // if (model != null) {
        // // Process the model and export it to GLTF format
        // this.processModel(model);
        // } else {
        // log.warning("No model is currently loaded for GLTF export.");
        // }
        log.info(this.getAllUnitPaths().size() + " unit paths found for GLTF export.");
        log.info(this.getAllDoodadsPaths().size() + " doodad paths found for GLTF export.");
        var model0 = this.loadModel(this.getAllUnitPaths().get(0));
        log.info("name: " + model0.getName());
    }

    private List<String> getAllUnitPaths() {
        List<String> unitPaths = new ArrayList<String>();
        var unitData = mainframe.getUnitData();
        log.info("Unit data: " + unitData.keySet().size());
        final War3ID modelFileId = War3ID.fromString("umdl");
        for (var id : unitData.keySet()) {
            var unit = unitData.get(id);
            String modelPath = convertPathToMDX(unit.getFieldAsString(modelFileId, 0));
            if (!modelPath.isEmpty()) {
                unitPaths.add(modelPath);
            }
        }
        return unitPaths;
    }

    private List<String> getAllDoodadsPaths() {
        List<String> doodadPaths = new ArrayList<String>();
        var data = mainframe.getDoodadData();
        log.info("Doodad data: " + data.keySet().size());
        final War3ID modelFileId = War3ID.fromString("umdl");
        for (var id : data.keySet()) {
            var obj = data.get(id);
            final int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
            if (numberOfVariations > 1) {
                for (int i = 0; i < numberOfVariations; i++) {
                    final String path = convertPathToMDX(
                            obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl");
                    doodadPaths.add(path);
                }
            } else {
                final String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("dfil"), 0));
                if (!path.isEmpty()) {
                    doodadPaths.add(path);
                }
            }
        }
        return doodadPaths;
    }

    private void processModel(EditableModel model) {
        // Implement the logic to process the model and export it to GLTF format
        log.info("Processing model for GLTF export: " + model.getName());
        // Add your GLTF export logic here
    }

    private EditableModel loadModel(String path) {
        var f = MpqCodebase.get().getResourceAsStream(path);
        try (BlizzardDataInputStream in = new BlizzardDataInputStream(f)) {
            final EditableModel model = new EditableModel(MdxUtils.loadModel(in));
            return model;
        }
        catch (Exception e) {
            log.severe("Failed to load model from path: " + path + " due to " + e.getMessage());
            return null;
        }
    }

    private String convertPathToMDX(String filepath) {
        if (filepath.endsWith(".mdl")) {
            filepath = filepath.replace(".mdl", ".mdx");
        } else if (!filepath.endsWith(".mdx")) {
            filepath = filepath.concat(".mdx");
        }
        return filepath;
    }
}
