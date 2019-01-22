package com.hiveworkshop.wc3.jworldedit.wipdesign;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHT1;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LIGHT_MODEL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NORMALIZE;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLight;
import static org.lwjgl.opengl.GL11.glLightModel;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Triangle;

public class ModelScreenshotter {
	private static boolean init;
	private static boolean fbo;
	private static boolean pbuffer;
	private static boolean pbufferRT;
	private final MDLDisplay mdlDisplay;

	public ModelScreenshotter(final MDLDisplay mdlDisplay) {
		this.mdlDisplay = mdlDisplay;
	}
	
	public void doStuff() {

        try {
            if (getWidth() != current_width || getHeight() != current_height) {
                current_width = getWidth();
                current_height = getHeight();
                glViewport(0, 0, current_width, current_height);
            }
            if(  mdlDisplay.getProgramPreferences() != null && mdlDisplay.getProgramPreferences().viewMode() == 0 ) {
				glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
			} else if(  mdlDisplay.getProgramPreferences() == null || mdlDisplay.getProgramPreferences().viewMode() == 1 ) {
				glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
			}
            glViewport(0, 0, getWidth(), getHeight());
            glEnable(GL_DEPTH_TEST);

            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(true);
            glEnable(GL_COLOR_MATERIAL);
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT0);
            glEnable(GL_LIGHT1);
            glEnable(GL_NORMALIZE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
           // System.out.println("max: "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
            if( renderTextures() ) {
				glEnable(GL11.GL_TEXTURE_2D);
			}
            GL11.glEnable(GL11.GL_BLEND);
            glClearColor(0.3137254901960784f, 0.3137254901960784f, 0.3137254901960784f, 1.0f);
//            glClearColor(0f, 0f, 0f, 1.0f);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            gluPerspective(45f, (float)current_width/(float)current_height, 1.0f, 600.0f);
            //GLU.gluOrtho2D(45f, (float)current_width/(float)current_height, 1.0f, 600.0f);
//            glRotatef(angle, 0, 0, 0);
//            glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
//            gluOrtho2D(0.0f, (float) getWidth(), 0.0f, (float) getHeight());
//            GLU.
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
//            GL11.glShadeModel(GL11.GL_SMOOTH);

            glTranslatef(0f+(float)cameraPos.x*(float)m_zoom,-70f-(float)cameraPos.y*(float)m_zoom,-200f-(float)cameraPos.z*(float)m_zoom);
            glRotatef(yangle,1f,0f,0f);
            glRotatef(xangle,0f,1f,0f);
            glScalef((float)m_zoom,(float)m_zoom,(float)m_zoom);

            final FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
            ambientColor.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
//            float [] ambientColor = {0.2f, 0.2f, 0.2f, 1f};
//            FloatBuffer buffer = ByteBuffer.allocateDirect(ambientColor.length*8).asFloatBuffer();
//            buffer.put(ambientColor).flip();
            glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientColor);


            final FloatBuffer lightColor0 = BufferUtils.createFloatBuffer(4);
            lightColor0.put(0.5f).put(0.5f).put(0.5f).put(1f).flip();
            final FloatBuffer lightPos0 = BufferUtils.createFloatBuffer(4);
            lightPos0.put(40.0f).put(100.0f).put(80.0f).put(1f).flip();
            glLight(GL_LIGHT0, GL_DIFFUSE, lightColor0);
            glLight(GL_LIGHT0, GL_POSITION, lightPos0);


            final FloatBuffer lightColor1 = BufferUtils.createFloatBuffer(4);
            lightColor1.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
            final FloatBuffer lightPos1 = BufferUtils.createFloatBuffer(4);
            lightPos1.put(-100.0f).put(100.5f).put(0.5f).put(1f).flip();

            glLight(GL_LIGHT1, GL_DIFFUSE, lightColor1);
            glLight(GL_LIGHT1, GL_POSITION, lightPos1);


//            glColor3f(1f,1f,0f);
//            glColorMaterial ( GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE ) ;
//            glEnable(GL_COLOR_MATERIAL);
            glColor4f(0.5882352941176471f,0.5882352941176471f,1f,0.3f);
//            glPushMatrix();
//            glTranslatef(getWidth() / 2.0f, getHeight() / 2.0f, 0.0f);
//            glRotatef(2*angle, 0f, 0f, -1.0f);
//            glRectf(-50.0f, -50.0f, 50.0f, 50.0f);
			for (final Geoset geo : mdlDisplay.visibleGeosets) {//.getMDL().getGeosets()
				if( !mdlDisplay.editableGeosets.contains(geo) && mdlDisplay.highlight != geo )
				{
					for( int i = 0; i < geo.getMaterial().getLayers().size(); i++ )
					{
						final Layer layer = geo.getMaterial().getLayers().get(i);
						final Bitmap tex = layer.firstTexture();
						final Integer texture = textureMap.get(tex);
						if( texture != null )
						{
							//texture.bind();
				            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				            if( renderTextures() ) {
								GL11.glEnable(GL11.GL_TEXTURE_2D);
							}
							GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
						}
						else
						{
				            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COLOR);
				            GL11.glDisable(GL11.GL_TEXTURE_2D);
						}
						if( layer.getFilterMode().equals("Additive") )
						{
							//GL11.glDisable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(false);
							GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
						}
						else if( layer.getFilterMode().equals("AddAlpha") )
						{
							//GL11.glDisable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(false);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
						}
						else
						{
							//GL11.glEnable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(true);
				            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						}
						glBegin(GL11.GL_TRIANGLES);
						for (final Triangle tri : geo.getTriangle()) {
							for (final GeosetVertex v : tri.getVerts()) {
								GL11.glNormal3f((float) v.getNormal().y, (float) v.getNormal().z, (float) v.getNormal().x);
								GL11.glTexCoord2f((float) v.getTverts().get(v.getTverts().size()-1-layer.getCoordId()).x, (float) v.getTverts().get(v.getTverts().size()-1-layer.getCoordId()).y);
								GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);
							}
						}
//						if( texture != null )
//						{
//							texture.release();
//						}
						glEnd();
					}
				}
			}
            glColor3f(2f,2f,2f);
			for (final Geoset geo : mdlDisplay.editableGeosets) {//.getMDL().getGeosets()
				if( mdlDisplay.highlight != geo )
				{
					for( int i = 0; i < geo.getMaterial().getLayers().size(); i++ )
					{
						final Layer layer = geo.getMaterial().getLayers().get(i);
						final Bitmap tex = layer.firstTexture();
						final Integer texture = textureMap.get(tex);
						if( texture != null )
						{
							//texture.bind();
							GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
						}
						if( layer.getFilterMode().equals("Additive") )
						{
							//GL11.glDisable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(false);
							GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
						}
						else if( layer.getFilterMode().equals("AddAlpha") )
						{
							//GL11.glDisable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(false);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
						}
						else
						{
							//GL11.glEnable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(true);
				            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						}
						glBegin(GL11.GL_TRIANGLES);
						for (final Triangle tri : geo.getTriangle()) {
							for (final GeosetVertex v : tri.getVerts()) {
								GL11.glNormal3f((float) v.getNormal().y, (float) v.getNormal().z, (float) v.getNormal().x);
								GL11.glTexCoord2f((float) v.getTverts().get(v.getTverts().size()-1-layer.getCoordId()).x, (float) v.getTverts().get(v.getTverts().size()-1-layer.getCoordId()).y);
								GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);
							}
						}
//						if( texture != null )
//						{
//							texture.release();
//						}
						glEnd();
					}
				}
			}
            GL11.glDepthMask(true);
           // System.out.println("max: "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
            if( mdlDisplay.highlight != null)
            {
//				for( int i = 0; i < mdlDisplay.highlight.material.layers.size(); i++ )
//				{
	            	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COLOR);
		            GL11.glDisable(GL11.GL_TEXTURE_2D);
