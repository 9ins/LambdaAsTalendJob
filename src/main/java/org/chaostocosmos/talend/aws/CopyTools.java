package org.chaostocosmos.talend.aws;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CopyTools {
	/**
	 * Copy all files of source including with keyword list to target.
	 * 
	 * @param source
	 * @param target
	 * @param removeSource
	 * @throws IOException
	 */
	public static void copyDirectory(Path source, Path target, List<String> includingKeywords, boolean deleteSrc) throws IOException {
		if(target.toFile().exists()) {
			Files.walk(target).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}		
		List<Path> sourcePaths = Files.walk(source).sorted().filter( p -> includingKeywords.stream().anyMatch(k -> p.toString().contains(k))).collect(Collectors.toList());
		//System.out.println(sourcePaths.size());
		for(Path path : sourcePaths) {
			Path tp = target.resolve(source.relativize(path));
			if(!tp.toFile().exists()) {
				tp.toFile().mkdirs();
			}
			Files.copy(path, tp, StandardCopyOption.REPLACE_EXISTING);
		}
		if(deleteSrc) {
			Files.walk(source).sorted(Comparator.reverseOrder()).filter(p -> !p.equals(source)).map(Path::toFile).forEach(File::delete);
		}
	}
}
