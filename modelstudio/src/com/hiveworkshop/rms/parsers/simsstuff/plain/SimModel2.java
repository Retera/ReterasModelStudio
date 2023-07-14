package com.hiveworkshop.rms.parsers.simsstuff.plain;

import com.hiveworkshop.rms.parsers.simsstuff.LineReaderThingi;

public class SimModel2 {

	int version;
	String name;
	String texture;
	int boneCount;
	String[] boneNames;
	int faceCount;
	int[][] faces;
	int bindingCount;
	int[][] bindings;
	int realVertsCount;
	float[][] texVerts;
	int blendVertCount;
	int[][] blendWeights;
	int totVertCount;
	float[][][] realVerts;
	float[][][] blendVerts;


	public SimModel2(){}

	public SimModel2(LineReaderThingi lineReaderThingi){

		name = lineReaderThingi.readString();
		texture = lineReaderThingi.readString();
//		version = lineReaderThingi.readInt();
		boneCount = lineReaderThingi.readInt();
		boneNames = new String[boneCount];
		for(int i = 0; i < boneCount; i++) {
			boneNames[i] = lineReaderThingi.readString();
		}
		faceCount = lineReaderThingi.readInt();
		faces = new int[faceCount][];
		for(int i = 0; i < faceCount; i++) {
			faces[i] = lineReaderThingi.readInts();
		}

		bindingCount = lineReaderThingi.readInt();
		bindings = new int[bindingCount][];
		for(int i = 0; i < bindingCount; i++) {
			bindings[i] = lineReaderThingi.readInts();
		}

		realVertsCount = lineReaderThingi.readInt();
		texVerts = new float[realVertsCount][];
		for(int i = 0; i < realVertsCount; i++) {
			texVerts[i] = lineReaderThingi.readFloats();
		}

		blendVertCount = lineReaderThingi.readInt();
		blendWeights = new int[blendVertCount][];
		for(int i = 0; i < blendVertCount; i++) {
			blendWeights[i] = lineReaderThingi.readInts();
		}

		totVertCount = lineReaderThingi.readInt();
		realVerts = new float[realVertsCount][][];
		for(int i = 0; i < realVertsCount; i++) {
			float[] floats = lineReaderThingi.readFloats();
			realVerts[i] = new float[2][];
			realVerts[i][0] = new float[] {floats[0], floats[1], floats[2]};
			realVerts[i][1] = new float[] {floats[3], floats[4], floats[5]};
		}

		blendVerts = new float[blendVertCount][][];
		for(int i = 0; i < blendVertCount; i++) {
			float[] floats = lineReaderThingi.readFloats();
			blendVerts[i] = new float[2][];
			blendVerts[i][0] = new float[] {floats[0], floats[1], floats[2]};
			blendVerts[i][1] = new float[] {floats[3], floats[4], floats[5]};
		}
	}

	public String getName() {
		return name;
	}

	public SimModel2 setName(String name) {
		this.name = name;
		return this;
	}

	public String getTexture() {
		return texture;
	}

	public SimModel2 setTexture(String texture) {
		this.texture = texture;
		return this;
	}

	public int getVersion() {
		return version;
	}

	public SimModel2 setVersion(int version) {
		this.version = version;
		return this;
	}

	public int getBoneCount() {
		return boneCount;
	}

	public SimModel2 setBoneCount(int boneCount) {
		this.boneCount = boneCount;
		return this;
	}

	public String[] getBoneNames() {
		return boneNames;
	}

	public SimModel2 setBoneNames(String[] boneNames) {
		this.boneNames = boneNames;
		return this;
	}

	public int getFaceCount() {
		return faceCount;
	}

	public SimModel2 setFaceCount(int faceCount) {
		this.faceCount = faceCount;
		return this;
	}

	public int[][] getFaces() {
		return faces;
	}

	public SimModel2 setFaces(int[][] faces) {
		this.faces = faces;
		return this;
	}

	public int getBindingCount() {
		return bindingCount;
	}

	public SimModel2 setBindingCount(int bindingCount) {
		this.bindingCount = bindingCount;
		return this;
	}

	public int[][] getBindings() {
		return bindings;
	}

	public SimModel2 setBindings(int[][] bindings) {
		this.bindings = bindings;
		return this;
	}

	public int getRealVertsCount() {
		return realVertsCount;
	}

	public SimModel2 setRealVertsCount(int realVertsCount) {
		this.realVertsCount = realVertsCount;
		return this;
	}

	public float[][] getTexVerts() {
		return texVerts;
	}

	public SimModel2 setTexVerts(float[][] texVerts) {
		this.texVerts = texVerts;
		return this;
	}

	public int getBlendVertCount() {
		return blendVertCount;
	}

