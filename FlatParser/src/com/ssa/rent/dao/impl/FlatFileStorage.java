package com.ssa.rent.dao.impl;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.ssa.rent.Flat;
import com.ssa.rent.dao.IFlatStorage;

public class FlatFileStorage implements IFlatStorage{

    private final String FILE_STORAGE = "flat.ser";
    @Override
    public ArrayList<Flat> readFlatList() {
	ArrayList<Flat> flatList = new ArrayList<Flat>();
	try {
	    ObjectInput ois = new ObjectInputStream(new FileInputStream(FILE_STORAGE));
	    int flatCount = ois.readInt();
	    for(int i = 0; i < flatCount; i++){
		Flat flat = (Flat)ois.readObject();
		flatList.add(flat);
	    }
	    ois.close();
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return flatList;
    }

    @Override
    public boolean storeFlat(ArrayList<Flat> flatList) {
	ObjectOutput out;
	try {
	    out = new ObjectOutputStream(new FileOutputStream(FILE_STORAGE));
	    out.writeInt(flatList.size());
	    for(Object flat:flatList){
		out.writeObject(flat);
	    }
	    out.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    return false;
	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

}
