package com.kms.katalon.composer.testcase.providers;

import org.eclipse.swt.graphics.Image;

import com.kms.katalon.composer.components.impl.providers.TypeCheckedStyleTreeCellLabelProvider;
import com.kms.katalon.composer.testcase.ast.treetable.AstMethodTreeTableNode;
import com.kms.katalon.composer.testcase.ast.treetable.AstStatementTreeTableNode;
import com.kms.katalon.composer.testcase.ast.treetable.AstTreeTableNode;

public class AstTreeItemLabelProvider extends TypeCheckedStyleTreeCellLabelProvider<AstTreeTableNode> {

    private static final int CLMN_ITEM_IDX = 0;

    public AstTreeItemLabelProvider() {
        super(CLMN_ITEM_IDX);
    }

    /**
     * Add 10 pixels for the space between image and text because of ELSE-IF label's is not 16x16.
     * 
     * @see <a href="https://incubation.kms-technology.com/browse/KAT-969">KAT-969</a>
     */
    @Override
    protected int getSpace() {
        return 10;
    }

    @Override
    protected Class<AstTreeTableNode> getElementType() {
        return AstTreeTableNode.class;
    }

    @Override
    protected Image getImage(AstTreeTableNode element) {
        return element.getIcon();
    }

    @Override
    protected String getText(AstTreeTableNode element) {
        return ((element instanceof AstStatementTreeTableNode) ? (getStringIndex(element) + " - ") : "")
                + element.getItemText();
    }

    private String getStringIndex(AstTreeTableNode node) {
        if (node.getParent() == null || !(node.getParent().canHaveChildren()) || node instanceof AstMethodTreeTableNode) {
            return "";
        }
        String parentIndex = getStringIndex(node.getParent());
        String thisIndex = String.valueOf(node.getParent().getChildren().indexOf(node) + 1);
        if (!parentIndex.isEmpty()) {
            return parentIndex + "." + thisIndex;
        }
        return thisIndex;
    }

    @Override
    public String getElementToolTipText(AstTreeTableNode element) {
        return element.getItemTooltipText();
    }
}
