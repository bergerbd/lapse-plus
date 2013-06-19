/**
 * (c) 2013 Bernhard J. Berger
 * 
 * Lapse+ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package lapsePlus.views;

import lapsePlus.HistoryDefinitionLocation;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyImageDescriptor;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public final class HistoryDefinitionLocationLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Image getImage(Object element) {
		if(!(element instanceof HistoryDefinitionLocation)) return null;
		HistoryDefinitionLocation hdl = (HistoryDefinitionLocation)element;
		Image result = null;
		switch(hdl.getType()) {
			case HistoryDefinitionLocation.CALL_ARG 		:
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE);			// actual parameter of a call
				break;
			case HistoryDefinitionLocation.FORMAL_PARAMETER	:
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);			// formal parameter of a method
				break;
			case HistoryDefinitionLocation.RETURN	:
				result = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_INTERFACE_DEFAULT);	// return result of a method 
				break;
			case HistoryDefinitionLocation.DECLARATION 		:
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);			// local declaration
				break;
			case HistoryDefinitionLocation.UNDEFINED   		:
				result = JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PRIVATE);			// no definition found for some reason
				break;
			case HistoryDefinitionLocation.INITIAL     		:
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_DEFAULT);			// initial query
				break;
			default:			
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);			
		}
		int flags = 0;
		if (hdl.isRecursive()) {
			flags |= CallHierarchyImageDescriptor.RECURSIVE;
		}
		if (hdl.isMaxLevel()) {
			flags |= CallHierarchyImageDescriptor.MAX_LEVEL;
		}
		// adorn the image with a small arrow
		ImageDescriptor baseImage= new ImageImageDescriptor(result);
        Rectangle bounds= result.getBounds();
        return JavaPlugin.getImageDescriptorRegistry().get(
        		new CallHierarchyImageDescriptor(
        				baseImage, 
        				flags, 
						new Point(bounds.width, bounds.height)));

	}

	@Override
	public String getText(Object element) {
		HistoryDefinitionLocation curElem = (HistoryDefinitionLocation) element;
		if (curElem == null) {
			return "(error)";
		}
		return curElem.toString();
	}

}
