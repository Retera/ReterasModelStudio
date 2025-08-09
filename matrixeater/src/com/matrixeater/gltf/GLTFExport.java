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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;
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
        // Build a simple modal dialog for export options
        final JDialog dialog = new JDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(mainframe),
                "GLTF Export", true);
        JPanel panel = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.insets = new java.awt.Insets(4, 4, 4, 4);
        gc.anchor = java.awt.GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;

        // Checkbox for visibility-by-animation filtering
        final JCheckBox visibilityCheck = new JCheckBox("Filter by animation visibility");
        panel.add(visibilityCheck, gc);

        // Animation text field (name or index)
        gc.gridy++;
        panel.add(new JLabel("Animation (name or index):"), gc);
        gc.gridx = 1;
        final JTextField animationField = new JTextField(18);
        animationField.setEnabled(false);
        panel.add(animationField, gc);

        visibilityCheck.addActionListener(ev -> animationField.setEnabled(visibilityCheck.isSelected()));

        // Buttons
        gc.gridx = 0;
        gc.gridy++;
        final JButton exportCurrentBtn = new JButton("Export Current Model");
        panel.add(exportCurrentBtn, gc);
        gc.gridx = 1;
        final JButton exportAllBtn = new JButton("Export All Models");
        panel.add(exportAllBtn, gc);

        // Progress bar row
        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 2;
        final JProgressBar exportAllProgress = new JProgressBar();
        exportAllProgress.setStringPainted(true);
        exportAllProgress.setMinimum(0);
        exportAllProgress.setMaximum(100);
        exportAllProgress.setValue(0);
        exportAllProgress.setString("Idle");
        panel.add(exportAllProgress, gc);
        gc.gridwidth = 1;

        gc.gridx = 0;
        gc.gridy++;
        final JButton closeBtn = new JButton("Close");
        panel.add(closeBtn, gc);

        closeBtn.addActionListener(ev -> dialog.dispose());

        // Helper to resolve animation by text (index or partial name)
        BiFunction<com.hiveworkshop.wc3.mdl.EditableModel, String, com.hiveworkshop.wc3.mdl.Animation> resolveAnimation = (
                model, text) -> {
            if (model == null || text == null || text.isBlank())
                return null;
            text = text.trim();
            // Try index
            try {
                int idx = Integer.parseInt(text);
                if (idx >= 0 && idx < model.getAnims().size()) {
                    return model.getAnim(idx);
                }
            } catch (NumberFormatException ex) {
                // ignore
            }
            // Try substring name match (case-insensitive)
            for (com.hiveworkshop.wc3.mdl.Animation anim : model.getAnims()) {
                if (anim.getName() != null && anim.getName().toLowerCase().contains(text.toLowerCase())) {
                    return anim;
                }
            }
            return null;
        };

        // Export current model action
        exportCurrentBtn.addActionListener(ev -> {
            exportCurrentBtn.setEnabled(false);
            new Thread(() -> {
                try {
                    var model = mainframe.currentMDL();
                    if (model == null) {
                        log.warning("No current model to export.");
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog,
                                "No current model loaded.", "Export", JOptionPane.WARNING_MESSAGE));
                        return;
                    }
                    com.hiveworkshop.wc3.mdl.Animation anim = null;
                    if (visibilityCheck.isSelected()) {
                        anim = resolveAnimation.apply(model, animationField.getText());
                        if (anim == null) {
                            log.warning("Animation not found; exporting all geosets.");
                        } else {
                            log.info("Using animation for visibility filter: " + anim.getName());
                        }
                    }
                    GLTFExport.export(model, anim, "models");
                    log.info("Exported current model: " + model.getName());
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog,
                            "Exported: " + model.getName(), "Export", JOptionPane.INFORMATION_MESSAGE));
                } catch (Exception ex2) {
                    log.severe("Export failed: " + ex2.getMessage());
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog,
                            "Export failed: " + ex2.getMessage(), "Export Error",
                            JOptionPane.ERROR_MESSAGE));
                    // print stack trace for debugging
                    ex2.printStackTrace();
                } finally {
                    SwingUtilities.invokeLater(() -> exportCurrentBtn.setEnabled(true));
                }
            }, "GLTF-Export-Current").start();
        });

        // Export all models action
        exportAllBtn.addActionListener(ev -> {
            exportAllBtn.setEnabled(false);
            exportAllProgress.setIndeterminate(true);
            exportAllProgress.setString("Preparing...");
            new Thread(() -> {
                long startNanos = System.nanoTime(); // timing start
                try {
                    List<String> failedModels = Collections.synchronizedList(new ArrayList<>()); // track failed exports
                    // Gather paths first to know total for progress
                    List<String> unitPaths = getAllUnitPaths();
                    List<String> doodadPaths = getAllDoodadsPaths();
                    int total = unitPaths.size() + doodadPaths.size();
                    SwingUtilities.invokeLater(() -> {
                        exportAllProgress.setIndeterminate(false);
                        exportAllProgress.setMaximum(total);
                        exportAllProgress.setValue(0);
                        exportAllProgress.setString("0 / " + total);
                    });

                    AtomicInteger success = new AtomicInteger(0);
                    AtomicInteger fail = new AtomicInteger(0);
                    AtomicInteger processed = new AtomicInteger(0);
                    var threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                    ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threads);
                    // Units
                    for (String path : unitPaths) {
                        try {
                            var model = loadModel(path);
                            if (model == null) {
                                fail.incrementAndGet();
                                failedModels.add(path);
                            } else {
                                executor.submit(() -> {
                                    com.hiveworkshop.wc3.mdl.Animation anim = visibilityCheck.isSelected()
                                            ? resolveAnimation.apply(model, animationField.getText())
                                            : null;
                                    try {
                                        GLTFExport.export(model, anim, "models/units");
                                        success.incrementAndGet();
                                    } catch (Exception ex) {
                                        log.warning("Failed exporting unit " + path + ": " + ex.getMessage());
                                        fail.incrementAndGet();
                                        failedModels.add(path);
                                        return;
                                    } finally {
                                        processed.incrementAndGet();
                                        final int fProcessed = processed.get();
                                        final int fTotal = total;
                                        SwingUtilities.invokeLater(() -> {
                                            exportAllProgress.setValue(fProcessed);
                                            exportAllProgress.setString(fProcessed + " / " + fTotal);
                                        });
                                    }
                                });
                            }
                        } catch (Exception one) {
                            fail.incrementAndGet();
                            failedModels.add(path);
                            log.warning("Failed exporting unit " + path + ": " + one.getMessage());
                        }

                    }

                    // Doodads
                    for (String path : doodadPaths) {
                        try {
                            var model = loadModel(path);
                            if (model == null) {
                                fail.incrementAndGet();
                                failedModels.add(path);
                            } else {
                                executor.submit(() -> {
                                    com.hiveworkshop.wc3.mdl.Animation anim = visibilityCheck.isSelected()
                                            ? resolveAnimation.apply(model, animationField.getText())
                                            : null;
                                    try { 
                                    GLTFExport.export(model, anim, "models/doodads");
                                    success.incrementAndGet();
                                    }
                                    catch (Exception ex) {
                                        log.warning("Failed exporting doodad " + path + ": " + ex.getMessage());
                                        fail.incrementAndGet();
                                        failedModels.add(path);
                                        return;
                                    }
                                    finally {
                                        processed.incrementAndGet();
                                        final int fProcessed = processed.get();
                                        final int fTotal = total;
                                        SwingUtilities.invokeLater(() -> {
                                            exportAllProgress.setValue(fProcessed);
                                            exportAllProgress.setString(fProcessed + " / " + fTotal);
                                        });
                                    }
                                });
                            }
                        } catch (Exception one) {
                            fail.incrementAndGet();
                            failedModels.add(path);
                            log.warning("Failed exporting doodad " + path + ": " + one.getMessage());
                        }

                    }
                    executor.shutdown();
                    executor.awaitTermination(1_000_000, TimeUnit.SECONDS);

                    int finalSuccess = success.get();
                    int finalFail = fail.get();
                    List<String> finalFailedModels = new ArrayList<>(failedModels);
                    long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L; // timing end
                    SwingUtilities.invokeLater(() -> {
                        double secs = elapsedMs / 1000.0;
                        exportAllProgress
                                .setString("Done: " + finalSuccess + "/" + total + " (Failed " + finalFail + ")");
                        if (finalFail > 0) {
                            var msgPanel = Box.createVerticalBox();
                            String summary = "Success: " + finalSuccess + "\nFailed: " + finalFail + "\nDuration: "
                                    + String.format(java.util.Locale.US, "%.3f s", secs);
                            msgPanel.add(new JLabel("Export Summary"), BorderLayout.NORTH);
                            msgPanel.add(new JTextArea(summary), BorderLayout.NORTH);
                            msgPanel.add(new JLabel("Failed models (paths):"), BorderLayout.NORTH);
                            JTextArea failList = new JTextArea();
                            failList.setEditable(false);
                            failList.setText(String.join("\n", finalFailedModels));
                            JScrollPane scrollPane = new JScrollPane(failList);
                            scrollPane.setPreferredSize(new Dimension(400, 300));
                            msgPanel.add(scrollPane, BorderLayout.CENTER);
                            JOptionPane.showMessageDialog(dialog, msgPanel, "Export All",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                    "All exports complete.\nSuccess: " + finalSuccess + "\nFailed: 0\nDuration: "
                                            + String.format(java.util.Locale.US, "%.3f s", secs),
                                    "Export All", JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                } catch (InterruptedException e2) {
                    log.warning("Export interrupted: " + e2.getMessage());
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog,
                            "Export interrupted: " + e2.getMessage(), "Export Error",
                            JOptionPane.ERROR_MESSAGE));
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        exportAllBtn.setEnabled(true);
                        exportAllProgress.setIndeterminate(false);
                    });
                }
            }, "GLTF-Export-All").start();
        });

        dialog.getContentPane().add(panel);
        dialog.pack();
        JOptionPane.showMessageDialog(
                dialog,
                "GLTF export is experimental.\nResults may be incomplete or incorrect.",
                "Experimental Feature",
                JOptionPane.WARNING_MESSAGE);
        dialog.setLocationRelativeTo(mainframe);
        dialog.setVisible(true);
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

    private static void export(EditableModel model, com.hiveworkshop.wc3.mdl.Animation animation, String baseDir)
            throws IOException {
        var gltf = createGltfModel(model, animation);
        File outputFile = new File(baseDir + "/" + model.getName() + ".gltf");

        // Ensure parent directories exist
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directories: " + parentDir.getAbsolutePath());
            }
        }

        try (OutputStream os = new FileOutputStream(outputFile)) {
            GltfWriter writer = new GltfWriter();
            writer.write(gltf, os);
        } catch (IOException e) {
            e.printStackTrace();
            throw e; // rethrow so caller knows it failed
        }
    }

    private static GlTF createGltfModel(EditableModel model, com.hiveworkshop.wc3.mdl.Animation animation) {

        GlTF gltf = new GlTF();
        Asset asset = new Asset();
        asset.setVersion("2.0");
        asset.setGenerator(model.getName());
        gltf.setAsset(asset);

        loadMeshIntoModel(model, gltf, animation);

        return gltf;
    }

    private static void loadMeshIntoModel(EditableModel model, GlTF gltf,
            com.hiveworkshop.wc3.mdl.Animation animation) {

        List<Buffer> buffers = new ArrayList<>();
        List<BufferView> bufferViews = new ArrayList<>();
        List<Accessor> accessors = new ArrayList<>();
        List<Mesh> meshes = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        List<Integer> geoNodes = new ArrayList<>();
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
            // ! The best approximation I could find so far, works well on the models I
            // tested
            glMaterial.setAlphaMode("MASK");
            glMaterial.setAlphaCutoff(0.5f); // or whatever cutoff works best

            MaterialPbrMetallicRoughness pbr = new MaterialPbrMetallicRoughness();
            pbr.setBaseColorFactor(new float[] { 1, 1, 1, 1 });
            pbr.setMetallicFactor(0.0f);
            pbr.setRoughnessFactor(1.0f);
            pbr.setBaseColorTexture(textureInfo);

            glMaterial.setPbrMetallicRoughness(pbr);
            materials.add(glMaterial);
        }
        // MESH
        log.info("Geosets: " + model.getGeosets().size());
        List<Bone> mdxBones = new ArrayList<>();
        for (final IdObject object : model.getIdObjects()) {
            if (object instanceof Bone) {
                mdxBones.add((Bone) object);
            }
        }
        Map<Bone, Integer> boneToNode = new java.util.HashMap<>();
        // First pass: create nodes (no translation yet)
        for (Bone bone : mdxBones) {
            Node boneNode = new Node();
            boneNode.setName(bone.getName());
            nodes.add(boneNode);
            boneToNode.put(bone, nodes.size() - 1);
        }
        // Establish bone hierarchy
        for (Bone bone : mdxBones) {
            IdObject parent = bone.getParent();
            if (parent instanceof Bone) {
                Integer parentIdx = boneToNode.get((Bone) parent);
                if (parentIdx != null) {
                    Node pNode = nodes.get(parentIdx);
                    List<Integer> kids = pNode.getChildren();
                    if (kids == null) {
                        kids = new ArrayList<>();
                        kids.add(boneToNode.get(bone));
                        pNode.setChildren(kids);
                    } else {
                        kids.add(boneToNode.get(bone));
                    }
                }
            }
        }
        // Second pass: assign local translations = pivot - parentPivot
        for (Bone bone : mdxBones) {
            if (bone.getPivotPoint() == null) {
                continue;
            }
            double bx = bone.getPivotPoint().x;
            double by = bone.getPivotPoint().y;
            double bz = bone.getPivotPoint().z;
            double tx = bx, ty = by, tz = bz;
            IdObject parent = bone.getParent();
            if (parent instanceof Bone) {
                Bone pb = (Bone) parent;
                if (pb.getPivotPoint() != null) {
                    tx = bx - pb.getPivotPoint().x;
                    ty = by - pb.getPivotPoint().y;
                    tz = bz - pb.getPivotPoint().z;
                }
            }
            nodes.get(boneToNode.get(bone)).setTranslation(new float[] { (float) tx, (float) ty, (float) tz });
        }
        List<Integer> topLevelBoneNodeIndices = new ArrayList<>();
        for (Bone bone : mdxBones) {
            if (!(bone.getParent() instanceof Bone)) {
                var topLevelBoneIndex = boneToNode.get(bone);
                topLevelBoneNodeIndices.add(topLevelBoneIndex); 
            }
        }

        int skinIndex = -1;
        if (!mdxBones.isEmpty()) {
            int boneCount = mdxBones.size();
            // Build inverse bind matrices: inverse(worldBind) = translate(-pivot)
            float[] ibm = new float[boneCount * 16];
            for (int i = 0; i < boneCount; i++) {
                Bone b = mdxBones.get(i);
                double px = 0, py = 0, pz = 0;
                if (b.getPivotPoint() != null) {
                    px = b.getPivotPoint().x;
                    py = b.getPivotPoint().y;
                    pz = b.getPivotPoint().z;
                }
                int o = i * 16;
                // column-major identity
                ibm[o] = 1;
                ibm[o + 5] = 1;
                ibm[o + 10] = 1;
                ibm[o + 15] = 1;
                // translation components (last column except bottom-right)
                ibm[o + 12] = (float) (-px);
                ibm[o + 13] = (float) (-py);
                ibm[o + 14] = (float) (-pz);
            }
            byte[] ibmBytes = new byte[ibm.length * 4];
            ByteBuffer.wrap(ibmBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(ibm);
            String ibmUri = "data:application/octet-stream;base64," +
                    java.util.Base64.getEncoder().encodeToString(ibmBytes);
            Buffer ibmBuffer = new Buffer();
            ibmBuffer.setByteLength(ibmBytes.length);
            ibmBuffer.setUri(ibmUri);
            buffers.add(ibmBuffer);
            int ibmBufferIndex = buffers.size() - 1;

            BufferView ibmView = new BufferView();
            ibmView.setBuffer(ibmBufferIndex);
            ibmView.setByteOffset(0);
            ibmView.setByteLength(ibmBytes.length);
            bufferViews.add(ibmView);
            int ibmViewIndex = bufferViews.size() - 1;

            Accessor ibmAccessor = new Accessor();
            ibmAccessor.setBufferView(ibmViewIndex);
            ibmAccessor.setComponentType(5126); // FLOAT
            ibmAccessor.setCount(boneCount);
            ibmAccessor.setType("MAT4");
            ibmAccessor.setByteOffset(0);
            accessors.add(ibmAccessor);
            int ibmAccessorIndex = accessors.size() - 1;

            Skin skin = new Skin();
            List<Integer> joints = new ArrayList<>();
            for (Bone b : mdxBones) {
                joints.add(boneToNode.get(b));
            }
            skin.setJoints(joints);
            skin.setInverseBindMatrices(ibmAccessorIndex);
            skins.add(skin);
            skinIndex = skins.size() - 1;
        }

        // log.info("Geosets: " + model.getGeosets().size());
        for (Geoset geoset : model.getGeosets()) {
            if (!isGeosetVisibleInAnimation(geoset, animation)) {
                log.info("Skipping geoset " + geoset.getName() + " due to visibility filter.");
                continue; // Skip geosets that are not visible in the animation
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
            var minExtents = new Number[] { geoset.getExtents().getMinimumExtent().x,
                    geoset.getExtents().getMinimumExtent().y, geoset.getExtents().getMinimumExtent().z };
            var maxExtents = new Number[] { geoset.getExtents().getMaximumExtent().x,
                    geoset.getExtents().getMaximumExtent().y, geoset.getExtents().getMaximumExtent().z };

            positionAccessor.setMax(maxExtents); // Default max extent
            positionAccessor.setMin(minExtents); // Updated to use calculated min extents
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

            Integer jointsAccessorIndex = null;
            Integer weightsAccessorIndex = null;
            if (skinIndex >= 0) {
                int vertexCount = geoset.getVertices().size();
                short[] joints = new short[vertexCount * 4];
                float[] weights = new float[vertexCount * 4];
                for (int v = 0; v < vertexCount; v++) {
                    GeosetVertex gv = geoset.getVertices().get(v);
                    List<GeosetVertexBoneLink> links = gv.getLinks();
                    int influenceCount = Math.min(links.size(), 4);
                    int total = 0;
                    for (int i = 0; i < influenceCount; i++) {
                        GeosetVertexBoneLink link = links.get(i);
                        Integer nodeIdx = boneToNode.get(link.bone);
                        if (nodeIdx == null)
                            continue;
                        joints[v * 4 + i] = (short) (int) nodeIdx;
                        weights[v * 4 + i] = link.weight;
                        total += link.weight;
                    }
                    if (total == 0 && !mdxBones.isEmpty()) {
                        joints[v * 4] = (short) (int) boneToNode.get(mdxBones.get(0));
                        weights[v * 4] = 255f;
                        total = 255;
                    }
                    for (int i = 0; i < 4; i++) {
                        weights[v * 4 + i] = weights[v * 4 + i] / 255f;
                    }
                    float sum = weights[v * 4] + weights[v * 4 + 1] + weights[v * 4 + 2] + weights[v * 4 + 3];
                    if (sum > 0) {
                        for (int i = 0; i < 4; i++) {
                            weights[v * 4 + i] /= sum;
                        }
                    }
                }
                // JOINTS buffer
                byte[] jointsBytes = new byte[joints.length * 2];
                ByteBuffer.wrap(jointsBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(joints);
                String jointsUri = "data:application/octet-stream;base64," +
                        java.util.Base64.getEncoder().encodeToString(jointsBytes);
                Buffer jointsBuffer = new Buffer();
                jointsBuffer.setByteLength(jointsBytes.length);
                jointsBuffer.setUri(jointsUri);
                buffers.add(jointsBuffer);
                int jointsBufferIndex = buffers.size() - 1;

                BufferView jointsView = new BufferView();
                jointsView.setBuffer(jointsBufferIndex);
                jointsView.setByteOffset(0);
                jointsView.setByteLength(jointsBytes.length);
                jointsView.setTarget(34962);
                bufferViews.add(jointsView);
                int jointsViewIndex = bufferViews.size() - 1;

                Accessor jointsAccessor = new Accessor();
                jointsAccessor.setBufferView(jointsViewIndex);
                jointsAccessor.setComponentType(5123); // UNSIGNED_SHORT
                jointsAccessor.setCount(vertexCount);
                jointsAccessor.setType("VEC4");
                jointsAccessor.setByteOffset(0);
                accessors.add(jointsAccessor);
                jointsAccessorIndex = accessors.size() - 1;

                // WEIGHTS buffer
                byte[] weightsBytes = new byte[weights.length * 4];
                ByteBuffer.wrap(weightsBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(weights);
                String weightsUri = "data:application/octet-stream;base64," +
                        java.util.Base64.getEncoder().encodeToString(weightsBytes);
                Buffer weightsBuffer = new Buffer();
                weightsBuffer.setByteLength(weightsBytes.length);
                weightsBuffer.setUri(weightsUri);
                buffers.add(weightsBuffer);
                int weightsBufferIndex = buffers.size() - 1;

                BufferView weightsView = new BufferView();
                weightsView.setBuffer(weightsBufferIndex);
                weightsView.setByteOffset(0);
                weightsView.setByteLength(weightsBytes.length);
                weightsView.setTarget(34962);
                bufferViews.add(weightsView);
                int weightsViewIndex = bufferViews.size() - 1;

                Accessor weightsAccessor = new Accessor();
                weightsAccessor.setBufferView(weightsViewIndex);
                weightsAccessor.setComponentType(5126); // FLOAT
                weightsAccessor.setCount(vertexCount);
                weightsAccessor.setType("VEC4");
                weightsAccessor.setByteOffset(0);
                accessors.add(weightsAccessor);
                weightsAccessorIndex = accessors.size() - 1;
            }

            Mesh mesh = new Mesh();
            MeshPrimitive primitive = new MeshPrimitive();
            primitive.setAttributes(Map.of(
                    "POSITION", positionAccessorIndex,
                    "TEXCOORD_0", uvAccessorIndex));
            if (jointsAccessorIndex != null && weightsAccessorIndex != null) {
                primitive.setAttributes(Map.of(
                        "POSITION", positionAccessorIndex,
                        "TEXCOORD_0", uvAccessorIndex,
                        "JOINTS_0", jointsAccessorIndex,
                        "WEIGHTS_0", weightsAccessorIndex));
            }
            primitive.setIndices(indicesAccessorIndex);
            primitive.setMode(4); // TRIANGLES
            primitive.setMaterial(data.materialIndex);
            mesh.setPrimitives(Arrays.asList(primitive));
            meshes.add(mesh);
            var meshIndex = meshes.size() - 1; // Get the index of the mesh

            Node node = new Node();
            node.setMesh(meshIndex);
            if (skinIndex >= 0) {
                node.setSkin(skinIndex);
            }
            node.setName(geoset.getName());
            nodes.add(node);
            geoNodes.add(nodes.size() - 1);
        }

        List<Integer> rootChildren = new ArrayList<>();
        rootChildren.addAll(geoNodes);

        Node rootNode = new Node();
        rootNode.setName("Root");
        rootNode.setChildren(topLevelBoneNodeIndices); // only bones because glTF validator complains if we add skinned meshes as children to a node
        rootNode.setRotation(new float[] { -0.7071068f, 0, 0, 0.7071068f }); // lazy rotation to match
                                                                                             // expected axis
        nodes.add(rootNode); 
        var rootNodeIndex = nodes.size() - 1;
        rootChildren.add(rootNodeIndex); // Add root node to the scene

        Scene scene = new Scene();
        scene.setNodes(rootChildren);
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
        if (!skins.isEmpty()) { // NEW set skins only if present
            gltf.setSkins(skins);
        }
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
    // - If has Visibility/Alpha AnimFlag (non-global) => sample a few times in
    // [start,end]
    private static boolean isGeosetVisibleInAnimation(Geoset geoset, com.hiveworkshop.wc3.mdl.Animation animation) {
        if (isGeosetTeamGlow(geoset)) { // don't export the team glow geometry
            return false; // Always export team glow geosets
        }

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

    private static boolean isGeosetTeamGlow(Geoset geoset) {
        // ge the material
        if (geoset.getMaterial() == null) {
            return false; // cannot be team glow, team glow has a material
        }
        var material = geoset.getMaterial();
        if (material.getLayers().size() == 0) {
            return false; // no layers, cannot be team glow
        }
        if (material.getLayers().size() > 1) {
            return false; // Multiple layers, cannot be team glow
        }
        var layer = material.getLayers().get(0);
        for (Map.Entry<ShaderTextureTypeHD, Bitmap> entry : layer.getShaderTextures().entrySet()) {
            if (entry.getValue().getReplaceableId() == 2) {
                return true; // Team glow texture found
            }
        }
        return false;
    }

    // Best-effort sampler without wiring a full AnimatedRenderEnvironment:
    private static float sampleGeosetVisibilityAtTime(GeosetAnim ga, int time) {
        try {
            // Prefer the existing helper if available
            // (If you later wire an AnimatedRenderEnvironment, replace with:
            // ga.getRenderVisibility(envAt(time))
            // )
            var visFlag = ga.getVisibilityFlag();
            if (visFlag == null) {
                double staticAlpha = ga.getStaticAlpha();
                return (float) (staticAlpha == -1 ? 1.0 : staticAlpha);
            }

            // Heuristic: if flag has only one keyframe, use its value; otherwise assume
            // visible
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
                // times is sorted, hopefully no duplicates, so Collections.binarySearch is
                // behaving like a bisect left
                int keyframeTime = Collections.binarySearch(visFlag.getTimes(), time);
                Object v = visFlag.getValues().get(keyframeTime);
                if (v instanceof Number) {
                    float vis = ((Number) v).floatValue();
                    if (vis < 0.9f) { // Threshold for visibility, I assume alpha is generally either 0 or 1, so 0.9
                                      // is as good as any value
                        return vis; // Return the visibility value
                    }
                }
                return 1.0f; // Fallback to visible
            }
        } catch (Throwable t) {
            // Fallback: do not exclude on errors
            double staticAlpha = ga.getStaticAlpha();
            return (float) (staticAlpha == -1 ? 1.0 : staticAlpha);
        }
    }

    private static EditableModel loadModel(String path) {
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
