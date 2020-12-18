package org.chaostocosmos.talend.aws;

import java.io.File;

public class TalendLambdaMain {
	
	File lambdaJar;
	File talendFile;
	File outputDir;
	
	public TalendLambdaMain(File talendJobFile, File outputDir) throws Exception {
		this.talendFile = talendJobFile;
		this.outputDir = outputDir;
	}
	
	private void createLambdaJar() throws Exception {
        this.lambdaJar = TalendLambdaUtils.createUnifiedJarForLambda(this.talendFile.toPath(), this.outputDir.toPath());
	}
	
	private void createGUI() {
		
	}

    public static void main(String[] args) throws Exception {
    	TalendLambdaMain talendLambda = new TalendLambdaMain(new File("D:\\Temp\\jobdrill_subjob_0.1.zip"), new File("D:\\\\Temp\\\\out"));
    	talendLambda.createLambdaJar();
    }  
}
