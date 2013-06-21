package lapsePlus.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

abstract class Match {

	private ASTNode ast;
	private String category;
	private boolean error = false;
	private IMember member;
	private String message;
	private IResource resource;
	private String type;
	private CompilationUnit unit;

	protected Match(String message, ASTNode ast, CompilationUnit unit,
			IResource resource, String type, String category,
			IMember member, boolean error) {
		this.message = message;
		this.ast = ast;
		this.unit = unit;
		this.resource = resource;
		this.type = type;
		this.category = category;
		this.member = member;
		this.error = error;
	}

	public final ASTNode getAST() {
        return ast;
    }

    public final String getCategory() {
        return category;
    }

    public final String getFileName() {
		return resource.getName();
	}

    public final String getFullFileName() {
		return resource.getFullPath().toString();
	}

    public final int getLineNumber() {
		return isSource() ? unit.getLineNumber(ast.getStartPosition()) : -1;
	}

    public final IMember getMember() {
        return member;
    }

    public final String getMessage() {
		return message;
	}

    public final IProject getProject() {
		return resource.getProject();
	}

	public final IResource getResource() {
        return resource;
    }

	public final String getType() {
        return type;
    }

	public final CompilationUnit getUnit() {
        return unit;
    }

	public final boolean isError() {
		return error;
	}

	public final boolean isSource() {
		return unit != null;
	}

	public abstract String toLongString();

	public final String toString() {
        return message;
    }

}
