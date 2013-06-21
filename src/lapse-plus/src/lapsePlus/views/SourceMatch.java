package lapsePlus.views;

import lapsePlus.utils.StringUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * This corresponds to a particular sink.
 */
class SourceMatch extends Match {
	private boolean nonWeb;

	SourceMatch(String message, ASTNode ast, CompilationUnit unit,
			IResource resource, String type, String category, IMember member,
			boolean error, boolean nonWeb) {
		super(message, ast, unit, resource, type, category, member, error);
		this.nonWeb = nonWeb;
	}

	public String toLongString() {
		return StringUtils.cutto(this.getAST().toString(), 65) + "\t"
				+ StringUtils.cutto(this.getType(), 20) + "\t"
				+ StringUtils.cutto(this.getProject().getProject().getName(), 12) + "\t"
				+ this.getResource().getFullPath().toString() + ":"
				+ this.getLineNumber();
	}

	public boolean isNonWeb() {
		return this.nonWeb;
	}
}
