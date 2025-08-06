package com.matrixeater.gltf;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorPanel;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.matrixeater.src.MainPanel;

import java.awt.event.ActionEvent;

public class GLTFExport implements ActionListener{
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
        //     // Process the model and export it to GLTF format
        //     this.processModel(model);
        // } else {
        //     log.warning("No model is currently loaded for GLTF export.");
        // }
        log.info(this.getAllUnitPaths().size() + " unit paths found for GLTF export.");
    }

    private List<String> getAllUnitPaths() { 
        List<String> unitPaths = new ArrayList<String>();
        var unitData = mainframe.getUnitData();
        log.info("Unit data: " + unitData.keySet().size());
        final War3ID modelFileId = War3ID.fromString("umdl");
        for (var id : unitData.keySet())
        {
            var unit = unitData.get(id);
            String modelPath = unit.getFieldAsString(modelFileId, 0);
            if (!modelPath.isEmpty()) {
                unitPaths.add(modelPath);
            }
        }
        return unitPaths;
    }

    private void processModel(EditableModel model) {
        // Implement the logic to process the model and export it to GLTF format
        log.info("Processing model for GLTF export: " + model.getName());
        // Add your GLTF export logic here
    }
}
