package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.scripts.ChangeAnimationLengthFrame;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScriptActions {
    static void mergeGeosetActionRes(MainPanel mainPanel) throws IOException {
        FileDialog fileDialog = new FileDialog(mainPanel);

//        mainPanel.fc.setDialogTitle("Merge Single Geoset (Oinker-based)");
//
        final EditableModel current = mainPanel.currentMDL();
//        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
//            mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
//        } else if (mainPanel.profile.getPath() != null) {
//            mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
//        }
//        final int returnValue = mainPanel.fc.showOpenDialog(mainPanel);
        final EditableModel geoSource = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);

//        if (returnValue == JFileChooser.APPROVE_OPTION) {
//            mainPanel.currentFile = mainPanel.fc.getSelectedFile();
//            final EditableModel geoSource = MdxUtils.loadEditable(mainPanel.currentFile);
//            mainPanel.profile.setPath(mainPanel.currentFile.getParent());
        if (geoSource != null) {
            boolean going = true;
            Geoset host = null;
            while (going) {
                final String s = JOptionPane.showInputDialog(mainPanel,
                        "Geoset into which to Import: (1 to " + current.getGeosetsSize() + ")");
                try {
                    final int x = Integer.parseInt(s);
                    if ((x >= 1) && (x <= current.getGeosetsSize())) {
                        host = current.getGeoset(x - 1);
                        going = false;
                    }
                } catch (final NumberFormatException ignored) {

                }
            }
            Geoset newGeoset = null;
            going = true;
            while (going) {
                final String s = JOptionPane.showInputDialog(mainPanel,
                        "Geoset to Import: (1 to " + geoSource.getGeosetsSize() + ")");
                try {
                    final int x = Integer.parseInt(s);
                    if ((x >= 1) && x <= geoSource.getGeosetsSize()) {
                        newGeoset = geoSource.getGeoset(x - 1);
                        going = false;
                    }
                } catch (final NumberFormatException ignored) {

                }
            }
            newGeoset.updateToObjects(current);
            System.out.println("putting " + newGeoset.numUVLayers() + " into a nice " + host.numUVLayers());
            for (int i = 0; i < newGeoset.numVerteces(); i++) {
	            final GeosetVertex ver = newGeoset.getVertex(i);
	            host.add(ver);
	            ver.setGeoset(host);// geoset = host;
	            // for( int z = 0; z < host.n.numUVLayers(); z++ ){
	            // host.getUVLayer(z).addTVertex(newGeoset.getVertex(i).getTVertex(z));}
            }
            for (int i = 0; i < newGeoset.numTriangles(); i++) {
                final Triangle tri = newGeoset.getTriangle(i);
                host.add(tri);
                tri.setGeoRef(host);
            }
        }

        mainPanel.fc.setSelectedFile(null);
    }

