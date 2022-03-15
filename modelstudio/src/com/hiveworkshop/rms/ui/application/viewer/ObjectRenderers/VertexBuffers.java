package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Used for the different rendering functions.
 *
 * Call ensureCapacity(vertices) to ensure there is space.
 *
 * Every vertex can contain up to:
 *
 *     Vec3 position
 * 	   Vec2 uv
 * 	   Vec3 normal
 * 	   Vec4 color
 *
 * "up to" because for example the normals rendering only uses the position and the RGB part of the color.
 */
public class VertexBuffers {
	private int capacity = 0;
	private FloatBuffer positions;
	private FloatBuffer texCoords;
	private FloatBuffer normals;
	private FloatBuffer colors;
	private int index = 0;

	public void ensureCapacity(int vertices) {
		if (capacity < vertices) {
			capacity = vertices;
			positions = ByteBuffer.allocateDirect(vertices * 3 * 4).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			texCoords = ByteBuffer.allocateDirect(vertices * 2 * 4).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			normals = ByteBuffer.allocateDirect(vertices * 3 * 4).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			colors = ByteBuffer.allocateDirect(vertices * 4 * 4).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
		}
	}
	public void resetIndex(){
		index = 0;
	}
	public void incrementIndex(){
		index++;
	}
	void setMultiple(Vec3 position, Vec3 normal, float[] color) {
		setPosition(index, position.x, position.y, position.z);
		setNormal(index, normal);
		setColorRGBA(index, color);
		index++;
	}
	void setMultiple(Vec3 position, Vec3 normal, Vec2 texCoord, float[] color) {
		setPosition(index, position.x, position.y, position.z);
		setNormal(index, normal);
		setTexCoord(index, texCoord);
		setColorRGBA(index, color);
		index++;
	}

	void setPosition(float x, float y, float z) {
		setPosition(index, x, y, z);
	}
	void setPosition(int index, float x, float y, float z) {
		int offset = index * 3;

		positions.put(offset + 0, x);
		positions.put(offset + 1, y);
		positions.put(offset + 2, z);
	}

	void setPosition(float[] position) {
		setPosition(index, position);
	}
	void setPosition(int index, float[] position) {
		setPosition(index, position[0], position[1], position[2]);
	}

	void setPosition(Vec3 position) {
		setPosition(index, position);
	}
	void setPosition(int index, Vec3 position) {
		setPosition(index, position.x, position.y, position.z);
	}

	void setTexCoord(float x, float y) {
		setTexCoord(index, x, y);
	}
	void setTexCoord(int index, float x, float y) {
		int offset = index * 2;

		texCoords.put(offset + 0, x);
		texCoords.put(offset + 1, y);
	}

	void setTexCoord(float[] texCoord) {
		setTexCoord(index, texCoord);
	}
	void setTexCoord(int index, float[] texCoord) {
		setTexCoord(index, texCoord[0], texCoord[1]);
	}

	void setTexCoord(Vec2 texCoord) {
		setTexCoord(index, texCoord);
	}
	void setTexCoord(int index, Vec2 texCoord) {
		setTexCoord(index, texCoord.x, texCoord.y);
	}

	void setNormal(float x, float y, float z) {
		setNormal(index, x, y, z);
	}
	void setNormal(int index, float x, float y, float z) {
		int offset = index * 3;

		normals.put(offset + 0, x);
		normals.put(offset + 1, y);
		normals.put(offset + 2, z);
	}

	void setNormal(float[] normal) {
		setNormal(index, normal);
	}
	void setNormal(int index, float[] normal) {
		setNormal(index, normal[0], normal[1], normal[2]);
	}

	void setNormal(Vec3 normal) {
		setNormal(index, normal);
	}
	void setNormal(int index, Vec3 normal) {
		setNormal(index, normal.x, normal.y, normal.z);
	}

	void setColorRGB(int index, float r, float g, float b) {
		int offset = index * 3;

		colors.put(offset + 0, r);
		colors.put(offset + 1, g);
		colors.put(offset + 2, b);
	}

	void setColorRGB(Vec3 color) {
		setColorRGB(index, color);
	}
	void setColorRGB(int index, Vec3 color) {
		setColorRGB(index, color.x, color.y, color.z);
	}

	void setColorRGB(float[] color) {setColorRGB(index, color);}
	void setColorRGB(int index, float[] color) {
		setColorRGB(index, color[0], color[1], color[2]);
	}

	void setColorRGBA(float r, float g, float b, float a) {
		setColorRGBA(index, r, g, b, a);
	}
	void setColorRGBA(int index, float r, float g, float b, float a) {
		int offset = index * 4;

		colors.put(offset + 0, r);
		colors.put(offset + 1, g);
		colors.put(offset + 2, b);
		colors.put(offset + 3, a);
	}

	void setColorRGBA(float[] color) {
		setColorRGBA(index, color);
	}
	void setColorRGBA(int index, float[] color) {
		setColorRGBA(index, color[0], color[1], color[2], color[3]);
	}

	void setColorRGBA(Vec4 color) {
		setColorRGBA(index, color);}
	void setColorRGBA(int index, Vec4 color) {
		setColorRGBA(index, color.x, color.y, color.z, color.w);
	}

	public FloatBuffer getPositions() {
		return positions.asReadOnlyBuffer();
	}

	public FloatBuffer getTexCoords() {
		return texCoords.asReadOnlyBuffer();
	}

	public FloatBuffer getNormals() {
		return normals.asReadOnlyBuffer();
	}

	public FloatBuffer getColors() {
		return colors.asReadOnlyBuffer();
	}

	public int getIndex() {
		return index;
	}
}