package lapsePlus.views;

/*
* SourceView.java,version 2.8, 2010
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.Collator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import lapsePlus.CallerFinder;
import lapsePlus.LapsePlugin;
import lapsePlus.XMLConfig;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class SourceView extends ViewPart {	
	
	static TableViewer viewer;
	ViewContentProvider contentProvider;
	
	Action runAction;
	Action doubleClickAction;
	Action hideNonWebAction;
	Action hideNoSourceAction;
	
	IAction copyToClipboardAction;
	Clipboard fClipboard;
	
	// Constants
	static final String ANT_TASK_TYPE = "org.apache.tools.ant.Task";
	static final String ANT_TASK_METHODS = "set*";	
	
	public static class ViewContentProvider implements IStructuredContentProvider {
		
		Vector<SourceMatch> matches = new Vector<SourceMatch>();
		
		public void addMatch(SourceMatch match) {
			matches.add(match);
		}
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			return matches.toArray();
		}

		public int getMatchCount() {
			return matches.size();
		}

		public void clearMatches() {
			matches.clear();
		}
	}
	
	/*
	 * TODO Perhaps we can reuse org.eclipse.jdt.ui.JavaElementLabelProvider to fetch the images

	 */
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {
		
		public String getColumnText(Object obj, int index) {
			
			SourceMatch match = (SourceMatch) obj;
			
			if(index == 0) {
				return match.getMessage();
			} else
			if (index == 1) {
				return match.getMember() != null ? match.getMember().getElementName() : null;
			} else
			if (index == 2) {
				return match.getType();
			} else
			if (index == 3) {
					return match.getCategory();
				} else
			if (index == 4) {
				return match.getProject() != null ? match.getProject().getName() : null;
			} else
			if (index == 5) {
				return match.getFileName();
			} else
			if (index == 6) {
				return match.isSource() ? "" + match.getLineNumber() : "";
			} else {
				return null;
			}
		}
		
		public Image getColumnImage(Object obj, int index) {
			SourceMatch match = (SourceMatch) obj;
			
			if(index == 0) {
				return match.isError() ?
						JavaPluginImages.get(JavaPluginImages.IMG_OBJS_ERROR) :
						JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
			}
			return null;
		}
		
		public Color getForeground(Object element) {
			Display display = Display.getCurrent();
			SourceMatch match = (SourceMatch) element;
			
			return match.isSource() ?
					display.getSystemColor(SWT.COLOR_LIST_FOREGROUND) :
					display.getSystemColor(SWT.COLOR_GRAY);
		}
		
		public Color getBackground(Object element) {
			return null;
		}
		
	}

	static class ColumnBasedSorter extends ViewerSorter {
		private int columnNum;
		private int orientation = 1;
		
		ColumnBasedSorter(int columnNum, int orientation){
			super ( Collator.getInstance(Locale.getDefault()) );
			
			this.columnNum = columnNum;
			this.orientation = orientation;
		}
		
		ColumnBasedSorter(int columnNum){
			this(columnNum, 1);
		}
		
		public int category(Object element) {
			SourceMatch match  = (SourceMatch) element;
			if(match.isError()) {
				return 1; 
			} else {
				return 0;
			}
		}
		
		public int compare(Viewer viewer, Object e1, Object e2) {
			SourceMatch match1 = (SourceMatch)e1;
			SourceMatch match2 = (SourceMatch)e2;
			int result = Integer.MAX_VALUE; 
			String s1, s2;
		
			
			//{ "Suspicious call", "Method", "Type", "Category", "Project", "File", "Line" };
			if(columnNum == 0) {
				s1 = match1.getMessage();
				s2 = match2.getMessage();															
			} else
			if(columnNum == 1) {
				s1 = match1.getMember() != null ? match1.getMember().getElementName() : "";
				s2 = match2.getMember() != null ? match2.getMember().getElementName() : "";
			} else
			if(columnNum == 2) {
				s1 = match1.getType();
				s2 = match2.getType();
			} else
			if(columnNum == 3) {
				s1 = match1.getCategory();
				s2 = match2.getCategory();
			} else 
			if(columnNum == 4) {
				s1 = match1.getProject() != null ? match1.getProject().getName() : "";
				s2 = match2.getProject() != null ? match2.getProject().getName() : "";
			} else
			if(columnNum == 5) {
				s1 = "" + match1.getFileName();
				s2 = "" + match2.getFileName();
			} else
			if(columnNum == 6) {
				s1 = "" + match1.getLineNumber();
				s2 = "" + match2.getLineNumber();
			} else {
				logError("Unknown column: " + columnNum);
				return 0;
			}
			
			result = orientation*s1.compareToIgnoreCase(s2);
			
			return result;
		}
		
		public void toggle() {
			orientation = orientation * -1;
		}
		
		public int getColumn() {
			return this.columnNum;
		}
		
		public int getOrientation() {
			return orientation;
		}
	}

	public SourceView() {}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		fClipboard= new Clipboard(parent.getDisplay());
		contentProvider = new ViewContentProvider();
		
		viewer = new LocationViewer(parent);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ColumnBasedSorter(2));
		viewer.setInput(getViewSite());
		
		makeActions();
		
		hookContextMenu();
		hookDoubleClickAction();
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if(sel != null) {
					int size = sel.toArray().length;
					if(size > 0) {
						IStatusLineManager slManager = getViewSite().getActionBars().getStatusLineManager();
						slManager.setMessage("Selected " + size + (size > 1 ? " entries." : "entry."));
					}
				}
			}
		});
		
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SourceView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
		
		LapseMultiActionGroup group = 
			new LapseCheckboxActionGroup(
					new IAction[] {hideNonWebAction, hideNoSourceAction},
					new boolean[] {true		   , 	 true});
		group.addActions(bars.getMenuManager());
	}

	//Add actions to the plugin
	private void fillContextMenu(IMenuManager manager) {
		manager.add(runAction);
		manager.add(copyToClipboardAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(runAction);
		manager.add(copyToClipboardAction);
	}
	
	private void computeSources() {
		
		(new ComputeSourcesJob(contentProvider)).schedule();
	}
	

    /*
	private Type findLocallyDeclaredType(String argName, CompilationUnit compilationUnit) {
		final Map<String, Type> var2type = new HashMap<String, Type>();
		
		ASTVisitor visitor = new ASTVisitor() {
			public boolean visit(VariableDeclarationStatement node) {
				Type type = node.getType();
				//node.getType()
				for(Iterator iter = node.fragments().iterator(); iter.hasNext();) {
					VariableDeclarationFragment frag = (VariableDeclarationFragment)iter.next(); 
					SimpleName var = frag.getName();
					
					//System.out.println("Storing " + var.getFullyQualifiedName() +  " of type " + type);
					var2type.put(var.getFullyQualifiedName(), type);
				}
				
				return false;
			}
		};
		compilationUnit.accept(visitor);
		//System.out.println("There are " + var2type.size() + " elements in the map");
		
		return (Type) var2type.get(argName);
	}*/
	
	private void makeActions() {
		
		runAction = new Action() {
			public void run() {
				computeSources();
			}			
		};
		
		copyToClipboardAction = new CopyMatchViewAction(this, fClipboard);
		copyToClipboardAction.setText("Copy selection to clipboard");
		copyToClipboardAction.setToolTipText("Copy selection to clipboard");
		copyToClipboardAction.setImageDescriptor(JavaPluginImages.DESC_DLCL_COPY_QUALIFIED_NAME);
				
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				SourceMatch match = (SourceMatch) sel.getFirstElement();
				
				try {
//					System.out.println("Double-clicked on " + match.getMember().getClass());
					EditorUtility.openInEditor(match.getMember(), true);
					if(match.getUnit() != null) {
						ITextEditor editor = (ITextEditor) EditorUtility.openInEditor(match.getMember());
						editor.selectAndReveal(
								match.getAST().getStartPosition(), 
								match.getAST().getLength());
					}
				} catch (PartInitException e) {
					log(e.getMessage(), e);
				} catch (Exception e) {
                    log(e.getMessage(), e);
				}
			}				
		};
		
		runAction.setText("Find sources");
		runAction.setToolTipText("Find sources");
		//Find Sources image
		runAction.setImageDescriptor(JavaPluginImages.DESC_OBJS_JSEARCH);
		
		hideNonWebAction = new Action("Hide non-Web sources", IAction.AS_CHECK_BOX) {
			
			boolean hasFilter = false;
			
			ViewerFilter filter = new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					SourceMatch match = (SourceMatch) element;
					
					return !match.isNonWeb();
				}	
			};
			
			public void run() {
				if(!hasFilter) {
					viewer.addFilter(filter);
					hasFilter = true;
				} else {
					viewer.removeFilter(filter);
					hasFilter = false;
				}
			}
		};
		
		hideNonWebAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
		
		hideNoSourceAction = new Action("Hide vulnerability sources with no source code", IAction.AS_CHECK_BOX) {
			boolean hasFilter = false;
			ViewerFilter filter = new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					SourceMatch match = (SourceMatch) element;
					
					return match.isSource();
				}									
			};
			public void run() {
				if(!hasFilter) {
					viewer.addFilter(filter);
					hasFilter = true;
				} else {
					viewer.removeFilter(filter);
					hasFilter = false;
				}
			}
		};
		
		hideNoSourceAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}		

	static class LocationViewer extends TableViewer {
	    private final String columnHeaders[] = { "Suspicious call", "Method", "Type", "Category", "Project", "File", "Line" }; 
	    private ColumnLayoutData columnLayouts[] = {
	        new ColumnPixelData(300),
	        new ColumnWeightData(100),
	        new ColumnWeightData(100),
	        new ColumnWeightData(100),
	        new ColumnWeightData(100),
	        new ColumnWeightData(100),
	        new ColumnWeightData(20)};	    

	    LocationViewer(Composite parent) {
	        super(createTable(parent));

	        createColumns();
	        
//	        JavaUIHelp.setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_VIEW);
	    }

	    /**
	     * Creates the table control.
	     */
	    private static Table createTable(Composite parent) {
	        Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
	        table.setLinesVisible(true);
	        return table;
	    }
	    
	    private void createColumns() {
	        TableLayout layout = new TableLayout();
	        getTable().setLayout(layout);
	        getTable().setHeaderVisible(true);
	        for (int i = 0; i < columnHeaders.length; i++) {
	            layout.addColumnData(columnLayouts[i]);
	            TableColumn tc = new TableColumn(getTable(), SWT.NONE,i);
	            tc.setResizable(columnLayouts[i].resizable);
	            tc.setText(columnHeaders[i]);
	            final int j = i;
	            tc.addSelectionListener(new SelectionAdapter() {           	
                	public void widgetSelected(SelectionEvent e) {
                		ViewerSorter oldSorter = viewer.getSorter();
                		if(oldSorter instanceof ColumnBasedSorter) {
                			ColumnBasedSorter sorter = (ColumnBasedSorter) oldSorter;	                			 
                			if(sorter.getColumn() == j) {
                				sorter.toggle();
                				viewer.refresh();
//                				System.err.println("Resorting column " + j + " in order " + sorter.getOrientation());
                				return;
                			}
                		}
                		
                		viewer.setSorter(new ColumnBasedSorter(j));
//                		System.err.println("Sorting column " + j + " in order " + 1);	                		
                		viewer.refresh();
                    }	                
                });
	        }
	    }

	    /**
	     * Attaches a contextmenu listener to the tree
	     */	   
	    void initContextMenu(IMenuListener menuListener, String popupId, IWorkbenchPartSite viewSite) {
	        MenuManager menuMgr= new MenuManager();
	        menuMgr.setRemoveAllWhenShown(true);
	        menuMgr.addMenuListener(menuListener);
	        Menu menu= menuMgr.createContextMenu(getControl());
	        getControl().setMenu(menu);
	        viewSite.registerContextMenu(popupId, menuMgr, this);
	    }
	
	    void clearViewer() {
	        setInput(""); //$NON-NLS-1$
	    }
	}
	
	class CopyMatchViewAction extends Action {
	    //private static final char INDENTATION= '\t';  //$NON-NLS-1$
	    
	    private SourceView fView;
		private final Clipboard fClipboard;

		public CopyMatchViewAction(SourceView view, Clipboard clipboard) {
			super("Copy matches to clipboard"); 
			Assert.isNotNull(clipboard);
			fView= view;
			fClipboard= clipboard;
		}

		public void run() {
	        StringBuffer buf = new StringBuffer();
	        addCalls(viewer.getTable().getSelection(), buf);

			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			try{
				fClipboard.setContents(
					new String[]{ convertLineTerminators(buf.toString()) }, 
					new Transfer[]{ plainTextTransfer });
			}  catch (SWTError e){
				if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) 
					throw e;
				if (MessageDialog.openQuestion(fView.getViewSite().getShell(), 
						("CopyCallHierarchyAction.problem"), ("CopyCallHierarchyAction.clipboard_busy"))
				) 
				{
					run();
				}
			}
		}
		
		private void addCalls(TableItem[] items, StringBuffer buf) {
			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				SourceMatch match = (SourceMatch) item.getData();
				
				buf.append(match.toLongString());
		        buf.append('\n');			
			}                
	    }

	    private String convertLineTerminators(String in) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			StringReader stringReader = new StringReader(in);
			BufferedReader bufferedReader = new BufferedReader(stringReader);		
			String line;
			try {
				while ((line= bufferedReader.readLine()) != null) {
					printWriter.println(line);
				}
			} catch (IOException e) {
				return in; // return the call hierarchy unfiltered
			}
			return stringWriter.toString();
		}
	}

    static void log(String message, Throwable e) {
        LapsePlugin.trace(LapsePlugin.SOURCE_DEBUG, "Source view: " + message, e);
    }
    
    static void log(String message) {
        log(message, null);
    }
    
    static void logError(String message) {
        log(message, new Throwable());
    }
}


