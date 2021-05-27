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

class LapseCheckboxActionGroup extends LapseMultiActionGroup {
	private final boolean fInitial[]; // initial values

	public LapseCheckboxActionGroup(final IAction[] actions, final boolean[] initial) {
		super(actions);

		this.fInitial = initial;
	}

	/**
	 * Add the actions to the given menu manager.
	 */
	@Override
	protected void addActions(IMenuManager viewMenu) {
		viewMenu.add(new Separator());

		for (int i = 0; i < fActions.length; i++) {
			final int j = i;

			if (fInitial[j]) {
				fActions[j].run();
			}

			viewMenu.add(new ContributionItem() {
				public void fill(Menu menu, int index) {
					// System.err.println("Filling the menu");
					int style = SWT.CHECK;

					MenuItem mi = new MenuItem(menu, style, index);
					ImageDescriptor d = fActions[j].getImageDescriptor();
					mi.setImage(JavaPlugin.getImageDescriptorRegistry().get(d));
					fItems[j] = mi;

					mi.setEnabled(true);
					mi.setText(fActions[j].getText());

					mi.setSelection(fInitial[j]);
					fStatus[j] = fInitial[j];

					mi.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							MenuItem item = fItems[j];
							// System.err.println("Old value: " +
							// item.getSelection());
							item.setSelection(!fStatus[j]);
							fStatus[j] = !fStatus[j];
							// System.err.println("New value: " +
							// item.getSelection());

							fActions[j].run();

						}
					});
				}
			});
		}
	}
}