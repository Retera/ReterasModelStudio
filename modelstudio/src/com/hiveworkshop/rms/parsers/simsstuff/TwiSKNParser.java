package com.hiveworkshop.rms.parsers.simsstuff;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class TwiSKNParser {
	TreeMap<Integer, SimsBone> boneTreeMap = new TreeMap<>();
	String name;
	String texture;
	SimModel simModel;
	SimsSkeleton simsSkeleton;

	public static void loadModel() {
		JTextArea textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea);
		JPanel jPanel = new JPanel(new MigLayout("fill, ins 0"));
		scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		jPanel.add(scrollPane, "growx, growy, wrap");
		JButton validate = new JButton("validate");
		validate.addActionListener((e) -> {
			(new TwiSKNParser(textArea.getText().split("\n"))).makeModel();
		});
		jPanel.add(validate);
		int opt = JOptionPane.showConfirmDialog(null, jPanel, "pasteModelStuff", 2);
		if (opt == 0) {
			String s = textArea.getText();
			TwiSKNParser parser = new TwiSKNParser(s.split("\n"));
			EditableModel model = parser.makeModel();
			ModelLoader.loadModel(true, true, new ModelPanel(new ModelHandler(model, null)));
		}

	}

	public static void loadModel(String s) {
		TwiSKNParser parser = new TwiSKNParser(s.split("\n"));
		ModelLoader.loadModel(true, true, new ModelPanel(new ModelHandler(parser.makeModel(), null)));
	}

	public TwiSKNParser() {
	}

	public TwiSKNParser(String[] lines) {
		this.simModel = new SimModel();
		LineReaderThingi lineReaderThingi = new LineReaderThingi(lines);
		this.simsSkeleton = new SimsSkeleton();
		this.name = lineReaderThingi.readString();
		this.texture = lineReaderThingi.readString();
		this.simModel.setName(this.name);
		this.simModel.setTexture(this.texture);
		this.readBones(lineReaderThingi, this.simsSkeleton);
		this.readFaces(lineReaderThingi);
		this.readBoneBindings(lineReaderThingi);
		this.readUVs(lineReaderThingi);
		this.readBlendData(lineReaderThingi);
		this.readVerts(lineReaderThingi);
	}

	public TwiSKNParser parseModel(String s) {
		this.simModel = new SimModel();
		LineReaderThingi lineReaderThingi = new LineReaderThingi(s);
		this.simsSkeleton = new SimsSkeleton();
		this.name = lineReaderThingi.readString();
		this.texture = lineReaderThingi.readString();
		this.simModel.setName(this.name);
		this.simModel.setTexture(this.texture);
		this.readBones(lineReaderThingi, this.simsSkeleton);
		this.readFaces(lineReaderThingi);
		this.readBoneBindings(lineReaderThingi);
		this.readUVs(lineReaderThingi);
		this.readBlendData(lineReaderThingi);
		this.readVerts(lineReaderThingi);
		return this;
	}

	private void readVerts(LineReaderThingi lineReaderThingi) {
		int numVerts = lineReaderThingi.readInt();
		int realVerts = this.simModel.getTextureIndexMap().size();

		for(int i = 0; i < realVerts; ++i) {
			this.simModel.addVertex(new SimsVert(i, false, lineReaderThingi));
		}

		for(int i = 0; i < numVerts - realVerts; ++i) {
			this.simModel.addVertex(new SimsVert(i, true, lineReaderThingi));
		}

	}

	private void readBlendData(LineReaderThingi lineReaderThingi) {
		int numWeigths = lineReaderThingi.readInt();

		for(int i = 0; i < numWeigths; ++i) {
			this.simModel.addWeight(i, lineReaderThingi.readInts());
		}

	}

	private void readUVs(LineReaderThingi lineReaderThingi) {
		int numTexVerts = lineReaderThingi.readInt();

		for(int i = 0; i < numTexVerts; ++i) {
			this.simModel.addUV(new SimsTexVert(i, lineReaderThingi));
		}

	}

	private void readBoneBindings(LineReaderThingi lineReaderThingi) {
		int numBonesBindings = lineReaderThingi.readInt();
		Map<Integer, SimsBone> boneTreeMap = this.simModel.getBoneTreeMap();

		for(int i = 0; i < numBonesBindings; ++i) {
			boneTreeMap.get(i).setStuff(lineReaderThingi);
		}

	}

	private void readFaces(LineReaderThingi lineReaderThingi) {
		int numFaces = lineReaderThingi.readInt();

		for(int i = 0; i < numFaces; ++i) {
			this.simModel.addFaceIndex(i, new SimsFace(lineReaderThingi));
		}

	}

	private void readBones(LineReaderThingi lineReaderThingi, SimsSkeleton simsSkeleton) {
		int numBones = lineReaderThingi.readInt();

		for(int i = 0; i < numBones; ++i) {
			SimsBone value = simsSkeleton.getBone(lineReaderThingi.readString()).setIndex(i);
			this.simModel.addBone(value);
		}

	}

	public EditableModel makeModel() {
		this.simModel.fillMaps();
		EditableModel model = new EditableModel(this.simModel.getName());
		Animation stand = new Animation("Stand", 0, 500);
		model.add(stand);
		String homeProfile = System.getProperty("user.home");
		model.setFileRef(new File(homeProfile + "\\Documents\\" + this.simModel.getName() + ".mdl"));
		model.setExtents(new ExtLog());
		Bitmap bitmap = new Bitmap(this.simModel.getTexture());
		model.add(bitmap);
		Material material = new Material(new Layer(bitmap));
		model.add(material);
		Geoset geoset = new Geoset();
		geoset.setMaterial(material);
		model.add(geoset);
		Geoset blendVerts = new Geoset();
		blendVerts.setMaterial(material);

		BiMap<SimsVert, GeosetVertex> vertMap = this.simModel.getVertMap();
		System.out.println("adding " + vertMap.values().size() + " vetrices to the geoset!");
		for (GeosetVertex vertex : vertMap.values()) {
			if (vertex.getTriangles().isEmpty()) {
				vertex.setGeoset(blendVerts);
				blendVerts.add(vertex);
				SimsVert var10001 = vertMap.getByValue(vertex);
				System.out.println("Vert wo tris: " + var10001.getIndex());
			} else {
				vertex.setGeoset(geoset);
				geoset.add(vertex);
			}
		}

		for(GeosetVertex vertex : simModel.getBlendVertMap().values()){
			vertex.setGeoset(blendVerts);
			blendVerts.add(vertex);
		}

		if (0 < blendVerts.numVerteces()) {
			model.add(blendVerts);
		}

		System.out.println("vertices wo tris: " + blendVerts.getVertices().size());
		System.out.println("adding " + this.simModel.getFaceMap().size() + " triangles to the geoset!");

		for (Triangle triangle : this.simModel.getFaceMap().values()) {
			triangle.setGeoset(geoset);
			for (GeosetVertex vertex1 : triangle.getVerts()) {
				if (!geoset.contains(vertex1)) {
					System.out.println("vertex: " + vertex1 + " not in geoset! in map: " + vertMap.containsValue(vertex1) + ", " + vertMap.values().contains(vertex1));
				}
			}

			geoset.add(triangle);
		}

		for (SimsBone simsBone : this.simsSkeleton.getSkinBoneMap().values()) {
			Bone bone = new Bone(simsBone.getName());
			Vec3AnimFlag transl = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
			transl.addEntry(0, simsBone.getPos(), stand);
			bone.add(transl);
			QuatAnimFlag rot = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
			rot.addEntry(0, simsBone.getQuat(), stand);
			bone.add(rot);
//			bone.setPivotPoint(simsBone.getWorldLoc());
			model.add(bone);

			for (int i = 0; i < simsBone.getRawVertexCount(); ++i) {
				int vertId = simsBone.getRawVertexIndex() + i;
				GeosetVertex vertex1 = this.simModel.getVertex(vertId);
				vertex1.addBoneAttachment(bone);
			}

			for (int i = 0; i < simsBone.getBlendedVertexCount(); ++i) {
				int vertId = simsBone.getBlendedVertexIndex() + i;
				GeosetVertex vertex1 = this.simModel.getBlendVertex(vertId);
				vertex1.addBoneAttachment(bone);
			}

			IdObject object = model.getObject(simsBone.getParName());
			if (object != null) {
				bone.setParent(object);
			}
		}
//		for (SimsBone simsBone : this.simsSkeleton.getSkinBoneMap().values()) {
//			Bone bone = new Bone(simsBone.getName());
//			bone.setPivotPoint(simsBone.getWorldLoc());
//			model.add(bone);
//
//			for (int i = 0; i < simsBone.getRawVertexCount(); ++i) {
//				int vertId = simsBone.getRawVertexIndex() + i;
//				GeosetVertex vertex1 = this.simModel.getVertex(vertId);
//				vertex1.addBoneAttachment(bone);
//			}
//
//			for (int i = 0; i < simsBone.getBlendedVertexCount(); ++i) {
//				int vertId = simsBone.getBlendedVertexIndex() + i;
//				GeosetVertex vertex1 = this.simModel.getBlendVertex(vertId);
//				vertex1.addBoneAttachment(bone);
//			}
//
//			IdObject object = model.getObject(simsBone.getParName());
//			if (object != null) {
//				bone.setParent(object);
//			}
//		}

		return model;
	}

	public SimModel getSimModel() {
		return this.simModel;
	}

	public SimModel makeHeadFromCurrentModel(EditableModel model) {
		SimModel simModel = new SimModel();
		simModel.setName(model.getName());
		Bitmap texture = model.getTexture(0);
		if (texture != null) {
			simModel.setTexture(texture.getName());
		} else {
			simModel.setTexture("TEXTURE_NAME_HERE");
		}

		int vertIndex = 0;
		int faceIndex = 0;

		for (Geoset geoset : model.getGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				vertIndex++;
				if (!vertex.getTriangles().isEmpty()) {
					simModel.addUV(new SimsTexVert(vertIndex, vertex.getTVertex(0)));
				}

				simModel.addVertex(new SimsVert(vertIndex, vertex.getTriangles().isEmpty(), vertex, vertex.getNormal()));
			}

			for (Triangle triangle : geoset.getTriangles()) {

				faceIndex++;
				SimsFace value = new SimsFace(triangle.getId(0), triangle.getId(1), triangle.getId(2));
				simModel.addFaceIndex(faceIndex, value);
			}
		}

		simModel.addBone((new SimsBone(0, "HEAD")).setRawVertexIndex(0).setRawVertexCount(vertIndex).setBlendedVertexIndex(-1).setBlendedVertexCount(0));
		simModel.addWeight(0, new int[]{0, 0});
		return simModel;
	}

	public SimModel makeModelCurrentModel(EditableModel model) {
		SimModel simModel = new SimModel();
		simModel.setName(model.getName());
		Bitmap texture = model.getTexture(0);
		if (texture != null) {
			simModel.setTexture(texture.getName());
		} else {
			simModel.setTexture("TEXTURE_NAME_HERE");
		}

		Map<Bone, List<GeosetVertex>> totBoneMap = new HashMap<>();

		for (Geoset geoset : model.getGeosets()) {
			Map<Bone, List<GeosetVertex>> boneMap = geoset.getBoneMap();

			for (Bone bone : boneMap.keySet()) {
				totBoneMap.computeIfAbsent(bone, (k) -> new ArrayList<>()).addAll(boneMap.get(bone));
			}
		}

		List<Bone> sortedBones = this.setupHierarchy(totBoneMap.keySet());
		int totRealVerts = 0;
		int totBlendVerts = 0;
		Set<GeosetVertex> addedVerts = new HashSet<>();
		Map<GeosetVertex, SimsVert> vertMap = new HashMap<>();

		for(int faceIndex = 0; faceIndex < sortedBones.size(); ++faceIndex) {
			int realVerts = 0;
			int blendVerts = 0;
			Bone bone = sortedBones.get(faceIndex);

			for (GeosetVertex vertex : totBoneMap.get(bone)) {
				if (!addedVerts.contains(vertex)) {
					SimsVert value;
					if (vertex.getTriangles().isEmpty()) {
						value = new SimsVert(totBlendVerts + blendVerts, true, vertex, vertex.getNormal());
						++blendVerts;
					} else {
						simModel.addUV(new SimsTexVert(totRealVerts + realVerts, vertex.getTVertex(0)));
						value = new SimsVert(totRealVerts + realVerts, false, vertex, vertex.getNormal());
						++realVerts;
					}

					simModel.addVertex(value);
					vertMap.put(vertex, value);
					addedVerts.add(vertex);
				}
			}

			SimsBone value = new SimsBone(faceIndex, bone.getName());
			value.setStuff(totRealVerts, realVerts, totBlendVerts, blendVerts);
			simModel.addBone(value);
			totRealVerts += realVerts;
			totBlendVerts += blendVerts;
		}

		int faceIndex = 0;

		for (Geoset geoset : model.getGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				++faceIndex;
				int v0 = vertMap.get(triangle.get(0)).getIndex();
				int v1 = vertMap.get(triangle.get(1)).getIndex();
				int v2 = vertMap.get(triangle.get(2)).getIndex();
				SimsFace value = new SimsFace(v0, v1, v2);
				simModel.addFaceIndex(faceIndex, value);
			}
		}

		simModel.addWeight(0, new int[]{0, 0});
		return simModel;
	}

	private java.util.List<Bone> setupHierarchy(Collection<Bone> bones) {
		List<Bone> roots = this.getRoots(bones);
		List<Bone> sortedNodes = new ArrayList<>();

		for (Bone root : roots) {
			this.setupHierarchy(root, sortedNodes);
		}

		return sortedNodes;
	}

	private java.util.List<Bone> getRoots(Collection<Bone> bones) {
		List<Bone> rootBones = new ArrayList<>();

		for (Bone object : bones) {
			if (object.getParent() == null) {
				rootBones.add(object);
			}
		}

		return rootBones;
	}

	private void setupHierarchy(Bone bone, List<Bone> sortedNodes) {
		sortedNodes.add(bone);

		for (IdObject child : bone.getChildrenNodes()) {
			if (child instanceof Bone) {
				this.setupHierarchy((Bone) child, sortedNodes);
			}
		}

	}
}