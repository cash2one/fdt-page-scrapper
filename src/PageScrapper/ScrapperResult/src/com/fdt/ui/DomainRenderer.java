
package com.fdt.ui;

import com.fdt.scrapper.Domain;
import com.fdt.scrapper.task.PageTasks;
import org.netbeans.swing.outline.RenderDataProvider;

public class DomainRenderer implements RenderDataProvider {

    @Override
    public java.awt.Color getBackground(Object o) {
        return null;
    }

    @Override
    public String getDisplayName(Object o) {
        if(o instanceof PageTasks){
            return ((PageTasks) o).getDomain().getName();
        }else{
            return ((Domain) o).getName();
        }
    }

    @Override
    public java.awt.Color getForeground(Object o) {
        return null;
    }

    @Override
    public javax.swing.Icon getIcon(Object o) {
        return null;

    }

    @Override
    public String getTooltipText(Object o) {
        return "Text there";
    }

    @Override
    public boolean isHtmlDisplayName(Object o) {
        return false;
    }

}