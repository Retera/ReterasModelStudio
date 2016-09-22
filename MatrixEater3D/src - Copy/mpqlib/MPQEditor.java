package mpqlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;

import javax.swing.JOptionPane;

public class MPQEditor {
	public static void main(String [] args)
	{
		getFile("Scripts\\Blizzard.j");
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	public static File getFile(String targetFile)
	{
		File f = getFile("war3patch.mpq",targetFile);
		return null;
	}
	public static File getFile(String mpq, String targetFile)
	{
		try {
			String wcDir = autoWarcraftDirectory();
			wcDir = "C:\\Windows.old\\Program Files (x86)\\Warcraft III\\";
			
			File targetFileRef = new File(targetFile);
			File script = new File(System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\operate.txt");//System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\
			PrintWriter writer = new PrintWriter(script);
			writer.println("chdir \""+wcDir+"\"");
			writer.println("e "+mpq+" \""+targetFile+"\" \""+System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\"+"\" /fp");
			writer.close();

			script = new File(System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\operate.bat");//System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\
			writer = new PrintWriter(script);
			writer.println("pushd %~dp0");
			writer.println("MPQeditor /script operate.txt");
			writer.close();
			
			File newTempFile = new File(System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\"+targetFile);
			copyFile(new File("MPQEditor.exe"),new File(System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\"+"MPQEditor.exe"));
			Runtime.getRuntime().exec(new String [] {System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\"+"operate.bat"});

			//String targetFile = "Scripts\\Blizzard.j";
			//System.out.println(targetFileRef.getParent());
			
			//newTempFile.deleteOnExit();
			return newTempFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String autoWarcraftDirectory()
	{
		String wcDirectory = WindowsRegistry.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III","InstallPathX");
		if( wcDirectory == null )
		{
			wcDirectory = WindowsRegistry.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III","InstallPathX");
		}
		if( wcDirectory == null )
		{
			wcDirectory = WindowsRegistry.readRegistry("HKEY_CURRENT_USER\\Software\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\Blizzard Entertainment\\Warcraft III","InstallPath");
		}
		if( wcDirectory == null )
		{
			JOptionPane.showMessageDialog(null,"Error retrieving Warcraft III game directory.\nIs Warcraft III improperly installed on this machine?");
			wcDirectory = System.getProperty("user.home");
			if( wcDirectory == null )
			{
				wcDirectory = "C:\\";
			}
		}
		wcDirectory = wcDirectory.replace("\n","").replace("\r","");
		if( !(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\")) )
		{
			//legacyFix(wcDirectory);
			wcDirectory = wcDirectory + "\\";
		}
		System.out.println("WC3: "+wcDirectory);
		return wcDirectory;
	}
}
