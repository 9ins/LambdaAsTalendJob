package org.chaostocosmos.talend.aws;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Jar Utility Tools class
 * 
 * This class contribute to manage Jar file to extract / unifiy / compress.
 * 
 * 
 * @author Kooin-Shin
 * @version 1.0.0
 */
public class JarTools {

    /**
     * Extract Jar file with specified parameters
     * @param jarFile
     * @param outputPath
     * @return
     * @throws Exception
     */
    public static List<File> extractJar(File jarFile, List<String> wildcardPatterns, Path outputPath, int bufferSize) throws Exception {
        return extractJar(jarFile, outputPath, wildcardPatterns, true, bufferSize);
    }

    /**
     * Extract Jar file with specified parameters
     * @param jarFile
     * @param outputPath
     * @param excludePattern
     * @param bufferSize
     * @param forceOnError
     * @return
     * @throws Exception
     */
    public static List<File> extractJar(File jarFile, Path outputPath, List<String> excludeWildcardPatterns, boolean forceOnError, int bufferSize) throws Exception {
        List<File> allFiles = new ArrayList<File>();
        JarFile jar = null;
        try {
            jar = new JarFile(jarFile);
            Enumeration<JarEntry> enumEntries = jar.entries();
            if (outputPath.toFile().isFile()) {
                jar.close();
                throw new IllegalArgumentException("Output path must directory!!!");
            }
            while (enumEntries.hasMoreElements()) {
                JarEntry jarEntry = enumEntries.nextElement();
                try {
                    if (excludeWildcardPatterns != null && excludeWildcardPatterns.size() > 0 && excludeWildcardPatterns.stream().anyMatch(p -> {
                        return jarEntry.getName().matches(".*"+Arrays.asList(p.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : "("+Pattern.quote(s)+")").collect(Collectors.joining("(.*)"))+".*");
                    })) {
                        System.err.println("EXCLUDE: "+jarEntry.getName());
                        continue;
                    }
                    String entryName = jarEntry.getName();
                    // System.out.println(entryName);
                    File file = outputPath.resolve(entryName).toFile();
                    if (jarEntry.isDirectory()) {
                        continue;
                    } else if (!jarEntry.isDirectory())
                        file.getParentFile().mkdirs();
                    InputStream is = jar.getInputStream(jarEntry);
                    byte[] buffer = new byte[bufferSize];
                    FileOutputStream fos = new FileOutputStream(file);
                    int len;
                    while((len=is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    is.close();
                    allFiles.add(file);
                } catch (Exception e) {
                    if (!forceOnError) {
                        throw new Exception(jarEntry.getName(), e);
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            String jarName = jar != null ? jar.getName() : jarFile.getAbsolutePath();
            if (!forceOnError) {
                throw new Exception("ERROR: " + jarName, e);
            } else {
                e.printStackTrace();
            }
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
        return allFiles;
    }

    /**
     * Extract Jar file with specified parameters
     * @param jarList
     * @param outputPath
     * @param merge
     * @param forceOnError
     * @return
     * @throws Exception
     */
    public static List<File> extractAllJar(List<File> jarList, Path outputPath, List<String> excludeWildcardPatterns, boolean merge, boolean forceOnError, int bufferSize) throws Exception {
        List<File> allFiles = new ArrayList<File>();
        for (File jar : jarList) {
            try {
                String jarPath = File.separator + jar.getName().substring(0, jar.getName().lastIndexOf("."));
                Path path = merge ? outputPath : outputPath.resolve(jarPath);
                extractJar(jar, path, excludeWildcardPatterns, forceOnError, bufferSize).forEach(s -> allFiles.add(s));
            } catch (Exception e) {
                if (!forceOnError) {
                    throw e;
                }
            }
        }
        return allFiles;
    }

    /**
     * Extract Jar file with specified parameters
     * @param rootPath
     * @param outputPath
     * @param merge
     * @param forceOnError
     * @return
     * @throws Exception
     */
    public static List<File> extractAllJar(Path rootPath, Path outputPath, List<String> excludeWildcardPatterns, boolean merge, boolean forceOnError, int bufferSize) throws Exception {
        List<File> allFiles = Files.walk(rootPath)
                .filter(f -> f.toFile().isFile() && f.toFile().getName().endsWith(".jar")).map(Path::toFile)
                .collect(Collectors.toList());
        return extractAllJar(allFiles, outputPath, excludeWildcardPatterns, merge, forceOnError, bufferSize);
    }

    /**
     * Make unified Jar with specified parameters
     * 
     * @param rootLibPath
     * @param unifiedJarName
     * @param manifestMap
     * @param forceRunOnError
     * @param deleteTemp
     * @return
     * @throws Exception
     */
    public static void makeUnifiedJar(Path rootLibPath, File unifiedJar, Map<Attributes.Name, ?> manifestMap, List<String> excludeWildcardPatterns, boolean forceRunOnError, boolean deleteTemp) throws Exception {
        if(!unifiedJar.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Unified file must be a Jar file");
        }
        Manifest manifest = new Manifest();
        manifestMap.entrySet().stream().forEach(e -> manifest.getMainAttributes().put(e.getKey(), e.getValue()));
        makeUnifiedJar(rootLibPath, unifiedJar, manifest, excludeWildcardPatterns, forceRunOnError, deleteTemp);
    }

    /**
     * Make unified Jar with specified parameters
     * @param rootLibPath
     * @param unifiedJarName
     * @param manifiest
     * @param excludeWildcardPatterns
     * @param forceOnError
     * @param deleteTemp
     * @return
     * @throws Exception
     */
    public static void makeUnifiedJar(Path rootLibPath, File unifiedJar, Manifest manifest, List<String> excludeWildcardPatterns, boolean forceOnError, boolean deleteTemp) throws Exception {
        Path tmp = rootLibPath.getParent().resolve("extract-jar-temp");
        List<File> allFiles = extractAllJar(rootLibPath, tmp, excludeWildcardPatterns, true, forceOnError, 5120);
        final JarOutputStream jos = new JarOutputStream(new FileOutputStream(unifiedJar.getCanonicalFile()), manifest);
        addTojar(tmp, tmp.toFile(), jos);
        jos.close();
        if (deleteTemp) {
            Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    /**
     * Make jar file with all files specified directory to output.
     * @param rootPath
     * @param outputPath
     * @return
     * @throws IOException
     */
    public static void makeJar(Path rootPath, Path outputPath, Manifest manifest) throws IOException {
        List<File> allFiles = Files.walk(rootPath).sorted().map(Path::toFile).collect(Collectors.toList());
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputPath.toFile()), manifest);
        addTojar(rootPath, rootPath.toFile(), jos);
        jos.close();
    }
    
    /**
     * Add to unified Jar with specified files.
     * @param rootLibPath
     * @param allFiles
     * @param jos
     * @return
     * @throws IOException 
     */
    public static void addTojar(Path rootPath, File file, JarOutputStream jos) throws IOException {
        String relativePath = rootPath.relativize(file.toPath()).toString().replace("\\", "/");
        if(file.isDirectory()) {                	
            if(!relativePath.equals("")) {
            	relativePath = relativePath.endsWith("/") ? relativePath : relativePath+"/";
                JarEntry jarEntry = new JarEntry(relativePath);
                jarEntry.setTime(file.lastModified());
                jos.putNextEntry(jarEntry); 
                jos.closeEntry();
            }
            for(File f : file.listFiles()) {
            	addTojar(rootPath, f, jos);
            }
            return;
        }
        try {
	        //System.out.println(relativePath);
	        JarEntry jarEntry = new JarEntry(relativePath);
	        jarEntry.setTime(file.lastModified());
	        jos.putNextEntry(jarEntry);
	        byte[] allBytes = Files.readAllBytes(file.toPath());
	        jos.write(allBytes);
	        jos.closeEntry();
        } catch(Exception e) {
        	e.printStackTrace();
        }
    }

    /**
     * Add file to specified Jar output stream.
     * @param rootLibPath
     * @param file
     * @param jos
     * @param bufferSize
     * @param forceOnError
     * @throws IOException
     */
    private static void addToJar(Path rootLibPath, File file, JarOutputStream jos, int bufferSize, boolean forceOnError) throws IOException {
        String relativePath = rootLibPath.relativize(file.toPath()).toString().replace("\\", "/");
        if(file.isDirectory()) {
            if(!relativePath.isEmpty()) {
                JarEntry jarEntry = new JarEntry(relativePath.endsWith("/") ? relativePath : relativePath+"/");
                jarEntry.setTime(file.lastModified());
                jos.putNextEntry(jarEntry); 
                jos.closeEntry();
            }
            for(File nestedFile : file.listFiles()) {
                addToJar(rootLibPath, nestedFile, jos, bufferSize, forceOnError);
            }
            return;
        }
        if(file.getName().equalsIgnoreCase("MANIFEST.MF")) {
            return;
        }
        JarEntry jarEntry = new JarEntry(relativePath);
        jarEntry.setTime(file.lastModified());
        jos.putNextEntry(jarEntry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[bufferSize];
        int len;
        while((len=bis.read(buffer)) > 0) {
            jos.write(buffer, 0, len);
        }
        bis.close();
        jos.closeEntry();
    }
}