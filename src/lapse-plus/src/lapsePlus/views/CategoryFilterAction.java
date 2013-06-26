package lapsePlus.views;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

final class CategoryFilterAction extends Action {
    private boolean hasFilter = false;
   
    private final ViewerFilter filter;

	private final TableViewer viewer;
    
	CategoryFilterAction(final TableViewer viewer, final String labelText, final String categoryName) {
		super(labelText, IAction.AS_CHECK_BOX);
		
		this.viewer = viewer;
		
	    filter = new ViewerFilter() {
	        public boolean select(Viewer viewer, Object parentElement, Object element) {
	            SinkMatch match = (SinkMatch) element;
	            return match.getCategory().equalsIgnoreCase(categoryName);
	        }
	    };

        setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
	}

    public void run() {
        if (!hasFilter) {
            viewer.addFilter(filter);
            hasFilter = true;
        } else {
            viewer.removeFilter(filter);
            hasFilter = false;
        }
    }
}
