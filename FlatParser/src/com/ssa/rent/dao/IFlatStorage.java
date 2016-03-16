package com.ssa.rent.dao;
import java.util.ArrayList;
import java.util.List;

import com.ssa.rent.Flat;

public interface IFlatStorage {
	public boolean storeFlat(ArrayList<Flat> flatList);
	public ArrayList<Flat> readFlatList();
}
