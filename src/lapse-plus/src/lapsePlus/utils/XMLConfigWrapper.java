package lapsePlus.utils;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import lapsePlus.XMLConfig;
import lapsePlus.XMLConfig.SafeDescription;
import lapsePlus.XMLConfig.SinkDescription;
import lapsePlus.XMLConfig.SourceDescription;

public final class XMLConfigWrapper {
	private XMLConfigWrapper() {
	}

	public static Collection<SafeDescription> readSafes(final IProject project) {
        final IFile sinkFile = project.getFile("safes.xml");

        if(sinkFile.exists()) {
	    	return XMLConfig.readSafes("safes.xml", project.getLocation().toFile().toString());
	    } else {
	    	return XMLConfig.readSafes("safes.xml");
	    }
	}

	public static Collection<SinkDescription> readSinks(final IProject project) {
        final IFile sinkFile = project.getFile("sinks.xml");

        if(sinkFile.exists()) {
	    	return XMLConfig.readSinks("sinks.xml", project.getLocation().toFile().toString());
	    } else {
	    	return XMLConfig.readSinks("sinks.xml");
	    }
	}

	public static Collection<SourceDescription> readSources(final IProject project) {
        final IFile sourceFile = project.getFile("sources.xml");

        if(sourceFile.exists()) {
	    	return XMLConfig.readSources("sources.xml", project.getLocation().toFile().toString());
	    } else {
	    	return XMLConfig.readSources("sources.xml");
	    }
	}
	
	public static boolean isSourceName(final IProject project, final String identifier) {
		final Collection<SourceDescription> sources = XMLConfigWrapper.readSources(project);
		
		for(Iterator<SourceDescription> iter = sources.iterator(); iter.hasNext(); ){
			final SourceDescription sourceDesc = iter.next();

			final int i = sourceDesc.getMethodName().lastIndexOf('.');
			final String sub = sourceDesc.getMethodName().substring(i+1);

			if(sub.equals(identifier)){
				return true;
			}
		}
	
		// none matched
		return false;
	}

    public static boolean isSafeName(final IProject project, final String identifier) {
		final Collection<SafeDescription> safes = XMLConfigWrapper.readSafes(project);
		
		if(safes == null) {
			return false;
		}

		for(Iterator<SafeDescription> iter = safes.iterator(); iter.hasNext(); ){
			final SafeDescription safeDesc = (XMLConfig.SafeDescription) iter.next();
			if(safeDesc.getMethodName().equals(identifier)){
				return true;
			}
		}
	
		// none matched
		return false;
    }

}
