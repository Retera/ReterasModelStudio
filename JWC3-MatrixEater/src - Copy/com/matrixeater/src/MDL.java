package com.matrixeater.src;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
/**
 * A java object to represent and store an MDL 3d model (Warcraft III file format).
 * 
 * Eric Theller 
 * 11/5/2011
 */
public class MDL implements Named
{
//    private static String [] tags = {"Model ","Sequences ","GlobalSequences ","Bitmap ","Material ","Geoset ",};

    private File fileRef;
    private String name;
    private int BlendTime;
    private ExtLog extents;
    private int formatVersion = 800;
    protected ArrayList<String> m_header = new ArrayList<String>();
    protected ArrayList<Animation> m_anims = new ArrayList<Animation>();
    protected ArrayList<Integer> m_globalSeqs = new ArrayList<Integer>();
    protected ArrayList<Bitmap> m_textures = new ArrayList<Bitmap>();
    protected ArrayList<Material> m_materials = new ArrayList<Material>();
    protected ArrayList<TextureAnim> m_texanims = new ArrayList<TextureAnim>();
    protected ArrayList<Geoset> m_geosets = new ArrayList<Geoset>();
    protected ArrayList<GeosetAnim> m_geosetanims = new ArrayList<GeosetAnim>();
    protected ArrayList<IdObject> m_idobjects = new ArrayList<IdObject>();
    protected ArrayList<Vertex> m_pivots = new ArrayList<Vertex>();
    protected ArrayList<Camera> m_cameras = new ArrayList<Camera>();
    
    protected ArrayList m_junkCode = new ArrayList();//A series of UnrecognizedElements
    
    protected ArrayList m_allParts = new ArrayList();//A compilation of array list components in the model, to contain all parts
    private int c;
    private boolean loading;
    public File getFile()
    {
        return fileRef;
    }
    public String getName()
    {
        return fileRef.getName().split("\\.")[0];
    }
    public void setFile(File file)
    {
        fileRef = file;
    }
    public void copyHeaders(MDL other)
    {
    	fileRef = other.fileRef;
    	BlendTime = other.BlendTime;
    	if( other.extents != null )
    		extents = new ExtLog(other.extents);
    	formatVersion = other.formatVersion;
    	m_header = new ArrayList<String>(other.m_header);
    	name = other.name;
    }
    public static MDL clone(MDL what, String newName)
    {
    	MDL newModel = new MDL(what);
    	newModel.setName(newName);
    	return newModel;
    }
    public static MDL deepClone(MDL what, String newName)
    {
    	File temp;
		try {
			temp = File.createTempFile("model_clone", "mdl");
	    	what.printTo(temp);
	    	MDL newModel = MDL.read(temp);
	    	
	    	
	    	newModel.setName(newName);
	    	newModel.setFile(what.getFile());
	    	temp.deleteOnExit();
	    	
	    	return newModel;
		} catch (IOException e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
		}
    	//Write some legit deep clone code later
    	
//    	MDL newModel = new MDL(what);
//
//    	newModel.m_anims.clear();
//    	for( Animation anim: what.m_anims )
//    	{
//    		newModel.add(new Animation(anim));
//    	}
//    	newModel.m_textures.clear();
//    	for( Bitmap tex: what.m_textures )
//    	{
//    		newModel.add(new Bitmap(tex));
//    	}
//    	newModel.m_materials.clear();
//    	for(Material mat: what.m_materials)
//    	{
//    		newModel.add(new Material(mat));
//    	}
//    	m_geosets = new ArrayList(other.m_geosets);
//    	m_geosetanims = new ArrayList(other.m_geosetanims);
//    	m_idobjects = new ArrayList(other.m_idobjects);
//    	m_pivots = new ArrayList(other.m_pivots);
//    	m_cameras = new ArrayList(other.m_cameras);
		
		return null;
    }
    public void clearToHeader()
    {
    	m_anims.clear();
    	m_globalSeqs.clear();
    	m_textures.clear();
    	m_materials.clear();
    	m_texanims.clear();
    	m_geosets.clear();
    	m_geosetanims.clear();
    	m_idobjects.clear();
    	m_pivots.clear();
    	m_cameras.clear();
    }
    public MDL()
    {
    	name = "UnnamedModel";
    }
    public MDL(String newName)
    {
    	name = newName;
    }
    public MDL(MDL other)
    {
    	fileRef = other.fileRef;
    	name = other.name;
    	BlendTime = other.BlendTime;
    	extents = new ExtLog(other.extents);
    	formatVersion = other.formatVersion;
    	m_header = new ArrayList<String>(other.m_header);
    	m_anims = new ArrayList(other.m_anims);
    	m_globalSeqs = new ArrayList(other.m_globalSeqs);
    	m_textures = new ArrayList(other.m_textures);
    	m_materials = new ArrayList(other.m_materials);
    	m_texanims = new ArrayList(other.m_texanims);
    	m_geosets = new ArrayList(other.m_geosets);
    	m_geosetanims = new ArrayList(other.m_geosetanims);
    	m_idobjects = new ArrayList(other.m_idobjects);
    	m_pivots = new ArrayList(other.m_pivots);
    	m_cameras = new ArrayList(other.m_cameras);
    }
    public void loadFile(File f)
    {
        fileRef = f;
        BufferedReader mdl;
        try
        {
            mdl = new BufferedReader( new FileReader(f));
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Attempted to read file, but file was not found.");
            return;
        }
        String line = "opening";
        loading = true;
        int geoId = 0;
        while( loading )
        {
            boolean done = false;
            
            while( !(line = nextLine(mdl)).startsWith("Geoset ") && loading )
            {
                System.out.println(line);
            }
            line = nextLine(mdl);
            Geoset geo = new Geoset();
            if( !doesContainString(line,"Vertices") && loading )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Error: Vertices not found at beginning of Geoset "+geoId+"!");
            }
            while( doesContainString((line = nextLine(mdl)),"{" ) && loading )
            {
                parseVertex(line,geo);
            }
            while( !doesContainString((line = nextLine(mdl)),"Triangles" ) && loading )
            {
                System.out.println(line);
            }
            line = nextLine(mdl);
            //JOptionPane.showMessageDialog(MainFrame.panel,"Triangles found on line"+c);
            if( loading )
            {
                parseTriangles(line,geo);
                addGeoset(geo);
            }
            geoId++;
        }
    }
    public void parseVertex(String input, Geoset geoset)
    {
        String [] entries = input.split(",");
        try
        {
            geoset.addVertex(new GeosetVertex(Double.parseDouble(entries[0].substring(4,entries[0].length())),Double.parseDouble(entries[1]),Double.parseDouble(entries[2].substring(0,entries[2].length()-1))));
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error (on line "+c+"): Vertex coordinates could not be interpreted.");
        }
    }
    public void parseTriangles(String input, Geoset g)
    {
        //Loading triangles to a geoset requires verteces to be loaded first
        String [] s = input.split(",");
        s[0] = s[0].substring(4,s[0].length());
        int s_size = countContainsString(input,",");
        s[s_size-1]=s[s_size-1].substring(0,s[s_size-1].length()-2);
        for( int t = 0; t < s_size-1; t+=3  )//s[t+3].equals("")||
        {
            for( int i = 0; i < 3; i++ )
            {
                s[t+i]=s[t+i].substring(1);
            }
            try
            {
                g.addTriangle(new Triangle(Integer.parseInt(s[t]),Integer.parseInt(s[t+1]),Integer.parseInt(s[t+2]),g) );
            }
            catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Error: Unable to interpret information in Triangles: "+s[t]+", "+s[t+1]+", or "+s[t+2]);
            }
        }
    }
    public String nextLine(BufferedReader reader)
    {
        String output = "";
        try
        {
            output = reader.readLine();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error reading file.");
        }
        c++;
        if( output == null )
        {
            loading = false;
            output = "COMPLETED PARSING";
        }
        return output;
    }
    public boolean doesContainString(String a, String b)//see if a contains b
    {
        int l = a.length();
        for( int i = 0; i < l; i++ )
        {
            if( a.startsWith(b,i) )
            {
                return true;
            }
        }
        return false;
    }
    public int countContainsString(String a, String b)//see if a contains b
    {
        int l = a.length();
        int x = 0;
        for( int i = 0; i < l; i++ )
        {
            if( a.startsWith(b,i) )
            {
                x++;
            }
        }
        return x;
    }
    
    //INTERNAL PARTS CODING
    public void setName(String text)
    {
        name = text;
    }
    public void addToHeader(String comment)
    {
        m_header.add(comment);
    }
    public void addAnimation(Animation a)
    {
        m_anims.add(a);
    }
    public Animation getAnim(int index)
    {
        return m_anims.get(index);
    }
    public void addGlobalSeq(int i)
    {
        m_globalSeqs.add(i);
    }
    public int getGlobalSeqId(Integer inte)
    {
        return m_globalSeqs.indexOf(inte);
    }
    public Integer getGlobalSeq(int id)
    {
        return m_globalSeqs.get(id);
    }
    public void addTexture(Bitmap b)
    {
        m_textures.add(b);
    }
    public Bitmap getTexture(int index)
    {
        return m_textures.get(index);
    }
    public int getTextureId(Bitmap b)
    {
        if( b == null )
        {
            return -1;
        }
        for( Bitmap btm: m_textures )
        {
            if( b.equals(btm) )
            {
                return m_textures.indexOf(btm);
            }
        }
        return m_textures.indexOf(b);
    }
    public int getTextureAnimId(TextureAnim texa)
    {
        return m_texanims.indexOf(texa);
    }
    public void addMaterial(Material x)
    {
        m_materials.add(x);
    }
    public Material getMaterial(int i)
    {
        return m_materials.get(i);
    }
    public void addGeosetAnim(GeosetAnim x)
    {
        m_geosetanims.add(x);
    }
    public GeosetAnim getGeosetAnim(int index)
    {
        return m_geosetanims.get(index);
    }
    public void addCamera(Camera x)
    {
        m_cameras.add(x);
    }
    public void addIdObject(IdObject x)
    {
        m_idobjects.add(x);
    }
    public IdObject getIdObject( int index )
    {
        return m_idobjects.get(index);
    }
    public Bone getBone( int index )
    {
        try {
            if( index < m_idobjects.size() )
            {
                IdObject temp = m_idobjects.get(index);
                if( temp.getClass() == (Bone.class) )
                {
                    return (Bone)temp;
                }
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null,"Bone reference broken or invalid!");
        }
        return null;
    }
    public int getObjectId(IdObject what)
    {
        return m_idobjects.indexOf(what);
    }
    public void addPivotPoint(Vertex x)
    {
        m_pivots.add(x);
    }
    
    public void addGeoset(Geoset g)
    {
        m_geosets.add(g);
    }
    public Geoset getGeoset(int index)
    {
        return m_geosets.get(index);
    }
    public int getGeosetId(Geoset g)
    {
        return m_geosets.indexOf(g);
    }
