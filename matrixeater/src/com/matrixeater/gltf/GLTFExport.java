package com.matrixeater.gltf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
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
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.javagl.jgltf.impl.v2.Skin;
import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.model.animation.AnimationManager.AnimationPolicy;
import de.javagl.jgltf.model.io.GltfWriter;
import de.javagl.jgltf.impl.v2.*;
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
        var model0 = this.loadModel(this.getAllUnitPaths().get(689));
        var anim = model0.getAnim(0);
        var corpse = model0.getGeoset(0);// last geoset is a corpse, shouldn't be visible in a walk
        log.info("Geoset" + corpse.getName() + " visibility: "
                + isGeosetVisibleInAnimation(corpse, anim));
        int visibilityCount = 0;
        for (Geoset geoset : model0.getGeosets()) {
            if (isGeosetVisibleInAnimation(geoset, anim)) {
                visibilityCount++;
                System.out.println("Geoset " + geoset.getName() + " is visible in animation.");
            }
        }
        log.info("Geosets visible in animation: " + visibilityCount + " out of " + model0.getGeosets().size());

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
        File outputFile = new File(model.getName() + ".gltf");
        try (OutputStream os = new FileOutputStream(outputFile)) {
            GltfWriter writer = new GltfWriter();
            writer.write(gltf, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static GlTF createGltfModel(EditableModel model) {

        GlTF gltf = new GlTF();
        Asset asset = new Asset();
        asset.setVersion("2.0");
        asset.setGenerator(model.getName());
        gltf.setAsset(asset);

        loadMeshIntoModel(model, gltf, geoset -> true);

        return gltf;
    }

    private static void loadMeshIntoModel(EditableModel model, GlTF gltf, Predicate<Geoset> visibilityFilter) {

        List<Buffer> buffers = new ArrayList<>();
        List<BufferView> bufferViews = new ArrayList<>();
        List<Accessor> accessors = new ArrayList<>();
        List<Mesh> meshes = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        List<Integer> geoNodes = new ArrayList<>(); // called geo because it contains nodes made form geosets
        List<Image> images = new ArrayList<>();
        List<Sampler> samplers = new ArrayList<>();
        List<Texture> textures = new ArrayList<>();
        List<Material> materials = new ArrayList<>();
        List<Skin> skins = new ArrayList<>();

        // Materials
        for (var material : model.getMaterials()) {
            if (material.getLayers().size() > 1) {
                log.warning("Material " + material.getName()
                        + " has more than one layer, which is not supported in GLTF export, conversion might have errors.");
            }

            var pngBytes = getPngFromMaterial(material, model.getWrappedDataSource());
            Buffer materialTextureBuffer = new Buffer();
            String pngBase64Data = java.util.Base64.getEncoder().encodeToString(pngBytes);
            String uri = "data:application/octet-stream;base64," + pngBase64Data;
            materialTextureBuffer.setByteLength(pngBytes.length);
            materialTextureBuffer.setUri(uri);
            buffers.add(materialTextureBuffer);
            var materialTextureBufferIndex = buffers.size() - 1;

            BufferView materialTextureBufferView = new BufferView();
            materialTextureBufferView.setBuffer(materialTextureBufferIndex);
            materialTextureBufferView.setByteOffset(0);
            materialTextureBufferView.setByteLength(pngBytes.length);
            bufferViews.add(materialTextureBufferView);
            var materialTextureBufferViewIndex = bufferViews.size() - 1;

            Image materialImage = new Image();
            materialImage.setBufferView(materialTextureBufferViewIndex);
            materialImage.setMimeType("image/png");
            images.add(materialImage);
            var materialImageIndex = images.size() - 1;

            Sampler sampler = new Sampler();
            sampler.setMagFilter(9729); // LINEAR
            sampler.setMinFilter(9987); // LINEAR_MIPMAP_LINEAR
            sampler.setWrapS(10497); // REPEAT
            sampler.setWrapT(10497); // REPEAT
            samplers.add(sampler);
            var samplerIndex = samplers.size() - 1;

            Texture texture = new Texture();
            texture.setSource(materialImageIndex);
            texture.setSampler(samplerIndex);
            textures.add(texture);
            var textureIndex = textures.size() - 1;

            TextureInfo textureInfo = new TextureInfo();
            textureInfo.setIndex(textureIndex);

            Material glMaterial = new Material();
            glMaterial.setName(material.getName());
            // glMaterial.setAlphaMode("BLEND");
            // glMaterial.setAlphaCutoff(null);
            //! The best approximation I could find so far
            glMaterial.setAlphaMode("MASK");
            glMaterial.setAlphaCutoff(0.5f);  // or whatever cutoff works best


            MaterialPbrMetallicRoughness pbr = new MaterialPbrMetallicRoughness();
            pbr.setBaseColorFactor(new float[]{1, 1, 1, 1});
            pbr.setMetallicFactor(0.0f);
            pbr.setRoughnessFactor(1.0f);
            pbr.setBaseColorTexture(textureInfo);


            glMaterial.setPbrMetallicRoughness(pbr);
            materials.add(glMaterial);
        }
        // MESH
        log.info("Geosets: " + model.getGeosets().size());
        for (Geoset geoset : model.getGeosets()) {
            if (!visibilityFilter.test(geoset)) {
                continue;
            }
            var data = new GeosetData(geoset);
            byte[] positionBytes = new byte[data.positions.length * 4];
            ByteBuffer.wrap(positionBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(data.positions);
            String base64positionData = java.util.Base64.getEncoder().encodeToString(positionBytes);
            String uri = "data:application/octet-stream;base64," + base64positionData;

            Buffer positionBuffer = new Buffer();
            positionBuffer.setByteLength(positionBytes.length);
            positionBuffer.setUri(uri);
            buffers.add(positionBuffer);
            var vertexBufferIndex = buffers.size() - 1;

            BufferView positionBufferView = new BufferView();
            positionBufferView.setTarget(34962); // ARRAY_BUFFER
            positionBufferView.setBuffer(vertexBufferIndex);
            positionBufferView.setByteOffset(0);
            positionBufferView.setByteLength(positionBytes.length);
            bufferViews.add(positionBufferView);
            var positionBufferViewIndex = bufferViews.size() - 1;

            Accessor positionAccessor = new Accessor();
            // TODO: should define min/max values for positionAccessor, after I figure out
            // how vertices are expresssed in the model
            positionAccessor.setBufferView(positionBufferViewIndex);
            positionAccessor.setComponentType(5126); // FLOAT
            positionAccessor.setCount(data.positions.length / 3);
            positionAccessor.setType("VEC3");
            positionAccessor.setByteOffset(0);
            accessors.add(positionAccessor);
            var positionAccessorIndex = accessors.size() - 1;

            byte[] indicesBytes = new byte[data.indices.length * 4];
            ByteBuffer.wrap(indicesBytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(data.indices);
            String base64IndicesData = java.util.Base64.getEncoder().encodeToString(indicesBytes);
            String indicesUri = "data:application/octet-stream;base64," + base64IndicesData;

            Buffer indicesBuffer = new Buffer();
            indicesBuffer.setByteLength(indicesBytes.length);
            indicesBuffer.setUri(indicesUri);
            buffers.add(indicesBuffer);
            // gltf.getBuffers().add(indicesBuffer); // Updated to use getBuffers(), now
            // it's not null because we added
            // positionBuffer
            var indicesBufferIndex = buffers.size() - 1; // Get the index of the indices buffer

            BufferView indicesBufferView = new BufferView();
            indicesBufferView.setTarget(34963); // ELEMENT_ARRAY_BUFFER
            indicesBufferView.setBuffer(indicesBufferIndex);
            indicesBufferView.setByteOffset(0);
            indicesBufferView.setByteLength(indicesBytes.length);
            bufferViews.add(indicesBufferView);
            var indicesBufferViewIndex = bufferViews.size() - 1; // Get the index of the indices buffer view

            Accessor indicesAccessor = new Accessor();
            indicesAccessor.setBufferView(indicesBufferViewIndex);
            indicesAccessor.setComponentType(5125); // UNSIGNED_SHORT
            indicesAccessor.setCount(data.indices.length);
            indicesAccessor.setType("SCALAR");
            indicesAccessor.setByteOffset(0);
            accessors.add(indicesAccessor);
            var indicesAccessorIndex = accessors.size() - 1; // Get the index of the indices accessor

            byte[] uvBytes = new byte[data.uvs.length * 4];
            ByteBuffer.wrap(uvBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(data.uvs);
            String base64UvData = java.util.Base64.getEncoder().encodeToString(uvBytes);
            String uvUri = "data:application/octet-stream;base64," + base64UvData;

            Buffer uvBuffer = new Buffer();
            uvBuffer.setByteLength(uvBytes.length);
            uvBuffer.setUri(uvUri);
            buffers.add(uvBuffer);
            var uvBufferIndex = buffers.size() - 1; // Get the index of the

            BufferView uvBufferView = new BufferView();
            uvBufferView.setTarget(34962); // ARRAY_BUFFER
            uvBufferView.setBuffer(uvBufferIndex);
            uvBufferView.setByteOffset(0);
            uvBufferView.setByteLength(uvBytes.length);
            bufferViews.add(uvBufferView);
            var uvBufferViewIndex = bufferViews.size() - 1; // Get the index

            Accessor uvAccessor = new Accessor();
            uvAccessor.setBufferView(uvBufferViewIndex);
            uvAccessor.setComponentType(5126);
            uvAccessor.setCount(data.uvs.length / 2);
            uvAccessor.setType("VEC2");
            uvAccessor.setByteOffset(0);
            accessors.add(uvAccessor);
            var uvAccessorIndex = accessors.size() - 1;

            Mesh mesh = new Mesh();
            MeshPrimitive primitive = new MeshPrimitive();
            primitive.setAttributes(Map.of("POSITION", positionAccessorIndex, "TEXCOORD_0", uvAccessorIndex));
            primitive.setIndices(indicesAccessorIndex);
            primitive.setMode(4); // TRIANGLES
            primitive.setMaterial(data.materialIndex); // Assuming materialIndex is the index of the material in the
                                                       // glTF
            mesh.setPrimitives(Arrays.asList(primitive));
            meshes.add(mesh);
            var meshIndex = meshes.size() - 1; // Get the index of the mesh

            Node node = new Node();
            node.setMesh(meshIndex);
            node.setName(geoset.getName());
            nodes.add(node);
            geoNodes.add(nodes.size() - 1); // Add the node to the root nodes list
        }

        // Bones
        List<Bone> mdxBones = new ArrayList<>();
        for (final IdObject object : model.getIdObjects()) {
            if (object instanceof Bone) {
                mdxBones.add((Bone) object);
            }
        }

        log.info("Bones: " + mdxBones.size());
        // if (mdxBones.size() > 0) {
        // // Create a skin for the bones
        // Skin skin = new Skin();
        // List<Integer> jointIndices = new ArrayList<>();
        // List<float[]> inverseBindMatrices = new ArrayList<>(); // Changed to
        // List<float[]> for inverse bind matrices
        // for (Bone bone : mdxBones) {
        // jointIndices.add(nodes.size()); // Add the node index to the joint indices
        // Node boneNode = new Node();
        // boneNode.setName(bone.getName());
        // nodes.add(boneNode);
        // // Create an inverse bind matrix for the bone
        // float[] inverseBindMatrix = new float[16];
        // // Assuming the bone has a method to get its transformation matrix
        // // Here we just create an identity matrix for simplicity
        // for (int i = 0; i < 16; i++) {
        // inverseBindMatrix[i] = (i % 5 == 0) ? 1
        // : 0; // Identity matrix
        // }
        // inverseBindMatrices.add(inverseBindMatrix);
        // }
        // skins.add(skin);
        // }

        // Merge
        Node rootNode = new Node();
        rootNode.setName(model.getName());
        rootNode.setChildren(geoNodes);
        // Rotate -90 degrees around X-axis to convert from Z-up (MDX) to Y-up (glTF)
        rootNode.setRotation(new float[] { -0.7071068f, 0, 0, 0.7071068f });
        nodes.add(rootNode);
        var rootNodeIndex = nodes.size() - 1; // Get the index of the root node

        Scene scene = new Scene();
        scene.setNodes(Arrays.asList(rootNodeIndex));
        gltf.setScenes(Arrays.asList(scene));
        gltf.setScene(0);

        gltf.setBuffers(buffers);
        gltf.setBufferViews(bufferViews);
        gltf.setAccessors(accessors);
        gltf.setMeshes(meshes);
        gltf.setNodes(nodes);
        gltf.setImages(images);
        gltf.setSamplers(samplers);
        gltf.setTextures(textures);
        gltf.setMaterials(materials);
        // gltf.setSkins(skins);
    }

    private static class GeosetData {
        float[] positions;
        float[] normals;
        float[] uvs;
        int[] indices;
        int materialIndex;

        public GeosetData(Geoset geoset) {
            positions = new float[geoset.getVertices().size() * 3];
            normals = new float[geoset.getVertices().size() * 3];
            uvs = new float[geoset.getVertices().size() * 2];
            indices = new int[geoset.getTriangles().size() * 3];
            materialIndex = geoset.getMaterialID();
            int vertexIndex = 0;
            int triangleIndex = 0;
            if (geoset.getVertices().size() == 0) {
                return;
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
                indices[triangleIndex * 3] = geoset.getVertices().indexOf(triangle.getVerts()[0]);
                indices[triangleIndex * 3 + 1] = geoset.getVertices().indexOf(triangle.getVerts()[1]);
                indices[triangleIndex * 3 + 2] = geoset.getVertices().indexOf(triangle.getVerts()[2]);
                triangleIndex++;
            }
        }
    }

    private static byte[] getPngFromMaterial(com.hiveworkshop.wc3.mdl.Material material, DataSource dataSource) {
        var img = material.getBufferedImage(dataSource);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.severe("Failed to write image for material " + material.getName() + ": " + e.getMessage());
            return null;
        }
    }

    // Lightweight visibility check:
    // - If animation == null => export everything (back-compat)
    // - If no GeosetAnim => visible
    // - If static alpha (-1 => default 1) or > 0 => visible
    // - If has Visibility/Alpha AnimFlag (non-global) => sample a few times in [start,end]
    private static boolean isGeosetVisibleInAnimation(Geoset geoset, com.hiveworkshop.wc3.mdl.Animation animation) {
        if (animation == null) {
            return true; // no filtering requested
        }
        GeosetAnim ga = geoset.getGeosetAnim();
        if (ga == null) {
            return true;
        }

        // Static alpha path
        var visFlag = ga.getVisibilityFlag();
        if (visFlag == null) {
            double staticAlpha = ga.getStaticAlpha(); // -1 => default visible (1)
            return staticAlpha != -1 && staticAlpha < 0.0;
        }

        // AnimFlag path (non-global preferred). We’ll sample a few times.
        int start = animation.getIntervalStart();
        int end = animation.getIntervalEnd();
        if (end <= start) {
            // Degenerate animation; treat as visible if any positive static visibility
            double staticAlpha = ga.getStaticAlpha();
            return staticAlpha != -1 && staticAlpha < 0.0;
        }

        // Simple sampler: 9 samples across the interval (start..end)
        final int samples = 9;
        for (int i = 0; i <= samples; i++) {
            int t = start + (int) ((long) (end - start) * i / samples);
            var times = visFlag.getTimes();
            if (t < times.get(0) || t > times.get(times.size() - 1)) {
                continue; // Skip times outside the animation interval
            }
            float alpha = sampleGeosetVisibilityAtTime(ga, t);
            if (alpha < 0.9f) {
                return false;
            }
        }
        return true;
    }

    // Best-effort sampler without wiring a full AnimatedRenderEnvironment:
    // - If vis flag exists but we can’t interpolate precisely, fall back to static
    //   (This keeps the change safe; refine later by wiring a proper time env.)
    private static float sampleGeosetVisibilityAtTime(GeosetAnim ga, int time) {
        try {
            // Prefer the existing helper if available
            // (If you later wire an AnimatedRenderEnvironment, replace with:
            //   ga.getRenderVisibility(envAt(time))
            // )
            var visFlag = ga.getVisibilityFlag();
            if (visFlag == null) {
                double staticAlpha = ga.getStaticAlpha();
                return (float) (staticAlpha == -1 ? 1.0 : staticAlpha);
            }

            // Heuristic: if flag has only one keyframe, use its value; otherwise assume visible
            // NOTE: Replace this with proper interpolation using your AnimFlag API if available.
            if (visFlag.size() == 0) {
                double staticAlpha = ga.getStaticAlpha();
                return (float) (staticAlpha == -1 ? 1.0 : staticAlpha);
            } else if (visFlag.size() == 1) {
                Object v = visFlag.getValues().get(0); // may be Number
                if (v instanceof Number) {
                    return ((Number) v).floatValue();
                }
                return 1.0f;
            } else {
                for (int i = 0; i < visFlag.size(); i++) {
                    if (visFlag.getTimes().get(i) >= time) {
                        Object v = visFlag.getValues().get(i);
                        if (v instanceof Number) {
                            float vis = ((Number) v).floatValue();
                            if (vis < 0.9f) { // Threshold for visibility
                                return vis; // Return the visibility value
                            }
                        }
                        return 1.0f; // Fallback to visible
                    }
                }
            }
        } catch (Throwable t) {
            // Fallback: do not exclude on errors
            double staticAlpha = ga.getStaticAlpha();
            return (float) (staticAlpha == -1 ? 1.0 : staticAlpha);
        }
        return 1.0f; // Default visible if all else fails
    }

    private EditableModel loadModel(String path) {
        var f = MpqCodebase.get().getResourceAsStream(path);
        try (BlizzardDataInputStream in = new BlizzardDataInputStream(f)) {
            final EditableModel model = new EditableModel(MdxUtils.loadModel(in));
            return model;
        } catch (Exception e) {
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
