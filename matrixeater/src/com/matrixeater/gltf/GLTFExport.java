package com.matrixeater.gltf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.wc3data.stream.BlizzardDataInputStream;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.matrixeater.src.MainPanel;

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
        GltfModel gltfModel = createGltfModel(model0);
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

    private static void export(EditableModel model, File selectedFile) throws IOException {
        GltfModel gltfModel = createGltfModel(model);
        try (OutputStream os = new FileOutputStream(selectedFile)) {
            GltfModelWriter writer = new GltfModelWriter();
            writer.writeBinary(gltfModel, os);
        }
    }

    private static GltfModel createGltfModel(EditableModel model) {
        // Create vertex data arrays  
        List<Geoset> geosets = new ArrayList<Geoset>();
        for (Geoset geoset : model.getGeosets()) {
            geosets.add(geoset);
        }
        
        // Count total vertices and triangles
        int totalVertices = 0;
        int totalTriangles = 0;
        for (Geoset geoset : geosets) {
            totalVertices += geoset.getVertices().size();
            totalTriangles += geoset.getTriangles().size();
        }
        
        // Create buffers
        float[] positions = new float[totalVertices * 3];
        float[] normals = new float[totalVertices * 3];
        float[] uvs = new float[totalVertices * 2];
        int[] indices = new int[totalTriangles * 3];
        
        int vertexIndex = 0;
        int triangleIndex = 0;
        int baseVertexOffset = 0;
        
        for (Geoset geoset : geosets) {
            if (geoset.getVertices().size() == 0) {
                continue;
            }
            
            // Fill vertex data
            for (GeosetVertex vertex : geoset.getVertices()) {
                positions[vertexIndex * 3] = (float) vertex.x;
                positions[vertexIndex * 3 + 1] = (float) vertex.y;
                positions[vertexIndex * 3 + 2] = (float) vertex.z;
                
                if (vertex.getNormal() != null) {
                    normals[vertexIndex * 3] = (float) vertex.getNormal().x;
                    normals[vertexIndex * 3 + 1] = (float) vertex.getNormal().y;
                    normals[vertexIndex * 3 + 2] = (float) vertex.getNormal().z;
                } else {
                    normals[vertexIndex * 3] = 0;
                    normals[vertexIndex * 3 + 1] = 0;
                    normals[vertexIndex * 3 + 2] = 1;
                }
                
                if (vertex.getTverts().size() > 0) {
                    uvs[vertexIndex * 2] = (float) vertex.getTverts().get(0).x;
                    uvs[vertexIndex * 2 + 1] = (float) vertex.getTverts().get(0).y;
                } else {
                    uvs[vertexIndex * 2] = 0;
                    uvs[vertexIndex * 2 + 1] = 0;
                }
                
                vertexIndex++;
            }
            
            // Fill triangle indices
            for (Triangle triangle : geoset.getTriangles()) {
                indices[triangleIndex * 3] = geoset.getVertices().indexOf(triangle.getVerts()[0]) + baseVertexOffset;
                indices[triangleIndex * 3 + 1] = geoset.getVertices().indexOf(triangle.getVerts()[1]) + baseVertexOffset;
                indices[triangleIndex * 3 + 2] = geoset.getVertices().indexOf(triangle.getVerts()[2]) + baseVertexOffset;
                triangleIndex++;
            }
            
            baseVertexOffset += geoset.getVertices().size();
        }
        
        // For now, return null until we can determine the correct jgltf API
        // This is a placeholder that collects all the mesh data correctly
        System.out.println("Created glTF data with " + (positions.length / 3) + " vertices and " + (indices.length / 3) + " triangles");
        
        // TODO: Implement proper glTF model creation once the correct API is determined
        return null;
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
