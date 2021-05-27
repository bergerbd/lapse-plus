package lapsePlus.views;

import java.util.Collection;
import java.util.Iterator;

import lapsePlus.CallerFinder;
import lapsePlus.LapsePlugin;
import lapsePlus.Utils;
import lapsePlus.XMLConfig;
import lapsePlus.XMLConfig.SinkDescription;
import lapsePlus.jdom.JDomHelper;
import lapsePlus.utils.StringUtils;
import lapsePlus.utils.XMLConfigWrapper;
import lapsePlus.views.SinkView.ViewContentProvider;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;

final class ComputeSinksJob extends Job {
	private ViewContentProvider contentProvider;
	private TableViewer viewer;

	ComputeSinksJob(final ViewContentProvider contentProvider, final TableViewer viewer) {
		super("Computing Sinks");

		this.contentProvider = contentProvider;
		this.viewer = viewer;
	}

	protected IStatus run(final IProgressMonitor monitor) {
	    contentProvider.clearMatches();

	    Display.getDefault().syncExec(new Runnable() {
	        public void run() {
	            if (contentProvider.getMatchCount() > 0) {
	                monitor.subTask("Clearing the results");
	                viewer.refresh();
	            }
	        }
	    });
	    
	    contentProvider.getStatisticsManager().clearMatches();
	    final IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
	    IJavaProject[] projects;

	    try {
	        projects = model.getJavaProjects();
	    } catch (JavaModelException e) {
	        log(e.getMessage(), e);
	        return Status.CANCEL_STATUS;
	    }

	    int Matches = 0, Unsafe = 0;
	    for(final IJavaProject project : projects) {
//	        if(!project.isOpen()) {
//	        	log("------------------ Project "+ StringUtils.cutto(project.getProject().getName(), 20) + " (skipping) ------------------ ");
//	        	continue;
//	        }

	        log("------------------ Project "+ StringUtils.cutto(project.getProject().getName(), 20) + "------------------ ");
		    final Collection<SinkDescription> sinks = XMLConfigWrapper.readSinks(project.getProject());
		    
	        int matches = 0, unsafe = 0;
	        for (Iterator<SinkDescription> descIter = sinks.iterator(); descIter.hasNext();) {
	            XMLConfig.SinkDescription desc = descIter.next();
	            Assert.isNotNull(desc);

	            log("Analyzing project " + project.getProject().getName() + ": processing method " + desc.getID() + "...");
	            monitor.subTask("Project " + project.getProject().getName() + ": processing method " + desc.getID() + "...");
	            
	            //int matchesForMethod = matches;
	            int index=desc.getMethodName().lastIndexOf('.');
	            char aux=(desc.getMethodName().charAt(index+1));
	            boolean isConstructor = aux<='Z';
	         
	            Collection callers/* <MethodUnitPair> */= CallerFinder.findCallers(
	                monitor, desc.getID(), project, isConstructor);
	         
	            for (Iterator iter = callers.iterator(); iter.hasNext();) {
	            	
	                Utils.ExprUnitResourceMember element = (Utils.ExprUnitResourceMember) iter
	                    .next();
	                Expression expr = element.getExpression();
	               
	               
	                // do a case on the expression:
	                String message = null;
	                boolean isError = true;
	                boolean hasSource = true;
	                if (expr != null) {
	                    message = expr.toString();
	                   
	                    int argCount = 0;
	                    Expression arg = null;
	                    int argumentNumber = desc.getVulnerableParameter();
	                    if (expr instanceof MethodInvocation) {
	                        MethodInvocation mi = (MethodInvocation) expr;
	                        argCount = mi.arguments().size();
	                        if (argCount > 0) {
	                            arg = (Expression) mi.arguments().get(argumentNumber);
	                        }
	                        
	                    } else if (expr instanceof ClassInstanceCreation) {
	                        ClassInstanceCreation ci = (ClassInstanceCreation) expr;
	                        argCount = ci.arguments().size();
	                        if (argCount > 0) {
	                            arg = (Expression) ci.arguments().get(argumentNumber);
	                        }
	                       
	                    } else {
	                        logError("Can't match " + expr + " of type " + expr.getClass());
	                      
	                        continue;
	                    }
	                    if (argCount > 0) {
	               
	                        isError = !JDomHelper.isStringConstant(arg, element.getCompilationUnit(),
	                            element.getResource());
	                        
	                    } else {
	                        // no parameter to speak of... // TODO: this
	                        // is genrally odd
	                        isError = false;
	                    }
	                    hasSource = true;
	                } else {
	                    message = element.getMember().getElementName();
	                    hasSource = false;
	                }
	                SinkMatch match = new SinkMatch(message, expr, element
	                    .getCompilationUnit(), element.getResource(), desc.getID(), element
	                    .getMember(), desc.getCategoryName(), isError, hasSource);
	                contentProvider.addMatch(match);
	                matches++;
	                if (isError) unsafe++;
	            }
//                    log(matches - matchesForMethod + ".");
//                    Display.getDefault().syncExec(new Runnable() {
//                        public void run() {
//                            log(viewer.getTable().getItemCount() + " items.");
//                        }
//                    });
	            if (matches > 0) {
	                Display.getDefault().syncExec(new Runnable() {
	                    public void run() {
	                        viewer.refresh();
	                    }
	                });
	            }
	        }
	        log(StringUtils.cutto(project.getProject().getName(), 20) + "\t:\t"
	            + matches + "\ttotal sink(s),\t" + unsafe + "\tunsafe sink(s)");
	        Unsafe += unsafe;
	        Matches += matches;
	    }
	    Display.getDefault().syncExec(new Runnable() {
	        public void run() {
	            viewer.refresh();
	        }
	    });
	    Assert.isTrue(contentProvider.getMatchCount() == Matches, 
	        "There is a mismatch between the number of metches in the view (" + 
	        contentProvider.getMatchCount() + ") and the total number of matches (" + 
	        Matches + ")" );
	    log("\n" + StringUtils.cutto("All projects", 20) + "\t:\t" + Matches
	        + "\ttotal sink(s),\t" + Unsafe + "\tunsafe sink(s)");
	    //statisticsManager.printStatistics();
	    return Status.OK_STATUS;
	}

	static void log(String message, Throwable e) {
		LapsePlugin.trace(LapsePlugin.SOURCE_DEBUG, "Source view: " + message, e);
	}

    private static void log(String message) {
        log(message, null);
    }

    static void logError(String message) {
        log(message, new Throwable());
    }
}