	public SimModel2 setBlendVertCount(int blendVertCount) {
		this.blendVertCount = blendVertCount;
		return this;
	}

	public int[][] getBlendWeights() {
		return blendWeights;
	}

	public SimModel2 setBlendWeights(int[][] blendWeights) {
		this.blendWeights = blendWeights;
		return this;
	}

	public int getTotVertCount() {
		return totVertCount;
	}

	public SimModel2 setTotVertCount(int totVertCount) {
		this.totVertCount = totVertCount;
		return this;
	}

	public float[][][] getRealVerts() {
		return realVerts;
	}

	public SimModel2 setRealVerts(float[][][] realVerts) {
		this.realVerts = realVerts;
		return this;
	}

	public float[][][] getBlendVerts() {
		return blendVerts;
	}

	public SimModel2 setBlendVerts(float[][][] blendVerts) {
		this.blendVerts = blendVerts;
		return this;
	}


	// Version - A 4-byte unsigned integer specifying the version number of this mesh file; should be equal to 2
	// Bone count - A 4-byte unsigned integer specifying the number of bones referred to by this mesh
	// Bone names - A consecutive list of Pascal strings specifying all bones referred to by this mesh
	// Face count - A 4-byte unsigned integer specifying the number of triangle faces defined in this mesh
	// Faces - For each face:
	//  Vertex A index - A 4-byte unsigned integer specifying the zero-based index of a real vertex in Real Vertices used by this face
	//  Vertex B index - A 4-byte unsigned integer specifying the zero-based index of a real vertex in Real Vertices used by this face
	//  Vertex C index - A 4-byte unsigned integer specifying the zero-based index of a real vertex in Real Vertices used by this face
	// Note: All vertices referred to by the faces are real vertices.
	// Note: Faces oriented outside are defined in clockwise order. Thus, during rendering, you can enable back-face culling such that front faces are defined in clockwise order.
	// Binding count - A 4-byte unsigned integer specifying the number of bone bindings defined in this mesh; each bone should have exactly one binding
	// Bindings - For each bone binding:
	// Bone index - A 4-byte unsigned integer specifying the zero-based index of the bone in Bone names which this binding corresponds to
	//  First real vertex index - A 4-byte unsigned integer specifying the zero-based index of the first real vertex in Real Vertices to be wrapped around this bone
	//  Real vertex count - A 4-byte unsigned integer specifying the number of real vertices in Real Vertices starting from index First real vertex index to be wrapped around this bone
	//  First blend vertex index - A 4-byte unsigned integer specifying the zero-based index of the first blend vertex in Blend Vertices to be wrapped around this bone
	//  Blend vertex count - A 4-byte unsigned integer specifying the number of blend vertices in Blend Vertices starting from index First blend vertex index to be wrapped around this bone
	// Real vertex count - A 4-byte unsigned integer specifying the number of real vertices defined in this mesh
	// Texture vertices - Texture vertices are defined for real vertices only. For each real vertex:
	//  u - A 32-bit little-endian float specifying the u texture coordinate of this vertex
	//  v - A 32-bit little-endian float specifying the v texture coordinate of this vertex
	// These values assume top-down textures, meaning that for bottom-up textures, you have to flip the coordinates by negating V.
	// Blend vertex count - A 4-byte unsigned integer specifying the number of blend vertices defined in this mesh
	// Blend vertex properties - For each blend vertex:
	//  Weight - A 4-byte unsigned integer in Q1.15 fixed-point format specifying the percentage, from 0.0 (represented by 0x0000) to 1.0 (represented by 0x8000), of the distance that this blend vertex tugs its corresponding real vertex by, starting from the location of the real vertex (corresponding to the weight 0x0000 or 0.0) and ending at the "reach-for" location of this blend vertex (corresponding to the weight 0x8000 or 1.0). *After* wrapping the real and blend vertices around their respective bones on the skeleton, perform the following calculation: Real_Vertex_Location_After_Blending = Weight*Blend_Vertex_Location + (1-Weight)*Real_Vertex_Location_Before_Blending.
	//  Other vertex index - A 4-byte unsigned integer specifying the zero-based index of which vertex in Real Vertices to influence the location of
	//  Blend vertices themselves are not rendered. They simply affect the locations of real vertices.
	//      Note: Some real vertices on one bone may be blended by the blend vertices on other bones.
	// Total vertex count - A 4-byte unsigned integer specifying the total number of both real and blend vertices defined in this mesh
	// Real vertices - For each real vertex:
	//  Position - Three 32-bit little-endian floats: x,y,z
	//  Normal vector - Three 32-bit little-endian floats: x,y,z
	// Blend vertices - For each blend vertex:
	//  Position - Three 32-bit little-endian floats: x,y,z
	//  Normal vector - Three 32-bit little-endian floats: x,y,z

}
