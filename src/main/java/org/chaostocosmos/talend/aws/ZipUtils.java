package org.chaostocosmos.talend.aws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This provide that file to be zipped and zip file to be extracting.
 * 
 * @author Kooin-Shin
 * @class ZipUtils
 * @date 2020. 3. 11. 오전 10:51:41
 */
public class ZipUtils {
	
	/**
	 * To zip file or directory.
	 * @param sourceFile
	 * @param destZip
	 * @throws IOException
	 */
	public static void zipTo(String sourceFile, String destZip) throws IOException {
		zipTo(new File(sourceFile), new File(destZip), 1024*512);
	}
	
	/**
	 * To zip file or directory with specific buffer size.
	 * @param sourceFile
	 * @param destZip
	 * @param bufferSize
	 * @throws IOException
	 */
	public static void zipTo(File sourceFile, File destZip, int bufferSize) throws IOException {		
		FileOutputStream fos = new FileOutputStream(destZip);
		ZipOutputStream zipOut = new ZipOutputStream(fos);		
		zipToOutStream(sourceFile, "", zipOut, bufferSize);
		zipOut.close();
		fos.close();
	}
	
	/**
	 * To zip file or directory recursively with specific arguments.
	 * @param file
	 * @param filename
	 * @param zipOutStream
	 * @param bufferSize
	 * @throws IOException
	 */
	private static void zipToOutStream(File file, String filename, ZipOutputStream zipOutStream, int bufferSize) throws IOException {
		if(file.isDirectory()) {
			if(!filename.equals("")) { 
				ZipEntry ze = new ZipEntry(filename+"/");
				zipOutStream.putNextEntry(ze);
				zipOutStream.closeEntry();
				filename += "/";
			}
			final String fn = filename;
			Arrays.asList(file.listFiles()).forEach(f -> {
				try {
					zipToOutStream(f, fn+f.getName(), zipOutStream, bufferSize);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			return;
		}
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(filename);
		zipOutStream.putNextEntry(zipEntry);
		byte[] buffer = new byte[bufferSize];
		int len;
		while((len=fis.read(buffer)) >= 0) {
			zipOutStream.write(buffer, 0, len);
		}
		fis.close();		
	}
	
	/**
	 * To upzip and delete source zip file.
	 * @param zipFile
	 * @param destDir
	 * @throws IOException
	 */
	public static void unzipAndRemove(File zipFile, File destDir) {
		try {
			unzipTo(zipFile, destDir, true, 1024*512);
			if(!zipFile.delete()) {
				Logger.getInstance().error("Source zip file isn't deleted: "+zipFile.getAbsolutePath());
			}			
		} catch(IOException e) {
			Logger.getInstance().throwable(e);
		}
	}
	
	/**
	 * To unzip zip file to specific directory.
	 * @param zipFile
	 * @param destDir
	 * @param useZipFilenameFolder
	 * @throws IOException
	 */
	public static void unzipTo(String zipFile, String destDir, boolean useZipFilenameFolder) throws IOException {
		unzipTo(new File(zipFile), new File(destDir), useZipFilenameFolder, 1024*512);
	}
	
	/**
	 * To unzip zip file to specific directory with option.
	 * @param zipFile
	 * @param destDir
	 * @param useZipFilenameFolder
	 * @param bufferSize
	 * @throws IOException
	 */
	public static void unzipTo(File zipFile, File destDir, boolean useZipFilenameFolder, int bufferSize) throws IOException {
		if(useZipFilenameFolder) {
			int idx = zipFile.getName().lastIndexOf(".");
			destDir = new File(destDir, idx != -1 ? zipFile.getName().substring(0, idx) : zipFile.getName());
			destDir.mkdirs();
		}
		byte[] buffer = new byte[bufferSize];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
        	try {
	    		File destFile = new File(destDir, zipEntry.getName());
	        	if(!zipEntry.isDirectory()) {
	            	if(destFile.getCanonicalPath().startsWith(destDir.getCanonicalPath()+File.separator)) {
	                    FileOutputStream fos = new FileOutputStream(destFile);
	                    int len;
	                    while ((len = zis.read(buffer)) > 0) {
	                        fos.write(buffer, 0, len);
	                    }
	                    fos.close();
	            	} else {
	            		throw new IOException("Zip entry is outside of the target dir: "+zipEntry.getName());
	            	}
	        	} else {
	        		//System.out.println("create directory!!! "+destFile.getAbsolutePath());
	            	destFile.mkdirs();
	        	}
	        	zipEntry = zis.getNextEntry();
        	} catch(Exception e) {
        		throw e;
        	}
        }
        zis.closeEntry();
        zis.close(); 
	}
    
	/**
	 * main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ZipUtils.unzipAndRemove(new File("D:\\zero\\org.talend.rcp_7.2.1.20190610_1534.zip"), new File("D:\\zero"));
		//ZipUtils.zipTo("D:\\zero\\org.talend.rcp_7.2.1.20190610_1534", "D:\\zero\\org.talend.rcp_7.2.1.20190610_1534.zip");
	}

}
