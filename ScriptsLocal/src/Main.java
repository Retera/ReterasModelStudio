import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		File folder = new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Public Test\\Maps\\Campaign");
		traverse(folder);
	}
	
	public static void traverse(File file) {
		if(file.isDirectory()) {
			for(File subFile: file.listFiles()) {
				traverse(subFile);
			}
		} else {
			if(file.getPath().toLowerCase().endsWith(".j")) {
				try {
					List<String> lines = Files.readAllLines(file.toPath());
					boolean found = false;
					for(String line: lines) {
						if(line.contains("nitb")) {
							found = true;
						}
					}
					if(found) {
						
						System.out.println(file
								);
					}
				} catch (IOException e) {
				}
			} else if (file.getPath().toLowerCase().endsWith("units.doo")) {
				boolean found = false;
				try(FileInputStream stream = new FileInputStream(file)) {
					byte[] buffer = new byte[256];
					int len;
					char lastGoodMatch = 0;
					int matches = 0;
					int bestMatchLen = 0;
					while((len = stream.read(buffer))!=-1) {
						for(int i = 0; i < len; i++) {
							if(buffer[i]=='n') {
								lastGoodMatch = 'n';
								matches ++;
							} else if(lastGoodMatch=='n' && buffer[i] == 'i') {
								lastGoodMatch = 'i';
								matches ++;
							} else if(lastGoodMatch=='i' && buffer[i] == 't') {
								lastGoodMatch = 't';
								matches ++;
							} else if(lastGoodMatch=='t' && buffer[i] == 'b') {
								found = true;
								matches ++;
							} else {
								if(matches>bestMatchLen) {
									bestMatchLen = matches;
								}
								matches = 0;
								lastGoodMatch = 0;
							}
						}
					}
					if(found) {
						System.out.println(file);
					} else if(bestMatchLen>2) {
						System.out.println(file + ": " + bestMatchLen);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