//    static void exportAnimatedFramePNG(MainPanel mainPanel) {
//        final BufferedImage fBufferedImage = mainPanel.currentModelPanel().getAnimationViewer().getBufferedImage();
//
//        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
//            final EditableModel current = mainPanel.currentMDL();
//            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
//                mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
//            } else if (mainPanel.profile.getPath() != null) {
//                mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
//            }
//        }
//        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
//            mainPanel.exportTextureDialog.setSelectedFile(new File(mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator));
//        }
//
//        final int x = mainPanel.exportTextureDialog.showSaveDialog(mainPanel);
//        if (x == JFileChooser.APPROVE_OPTION) {
//            final File file = mainPanel.exportTextureDialog.getSelectedFile();
//            if (file != null) {
//                try {
//                    if (file.getName().lastIndexOf('.') >= 0) {
//                        BufferedImage bufferedImage = fBufferedImage;
//                        String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
//                                .toUpperCase();
//                        if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
//                                || fileExtension.equals("JPEG")) {
//                            JOptionPane.showMessageDialog(mainPanel,
//                                    "Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
//                            bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
//                        }
//                        if (fileExtension.equals("BLP")) {
//                            fileExtension = "blp";
//                        }
//                        final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
//                        if (!write) {
//                            JOptionPane.showMessageDialog(mainPanel, "File type unknown or unavailable");
//                        }
//                    } else {
//                        JOptionPane.showMessageDialog(mainPanel, "No file type was specified");
//                    }
//                } catch (final Exception e1) {
//                    ExceptionPopup.display(e1);
//                    e1.printStackTrace();
//                }
//            } else {
//                JOptionPane.showMessageDialog(mainPanel, "No output file was specified");
//            }
//        }
//    }

    static void exportAnimatedToStaticMesh(MainPanel mainPanel) {
        if (!mainPanel.animationModeState) {
            JOptionPane.showMessageDialog(mainPanel, "You must be in the Animation Editor to use that!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final Vec4 vertexHeap = new Vec4();
        final Vec4 appliedVertexHeap = new Vec4();
        final Vec4 vertexSumHeap = new Vec4();
        final Vec4 normalHeap = new Vec4();
        final Vec4 appliedNormalHeap = new Vec4();
        final Vec4 normalSumHeap = new Vec4();

        final ModelPanel modelContext = mainPanel.currentModelPanel();
        final RenderModel editorRenderModel = modelContext.getEditorRenderModel();
        final EditableModel model = modelContext.getModel();
        final ModelViewManager modelViewManager = modelContext.getModelViewManager();

        final EditableModel snapshotModel = EditableModel.deepClone(model, model.getHeaderName() + "At"
                + editorRenderModel.getAnimatedRenderEnvironment().getAnimationTime());

        for (int geosetIndex = 0; geosetIndex < snapshotModel.getGeosets().size(); geosetIndex++) {
            final Geoset geoset = model.getGeoset(geosetIndex);
            final Geoset snapshotGeoset = snapshotModel.getGeoset(geosetIndex);

            for (int vertexIndex = 0; vertexIndex < geoset.getVertices().size(); vertexIndex++) {
                final GeosetVertex vertex = geoset.getVertex(vertexIndex);
                final GeosetVertex snapshotVertex = snapshotGeoset.getVertex(vertexIndex);
                final List<Bone> bones = vertex.getBones();
                vertexHeap.x = (float) vertex.x;
                vertexHeap.y = (float) vertex.y;
                vertexHeap.z = (float) vertex.z;
                vertexHeap.w = 1;

                if (bones.size() > 0) {

                    vertexSumHeap.set(0, 0, 0, 0);
                    for (final Bone bone : bones) {
                        editorRenderModel.getRenderNode(bone).getWorldMatrix().transform(vertexHeap, appliedVertexHeap);
                        vertexSumHeap.add(appliedVertexHeap);
                    }

                    final int boneCount = bones.size();
                    vertexSumHeap.x /= boneCount;
                    vertexSumHeap.y /= boneCount;
                    vertexSumHeap.z /= boneCount;
                    vertexSumHeap.w /= boneCount;
                } else {
                    vertexSumHeap.set(vertexHeap);
                }
                snapshotVertex.x = vertexSumHeap.x;
                snapshotVertex.y = vertexSumHeap.y;
                snapshotVertex.z = vertexSumHeap.z;

                normalHeap.x = (float) vertex.getNormal().x;
                normalHeap.y = (float) vertex.getNormal().y;
                normalHeap.z = (float) vertex.getNormal().z;
                normalHeap.w = 0;
                if (bones.size() > 0) {

                    normalSumHeap.set(0, 0, 0, 0);
                    for (final Bone bone : bones) {
                        editorRenderModel.getRenderNode(bone).getWorldMatrix().transform(normalHeap, appliedNormalHeap);
                        normalSumHeap.add(appliedNormalHeap);
                    }

                    if (normalSumHeap.length() > 0) {
                        normalSumHeap.normalize();
                    } else {
                        normalSumHeap.set(0, 1, 0, 0);
                    }
                } else {
                    normalSumHeap.set(normalHeap);
                }
                snapshotVertex.getNormal().x = normalSumHeap.x;
                snapshotVertex.getNormal().y = normalSumHeap.y;
                snapshotVertex.getNormal().z = normalSumHeap.z;
            }
        }
        snapshotModel.getIdObjects().clear();
        final Bone boneRoot = new Bone("Bone_Root");
        boneRoot.setPivotPoint(new Vec3(0, 0, 0));
        snapshotModel.add(boneRoot);

        for (final Geoset geoset : snapshotModel.getGeosets()) {
            for (final GeosetVertex vertex : geoset.getVertices()) {
                vertex.getBones().clear();
                vertex.getBones().add(boneRoot);
            }
        }

        final Iterator<Geoset> geosetIterator = snapshotModel.getGeosets().iterator();

        while (geosetIterator.hasNext()) {
            final Geoset geoset = geosetIterator.next();
            final GeosetAnim geosetAnim = geoset.getGeosetAnim();

            if (geosetAnim != null) {
                final Object visibilityValue = geosetAnim.getVisibilityFlag().interpolateAt(editorRenderModel.getAnimatedRenderEnvironment());

                if (visibilityValue instanceof Float) {
                    final Float visibility = (Float) visibilityValue;
                    final double visvalue = visibility;

                    if (visvalue < 0.01) {
                        geosetIterator.remove();
                        snapshotModel.remove(geosetAnim);
                    }
                }

            }
        }
        snapshotModel.getAnims().clear();
        snapshotModel.add(new Animation("Stand", 333, 1333));
        final List<AnimFlag> allAnimFlags = snapshotModel.getAllAnimFlags();
        for (final AnimFlag flag : allAnimFlags) {
            if (!flag.hasGlobalSeq()) {
                if (flag.size() > 0) {
                    final Object value = flag.interpolateAt(mainPanel.animatedRenderEnvironment);
                    flag.setInterpType(InterpolationType.DONT_INTERP);
                    flag.getValues().clear();
                    flag.getTimes().clear();
                    flag.getInTans().clear();
                    flag.getOutTans().clear();
                    flag.addEntry(333, value);
                }
            }
        }

        FileDialog fileDialog = new FileDialog(mainPanel);
        fileDialog.onClickSaveAs(snapshotModel, FileDialog.SAVE_MODEL, false);

//        mainPanel.fc.setDialogTitle("Export Static Snapshot");
//        final int result = mainPanel.fc.showSaveDialog(mainPanel);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            File selectedFile = mainPanel.fc.getSelectedFile();
//            if (selectedFile != null) {
//                if (!selectedFile.getPath().toLowerCase().endsWith(".mdx")) {
//                    selectedFile = new File(selectedFile.getPath() + ".mdx");
//                }
//                try {
//                    MdxUtils.saveMdx(snapshotModel, selectedFile);
//                } catch (final IOException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
//            }
//        }
    }

    static void combineAnimations(MainPanel mainPanel) {
        final List<Animation> anims = mainPanel.currentMDL().getAnims();
        final Animation[] array = anims.toArray(new Animation[0]);
        final Object choice = JOptionPane.showInputDialog(mainPanel, "Pick the first animation",
                "Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
        final Animation animation = (Animation) choice;

        final Object choice2 = JOptionPane.showInputDialog(mainPanel, "Pick the second animation",
                "Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
        final Animation animation2 = (Animation) choice2;

        final String nameChoice = JOptionPane.showInputDialog(mainPanel,
                "What should the combined animation be called?");
        if (nameChoice != null) {
            final int anim1Length = animation.getEnd() - animation.getStart();
            final int anim2Length = animation2.getEnd() - animation2.getStart();
            final int totalLength = anim1Length + anim2Length;

            final EditableModel model = mainPanel.currentMDL();
            final int animTrackEnd = model.animTrackEnd();
            final int start = animTrackEnd + 1000;
            animation.copyToInterval(start, start + anim1Length, model.getAllAnimFlags(),
                    model.sortedIdObjects(EventObject.class));
            animation2.copyToInterval(start + anim1Length, start + totalLength, model.getAllAnimFlags(),
                    model.sortedIdObjects(EventObject.class));

            final Animation newAnimation = new Animation(nameChoice, start, start + totalLength);
            model.add(newAnimation);
            newAnimation.setNonLooping(true);
            newAnimation.setExtents(new ExtLog(animation.getExtents()));
            JOptionPane.showMessageDialog(mainPanel,
                    "DONE! Made a combined animation called " + newAnimation.getName(), "Success",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    static void scaleAnimations(MainPanel mainPanel) {
        // if( disp.animpanel == null )
        // {
        // AnimationPanel panel = new UVPanel(disp);
        // disp.setUVPanel(panel);
        // panel.showFrame();
        // }
        // else if(!disp.uvpanel.frameVisible() )
        // {
        // disp.uvpanel.showFrame();
        // }
        final ChangeAnimationLengthFrame aFrame = new ChangeAnimationLengthFrame(mainPanel.currentModelPanel(), () -> mainPanel.timeSliderPanel.revalidateKeyframeDisplay());
        aFrame.setVisible(true);
    }

    static void nullmodelButtonActionRes(MainPanel mainPanel) {
        nullModelFile(mainPanel);
        MenuBarActions.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    public static String incName(final String name) {
        String output = name;

//        int depth = 1;
//        boolean continueLoop = name != null && output.length()>0;
//        while (continueLoop) {
//            char c = '0';
//            try {
//                c = output.charAt(output.length() - depth);
//            } catch (final IndexOutOfBoundsException e) {
//                // c remains '0', name is not changed (this should only happen if name is "9" or "99...9")
//                continueLoop = false;
//            }
//            for (char n = '0'; (n < '9') && continueLoop; n++) {
//                if (c == n) {
//                    char x = c;
//                    x++;
//                    output = output.substring(0, output.length() - depth) + x
//                            + output.substring((output.length() - depth) + 1);
//                    continueLoop = false;
//                }
//            }
//            if (c == '9') {
//                output = output.substring(0, output.length() - depth) + 0
//                        + output.substring((output.length() - depth) + 1);
//            } else if (continueLoop) {
//                output = output.substring(0, (output.length() - depth) + 1) + 1
//                        + output.substring((output.length() - depth) + 1);
//                continueLoop = false;
//            }
//            depth++;
//        }
//        if (output == null) {
//            output = "name error";
//        } else if (output.equals(name)) {
//            output = output + "_edit";
//        }

        if (output != null) {
            for (int offsetFromEnd = 1; offsetFromEnd <= output.length(); offsetFromEnd++) {
                char charAt = output.charAt(output.length() - offsetFromEnd);
                if ('0' <= charAt && charAt <= '8') {
                    int numberLocation = output.length() - offsetFromEnd;
                    output = output.substring(0, numberLocation) + (charAt + 1) + output.substring((numberLocation) + 1);
                    break;
                } else if (charAt == '9') {
                    int numberLocation = output.length() - offsetFromEnd;
                    output = output.substring(0, numberLocation) + "0" + output.substring(numberLocation + 1);
                    if (numberLocation == 0) {
                        output = 1 + output; // if name == "999...9" -> output = "1000...0" instead of "000...0"
                    }
                } else { // charAt is not a digit
                    int numberLocation = output.length() - offsetFromEnd;
                    output = output.substring(0, numberLocation + 1) + "1" + output.substring(numberLocation + 1);
                    break;
                }
            }
        }

        if (output == null) {
            output = "name error";
        }
        if (output.equals(name)) {
            output = output + "_edit";
        }

        return output;
    }

    public static void nullModelFile(MainPanel mainPanel) {
        final EditableModel currentMDL = mainPanel.currentMDL();
        if (currentMDL != null) {
            final EditableModel newModel = new EditableModel();
            newModel.copyHeaders(currentMDL);
            if (newModel.getFileRef() == null) {
                newModel.setFileRef(
                        new File(System.getProperty("java.io.tmpdir") + "MatrixEaterExtract/matrixeater_anonymousMDL",
                                "" + (int) (Math.random() * Integer.MAX_VALUE) + ".mdl"));
            }
            while (newModel.getFile().exists()) {
                newModel.setFileRef(new File(currentMDL.getFile().getParent() + "/" + incName(newModel.getName()) + ".mdl"));
            }
            mainPanel.importPanel = new ImportPanel(newModel, EditableModel.deepClone(currentMDL, "CurrentModel"));

            final Thread watcher = new Thread(() -> {
                while (mainPanel.importPanel.getParentFrame().isVisible()
                        && (!mainPanel.importPanel.importStarted()
                        || mainPanel.importPanel.importEnded())) {
                    trySleep();
                }
                // if( !importPanel.getParentFrame().isVisible() &&
                // !importPanel.importEnded() )
                // JOptionPane.showMessageDialog(null,"bad voodoo
                // "+importPanel.importSuccessful());
                // else
                // JOptionPane.showMessageDialog(null,"good voodoo
                // "+importPanel.importSuccessful());
                // if( importPanel.importSuccessful() )
                // {
                // newModel.saveFile();
                // loadFile(newModel.getFile());
                // }

                if (mainPanel.importPanel.importStarted()) {
                    while (!mainPanel.importPanel.importEnded()) {
                        trySleep();
                    }

                    if (mainPanel.importPanel.importSuccessful()) {
                        try {
                            MdxUtils.saveMdx(newModel, newModel.getFile());
                        } catch (final IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        MPQBrowserView.loadFile(mainPanel, newModel.getFile());
                    }
                }
            });
            watcher.start();
        }
    }

    private static void trySleep() {
        try {
            Thread.sleep(1);
        } catch (final Exception e) {
            ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
        }
    }

    static void jokeButtonClickResponse(MainPanel mainPanel) {
        final StringBuilder sb = new StringBuilder();
        for (final File file : new File(
                "C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\CustomMapData\\LuaFpsMap\\Maps\\MultiplayerFun004")
                .listFiles()) {
            if (!file.getName().toLowerCase().endsWith("_init.txt")) {
                sb.setLength(0);
                try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("BlzSetAbilityActivatedIcon")) {
                            final int startIndex = line.indexOf('"') + 1;
                            final int endIndex = line.lastIndexOf('"');
                            final String dataString = line.substring(startIndex, endIndex);
                            sb.append(dataString);
                        }
                    }
                } catch (final IOException e1) {
                    e1.printStackTrace();
                }
                final String dataString = sb.toString();
                for (int i = 0; (i + 23) < dataString.length(); i += 24) {
                    final Geoset geo = new Geoset();
                    mainPanel.currentMDL().addGeoset(geo);
                    geo.setParentModel(mainPanel.currentMDL());
                    geo.setMaterial(new Material(new Layer("Blend", new Bitmap("textures\\white.blp"))));
                    final String data = dataString.substring(i, i + 24);
                    final int x = Integer.parseInt(data.substring(0, 3));
                    final int y = Integer.parseInt(data.substring(3, 6));
                    final int z = Integer.parseInt(data.substring(6, 9));
                    final int sX = Integer.parseInt(data.substring(9, 10));
                    final int sY = Integer.parseInt(data.substring(10, 11));
                    final int sZ = Integer.parseInt(data.substring(11, 12));
                    final int red = Integer.parseInt(data.substring(12, 15));
                    final int green = Integer.parseInt(data.substring(15, 18));
                    final int blue = Integer.parseInt(data.substring(18, 21));
                    final int alpha = Integer.parseInt(data.substring(21, 24));
                    final GeosetAnim forceGetGeosetAnim = geo.forceGetGeosetAnim();
                    forceGetGeosetAnim.setStaticColor(new Vec3(blue / 255.0, green / 255.0, red / 255.0));
                    forceGetGeosetAnim.setStaticAlpha(alpha / 255.0);
                    System.out.println(x + "," + y + "," + z);

                    final ModelUtils.Mesh mesh = ModelUtils.createBox(new Vec3(x * 10, y * 10, z * 10),
		                    new Vec3((x * 10) + (sX * 10), (y * 10) + (sY * 10), (z * 10) + (sZ * 10)), 1, 1,
		                    1, geo);
	                geo.getVertices().addAll(mesh.getVertices());
	                geo.getTriangles().addAll(mesh.getTriangles());
                }
            }

        }
	    mainPanel.modelStructureChangeListener.geosetsAdded(new ArrayList<>(mainPanel.currentMDL().getGeosets()));
    }
}
