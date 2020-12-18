package org.chaostocosmos.talend.aws;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class TalendAWSUtils {
	
    public static void createUnifiedJarForLambda(Path talendZipFile, Path outputPath) throws Exception {
        ZipUtils.unzipTo(talendZipFile.toFile(), outputPath.toFile(), false, 1024*5);
        List<String> keywords = new ArrayList<String>();
		keywords.add("items");
		keywords.add("local_project");
		keywords.add("src");
        CopyTools.copyDirectory(outputPath, outputPath.resolve("tmp"), keywords, false);
        
        Files.copy(Paths.get("include/aws-java-sdk-1.2.1.jar"), outputPath.resolve("tmp").resolve("aws-java-sdk-1.2.1.jar"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Paths.get("include/aws-lambda-java-core-1.2.1.jar"), outputPath.resolve("tmp").resolve("aws-lambda-java-core-1.2.1.jar"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Paths.get("include/commons-lang3-3.11.jar"), outputPath.resolve("tmp").resolve("commons-lang3-3.11.jar"), StandardCopyOption.REPLACE_EXISTING);


        String zipFileName = talendZipFile.toFile().getName();
        String folder = zipFileName.substring(0, zipFileName.lastIndexOf("_"));
        Path targetPath = outputPath.getParent().resolve("tmp").resolve(folder);
        String jarFileName = zipFileName.substring(0, zipFileName.lastIndexOf("."));

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, folder);
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0.0");
        
        JarTools.extractAllJar(outputPath, targetPath, new ArrayList<String>(), true, true, 2048);
        JarTools.makeJar(targetPath, outputPath.resolve(jarFileName+".jar"), manifest);
        Files.walk(targetPath.getParent()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }	

    public static void main(String[] args) throws Exception {
        Path rootPath = Paths.get("D:/Projects/ActiveMQ/lib/");
        List<String> exclude = new ArrayList<String>();
        exclude.add("org/apache/*");
        exclude.add("javax/*");
        //Path outputPath = Paths.get("D:\\tmp\\lib\\");
        //Map<Attributes.Name, String> manifest = new HashMap<>();
        //manifest.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        //List<Exception> exceptions = makeUnifiedJar(rootPath, new File("D:/Projects/ActiveMQ/active-mq-fat.jar"), manifest, exclude, true, true);
        //List<Exception> exceptions = makeJar(Paths.get("C:\\Users\\chaos\\Downloads\\tKafkaConsumer_simple_0.1"), Paths.get("C:\\Users\\chaos\\Downloads\\tKafkaConsumer_simple_0.1\\kafka.jar"));
        //exceptions.stream().forEach(Exception::printStackTrace);
        createUnifiedJarForLambda(Paths.get("d:\\Temp\\jobdrill_subjob_0.1.zip"), Paths.get("d:\\Temp\\output"));
    }  
}
