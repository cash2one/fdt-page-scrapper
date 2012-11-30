
package com.fdt.ui;

import com.fdt.scrapper.Domain;
import com.fdt.scrapper.task.PageTasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.tree.TreeModel;


public class FileTreeModel implements TreeModel {

    private ArrayList<PageTasks> root;

    public FileTreeModel(ArrayList<PageTasks> root) {
        this.root = root;
    }

    @Override
    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {
        //do nothing
    }

    @Override
    public Object getChild(Object parent, int index) {
        if(parent instanceof ArrayList){
            return ((ArrayList)parent).get(index);
        } else if(parent instanceof PageTasks){
            return ((PageTasks)parent).getDomain().getSubDomainsList().get(index);
        }
        return new Object();
    }

    @Override
    public int getChildCount(Object parent) {
        if(parent instanceof PageTasks){
            //SubDomain
            return ((PageTasks)parent).getDomain().getSubDomainsList().size();
        } else if(parent instanceof ArrayList){
            //PageTasks/Domains
            return ((ArrayList)parent).size();
        }
        return 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        //File par = (File) parent;
        //File ch = (File) child;
        //return Arrays.asList(par.listFiles()).indexOf(ch);
        if(parent instanceof ArrayList){
            int index=0;
            //PageTasks/Domains
            PageTasks entryChild = (PageTasks)child;
            for(Object tasks : ((ArrayList)parent)){
                PageTasks entry = (PageTasks)tasks;
                if(entry.getDomain().getName().equals(entryChild.getDomain().getName())){
                    return index;
                }
                index++;
            }
            return ((ArrayList)parent).size();
        } else {
            PageTasks ptEntry = (PageTasks)parent;
            Domain dEntry = (Domain)child;
            return ptEntry.getDomain().getSubDomainsIndexList().get(dEntry.getName());
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public boolean isLeaf(Object node) {
        if(node instanceof Domain){
            return true;
        } else if(node instanceof PageTasks){
            PageTasks ptEntry = (PageTasks)node;
            if(ptEntry.getDomain().getSubDomainsList().size() == 0){
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {
        //do nothing
    }

    @Override
    public void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
        //do nothing
    }

}