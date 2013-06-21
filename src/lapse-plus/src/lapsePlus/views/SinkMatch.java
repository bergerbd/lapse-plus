package lapsePlus.views;

import lapsePlus.utils.StringUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * This corresponds to a particular sink.
 */
class SinkMatch extends Match {
    private boolean done = false;
    private boolean safe = false;
    private boolean hasSource = true;

    SinkMatch(String message, ASTNode ast, CompilationUnit unit, IResource resource,
        String type, IMember member, String category, boolean error, boolean hasSource) {
		super(message, ast, unit, resource, type, category, member, error);
        this.hasSource = hasSource;
    }


    public boolean hasSource() {
        return hasSource;
    }

    public String toLongString() {
        return getStatusSymbol() + " " + StringUtils.cutto(this.getAST().toString(), 65) + "\t"
            + StringUtils.cutto(this.getType(), 40) + "\t"
            + StringUtils.cutto(this.getProject().getProject().getName(), 20) + "\t"
            + this.getResource().getFullPath().toString() + ":" + this.getLineNumber();
    }

    private String getStatusSymbol() {
        if (!isError()) {
            return "SAFE   ";
        } else if (isSafe()) {
            return "M-SAFE ";
        } else if (isDone()) {
            return "M-ERROR";
        } else {
            return "UNKNOWN";
        }
    }

    public void setDone(boolean b) {
        this.done = b;
    }

    public boolean isDone() {
        return this.done;
    }

    public boolean isSafe() {
        return this.safe;
    }

    public void setSafe(boolean b) {
        this.safe = b;
    }

    public boolean getSafe() {
        return this.safe;
    }
}
