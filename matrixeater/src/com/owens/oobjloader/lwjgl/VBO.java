package com.owens.oobjloader.lwjgl;

// This code was written by myself, Sean R. Owens, sean at guild dot net,
// and is released to the public domain. Share and enjoy. Since some
// people argue that it is impossible to release software to the public
// domain, you are also free to use this code under any version of the
// GPL, LPGL, Apache, or BSD licenses, or contact me for use of another
// license.  (I generally don't care so I'll almost certainly say yes.)
// In addition this code may also be used under the "unlicense" described
// at http://unlicense.org/ .  See the file UNLICENSE in the repo.

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class VBO {

    // sizeof float/sizeof int
    public final static int FL_SIZE = 4;
    public final static int INDICE_SIZE_BYTES = 4;
    // Vertex Attribute Data - i.e. x,y,z then normalx, normaly, normalz, then texture u,v - so 8 floats.
    public final static int ATTR_V_FLOATS_PER = 3;
    public final static int ATTR_N_FLOATS_PER = 3;
    public final static int ATTR_T_FLOATS_PER = 2;
    public final static int ATTR_SZ_FLOATS = ATTR_V_FLOATS_PER + ATTR_N_FLOATS_PER + ATTR_T_FLOATS_PER;
    public final static int ATTR_SZ_BYTES = ATTR_SZ_FLOATS * FL_SIZE;
    public final static int ATTR_V_OFFSET_BYTES = 0;
    public final static int ATTR_V_OFFSET_FLOATS = 0;
    public final static int ATTR_N_OFFSET_FLOATS = ATTR_V_FLOATS_PER;
    public final static int ATTR_N_OFFSET_BYTES = ATTR_N_OFFSET_FLOATS * FL_SIZE;
    ;

    public final static int ATTR_T_OFFSET_FLOATS = ATTR_V_FLOATS_PER + ATTR_N_FLOATS_PER;
    public final static int ATTR_T_OFFSET_BYTES = ATTR_T_OFFSET_FLOATS * FL_SIZE;
    public final static int ATTR_V_STRIDE2_BYTES = ATTR_SZ_FLOATS * FL_SIZE;
    public final static int ATTR_N_STRIDE2_BYTES = ATTR_SZ_FLOATS * FL_SIZE;
    public final static int ATTR_T_STRIDE2_BYTES = ATTR_SZ_FLOATS * FL_SIZE;


    private int textId = 0;
    private int verticeAttributesID = 0;      // Vertex Attributes VBO ID
    private int indicesID = 0;      // indice VBO ID
    private int indicesCount = 0;

    public VBO(int textId, int verticeAttributesID, int indicesID, int indicesCount) {
        this.textId = textId;
        this.verticeAttributesID = verticeAttributesID;
        this.indicesID = indicesID;
        this.indicesCount = indicesCount;
    }

    public void render() {


        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textId);    // Bind The Texture

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, verticeAttributesID);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, ATTR_V_STRIDE2_BYTES, ATTR_V_OFFSET_BYTES);

        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glNormalPointer(GL11.GL_FLOAT, ATTR_N_STRIDE2_BYTES, ATTR_N_OFFSET_BYTES);

        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, ATTR_T_STRIDE2_BYTES, ATTR_T_OFFSET_BYTES);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesID);
        GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_INT, 0);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public void destroy() {
        // NOTE: We don't delete the textureID because it may be used by other VBO objects.  Deciding when
        // to delete the texture id and doing so should be done at a higher level of the code.
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        ib.reset();
        ib.put(verticeAttributesID);
        GL15.glDeleteBuffers(ib);
        ib.reset();
        ib.put(indicesID);
        GL15.glDeleteBuffers(ib);
    }
}
