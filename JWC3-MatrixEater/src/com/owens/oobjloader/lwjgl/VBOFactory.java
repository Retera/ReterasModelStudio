package com.owens.oobjloader.lwjgl;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;

import com.owens.oobjloader.builder.Face;

// This code was written by myself, Sean R. Owens, sean at guild dot net,
// and is released to the public domain. Share and enjoy. Since some
// people argue that it is impossible to release software to the public
// domain, you are also free to use this code under any version of the
// GPL, LPGL, Apache, or BSD licenses, or contact me for use of another
// license.  (I generally don't care so I'll almost certainly say yes.)
// In addition this code may also be used under the "unlicense" described
// at http://unlicense.org/ .  See the file UNLICENSE in the repo.

import com.owens.oobjloader.builder.FaceVertex;

public class VBOFactory {
    
    private static Logger log = Logger.getLogger(VBOFactory.class.getName());


    public static VBO build(int textureID, ArrayList<Face> triangles) {
        //	log.log(INFO, "building a vbo!");

        if (triangles.size() <= 0) {
            throw new RuntimeException("Can not build a VBO if we have no triangles with which to build it.");
        }

        // Now sort out the triangle/vertex indices, so we can use a
        // VertexArray in our VBO.  Note the following is NOT the most efficient way
        // to do this, but hopefully it is clear.  

        // First build a map of the unique FaceVertex objects, since Faces may share FaceVertex objects.
        // And while we're at it, assign each unique FaceVertex object an index as we run across them, storing
        // this index in the map, for use later when we build the "index" buffer that refers to the vertice buffer.
        // And lastly, keep a list of the unique vertice objects, in the order that we find them in.  
        HashMap<FaceVertex, Integer> indexMap = new HashMap<FaceVertex, Integer>();
        int nextVertexIndex = 0;
        ArrayList<FaceVertex> faceVertexList = new ArrayList<FaceVertex>();
        for (Face face : triangles) {
            for (FaceVertex vertex : face.vertices) {
                if (!indexMap.containsKey(vertex)) {
                    indexMap.put(vertex, nextVertexIndex++);
                    faceVertexList.add(vertex);
                }
            }
        }

        // Now build the buffers for the VBO/IBO
        int verticeAttributesCount = nextVertexIndex;
        int indicesCount = triangles.size() * 3;

        int numMIssingNormals = 0;
        int numMissingUV = 0;
        FloatBuffer verticeAttributes;
        log.log(INFO, "Creating buffer of size " + verticeAttributesCount + " vertices at " + VBO.ATTR_SZ_FLOATS + " floats per vertice for a total of " + (verticeAttributesCount * VBO.ATTR_SZ_FLOATS) + " floats.");
        verticeAttributes = BufferUtils.createFloatBuffer(verticeAttributesCount * VBO.ATTR_SZ_FLOATS);
        if (null == verticeAttributes) {
            log.log(SEVERE, "Unable to allocate verticeAttributes buffer of size " + (verticeAttributesCount * VBO.ATTR_SZ_FLOATS) + " floats.");
        }
        for (FaceVertex vertex : faceVertexList) {
            verticeAttributes.put(vertex.v.x);
            verticeAttributes.put(vertex.v.y);
            verticeAttributes.put(vertex.v.z);
            if (vertex.n == null) {
                // @TODO: What's a reasonable default normal?  Maybe add code later to calculate normals if not present in .obj file.
                verticeAttributes.put(1.0f);
                verticeAttributes.put(1.0f);
                verticeAttributes.put(1.0f);
                numMIssingNormals++;
            } else {
                verticeAttributes.put(vertex.n.x);
                verticeAttributes.put(vertex.n.y);
                verticeAttributes.put(vertex.n.z);
            }
            // @TODO: What's a reasonable default texture coord?  
            if (vertex.t == null) {
//                verticeAttributes.put(0.5f);
//                verticeAttributes.put(0.5f);
                    verticeAttributes.put((float)Math.random());
                    verticeAttributes.put((float)Math.random());
                numMissingUV++;
            } else {
                verticeAttributes.put(vertex.t.u);
                verticeAttributes.put(vertex.t.v);
            }
        }
        verticeAttributes.flip();

        log.log(INFO, "Had " + numMIssingNormals + " missing normals and " + numMissingUV + " missing UV coords");

        IntBuffer indices;    // indices into the vertices, to specify triangles.
        indices = BufferUtils.createIntBuffer(indicesCount);
        for (Face face : triangles) {
            for (FaceVertex vertex : face.vertices) {
                int index = indexMap.get(vertex);
                indices.put(index);
            }
        }
        indices.flip();

        // Allrighty!  Now give them to OpenGL!
        IntBuffer verticeAttributesIDBuf = BufferUtils.createIntBuffer(1);

        ARBVertexBufferObject.glGenBuffersARB(verticeAttributesIDBuf);
        ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, verticeAttributesIDBuf.get(0));
        ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, verticeAttributes, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);

        IntBuffer indicesIDBuf = BufferUtils.createIntBuffer(1);
        ARBVertexBufferObject.glGenBuffersARB(indicesIDBuf);
        ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indicesIDBuf.get(0));
        ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indices, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);

        // our copy of the data is no longer necessary, it is safe in OpenGL.  
        // We don't need to null this out but it makes the point.
        verticeAttributes = null;
        indices = null;

        return new VBO(textureID, verticeAttributesIDBuf.get(0), indicesIDBuf.get(0), indicesCount);
    }
}