//					Layer layer = mdlDisplay.highlight.material.layers.get(i);
//					Bitmap tex = layer.firstTexture();
//					Texture texture = textureMap.get(tex);
//					if( texture != null )
//					{
//						texture.bind();
//						//GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture.getTextureID());
//					}
//					if( layer.getFilterMode().equals("Additive") )
//					{
//						//GL11.glDisable(GL11.GL_DEPTH_TEST);
//			            GL11.glDepthMask(false);
//						GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
//					}
//					else if( layer.getFilterMode().equals("AddAlpha") )
//					{
//						//GL11.glDisable(GL11.GL_DEPTH_TEST);
//			            GL11.glDepthMask(false);
//						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//					}
//					else
//					{
//						//GL11.glEnable(GL11.GL_DEPTH_TEST);
//			            GL11.glDepthMask(true);
//			            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//					}
		            glColor3f(1f, 3f,1f);
					glBegin(GL11.GL_TRIANGLES);
					for (final Triangle tri : mdlDisplay.highlight.getTriangle()) {
						for (final GeosetVertex v : tri.getVerts()) {
							GL11.glNormal3f((float) v.getNormal().y, (float) v.getNormal().z, (float) v.getNormal().x);
							GL11.glTexCoord2f((float) v.getTverts().get(0).x, (float) v.getTverts().get(0).y);
							GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);
						}
					}
					glEnd();
