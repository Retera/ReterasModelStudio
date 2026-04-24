package com.matrixeater.imp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.owens.oobjloader.parser.BuilderInterface;
import hiveworkshop.localizationmanager.localizationmanager;

public class MDLOBJBuilderInterface implements BuilderInterface {

	private final EditableModel model;
	private final List<Vertex> verticesGeometric = new ArrayList<Vertex>();
	private final List<TVertex> tvertices = new ArrayList<TVertex>();
	private final List<Normal> normals = new ArrayList<Normal>();
	private final Map<String, Material> nameToMaterial = new HashMap<String, Material>();
	private final Geoset currentGeoset = new Geoset();

	public MDLOBJBuilderInterface() {
		model = new EditableModel();
	}

	@Override
	public void setObjFilename(final String filename) {
		model.setName(filename);
	}

	@Override
	public void addVertexGeometric(final float x, final float y, final float z) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.addvertexgeometric") + x + "," + y + "," + z + ")");
		final GeosetVertex v = new GeosetVertex(x, y, z);
		currentGeoset.add(v);
		v.setGeoset(currentGeoset);
	}

	@Override
	public void addVertexTexture(final float u, final float v) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.addvertextexture") + u + "," + v + ")");
		tvertices.add(new TVertex(u, v));
	}

	@Override
	public void addVertexNormal(final float x, final float y, final float z) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.addvertexnormal") + x + "," + y + "," + z + ")");
		normals.add(new Normal(x, y, z));
	}

	@Override
	public void addPoints(final int[] values) {
		throw new RuntimeException(LocalizationManager.getInstance().get("matrixeater.exception.addpoints"));
	}

	@Override
	public void addLine(final int[] values) {
		throw new RuntimeException(LocalizationManager.getInstance().get("matrixeater.exception.addline"));
	}

	@Override
	public void addFace(final int[] vertexIndices) {
		System.out.println(LocalizationManager.getInstance().get("") + Arrays.toString(vertexIndices) + ")");
//		currentGeoset.addTriangle(new Triangle(vertexIndices[0],vertexIndices[1],vertexIndices[2]));
//		if( vertexIndices.length > 3 ) {
//			currentGeoset.addTriangle(new Triangle(vertexIndices[2],vertexIndices[3],vertexIndices[0]));
//		}
	}

	@Override
	public void addObjectName(final String name) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.addobjectname") + name + ")");
//		model.add(new Bone(name));
//		if( currentGeoset != null ) {
//			model.add(currentGeoset);
//		}
//		currentGeoset = new Geoset();
	}

	@Override
	public void addMapLib(final String[] names) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.addmaplib") + Arrays.toString(names));
		throw new RuntimeException("");
	}

	@Override
	public void setCurrentGroupNames(final String[] names) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.exception.addmaplib") + Arrays.toString(names));
	}

	@Override
	public void setCurrentSmoothingGroup(final int groupNumber) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setcurrentsmoothinggroup") + groupNumber + ")");
	}

	@Override
	public void setCurrentUseMap(final String name) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setcurrentusemap") + name + ")");
	}

	@Override
	public void setCurrentUseMaterial(final String name) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setcurrentusematerial") + name + ")");
		final Layer lay = new Layer(LocalizationManager.getInstance().get("matrixeater.layer.setcurrentusematerial"), -1);
		currentGeoset.setMaterial(new Material(lay));
		lay.getShaderTextures().put(ShaderTextureTypeHD.Diffuse, new Bitmap(name, -1));
	}

	@Override
	public void newMtl(final String name) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.newmtl") + name + ")");

	}

	@Override
	public void setXYZ(final int type, final float x, final float y, final float z) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.xyz") + type + ",x=" + x + ",y=" + y + ",z=" + z + ")");
	}

	@Override
	public void setRGB(final int type, final float r, final float g, final float b) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.rgb") + type + ",r=" + r + ",g=" + g + ",b=" + b + ")");
	}

	@Override
	public void setIllum(final int illumModel) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.illummodel") + illumModel + ")");
	}

	@Override
	public void setD(final boolean halo, final float factor) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setd_1") + halo + LocalizationManager.getInstance().get("matrixeater.println.setd_2") + factor + ")");
	}

	@Override
	public void setNs(final float exponent) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setns") + exponent + ")");
	}

	@Override
	public void setSharpness(final float value) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setsharpness") + value + ")");
	}

	@Override
	public void setNi(final float opticalDensity) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setni") + opticalDensity + ")");
	}

	@Override
	public void setMapDecalDispBump(final int type, final String filename) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setmapdecaldispbump_1") + type + LocalizationManager.getInstance().get("matrixeater.println.setmapdecaldispbump_2") + filename + ")");
	}

	@Override
	public void setRefl(final int type, final String filename) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.setrefl_1") + type + LocalizationManager.getInstance().get("matrixeater.println.setrefl_2") + filename + ")");
	}

	@Override
	public void doneParsingMaterial() {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.doneparsingmaterial"));
	}

	@Override
	public void doneParsingObj(final String filename) {
		System.out.println(LocalizationManager.getInstance().get("matrixeater.println.doneparsiongobj") + filename + ")");
	}

	public EditableModel createModel() {
		return model;
	}
}
