package com.matrixeater.gltf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.model.io.GltfWriter;
import de.wc3data.stream.BlizzardDataInputStream;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.matrixeater.src.MainPanel;

public class GLTFExport implements ActionListener {
    private final static Logger log = Logger.getLogger(GLTFExport.class.getName());
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
        try {
            GLTFExport.export(model0);
        } catch (IOException ex) {
            log.severe("Failed to export model to GLTF: " + ex.getMessage());
        }
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

    private static void export(EditableModel model) throws IOException {
        var gltf = createGltfModel(model);
        File outputFile = new File("ExportedFromRetera.gltf");
        try (OutputStream os = new FileOutputStream(outputFile)) {
            GltfWriter writer = new GltfWriter();
            writer.write(gltf, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static GlTF createGltfModel(EditableModel model) {
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
        log.info(Arrays.stream(indices).max().orElse(-1) + " is the max index in indices array");
        log.info(Arrays.stream(indices).min().orElse(-1) + " is the min index in indices array");

        GlTF gltf = new GlTF();
        Asset asset = new Asset();
        asset.setVersion("2.0");
        asset.setGenerator("MatrixEater GLTF Exporter");
        gltf.setAsset(asset);

        byte[] positionBytes = new byte[positions.length * 4];
        ByteBuffer.wrap(positionBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(positions);
        String base64positionData = java.util.Base64.getEncoder().encodeToString(positionBytes);
        String uri = "data:application/octet-stream;base64," + base64positionData;

        Buffer positionBuffer = new Buffer();
        positionBuffer.setByteLength(positionBytes.length);
        positionBuffer.setUri(uri);
        gltf.setBuffers(new ArrayList<>(Arrays.asList(positionBuffer)));

        BufferView positionBufferView = new BufferView();
        positionBufferView.setTarget(34962); // ARRAY_BUFFER
        positionBufferView.setBuffer(0);
        positionBufferView.setByteOffset(0);
        positionBufferView.setByteLength(positionBytes.length);
        gltf.setBufferViews(new ArrayList<>(Arrays.asList(positionBufferView)));

        Accessor positionAccessor = new Accessor();
        positionAccessor.setBufferView(0);
        positionAccessor.setComponentType(5126); // FLOAT
        positionAccessor.setCount(positions.length / 3);
        positionAccessor.setType("VEC3");
        positionAccessor.setByteOffset(0);
        gltf.setAccessors(new ArrayList<>(Arrays.asList(positionAccessor)));
        
        byte[] indicesBytes = new byte[indices.length * 4];
        ByteBuffer.wrap(indicesBytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(indices);
        String base64IndicesData = java.util.Base64.getEncoder().encodeToString(indicesBytes);
        String indicesUri = "data:application/octet-stream;base64," + base64IndicesData;

        Buffer indicesBuffer = new Buffer();
        indicesBuffer.setByteLength(indicesBytes.length);
        indicesBuffer.setUri(indicesUri);
        gltf.getBuffers().add(indicesBuffer); // Updated to use getBuffers(), now it's not null because we added positionBuffer

        BufferView indicesBufferView = new BufferView();
        indicesBufferView.setTarget(34963); // ELEMENT_ARRAY_BUFFER
        indicesBufferView.setBuffer(1);
        indicesBufferView.setByteOffset(0);
        indicesBufferView.setByteLength(indicesBytes.length);
        gltf.getBufferViews().add(indicesBufferView);

        Accessor indicesAccessor = new Accessor();
        indicesAccessor.setBufferView(1);
        indicesAccessor.setComponentType(5125); // UNSIGNED_SHORT
        indicesAccessor.setCount(indices.length);
        indicesAccessor.setType("SCALAR");
        indicesAccessor.setByteOffset(0);
        gltf.getAccessors().add(indicesAccessor);

        Mesh mesh = new Mesh();
        MeshPrimitive primitive = new MeshPrimitive();
        primitive.setAttributes(Map.of("POSITION", 0));
        primitive.setIndices(1);
        primitive.setMode(4); // TRIANGLES
        mesh.setPrimitives(Arrays.asList(primitive));
        gltf.setMeshes(Arrays.asList(mesh));

        Node node = new Node();
        node.setMesh(0);
        gltf.setNodes(Arrays.asList(node));
        
        Scene scene = new Scene();
        scene.setNodes(Arrays.asList(0));
        gltf.setScenes(Arrays.asList(scene));
        gltf.setScene(0);


        System.out.println("Created glTF data with " + (positions.length / 3) + " vertices and " + (indices.length / 3) + " triangles");
        return gltf;

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
