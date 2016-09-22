package mpqlib;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

///import at.mitteregger.fileformat.blp.DrawBlpTest;

import com.matrixeater.src.MPQHandler;
///import com.synaptik.jwow.blp.BLP;

public class TestMPQ {// extends DrawBlpTest {
	public static void main(String[] args) {
//		try {
//			File file = MPQHandler.get().getGameFile("Textures\\HeroLich.blp");//new File("Textures\\ArrowMissile.blp");
//			File file2 = new File("C:\\temp\\awesomesauce\\HeroLich.blp");
//			FileInputStream f = new FileInputStream( file );
//			FileChannel ch = f.getChannel( );
//			ByteBuffer myBuffer = ch.map(MapMode.READ_ONLY, 0, (int)file.length());
//			ByteBuffer bb = ByteBuffer.allocate( (int)file.length() );
//			long checkSum = 0L;
//			int nRead;
//			while ( (nRead=ch.read( bb )) != -1 )
//			{
//			    if ( nRead == 0 )
//			        continue;
//			    bb.position( 0 );
//			    bb.limit( nRead );
//			    while ( bb.hasRemaining() )
//			        checkSum += bb.get( );
//			    bb.clear( );
//			}
//			
//			BLP myBLP = BLP.read(myBuffer);
//			System.out.println(myBLP);
//			//Runtime.getRuntime().exec(new String [] {"notepad",MPQHandler.get().getGameFile("Scripts\\Blizzard.j").getAbsolutePath()});
//	        //new TestMPQ().drawBlp(BlpFactory.createBlp(MPQHandler.get().getGameFile("Textures\\ArrowMissile.blp")));
//	        //new TestMPQ().drawBlp(myBLP.getBufferedImage());//myBLP.getBufferedImage());
//	        
//	        File filez = new File("C:\\temp\\awesomesauce\\HeroLich.blp");
//
//	        //Find a suitable ImageReader
//	        Iterator readers = ImageIO.getImageReadersByFormatName("JPEG");
//	        ImageReader reader = null;
//	        while(readers.hasNext()) {
//	            reader = (ImageReader)readers.next();
//	            if(reader.canReadRaster()) {
//	                break;
//	            }
//	        }
//
//	        //Stream the image file (the original CMYK image)
//	        ImageInputStream input =   ImageIO.createImageInputStream(filez); 
//	        reader.setInput(input); 
//
//	        //Read the image raster
//	        Raster raster = reader.readRaster(0, null); 
//
//	        //Create a new RGB image
//	        BufferedImage bi = new BufferedImage(raster.getWidth(), raster.getHeight(), 
//	        BufferedImage.TYPE_4BYTE_ABGR); 
//
//	        //Fill the new image with the old raster
//	        bi.getRaster().setRect(raster);
//	        
//	        new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		try {
	        File filez = new File("C:\\temp\\awesomesauce\\HeroLich.blp");
	        File fileTGA = new File("C:\\temp\\awesomesauce\\HeroLich.tga");
	        Runtime.getRuntime().exec(new String [] {  "blplabcl/blplabcl.exe" , "\""+filez.getPath()+"\"" ,"\""+fileTGA.getPath()+"\"" ,"-type0", "-q256","-opt2"} );
	        BufferedImage bi = TargaReader.getImage(fileTGA.getPath());//ImageIO.read(fileTGA);

//	        new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public static void draw(BufferedImage bi)
//	{
//        new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
//	}

}
