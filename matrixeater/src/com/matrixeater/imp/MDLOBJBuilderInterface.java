package com.matrixeater.imp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.owens.oobjloader.parser.BuilderInterface;

public class MDLOBJBuilderInterface implements BuilderInterface {

	private final MDL model;
	private final List<Vertex> verticesGeometric = new ArrayList<Vertex>();
	private final List<TVertex> tvertices = new ArrayList<TVertex>();
	private final List<Normal> normals = new ArrayList<Normal>();
	private final Map<String,Material> nameToMaterial = new HashMap<String,Material>();
	private final Geoset currentGeoset = new Geoset();

	public MDLOBJBuilderInterface() {
		model = new MDL();
	}

	@Override
	public void setObjFilename(final String filename) {
		model.setName(filename);
	}

	@Override
	public void addVertexGeometric(final float x, final float y, final float z) {
		System.out.println("addVertexGeometric(" + x + "," + y + "," + z + ")");
		final GeosetVertex v = new GeosetVertex(x, y, z);
		currentGeoset.add(v);
		v.setGeoset(currentGeoset);
	}

	@Override
	public void addVertexTexture(final float u, final float v) {
		System.out.println("addVertexTexture(" + u + "," + v + ")");
		tvertices.add(new TVertex(u, v));
	}

	@Override
	public void addVertexNormal(final float x, final float y, final float z) {
		System.out.println("addVertexNormal(" + x + "," + y + "," + z + ")");
		normals.add(new Normal(x, y, z));
	}

	@Override
	public void addPoints(final int[] values) {
		throw new RuntimeException("addPoints not yet implemented for OBJ->MDL converter");
	}

	@Override
	public void addLine(final int[] values) {
		throw new RuntimeException("addLine not yet implemented for OBJ->MDL converter");
	}

	@Override
	public void addFace(final int[] vertexIndices) {
		System.out.println("addFace(" + Arrays.toString(vertexIndices) + ")");
//		currentGeoset.addTriangle(new Triangle(vertexIndices[0],vertexIndices[1],vertexIndices[2]));
//		if( vertexIndices.length > 3 ) {
//			currentGeoset.addTriangle(new Triangle(vertexIndices[2],vertexIndices[3],vertexIndices[0]));
//		}
	}

	@Override
	public void addObjectName(final String name) {
		System.out.println("addObjectName(" + name + ")");
//		model.add(new Bone(name));
//		if( currentGeoset != null ) {
//			model.add(currentGeoset);
//		}
//		currentGeoset = new Geoset();
	}

	@Override
	public void addMapLib(final String[] names) {
		System.out.println("addMapLib" + Arrays.toString(names));
		throw new RuntimeException("addMapLib not yet implemented for OBJ->MDL converter");
	}

	@Override
	public void setCurrentGroupNames(final String[] names) {
		System.out.println("setCurrentGroupNames" + Arrays.toString(names));
	}

	@Override
	public void setCurrentSmoothingGroup(final int groupNumber) {
		System.out.println("setCurrentSmoothingGroup(" + groupNumber + ")");
	}

	@Override
	public void setCurrentUseMap(final String name) {
		System.out.println("setCurrentUseMap(" + name + ")");
	}

	@Override
	public void setCurrentUseMaterial(final String name) {
		System.out.println("setCurrentUseMaterial(" + name + ")");
		final Layer lay = new Layer("None", -1);
		currentGeoset.setMaterial(new Material(lay));
		lay.setTexture(new Bitmap(name, -1));
	}

	@Override
	public void newMtl(final String name) {
		System.out.println("newMtl(" + name + ")");

	}

	@Override
	public void setXYZ(final int type, final float x, final float y, final float z) {
		System.out.println("newMtl(type=" + type + ",x=" + x + ",y=" + y + ",z=" + z + ")");
	}

	@Override
	public void setRGB(final int type, final float r, final float g, final float b) {
		System.out.println("setRGB(type=" + type + ",r=" + r + ",g=" + g + ",b=" + b + ")");
	}

	@Override
	public void setIllum(final int illumModel) {
		System.out.println("setIllum(illumModel=" + illumModel + ")");
	}

	@Override
	public void setD(final boolean halo, final float factor) {
		System.out.println("setD(halo=" + halo + ",factor=" + factor + ")");
	}

	@Override
	public void setNs(final float exponent) {
		System.out.println("setNs(exponent=" + exponent + ")");
	}

	@Override
	public void setSharpness(final float value) {
		System.out.println("setSharpness(value=" + value + ")");
	}

	@Override
	public void setNi(final float opticalDensity) {
		System.out.println("setNi(opticalDensity=" + opticalDensity + ")");
	}

	@Override
	public void setMapDecalDispBump(final int type, final String filename) {
		System.out.println("setMapDecalDispBump(type=" + type + ",filename=" + filename + ")");
	}

	@Override
	public void setRefl(final int type, final String filename) {
		System.out.println("setRefl(type=" + type + ",filename=" + filename + ")");
	}

	@Override
	public void doneParsingMaterial() {
		System.out.println("doneParsingMaterial()");
	}

	@Override
	public void doneParsingObj(final String filename) {
		System.out.println("doneParsingObj(filename=" + filename + ")");
	}

	public MDL createModel() {
		return model;
	}
}
