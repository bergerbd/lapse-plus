package lapsePlus.views;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

class LapseRadioActionGroup extends LapseMultiActionGroup {
	private int fCurrentSelection;

	public LapseRadioActionGroup(IAction[] actions, int currentSelection, boolean toggle) {
		super(actions);

		fCurrentSelection = currentSelection;
	}

	/**
	 * Add the actions to the given menu manager.
	 */
	@Override
	protected void addActions(IMenuManager viewMenu) {
		viewMenu.add(new Separator()); //we begin adding a Separator

		//We go all over the actions
		for (int i = 0; i < fActions.length; i++) {
			final int j = i;

			//We create the menu
			viewMenu.add(new ContributionItem() {//Contribution item in a menu is a button or a separator. A contribution item in a menu bar is a menu
				
				public void fill(Menu menu, int index) {
					// System.err.println("Filling the menu");
					int style = SWT.CHECK;
					
					if ((fActions[j].getStyle() & IAction.AS_RADIO_BUTTON) != 0)
						style = SWT.RADIO;

					//Initializing the menu and the images
					//The MenuItem receives a menu, the style of check or radio button
					MenuItem mi = new MenuItem(menu, style, index);
					ImageDescriptor d = fActions[j].getImageDescriptor();
					mi.setImage(JavaPlugin.getImageDescriptorRegistry().get(d));
					fItems[j] = mi;

					mi.setEnabled(true);
					mi.setText(fActions[j].getText());
					mi.setSelection(fCurrentSelection == j);
					fStatus[j] = (fCurrentSelection == j);

					//To know if the menu is selected
					mi.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							if (fCurrentSelection == j) {
								// already selected
								fItems[fCurrentSelection].setSelection(true);
								fStatus[fCurrentSelection] = true;
								return;
							}
							
							fActions[j].run();

							// Update checked state
							fItems[fCurrentSelection].setSelection(false);
							fStatus[fCurrentSelection] = false;
							fCurrentSelection = j;
							fItems[fCurrentSelection].setSelection(true);
							fStatus[fCurrentSelection] = true;
						}
					});
				}
			});
		}
	}
}