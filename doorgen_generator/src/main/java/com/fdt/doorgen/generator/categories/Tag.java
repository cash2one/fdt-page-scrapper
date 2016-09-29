package com.fdt.doorgen.generator.categories;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fdt.doorgen.generator.DoorgenGeneratorRunner;
import com.fdt.utils.Utils;

public class Tag {
	private String value;
	private String valueTmpl;
	private Integer parentId = 0;
	private CategoryParentType parentType;
	
	public enum CategoryParentType{
		CATEGORY,
		ITEM
	}
	
	public Tag(String valueTmpl, CategoryParentType parentType, String keyWord, HashMap<Integer,List<String>> preTitles) {
		super();
		this.value = Utils.synonymizeText(Utils.fillTemplate(valueTmpl, keyWord, preTitles, DoorgenGeneratorRunner.PRETITLES_REGEXP));
		this.valueTmpl = valueTmpl;
		this.parentId = 0;
		this.parentType = parentType;
	}
	
	public Tag(String valueTmpl, Integer parentId, CategoryParentType parentType, String keyWord, HashMap<Integer,List<String>> preTitles) {
		super();
		this.value = Utils.synonymizeText(Utils.fillTemplate(valueTmpl, keyWord, preTitles, DoorgenGeneratorRunner.PRETITLES_REGEXP));
		this.valueTmpl = valueTmpl;
		this.parentId = parentId;
		this.parentType = parentType;
	}
	
	public static List<Tag> loadTags(File path2TagsFile, Integer parentId, CategoryParentType parentType, String keyWord, HashMap<Integer,List<String>> preTitles){
		List<Tag> tags = new ArrayList<Tag>();
		
		for(String valueTmpl : Utils.loadFileAsStrList(path2TagsFile)){
			tags.add(new Tag(valueTmpl, parentId, parentType, keyWord, preTitles));
		}
		
		return tags; 
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValueTmpl() {
		return valueTmpl;
	}
	public void setValueTmpl(String valueTmpl) {
		this.valueTmpl = valueTmpl;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public CategoryParentType getParentType() {
		return parentType;
	}
	public void setParentType(CategoryParentType parentType) {
		this.parentType = parentType;
	}
}
