package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

public class MakeModelHD extends ActionFunction {
	public MakeModelHD(){
		super(TextKey.SD_TO_HD, MakeModelHD::makeItHD);
	}

	public static void makeItHD() {
		ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
		if(currentModelPanel != null){
			EditableModel model = currentModelPanel.getModel();

			for (Geoset geo : model.getGeosets()) {
				makeHd(geo);
			}
			for (Material m : model.getMaterials()) {
				makeMaterialHD(m);
			}
		}
	}


	public static void makeItHD2(EditableModel model) {
		for (Geoset geo : model.getGeosets()) {
			List<GeosetVertex> vertices = geo.getVertices();
			for (GeosetVertex gv : vertices) {
				Vec3 normal = gv.getNormal();
				if (normal != null) {
					gv.initV900();
					gv.setTangent(normal, 1);
				}
				int bones = Math.min(4, gv.getBones().size());
				short weight = (short) (255 / bones);
				for (int i = 0; i < bones; i++) {
					if (i == 0) {
						gv.setSkinBone(gv.getBones().get(i), (short) (weight + (255 % bones)), i);
					} else {
						gv.setSkinBone(gv.getBones().get(i), weight, i);

					}
				}
			}
		}
		for (Material m : model.getMaterials()) {
			makeMaterialHD(m);
		}
	}

	public static void makeHd(Geoset geoset) {
		List<GeosetVertex> vertices = geoset.getVertices();
		for (GeosetVertex gv : vertices) {
			Vec3 normal = gv.getNormal();
			gv.initV900();
			if (normal != null) {
				gv.setTangent(normal, 1);
			}
			magicSkinBones(gv);
		}
	}

	public static void magicSkinBones(GeosetVertex geosetVertex) {
		int bonesNum = Math.min(4, geosetVertex.getMatrix().size());
		short weight = 0;
		if (bonesNum > 0) {
			weight = (short) (255 / bonesNum);
		}

		for (int i = 0; i < 4; i++) {
			if (i < bonesNum) {
				geosetVertex.setSkinBone(geosetVertex.getMatrix().get(i), weight, i);
			} else {
				geosetVertex.setSkinBone((short) 0, i);
			}
		}
		if (!geosetVertex.getMatrix().isEmpty()) {
			geosetVertex.setSkinBone(geosetVertex.getMatrix().get(0), (short) (weight + (255 % bonesNum)), 0);
		}
	}

	public static void makeMaterialHD(Material material) {
		material.setShaderString("Shader_HD_DefaultUnit");
		Layer diffuseLayer;
		if (!material.getLayers().isEmpty()){
			diffuseLayer = material.getLayers().stream().filter(layer -> !layer.getTextureBitmap().getPath().equals("")).findFirst().orElse(material.getLayers().get(0));
		} else {
			diffuseLayer = new Layer("None", getBitmap("Textures\\White.dds"));
		}
		material.clearLayers();

		material.addLayer(HD_Material_Layer.DIFFUSE.ordinal(), diffuseLayer);
		material.addLayer(HD_Material_Layer.VERTEX.ordinal(), new Layer("None", getBitmap("Textures\\normal.dds")));
		material.addLayer(HD_Material_Layer.ORM.ordinal(), new Layer("None", getBitmap("Textures\\orm.dds")));
		material.addLayer(HD_Material_Layer.EMISSIVE.ordinal(), new Layer("None", getBitmap("Textures\\Black32.dds")));
		material.addLayer(HD_Material_Layer.TEAM_COLOR.ordinal(), new Layer("None", new Bitmap("", 1)));
		material.addLayer(HD_Material_Layer.REFLECTIONS.ordinal(), new Layer("None", getBitmap("ReplaceableTextures\\EnvironmentMap.dds")));

		for (final Layer l : material.getLayers()) {
			l.setEmissive(1.0);
		}
	}


	private static Bitmap getBitmap(String s) {
		Bitmap bitmap = new Bitmap(s);
		bitmap.setWrapHeight(true);
		bitmap.setWrapWidth(true);
		return bitmap;
	}
}
