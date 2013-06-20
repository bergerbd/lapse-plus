/*
 * Copyright (c) 2013 "Bernhard J. Berger"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Lapse+.
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
package lapsePlus.jdom;

import java.util.Iterator;

import lapsePlus.LapsePlugin;
import lapsePlus.views.DeclarationInfoManager;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Class with jdom helper functions. Extracted from other sources.
 * 
 * @author Bernhard J. Berger
 */

public final class JDomHelper {
    private static void log(String message, Throwable e) {
        LapsePlugin.trace(LapsePlugin.SOURCE_DEBUG, "Source view: " + message, e);
    }
    
    private static void logError(String message) {
        log(message, new Throwable());
    }
    
	public static VariableDeclaration name2decl(SimpleName sn, CompilationUnit cu, IResource resource){
		DeclarationInfoManager.DeclarationInfo info = DeclarationInfoManager.retrieveDeclarationInfo(cu);
		//System.out.println(info);
		VariableDeclaration decl = info.getVariableDeclaration(sn);
		if(decl == null) {
			logError("decl == null for " + sn);	
		}
		return decl;
	}
	/**
     * Tests whether a given expression is a String constant.
     * 
     * @param expr Argument that we want to test
     * @param unit The compilation unit of {@code expr}.
     * @param resource The workspace resource corresponding to {@code unit}.
     * 
     * This method does pattern-matching to find constant strings. If none of 
     * the patterns match, false is returned. 
     */
    public static boolean isStringConstant(final Expression expr, final CompilationUnit unit, final IResource resource) {
        if (expr instanceof StringLiteral) {
            return true;
        } else if (expr instanceof InfixExpression) {
            final InfixExpression infixExpr = (InfixExpression) expr;
            if (!isStringConstant(infixExpr.getLeftOperand(), unit, resource)) {
            	return false;
            }
            
            if (!isStringConstant(infixExpr.getRightOperand(), unit, resource)) {
            	return false;
            }
            
            final Iterator<?> iter = infixExpr.extendedOperands().iterator();
            while(iter.hasNext()) {
                if (!isStringConstant((Expression) iter.next(), unit, resource)) {
                    return false;
                }
            }
            return true;
        } else if (expr instanceof SimpleName) {
            final SimpleName name = (SimpleName) expr;
            final VariableDeclaration varDecl = name2decl(name, unit, resource);

            if (varDecl == null) {
            	logError("Cannot find declaration for " + name);
            	return false;
            } else if (varDecl instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration decl = (SingleVariableDeclaration) varDecl;
                if (decl.getInitializer() != null && decl.getInitializer() instanceof StringLiteral) {
                    return true;
                }
            } else {
                VariableDeclarationFragment decl = (VariableDeclarationFragment) varDecl;
                if (decl.getInitializer() != null) {
                    return isStringConstant(decl.getInitializer(), unit, resource);
                }
            }
        } else if (expr instanceof MethodInvocation) {
            MethodInvocation inv = (MethodInvocation) expr;
            if (inv.getName().getIdentifier().equals("toString")) {
                // TODO: StringBuffer.toString() return result
                // Expression target = inv.getExpression();
                // System.err.println("TODO -> methodInv: " + inv);
            }
        }
        // TODO: add final/const

        return false;       // this is a conservative return value
    }
}
