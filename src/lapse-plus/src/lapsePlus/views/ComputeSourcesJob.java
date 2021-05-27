package lapsePlus.views;

import java.util.Collection;
import java.util.Iterator;

import lapsePlus.CallerFinder;
import lapsePlus.LapsePlugin;
import lapsePlus.Utils;
import lapsePlus.MethodSearchRequestor.MethodDeclarationsSearchRequestor;
import lapsePlus.Utils.MethodDeclarationUnitPair;
import lapsePlus.XMLConfig.SourceDescription;
import lapsePlus.utils.StringUtils;
import lapsePlus.utils.XMLConfigWrapper;
import lapsePlus.views.SourceView.ViewContentProvider;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.swt.widgets.Display;

final class ComputeSourcesJob extends Job {
	private final ViewContentProvider contentProvider;

	ComputeSourcesJob(final ViewContentProvider contentProvider) {
		super("Computing Sources");

		this.contentProvider = contentProvider;
	}

	protected IStatus run(IProgressMonitor monitor) {
		contentProvider.clearMatches();

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				SourceView.viewer.refresh();
			}
		});

		final IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		IJavaProject[] projects;

		try {
			projects = model.getJavaProjects();
		} catch (JavaModelException e) {
			SourceView.log(e.getMessage(), e);
			return Status.CANCEL_STATUS;
		}

	    for(final IJavaProject project : projects) {
//	        if(!project.isOpen()) {
//	        	SourceView.log("------------------ Project "+ StringUtils.cutto(project.getProject().getName(), 20) + " (skipping) ------------------ ");
//	        	continue;
//	        }
	        
	        SourceView.log("------------------ Project " + StringUtils.cutto(project.getProject().getName(), 20) +  "------------------ ");
		    final Collection<SourceDescription> sources = XMLConfigWrapper.readSources(project.getProject());
		    
			if(sources == null || sources.size() == 0) {
				SourceView.logError("No interesting methods in " + project.getResource().getName());
				continue;
			}

			int matches = 0, old_matches = 0;

			SourceView.log("\tProcessing " + sources.size() + " methods");

			for (Iterator<SourceDescription> descIter = sources.iterator(); descIter.hasNext(); ){
				SourceDescription desc = descIter.next();				

				SourceView.log(
						"Project " + project.getProject().getName() + 
						": processing method " + desc.getID() + "...\t");
				monitor.subTask("Project " + project.getProject().getName() + 
						": processing method " + desc.getID() + "...\t");

				int index=desc.getMethodName().lastIndexOf('.');
				char aux=(desc.getMethodName().charAt(index+1));
				boolean isConstructor = aux<='Z';

				if(isConstructor)
					System.out.println(desc.getMethodName()+" is constrcutor");

				// boolean isContructor = desc.getTypeName().endsWith(desc.getMethodName());

				Collection callers/*<MethodUnitPair>*/ = CallerFinder.findCallers(monitor, desc.getID(), project, isConstructor);

				for (Iterator iter = callers.iterator(); iter.hasNext();) {
					Utils.ExprUnitResourceMember element = (Utils.ExprUnitResourceMember) iter.next();
					Expression expr = element.getExpression();
					if(expr == null) {
						SourceView.log("Unexpected NULL in one of the callers");
						continue;
					}
					// do a case on the expression:

					if(!(expr instanceof MethodInvocation)) {
						SourceView.logError("Can't match " + expr + " of type " + expr.getClass());
						continue;
					}
					MethodInvocation mi = (MethodInvocation) expr;

					SourceMatch match = new SourceMatch(
							expr.toString(), 
							expr, 
							element.getCompilationUnit(), 
							element.getResource(),																
							desc.getID(),
							desc.getCategoryName(),
							element.getMember(),
							false,
							false);
					contentProvider.addMatch(match);
					matches++;
				}

				if(matches > old_matches){
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							SourceView.viewer.refresh();
						}
					});
				}
				old_matches = matches;					
			}

			//				System.out.println("Found " + matches + " matche(s).");
			// find all main methods
			SourceView.log("Looking for 'main' arguments");
			monitor.subTask("Looking for 'main' arguments");
			matches += addMethodsByName("main", "'main' declaration", "main argument", project, monitor, true);

			// find Ant entry points
			SourceView.log("Looking for Ant task entry points");
			monitor.subTask("Looking for Ant task entry points");
			matches += addMethodsByName(SourceView.ANT_TASK_TYPE + "." + SourceView.ANT_TASK_METHODS, "Ant task entry point", "ANT", project, monitor, true);

			SourceView.logError(project.getProject().getName() + "\t:\t" + matches + " matche(s)");
		}
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				SourceView.viewer.refresh();
			}
		});
		SourceView.logError("There are " + contentProvider.getMatchCount() + " matches");

		return Status.OK_STATUS;
	}

	int addMethodsByName(String methodName, String type, String category, IJavaProject project, IProgressMonitor monitor, boolean nonWeb) {
		int matches = 0;
		try {
			MethodDeclarationsSearchRequestor requestor = new MethodDeclarationsSearchRequestor();
			SearchEngine searchEngine = new SearchEngine();

			IJavaSearchScope searchScope = CallerFinder.getSearchScope(project);
			SearchPattern pattern = SearchPattern.createPattern(
					methodName, 
					IJavaSearchConstants.METHOD,
					IJavaSearchConstants.DECLARATIONS, 
					SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE
					);

			searchEngine.search(
					pattern, 
					new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
					searchScope, 
					requestor, 
					monitor
					);
			Collection pairs =  requestor.getMethodUnitPairs();
			for(Iterator iter = pairs.iterator(); iter.hasNext();) {
				Utils.MethodDeclarationUnitPair pair = (MethodDeclarationUnitPair) iter.next();
				SourceMatch match = new SourceMatch(
						pair.getMember().getDeclaringType().getElementName() + "." + pair.getMember().getElementName(),  
						pair.getMethod() != null ? pair.getMethod().getName() : null, 
								pair.getCompilationUnit(), 
								pair.getResource(), 
								type,
								category,
								pair.getMember(),
								false,
								nonWeb);
				contentProvider.addMatch(match);
				monitor.subTask("Found " + matches + " matches");
				matches++;
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			log(e.getMessage(), e);
		}

		return matches;
	}

	static void log(String message, Throwable e) {
		LapsePlugin.trace(LapsePlugin.SOURCE_DEBUG, "Source view: " + message, e);
	}
}