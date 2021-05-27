package lapsePlus.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.actions.ActionGroup;

public abstract class LapseMultiActionGroup extends ActionGroup {
	
	protected final IAction[] fActions;
	protected final MenuItem[] fItems;
	protected final boolean[] fStatus;

	/**
	 * Creates a new action group with a given set of actions.
	 * 
	 * @param actions
	 *            the actions for this multi group
	 * @param currentSelection
	 *            decides which action is selected in the menu on start up.
	 *            Denotes the location in the actions array of the current
	 *            selected state. It cannot be null.
	 */
	public LapseMultiActionGroup(IAction[] actions) {
		fActions = actions;
		fItems = new MenuItem[fActions.length];
		fStatus = new boolean[fActions.length];
	}

	/**
	 * Add the actions to the given menu manager.
	 */
	protected abstract void addActions(IMenuManager viewMenu);

	public void setEnabled(boolean enabled) {
		for (int i = 0; i < fItems.length; i++) {
			MenuItem e = fItems[i];
			if (e != null) {
				e.setEnabled(enabled);
			}
		}
	}
}