//				}
            }

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
			if( mdlDisplay.getProgramPreferences() != null && mdlDisplay.getProgramPreferences().showNormals() )
			{
				glBegin(GL11.GL_LINES);
	            glColor3f(1f, 1f, 3f);
//	            if( wireframe.isSelected() )
				for (final Geoset geo : mdlDisplay.get) {//.getMDL().getGeosets()
					for (final Triangle tri : geo.getTriangle()) {
						for (final GeosetVertex v : tri.getVerts()) {
							GL11.glNormal3f((float) v.getNormal().y, (float) v.getNormal().z, (float) v.getNormal().x);
							GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);

							GL11.glNormal3f((float) v.getNormal().y, (float) v.getNormal().z, (float) v.getNormal().x);
							GL11.glVertex3f((float) v.y/1.0f+(float)(v.getNormal().y*6/m_zoom), (float) v.z/1.0f+(float) (v.getNormal().z*6/m_zoom), (float) v.x/1.0f+(float) (v.getNormal().x*6/m_zoom));
						}
					}
				}


//	        	glPolygonMode( GL_FRONT, GL_POINTS );
//				for (Geoset geo : mdlDisplay.visibleGeosets) {//.getMDL().getGeosets()
//					if( !mdlDisplay.editableGeosets.contains(geo) && mdlDisplay.highlight != geo )
//					for (Triangle tri : geo.m_triangle) {
//						for (GeosetVertex v : tri.m_verts) {
//							if( mdlDisplay.selection.contains(v))
//					            glColor3f(1f, 0f, 0f);
//							else
//					            glColor3f(0f, 0f, 0f);
//							GL11.glNormal3f((float) v.normal.y, (float) v.normal.z, (float) v.normal.x);
//							GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);
//						}
//					}
//				}

				glEnd();
	            //GL11.glDisable(GL11.GL_BLEND);
			}
	}

	public static void main(final String[] args) {
		try {
			init();
		} catch (final SlickException e) {
			throw new RuntimeException(e);
		}
		try {
			final Pbuffer pbuffer = new Pbuffer(256, 256, new PixelFormat(), null, null);
			pbuffer.makeCurrent();
		} catch (final LWJGLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise offscreen rendering by checking what buffers are supported
	 * by the card
	 *
	 * @throws SlickException Indicates no buffers are supported
	 */
	private static void init() throws SlickException {
		init = true;

		if (fbo) {
			fbo = GLContext.getCapabilities().GL_EXT_framebuffer_object;
		}
		pbuffer = (Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) != 0;
		pbufferRT = (Pbuffer.getCapabilities() & Pbuffer.RENDER_TEXTURE_SUPPORTED) != 0;

		if (!fbo && !pbuffer && !pbufferRT) {
			throw new SlickException("Your OpenGL card does not support offscreen buffers and hence can't handle the dynamic images required for this application.");
		}

		Log.info("Offscreen Buffers FBO="+fbo+" PBUFFER="+pbuffer+" PBUFFERRT="+pbufferRT);
	}
}