//     public void setGeosetVisible(int index, boolean flag)
//     {
//         Geoset geo = (Geoset)m_geosets.get(index);
//         geo.setVisible(flag);
//     }
//     public void setGeosetHighlight(int index, boolean flag)
//     {
//         Geoset geo = (Geoset)m_geosets.get(index);
//         geo.setHighlight(flag);
//     }
    public void clearGeosets()
    {
        m_geosets.clear();
    }
    public int getGeosetsSize()
    {
        return m_geosets.size();
    }
    public static MDL read(File f)
    {
    	try {
        	MDLReader.clearLineId();
            BufferedReader mdl;
            try
            {
                mdl = new BufferedReader( new FileReader(f));
            }
            catch (FileNotFoundException e)
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Attempted to read file, but file was not found.");
                return null;
            }
            MDL mdlr = new MDL();
            mdlr.fileRef = f;
            String line = "";
            while( (line = MDLReader.nextLineSpecial(mdl)).startsWith("//") )
            {
                if( !line.contains("// Saved by Retera's MDL Toolkit on ") )
                mdlr.addToHeader(line);
            }
            if( !line.contains("Version") )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"The file version is missing!");
            }
            line = MDLReader.nextLine(mdl);
            mdlr.formatVersion = MDLReader.readInt(line);
            if( mdlr.formatVersion != 800 )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"The format version was confusing!");
            }
            line = MDLReader.nextLine(mdl);// this is "}" for format version
            if( !line.startsWith("}") )    // now I'll prove it
            {                              // gotta have that sense of humor, right?
                JOptionPane.showMessageDialog(MainFrame.panel,"Model could not be understood. Program does not understand this type of file.");
            }
            line = MDLReader.nextLine(mdl);
            mdlr.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            while( !(line = MDLReader.nextLine(mdl)).startsWith("}") )
            {
                if( line.contains("BlendTime") )
                {
                    mdlr.BlendTime = MDLReader.readInt(line);
                }
                else if( line.contains("Extent") )
                {
                    MDLReader.reset(mdl);
                    mdlr.extents = ExtLog.read(mdl);
                }
                MDLReader.mark(mdl);
            }
            mdlr.m_anims = Sequences.read(mdl);
            
            //GlobalSequences
            MDLReader.mark(mdl);
            if( (line = MDLReader.nextLine(mdl)).contains("GlobalSequences" ) )
            {
                while( !(line = MDLReader.nextLine(mdl)).startsWith("}") )
                {
                    if( line.contains("Duration") )
                    {
                        mdlr.m_globalSeqs.add(new Integer(MDLReader.readInt(line)));
                    }
                }
            }
            else
            {
                MDLReader.reset(mdl);
            }
            mdlr.m_textures = Bitmap.readAll(mdl);
            mdlr.m_materials = Material.readAll(mdl,mdlr);
            mdlr.m_texanims = TextureAnim.readAll(mdl);
            if( mdlr.m_materials != null )
            {
                int sz = mdlr.m_materials.size();
                for( int i = 0; i < sz; i++ )
                {
                    mdlr.m_materials.get(i).updateTextureAnims(mdlr.m_texanims);
                }
            }
            MDLReader.mark(mdl);
            boolean hadGeosets = false;
            line = MDLReader.nextLine(mdl);
            System.out.println("Geoset block starting with with \""+line+"\".");
            while( (line).contains("Geoset ") )
            {
            	hadGeosets = true;
                MDLReader.reset(mdl);
                mdlr.addGeoset(Geoset.read(mdl));
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            System.out.println("Geoset blocks finished.");
//            if( hadGeosets )
            	MDLReader.reset(mdl);
                MDLReader.mark(mdl);
            boolean hadGeosetAnims = false;
            while( (line = MDLReader.nextLine(mdl)).contains("GeosetAnim ") )
            {
            	hadGeosetAnims = true;
                MDLReader.reset(mdl);
                mdlr.addGeosetAnim(GeosetAnim.read(mdl));
                MDLReader.mark(mdl);
            }
            System.out.println("GeosetAnim blocks finished.");
//            if( hadGeosetAnims )
            	MDLReader.reset(mdl);
            line = MDLReader.nextLine(mdl);
            System.out.println("IdObject starting with with \""+line+"\".");
            while( (line).length() > 1 && !line.equals("COMPLETED PARSING") )
            {
                if( line.startsWith("Bone ") )
                {
                    MDLReader.reset(mdl);
                    mdlr.addIdObject(Bone.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("Light ") )
                {
                    MDLReader.reset(mdl);
                    mdlr.addIdObject(Light.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("Helper ") )
                {
                    MDLReader.reset(mdl);
                    mdlr.addIdObject(Helper.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("Attachment ") )
                {
                    MDLReader.reset(mdl);
                    mdlr.addIdObject(Attachment.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("ParticleEmitter ") )
                {
                    MDLReader.reset(mdl);
                    mdlr.addIdObject(ParticleEmitter.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("ParticleEmitter2 ") )
                {
                    MDLReader.reset(mdl);
                    ParticleEmitter2 temp = ParticleEmitter2.read(mdl);
                    mdlr.addIdObject(temp);
                    temp.updateTextureRef(mdlr.m_textures);
                    MDLReader.mark(mdl);
                }
                else if( line.contains("RibbonEmitter ") )
                {
                    MDLReader.reset(mdl);
                    RibbonEmitter temp = RibbonEmitter.read(mdl);
                    mdlr.addIdObject(temp);
                    temp.updateMaterialRef(mdlr.m_materials);
                    MDLReader.mark(mdl);
                }
                else if( line.contains("Camera ") )
                {
                    MDLReader.reset(mdl);
                    mdlr.addCamera(Camera.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("EventObject ") )
                {
                    MDLReader.reset(mdl);
                    mdlr.addIdObject(EventObject.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("CollisionShape ") )
                {
                    MDLReader.reset(mdl);
                    mdlr.addIdObject(CollisionShape.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("PivotPoints ") )
                {
                    while( !(line = MDLReader.nextLine(mdl)).startsWith("}") )
                    {
                        mdlr.addPivotPoint(Vertex.parseText(line));
                    }
                    MDLReader.mark(mdl);
                }
                line = MDLReader.nextLine(mdl);
            }
            System.out.println("IdObject blocks finished with \""+line+"\".");
            mdlr.updateIdObjectReferences();
            for( Geoset geo: mdlr.m_geosets )
            {
                geo.updateToObjects(mdlr);
            }
            for( GeosetAnim geoAnim: mdlr.m_geosetanims )
            {
                if( geoAnim.geosetId != -1 )
                {
                    geoAnim.geoset = mdlr.getGeoset(geoAnim.geosetId);
                    geoAnim.geoset.geosetAnim = geoAnim;//YEAH THIS MAKES SENSE
                }
            }
            List<AnimFlag> animFlags = mdlr.getAllAnimFlags();//laggggg!
            System.out.println("AnimFlags:");
            for( AnimFlag af: animFlags )
            {
                af.updateGlobalSeqRef(mdlr);
                if( !af.getName().equals("Scaling") && !af.getName().equals("Translation") && !af.getName().equals("Rotation") )
                {
                    System.out.println(mdlr.getAnimFlagSource(af).getClass().getName()+": "+af.getName());
                }
            }
            try {
                mdl.close();
            }
            catch (Exception e)
            {
                
            }
            return mdlr;
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		ExceptionPopup.display(e);
//    		pane.getStyledDocument().
//    		JOptionPane.showMessageDialog(null,newJTextPane(e));
    	}
    	return null;
    }
    public void saveFile()
    {
    	System.out.println("Save to:" + fileRef);
        printTo(fileRef);
    }
    public void printTo(File f)
    {
        PrintWriter writer = null;
        
        rebuildLists();
        //If rebuilding the lists is to crash, then we want to crash the thread
        // BEFORE clearing the file
        
        try {
            writer = new PrintWriter(f);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null,"Unable to save MDL to file.");
        }
        
        
        for( String s: m_header )
        {
            writer.println(s);
        }
        writer.println("// Saved by Retera's MDL Toolkit on "+(new Date(System.currentTimeMillis())).toString());
        writer.println("Version {");
        writer.println("\tFormatVersion "+formatVersion+",");
        writer.println("}");
        writer.println("Model \""+name+"\" {");
        int sz = m_geosets.size();
        if( sz > 0 )
        writer.println("\tNumGeosets "+sz+",");
        sz = m_geosetanims.size();
        if( sz > 0 )
        writer.println("\tNumGeosetAnims "+sz+",");
        sz = countIdObjectsOfClass(Helper.class);
        if( sz > 0 )
        writer.println("\tNumHelpers "+sz+",");
        sz = countIdObjectsOfClass(Light.class);
        if( sz > 0 )
        writer.println("\tNumLights "+sz+",");
        sz = countIdObjectsOfClass(Bone.class);
        if( sz > 0 )
        writer.println("\tNumBones "+sz+",");
        sz = countIdObjectsOfClass(Attachment.class);
        if( sz > 0 )
        writer.println("\tNumAttachments "+sz+",");
        sz = countIdObjectsOfClass(ParticleEmitter.class);
        if( sz > 0 )
        writer.println("\tNumParticleEmitters "+sz+",");
        sz = countIdObjectsOfClass(ParticleEmitter2.class);
        if( sz > 0 )
        writer.println("\tNumParticleEmitters2 "+sz+",");
        sz = countIdObjectsOfClass(RibbonEmitter.class);
        if( sz > 0 )
        writer.println("\tNumRibbonEmitters "+sz+",");
        sz = countIdObjectsOfClass(EventObject.class);
        if( sz > 0 )
        writer.println("\tNumEvents "+sz+",");
        writer.println("\tBlendTime "+BlendTime+",");
        if( extents != null )
        extents.printTo(writer,1);
        writer.println("}");
        
        //Animations
        if( m_anims != null )
        {
            if( m_anims.size() > 0 )
            {
                writer.println("Sequences "+m_anims.size()+" {");
                for( int i = 0; i < m_anims.size(); i++ )
                {
                    m_anims.get(i).printTo(writer,1);
                }
                writer.println("}");
            }
        }
        
        //Global Sequences
        if( m_globalSeqs != null )
        {
            if( m_globalSeqs.size() > 0 )
            {
                writer.println("GlobalSequences "+m_globalSeqs.size()+" {");
                for( int i = 0; i < m_globalSeqs.size(); i++  )
                {
                    writer.println("\tDuration "+m_globalSeqs.get(i).toString()+",");
                }
                writer.println("}");
            }
        }
        
        //Textures
        if( m_textures != null )
        {
            if( m_textures.size() > 0 )
            {
                writer.println("Textures "+m_textures.size()+" {");
                for( int i = 0; i < m_textures.size(); i++  )
                {
                    m_textures.get(i).printTo(writer,1);
                }
                writer.println("}");
            }
        }
        
        
        //Materials
        if( m_materials != null )
        {
            if( m_materials.size() > 0 )
            {
                writer.println("Materials "+m_materials.size()+" {");
                for( int i = 0; i < m_materials.size(); i++ )
                {
                    m_materials.get(i).printTo(writer,1);
                }
                writer.println("}");
            }
        }
        
        //TextureAnims
        if( m_texanims != null )
        {
            if( m_texanims.size() > 0 )
            {
                writer.println("TextureAnims "+m_texanims.size()+" {");
                for( int i = 0; i < m_texanims.size(); i++ )
                {
                    m_texanims.get(i).printTo(writer,1);
                }
                writer.println("}");
            }
        }

        //Geosets -- delete if empty
        if( m_geosets != null )
        {
            if( m_geosets.size() > 0 )
            {
                for( int i = m_geosets.size()-1; i >= 0; i-- )
                {
                	if( m_geosets.get(i).isEmpty() )
                	{
                		if(m_geosets.get(i).geosetAnim != null)
                		{
                			m_geosetanims.remove(m_geosets.get(i).geosetAnim);
                		}
                		m_geosets.remove(i);
                	}
                }
            }
        }
        
        cureBoneGeoAnimIds();
        updateObjectIds();
        //We want to print out the right ObjectIds!
        
        //Geosets
        if( m_geosets != null )
        {
            if( m_geosets.size() > 0 )
            {
                for( int i = 0; i < m_geosets.size(); i++ )
                {
                	m_geosets.get(i).printTo(writer,this,true);
                }
            }
        }
        
        //GeosetAnims
        for( GeosetAnim geoAnim: m_geosetanims )
        {
            geoAnim.geosetId = m_geosets.indexOf(geoAnim.geoset);
        }
        if( m_geosetanims != null )
        {
            if( m_geosetanims.size() > 0 )
            {
                for( int i = 0; i < m_geosetanims.size(); i++ )
                {
                    m_geosetanims.get(i).printTo(writer,0);
                }
            }
        }
        
        
        //Clearing pivot points
        m_pivots.clear();
        for( int i = 0; i < m_idobjects.size(); i++ )
        {
            m_pivots.add(m_idobjects.get(i).pivotPoint);
        }
        
        
        boolean pivotsPrinted = false;
        if( m_pivots.size() == 0 )
        {
            pivotsPrinted = true;
        }
        boolean camerasPrinted = false;
        if( m_cameras.size() == 0 )
        {
            camerasPrinted = true;
        }
        
        for( int i = 0; i < m_idobjects.size(); i++ )
        {
            IdObject obj = m_idobjects.get(i);
            if( !pivotsPrinted && ( obj.getClass() == ParticleEmitter.class || obj.getClass() == ParticleEmitter2.class || obj.getClass() == RibbonEmitter.class || obj.getClass() == EventObject.class || obj.getClass() == CollisionShape.class ) )
            {
                writer.println("PivotPoints "+m_pivots.size()+" {");
                for( int p = 0; p < m_pivots.size(); p++ )
                {
                    writer.println("\t"+m_pivots.get(p).toString()+",");
                }
                writer.println("}");
                pivotsPrinted = true;
            }
            if( !camerasPrinted && ( obj.getClass() == EventObject.class || obj.getClass() == CollisionShape.class ) )
            {
                camerasPrinted = true;
                for( int c = 0; c < m_cameras.size(); c++ )
                {
                    m_cameras.get(c).printTo(writer);
                }
            }
            obj.printTo(writer);
        }
        
        if( !pivotsPrinted )
        {
            writer.println("PivotPoints "+m_pivots.size()+" {");
            for( int p = 0; p < m_pivots.size(); p++ )
            {
                writer.println("\t"+m_pivots.get(p).toString()+",");
            }
            writer.println("}");
        }
        
        if( !camerasPrinted )
        {
            for( int i = 0; i < m_cameras.size(); i++ )
            {
                m_cameras.get(i).printTo(writer);
            }
        }
        
        try {
            writer.close();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null,"Unable to... STOP saving MDL file data?\nWhat does that even mean?\nWhat did you do?\nPrograms should always be able to STOP working...");
        }
    }
    public void doSavePreps()
    {
        rebuildLists();
        //If rebuilding the lists is to crash, then we want to crash the thread
        // BEFORE clearing the file
        
        
        //Animations
        
        //Geosets -- delete if empty
        if( m_geosets != null )
        {
            if( m_geosets.size() > 0 )
            {
                for( int i = m_geosets.size()-1; i >= 0; i-- )
                {
                	if( m_geosets.get(i).isEmpty() )
                	{
                		if(m_geosets.get(i).geosetAnim != null)
                		{
                			m_geosetanims.remove(m_geosets.get(i).geosetAnim);
                		}
                		m_geosets.remove(i);
                	}
                }
            }
        }
        
        cureBoneGeoAnimIds();
        updateObjectIds();
        //We want to print out the right ObjectIds!
        
        //Geosets
        if( m_geosets != null )
        {
            if( m_geosets.size() > 0 )
            {
                for( int i = 0; i < m_geosets.size(); i++ )
                {
                    m_geosets.get(i).doSavePrep(this);
                }
            }
        }
        
        //GeosetAnims
        for( GeosetAnim geoAnim: m_geosetanims )
        {
            geoAnim.geosetId = m_geosets.indexOf(geoAnim.geoset);
        }
        
        
        //Clearing pivot points
        m_pivots.clear();
        for( int i = 0; i < m_idobjects.size(); i++ )
        {
            m_pivots.add(m_idobjects.get(i).pivotPoint);
        }
    }
    public int countIdObjectsOfClass(Class what)
    {
        int idoCount = 0;
        for( IdObject obj: m_idobjects )
        {
            if( obj.getClass() == what )
            {
                idoCount++;
            }
        }
        return idoCount;
    }
    public void rebuildMaterialList()
    {
        m_materials.clear();
        for(Geoset g: m_geosets)
        {
            if( g.material != null && !m_materials.contains(g.material) )
            {
                m_materials.add(g.material);
            }
            g.setMaterialId(m_materials.indexOf(g.material)); //-1 if null
        }
        ArrayList<RibbonEmitter> ribbons = (ArrayList<RibbonEmitter>)sortedIdObjects(RibbonEmitter.class);
        for( RibbonEmitter r: ribbons )
        {
            if( r.material != null && !m_materials.contains(r.material) )
            {
                m_materials.add(r.material);
            }
            else
            {
//                 JOptionPane.showMessageDialog(null,"Null material found for ribbon at temporary object id: "+m_idobjects.indexOf(r));
            }
            r.setMaterialId(m_materials.indexOf(r.material)); //-1 if null
        }
    }
    public void clearTexAnims()
    {
        if( m_texanims != null)
        {
            m_texanims.clear();
        }
        else
        {
            m_texanims = new ArrayList<TextureAnim>();
        }
    }
    public void rebuildTextureAnimList()
    {
        clearTexAnims();
        for(Material m: m_materials)
        {
            for( Layer lay: m.layers )
            {
                if( lay.textureAnim != null && !m_texanims.contains(lay.textureAnim) )
                {
                    m_texanims.add(lay.textureAnim);
                }
            }
        }
    }
    public void rebuildTextureList()
    {
        rebuildTextureAnimList();
        m_textures.clear();
        for(Material m: m_materials)
        {
            for( Layer lay: m.layers )
            {
                if( lay.texture != null && !m_textures.contains(lay.texture) )
                {
                    boolean good = true;
                    for( Bitmap btm: m_textures )
                    {
                        if( lay.texture.equals(btm) )
                        {
                            good = false;
                        }
                    }
                    if( good )
                    m_textures.add(lay.texture);
                }
                else
                {
                    AnimFlag af = lay.getFlag("TextureID");
                    if( af != null )
                    {
                        for( Bitmap temp: lay.textures )
                        {
                            boolean good = true;
                            for( Bitmap btm: m_textures )
                            {
                                if( temp.equals(btm) )
                                {
                                    good = false;
                                }
                            }
                            if( good )
                            m_textures.add(temp);
                        }
                    }
                }
                lay.updateIds(this);//keep those Ids straight, will be -1 if null
            }
        }
        ArrayList<ParticleEmitter2> particles = (ArrayList<ParticleEmitter2>)sortedIdObjects(ParticleEmitter2.class);
        for( ParticleEmitter2 pe: particles )
        {
                    boolean good = true;
            if( pe.texture != null && !m_textures.contains(pe.texture) )
            {
                    for( Bitmap btm: m_textures )
                    {
                        if( pe.texture.equals(btm) )
                        {
                            good = false;
                        }
                    }
                    if( good )
                m_textures.add(pe.texture);
            }
            pe.setTextureId(getTextureId(pe.texture));//will be -1 if null
        }
    }
    public void rebuildGlobalSeqList()
    {
        m_globalSeqs.clear();
        List<AnimFlag> animFlags = getAllAnimFlags();//laggggg!
        for( AnimFlag af: animFlags )
        {
            if( !m_globalSeqs.contains(af.globalSeq) && af.globalSeq != null )
            {
                m_globalSeqs.add(af.globalSeq);
            }
            af.updateGlobalSeqId(this);//keep the ids straight
        }
    }
    public void rebuildLists()
    {
        rebuildMaterialList();
        rebuildTextureList();//texture anims handled inside textures
        rebuildGlobalSeqList();
    }
    //The below commented stuff is now an internal part of list rebuilding
//     public void updateIdsAcrossModel()
//     {
//         //globalSeq ids update dynamically as the list rebuilds
//         
//         //Fixes material/layer TextureID and TVertexAnimIDs, assuming that the
//         // proper lists are already built for use
//         for( Material m: m_materials )
//         {
//             m.updateReferenceIds(this);
//         }
//     }
    public void updateIdObjectReferences()
    {
        ArrayList<Bone> bones = (ArrayList<Bone>)sortedIdObjects(Bone.class);
        ArrayList<Bone> helpers = (ArrayList<Bone>)sortedIdObjects(Helper.class);
        bones.addAll(helpers);
        
        for( int i = 0; i < m_idobjects.size(); i++ )
        {
            IdObject obj = m_idobjects.get(i);
            if( obj.parentId != -1 )
            obj.parent = m_idobjects.get(obj.parentId);
            if( i> m_pivots.size() )
            {
            	JOptionPane.showMessageDialog(null,"Error: More objects than PivotPoints were found.");
            }
            obj.setPivotPoint(m_pivots.get(i));
        }
        for( Bone b: bones )
        {
            if( b.geosetId != -1 && b.geosetId < m_geosets.size() )
            b.geoset = m_geosets.get(b.geosetId);
            if( b.geosetAnimId != -1 && b.geosetAnimId < m_geosetanims.size() )
            b.geosetAnim = m_geosetanims.get(b.geosetAnimId);
        }
    }
    public void updateObjectIds()
    {
        sortIdObjects();
        
        ArrayList<Bone> bones = (ArrayList<Bone>)sortedIdObjects(Bone.class);
        ArrayList<Bone> helpers = (ArrayList<Bone>)sortedIdObjects(Helper.class);
        bones.addAll(helpers);
        
        for( int i = 0; i < m_idobjects.size(); i++ )
        {
            IdObject obj = m_idobjects.get(i);
            obj.objectId = m_idobjects.indexOf(obj);
            obj.parentId = m_idobjects.indexOf(obj.parent);
        }
        for( Bone b: bones )
        {
            b.geosetId = m_geosets.indexOf(b.geoset);
            b.geosetAnimId = m_geosetanims.indexOf(b.geosetAnim);
        }
    }
    public void sortIdObjects()
    {
        ArrayList<IdObject> allObjects = new ArrayList<IdObject>();
        ArrayList<Bone> bones = (ArrayList<Bone>)sortedIdObjects(Bone.class);
        ArrayList<Light> lights = (ArrayList<Light>)sortedIdObjects(Light.class);
        ArrayList<Helper> helpers = (ArrayList<Helper>)sortedIdObjects(Helper.class);
        ArrayList<Attachment> attachments = (ArrayList<Attachment>)sortedIdObjects(Attachment.class);
        ArrayList<ParticleEmitter> particleEmitters = (ArrayList<ParticleEmitter>)sortedIdObjects(ParticleEmitter.class);
        ArrayList<ParticleEmitter2> particleEmitter2s = (ArrayList<ParticleEmitter2>)sortedIdObjects(ParticleEmitter2.class);
        ArrayList<RibbonEmitter> ribbonEmitters = (ArrayList<RibbonEmitter>)sortedIdObjects(RibbonEmitter.class);
        ArrayList<EventObject> events = (ArrayList<EventObject>)sortedIdObjects(EventObject.class);
        ArrayList<CollisionShape> colliders = (ArrayList<CollisionShape>)sortedIdObjects(CollisionShape.class);
        
        allObjects.addAll(bones);
        allObjects.addAll(lights);
        allObjects.addAll(helpers);
        allObjects.addAll(attachments);
        allObjects.addAll(particleEmitters);
        allObjects.addAll(particleEmitter2s);
        allObjects.addAll(ribbonEmitters);
        allObjects.addAll(events);
        allObjects.addAll(colliders);
        
        m_idobjects = allObjects;
    }
    /*public <T> ArrayList<T> sortedIdObjects(Class<T> objectClass)
    {
        ArrayList<T> objects = new ArrayList<T>();
        for( IdObject obj: m_idobjects )
        {
            if( obj.getClass() == objectClass )
            {
                objects.add((T)obj);
            }
        }
        return objects;
    }*/
    public ArrayList sortedIdObjects(Class kind)
    {
        ArrayList objects = new ArrayList();
        for( IdObject obj: m_idobjects )
        {
            if( obj.getClass() == kind )
            {
                objects.add(obj);
            }
        }
        return objects;
    }
    public List<AnimFlag> getAllAnimFlags()
    {
        //Probably will cause a bunch of lag, be wary
        List<AnimFlag> allFlags = Collections.synchronizedList(new ArrayList<AnimFlag>());
        for( Material m: m_materials )
        {
            for( Layer lay: m.layers )
            {
                allFlags.addAll(lay.anims);
            }
        }
        if( m_texanims != null )
        {
            for( TextureAnim texa: m_texanims )
            {
                if( texa != null )
                {
                    allFlags.addAll(texa.animFlags);
                }
                else
                {
                    JOptionPane.showMessageDialog(null,"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
                }
            }
        }
        if( m_geosetanims != null )
        {
            for( GeosetAnim ga: m_geosetanims )
            {
                if( ga != null )
                {
                    allFlags.addAll(ga.animFlags);
                }
                else
                {
                    JOptionPane.showMessageDialog(null,"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
                }
            }
        }
        ArrayList<Bone> bones = (ArrayList<Bone>)sortedIdObjects(Bone.class);
        bones.addAll(sortedIdObjects(Helper.class));//Hey, look at that!
        for( Bone b: bones )
        {
            allFlags.addAll(b.animFlags);
        }
        ArrayList<Light> lights = (ArrayList<Light>)sortedIdObjects(Light.class);
        for( Light l: lights )
        {
            allFlags.addAll(l.animFlags);
        }
        ArrayList<Attachment> atcs = (ArrayList<Attachment>)sortedIdObjects(Attachment.class);
        for( Attachment x: atcs )
        {
            allFlags.addAll(x.animFlags);
        }
        ArrayList<ParticleEmitter2> pes = (ArrayList<ParticleEmitter2>)sortedIdObjects(ParticleEmitter2.class);
        for( ParticleEmitter2 x: pes )
        {
            allFlags.addAll(x.animFlags);
        }
        ArrayList<ParticleEmitter> xpes = (ArrayList<ParticleEmitter>)sortedIdObjects(ParticleEmitter.class);
        for( ParticleEmitter x: xpes )
        {
            allFlags.addAll(x.animFlags);
        }
        ArrayList<RibbonEmitter> res = (ArrayList<RibbonEmitter>)sortedIdObjects(RibbonEmitter.class);
        for( RibbonEmitter x: res )
        {
            allFlags.addAll(x.animFlags);
        }
        ArrayList<CollisionShape> cs = (ArrayList<CollisionShape>)sortedIdObjects(CollisionShape.class);
        for( CollisionShape x: cs )
        {
            allFlags.addAll(x.animFlags);
        }
        ArrayList<EventObject> evt = (ArrayList<EventObject>)sortedIdObjects(EventObject.class);
        for( EventObject x: evt )
        {
            allFlags.addAll(x.animFlags);
        }
        if( m_cameras != null )
        {
            for( Camera x: m_cameras )
            {
                allFlags.addAll(x.animFlags);
                allFlags.addAll(x.targetAnimFlags);
            }
        }
        
        return allFlags;
    }
    public Object getAnimFlagSource(AnimFlag aflg)
    {
        //Probably will cause a bunch of lag, be wary
        for( Material m: m_materials )
        {
            for( Layer lay: m.layers )
            {
                if( lay.anims.contains(aflg) )
                {
                    return lay;
                }
            }
        }
        if( m_texanims != null )
        {
            for( TextureAnim texa: m_texanims )
            {
                if( texa.animFlags.contains(aflg) )
                {
                    return texa;
                }
            }
        }
        if( m_geosetanims != null )
        {
            for( GeosetAnim ga: m_geosetanims )
            {
                if( ga.animFlags.contains(aflg) )
                {
                    return ga;
                }
            }
        }
        ArrayList<Bone> bones = (ArrayList<Bone>)sortedIdObjects(Bone.class);
        bones.addAll(sortedIdObjects(Helper.class));//Hey, look at that!
        for( Bone b: bones )
        {
            if( b.animFlags.contains(aflg) )
            {
                return b;
            }
        }
        ArrayList<Light> lights = (ArrayList<Light>)sortedIdObjects(Light.class);
        for( Light l: lights )
        {
            if( l.animFlags.contains(aflg) )
            {
                return l;
            }
        }
        ArrayList<Attachment> atcs = (ArrayList<Attachment>)sortedIdObjects(Attachment.class);
        for( Attachment x: atcs )
        {
            if( x.animFlags.contains(aflg) )
            {
                return x;
            }
        }
        ArrayList<ParticleEmitter2> pes = (ArrayList<ParticleEmitter2>)sortedIdObjects(ParticleEmitter2.class);
        for( ParticleEmitter2 x: pes )
        {
            if( x.animFlags.contains(aflg) )
            {
                return x;
            }
        }
        ArrayList<ParticleEmitter> xpes = (ArrayList<ParticleEmitter>)sortedIdObjects(ParticleEmitter.class);
        for( ParticleEmitter x: xpes )
        {
            if( x.animFlags.contains(aflg) )
            {
                return x;
            }
        }
        ArrayList<RibbonEmitter> res = (ArrayList<RibbonEmitter>)sortedIdObjects(RibbonEmitter.class);
        for( RibbonEmitter x: res )
        {
            if( x.animFlags.contains(aflg) )
            {
                return x;
            }
        }
        ArrayList<CollisionShape> cs = (ArrayList<CollisionShape>)sortedIdObjects(CollisionShape.class);
        for( CollisionShape x: cs )
        {
            if( x.animFlags.contains(aflg) )
            {
                return x;
            }
        }
        if( m_cameras != null )
        {
            for( Camera x: m_cameras )
            {
                if( x.animFlags.contains(aflg) || x.targetAnimFlags.contains(aflg) )
                {
                    return x;
                }
            }
        }
        
        return null;
    }

    public void addFlagToParent(AnimFlag aflg, AnimFlag added)//aflg is the parent
    {
        //ADDS "added" TO THE PARENT OF "aflg"
        for( Material m: m_materials )
        {
            for( Layer lay: m.layers )
            {
                if( lay.anims.contains(aflg) )
                {
                    lay.anims.add(added);
                }
            }
        }
        if( m_texanims != null )
        {
            for( TextureAnim texa: m_texanims )
            {
                if( texa.animFlags.contains(aflg) )
                {
                    texa.animFlags.add(added);
                }
            }
        }
        if( m_geosetanims != null )
        {
            for( GeosetAnim ga: m_geosetanims )
            {
                if( ga.animFlags.contains(aflg) )
                {
                    ga.animFlags.add(added);
                }
            }
        }
        ArrayList<Bone> bones = (ArrayList<Bone>)sortedIdObjects(Bone.class);
        bones.addAll(sortedIdObjects(Helper.class));//Hey, look at that!
        for( Bone b: bones )
        {
            if( b.animFlags.contains(aflg) )
            {
                b.animFlags.add(added);
            }
        }
        ArrayList<Light> lights = (ArrayList<Light>)sortedIdObjects(Light.class);
        for( Light l: lights )
        {
            if( l.animFlags.contains(aflg) )
            {
                l.animFlags.add(added);
            }
        }
        ArrayList<Attachment> atcs = (ArrayList<Attachment>)sortedIdObjects(Attachment.class);
        for( Attachment x: atcs )
        {
            if( x.animFlags.contains(aflg) )
            {
                x.animFlags.add(added);
            }
        }
        ArrayList<ParticleEmitter2> pes = (ArrayList<ParticleEmitter2>)sortedIdObjects(ParticleEmitter2.class);
        for( ParticleEmitter2 x: pes )
        {
            if( x.animFlags.contains(aflg) )
            {
                x.animFlags.add(added);
            }
        }
        ArrayList<ParticleEmitter> xpes = (ArrayList<ParticleEmitter>)sortedIdObjects(ParticleEmitter.class);
        for( ParticleEmitter x: xpes )
        {
            if( x.animFlags.contains(aflg) )
            {
                x.animFlags.add(added);
            }
        }
        ArrayList<RibbonEmitter> res = (ArrayList<RibbonEmitter>)sortedIdObjects(RibbonEmitter.class);
        for( RibbonEmitter x: res )
        {
            if( x.animFlags.contains(aflg) )
            {
                x.animFlags.add(added);
            }
        }
        ArrayList<CollisionShape> cs = (ArrayList<CollisionShape>)sortedIdObjects(CollisionShape.class);
        for( CollisionShape x: cs )
        {
            if( x.animFlags.contains(aflg) )
            {
                x.animFlags.add(added);
            }
        }
        if( m_cameras != null )
        {
            for( Camera x: m_cameras )
            {
                if( x.animFlags.contains(aflg) || x.targetAnimFlags.contains(aflg) )
                {
                    x.animFlags.add(added);
                }
            }
        }
    }
    public void buildGlobSeqFrom(Animation anim, List<AnimFlag> flags)
    {
        Integer newSeq = new Integer(anim.length());
        for( AnimFlag af: flags )
        {
            if( !af.hasGlobalSeq )
            {
                AnimFlag copy = new AnimFlag(af);
                copy.setGlobSeq(newSeq);
                copy.copyFrom(af,anim.getStart(),anim.getEnd(),0,anim.length());
                addFlagToParent(af,copy);
            }
        }
    }
    public void buildGlobSeqFrom(Animation anim)
    {
        buildGlobSeqFrom(anim,getAllAnimFlags());
    }
    public GeosetAnim getGeosetAnimOfGeoset(Geoset g)
    {
        if( g.geosetAnim == null )
        {
            boolean noIds = true;
            for( int i = 0; i < m_geosetanims.size() && noIds; i++ )
            {
                GeosetAnim ga = m_geosetanims.get(i);
                if( ga.geosetId != -1 )
                {
                    noIds = false;
                }
            }
            if( noIds )
            {
                if( m_geosetanims.size() > m_geosets.indexOf(g) )
                {
                    g.geosetAnim = m_geosetanims.get(m_geosets.indexOf(g));
                }
                else
                {
                    return null;
                }
            }
            else
            {
                GeosetAnim temp = null;
                for( GeosetAnim ga: m_geosetanims )
                {
                    if( ga.geoset == g )
                    {
                        temp = ga;
                        break;
                    }
                }
                g.geosetAnim = temp;
            }
        }
        return g.geosetAnim;
    }
    public void cureBoneGeoAnimIds()
    {
        ArrayList<Bone> bones = (ArrayList<Bone>)sortedIdObjects(Bone.class);
        for( Bone b: bones )
        {
            b.multiGeoId = false;
            b.geoset = null;
            b.geosetAnim = null;
        }
        for( Geoset g: m_geosets )
        {
            GeosetAnim ga = getGeosetAnimOfGeoset(g);
            for( Matrix m: g.m_matrix )
            {
                for( Bone b: m.bones )
                {
                    if( !b.multiGeoId )
                    {
                        if( b.geoset == null )
                        {
                            //The bone has been found by no prior matrices
                            if( ga != null )
                            b.geosetAnim = ga;
                            b.geoset = g;
                        }
                        else if( b.geoset != g )
                        {
                            //The bone has only been found by ONE matrix
                            b.multiGeoId = true;
                            b.geoset = null;
                            if( ga != null )
                            b.geosetAnim = ga.getMostVisible(b.geosetAnim);
                            
                        }
                    }
                    else if( ga != null && ga != b.geosetAnim )
                    {
                        b.geosetAnim = ga.getMostVisible(b.geosetAnim);
                    }
                    IdObject bp = b.parent;
                    while( bp != null )
                    {
                        if( bp.getClass() == Bone.class )
                        {
                            Bone b2 = ((Bone)bp);
                            if( !b2.multiGeoId )
                            {
                                if( b2.geoset == null )
                                {
                                    //The bone has been found by no prior matrices
                                    if( ga != null )
                                    b2.geosetAnim = ga;
                                    b2.geoset = g;
                                }
                                else if( b2.geoset != g )
                                {
                                    //The bone has only been found by ONE matrix
                                    b2.multiGeoId = true;
                                    b2.geoset = null;
                                    if( ga != null )
                                    b2.geosetAnim = ga.getMostVisible(b2.geosetAnim);
                                    
                                }
                            }
                            else if( ga != null && ga != b2.geosetAnim )
                            {
                                b2.geosetAnim = ga.getMostVisible(b2.geosetAnim);
                            }
                        }
                        bp = bp.parent;
                    }
                }
            }
        }
    }
    public ArrayList getAllVisibilitySources()
    {
        List<AnimFlag> animFlags = getAllAnimFlags();//laggggg!
        ArrayList out = new ArrayList();
        for( AnimFlag af: animFlags )
        {
            if( af.getName().equals("Visibility") || af.getName().equals("Alpha") )
            {
                out.add(getAnimFlagSource(af));
            }
        }
        return out;
    }
    
    
    public int animTrackEnd()
    {
        int highestEnd = 0;
        for( Animation a: m_anims )
        {
            if( a.getStart() > highestEnd )
            {
                highestEnd = a.getStart();
            }
            if( a.getEnd() > highestEnd )
            {
                highestEnd = a.getEnd();
            }
        }
        return highestEnd;
    }
    //Ultimate List functions
    public void add(Animation x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null Anim component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        m_anims.add(x);
    }
    public void add(Integer x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null GlobalSeq component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        m_globalSeqs.add(x);
    }
    public void add(Bitmap x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null Bitmap component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        m_textures.add(x);
    }
    public void add(Material x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null Material component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        m_materials.add(x);
    }
    public void add(TextureAnim x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null TextureAnim component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        m_texanims.add(x);
    }
    public void add(Geoset x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null Geoset component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        x.parentModel = this;
        m_geosets.add(x);
    }
    public void add(GeosetVertex x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null GeosetVertex component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        if( !contains(x.geoset) )
        {
            add(x.geoset);
            x.geoset.add(x);
        }
        else
        {
            x.geoset.add(x);
        }
    }
    public void add(Triangle x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null Triangle component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        if( !contains(x.m_geoRef) )
        {
            add(x.m_geoRef);
            x.m_geoRef.add(x);
        }
        else
        {
            x.m_geoRef.add(x);
        }
    }
    public void add(GeosetAnim x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null GeosetAnim component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        m_geosetanims.add(x);
    }
    public void add(IdObject x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null IdObject component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        m_idobjects.add(x);
        if( !m_pivots.contains(x) )
        m_pivots.add(x.pivotPoint);
    }
    public void add(Camera x)
    {
        if( x == null )
        {
            JOptionPane.showMessageDialog(null,"Added null Camera component to model, which is really bad. Tell Retera you saw this once you have errors.");
        }
        m_cameras.add(x);
    }
    
    public boolean contains( Animation x )
    {
        return m_anims.contains(x);
    }
    public boolean contains( Integer x )
    {
        return m_globalSeqs.contains(x);
    }
    public boolean contains( Bitmap x )
    {
        return m_textures.contains(x);
    }
    public boolean contains( Material x )
    {
        return m_materials.contains(x);
    }
    public boolean contains( TextureAnim x )
    {
        return m_texanims.contains(x);
    }
    public boolean contains( Geoset x )
    {
        return m_geosets.contains(x);
    }
    public boolean contains( GeosetAnim x )
    {
        return m_geosetanims.contains(x);
    }
    public boolean contains( IdObject x )
    {
        return m_idobjects.contains(x);
    }
    public boolean contains( Camera x )
    {
        return m_cameras.contains(x);
    }
    public void remove(IdObject o)
    {
        m_idobjects.remove(o);
    }
    public void remove(Geoset g)
    {
        m_geosets.remove(g);
    }
    public void remove(GeosetAnim g)
    {
        m_geosetanims.remove(g);
    }
    
//    public void destroy()
//    {
//    	try {
//			this.finalize();
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//    }
}
