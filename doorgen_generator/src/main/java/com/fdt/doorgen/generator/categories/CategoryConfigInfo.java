package com.fdt.doorgen.generator.categories;

import com.fdt.scrapper.task.ConfigManager;

public class CategoryConfigInfo 
{
	private static final String CATEGORY_TITLE_LABEL = "main_cat_title";
	private static final String SUB_CATEGORY_NUM_LABEL = "sub_cat_label";
	private static final String IS_HAS_SYNONYM_LABEL = "is_has_synonym";
	
	private String catTitleStr;

	private String subCatNumStr;
	
	private boolean isHasSynonymText = false;

	public CategoryConfigInfo(ConfigManager cfgMgr)
	{
		this.catTitleStr = cfgMgr.getProperty(CATEGORY_TITLE_LABEL);
		this.subCatNumStr = cfgMgr.getProperty(SUB_CATEGORY_NUM_LABEL);
		this.isHasSynonymText = Boolean.valueOf(cfgMgr.getProperty(IS_HAS_SYNONYM_LABEL));
	}

	public String getCatTitleStr() {
		return catTitleStr;
	}

	public void setCatTitleStr(String catTitleStr) {
		this.catTitleStr = catTitleStr;
	}

	public String getSubCatNumStr() {
		return subCatNumStr;
	}

	public void setSubCatNumStr(String subCatNumStr) {
		this.subCatNumStr = subCatNumStr;
	}

	public boolean isHasSynonymText() {
		return isHasSynonymText;
	}

	public void setHasSynonymText(boolean isHasSynonymText) {
		this.isHasSynonymText = isHasSynonymText;
	}
}
