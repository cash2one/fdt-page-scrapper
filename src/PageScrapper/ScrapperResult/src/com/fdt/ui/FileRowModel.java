
package com.fdt.ui;

import com.fdt.scrapper.Domain;
import com.fdt.scrapper.task.PageTasks;
import org.netbeans.swing.outline.RowModel;

public class FileRowModel implements RowModel {

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return Long.class;
            default:
                assert false;
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Count";
            case 1:
                return "Alexa Rank";
            case 2:
                return "ALL index";
            case 3:
                return "Week index";
            case 4:
                return "PR";
            case 5:
                return "TIC";
        }
        return "";
    }

    @Override
    public Object getValueFor(Object node, int column) {
        if(node instanceof PageTasks){
        PageTasks pt = (PageTasks) node;
        switch (column) {
            case 0:
                return new Long(pt.getDomain().getCount());
            case 1:
                return new Long(pt.getTasks().get(0).getResult().get(0));
            case 2:
                return new Long(pt.getTasks().get(1).getResult().get(0));
            case 3:
                return new Long(pt.getTasks().get(2).getResult().get(0));
            case 4:
                return new Long(pt.getTasks().get(0).getResult().get(1));
            case 5:
                return new Long(pt.getTasks().get(0).getResult().get(2));
            default:
                assert false;
        }
        }
        else {
         Domain dmn = (Domain) node;
        switch (column) {
            case 0:
                return new Long(dmn.getCount());
            case 1:
                return 0;
            case 2:
                return 0;
            case 3:
                return 0;
            case 4:
                return 0;
            case 5:
                return 0;
            default:
                assert false;   
        }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    @Override
    public void setValueFor(Object node, int column, Object value) {
        //do nothing for now
    }

}