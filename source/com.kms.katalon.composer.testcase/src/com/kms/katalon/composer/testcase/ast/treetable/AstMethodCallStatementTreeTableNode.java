package com.kms.katalon.composer.testcase.ast.treetable;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.eclipse.swt.graphics.Image;

import com.kms.katalon.composer.testcase.constants.ImageConstants;
import com.kms.katalon.composer.testcase.constants.StringConstants;
import com.kms.katalon.core.ast.AstTextValueUtil;

public class AstMethodCallStatementTreeTableNode extends AstStatementTreeTableNode {
	private ExpressionStatement expressionStatement;

	public AstMethodCallStatementTreeTableNode(ExpressionStatement expressionStatement, AstTreeTableNode parentNode,
			ASTNode parentObject, ClassNode scriptClass) {
		super(expressionStatement, parentNode, parentObject, scriptClass);
		this.expressionStatement = expressionStatement;
	}

	@Override
	public String getItemText() {
		return StringConstants.TREE_METHOD_CALL_STATEMENT;
	}
	
	@Override
	public String getInputText() {
		return AstTextValueUtil.getTextValue(expressionStatement.getExpression());
	}

	@Override
	public Image getNodeIcon() {
		return ImageConstants.IMG_16_FUNCTION;
	}
	
	@Override
	public boolean hasChildren() {
		return false;
	}
}
