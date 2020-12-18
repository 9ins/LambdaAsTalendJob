package org.chaostocosmos.talend.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * 
 * TalendLambdaUtils
 *
 * @author 9ins
 * 2020. 11. 16.
 */
public class TalendLambdaUtils {	
	/**
	 * Create unified jar for AWS Lambda, and get that file.
	 * @param talendZipFile
	 * @param outputPath
	 * @return
	 * @throws Exception
	 */
    public static File createUnifiedJarForLambda(Path talendZipFile, Path outputPath) throws Exception {
    	//Unzip Talend zip file.
        ZipUtils.unzipTo(talendZipFile.toFile(), outputPath.toFile(), false, 1024*5);
        List<String> keywords = new ArrayList<String>();
		keywords.add("items");
		keywords.add("local_project");
		keywords.add("src");
		//Copy unzipped talend job to temp directory.
        CopyTools.copyDirectory(outputPath, outputPath.resolve("tmp"), keywords, false);
        
        //Copy must have library to temp directory
        Files.walk(Paths.get("include/")).sorted().forEach(p -> {
			try {
				Files.copy(p, outputPath.resolve("tmp").resolve(p), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
        
        String zipFileName = talendZipFile.toFile().getName();
        String folder = zipFileName.substring(0, zipFileName.lastIndexOf("_"));
        Path targetPath = outputPath.getParent().resolve("tmp").resolve(folder);
        String jarFileName = zipFileName.substring(0, zipFileName.lastIndexOf("."));
        
        //All of Talend library to target
        JarTools.extractAllJar(outputPath, targetPath, new ArrayList<String>(), true, true, 2048);
        
        //Create AWS Lambda RequestHandler to target
        String resource = "org/chaostocosmos/talend/aws/TalendJobRqeustHandler.class";
        File handlerFile = targetPath.resolve(resource).toFile();
        if(!handlerFile.getParentFile().exists()) {
        	handlerFile.getParentFile().mkdirs();
        }
        createResource(resource, handlerFile);
        
        //Create manifest
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, folder);
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0.0");
        
        //Make Lambda jar file with target files.
        JarTools.makeJar(targetPath, outputPath.resolve(jarFileName+".jar"), manifest);
        Files.walk(outputPath.getParent()).sorted(Comparator.reverseOrder())
        .filter(p -> !p.equals(talendZipFile) 
        		&& !p.equals(outputPath) 
        		&& !p.equals(outputPath.resolve(jarFileName+".jar")))
        .map(Path::toFile).forEach(File::delete);        
        return outputPath.resolve(jarFileName+".jar").toFile();
    }	
    
    /**
     * To create resource to target 
     * @param resourcePath
     * @param target
     * @throws IOException
     */
    public static void createResource(String resourcePath, File target) throws IOException {
        InputStream inStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
        FileOutputStream fos = new FileOutputStream(target);
        byte[] buffer = new byte[2048];
        int len;
        while((len=inStream.read(buffer)) > 0) {
        	fos.write(buffer, 0, len);
        }
        inStream.close();
        fos.close();
    }
}
