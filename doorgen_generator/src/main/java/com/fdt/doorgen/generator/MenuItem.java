package com.fdt.doorgen.generator;

public class MenuItem {
	
	private String label;
	
	private String href;
	
	private String styleClass;
	
	private String pageTitle;
	
	private String pageMetaKeywords;
	
	private String pageMetaDescription;
	
	private String tmplPageLabel;
	
	private String contentFile;
	
	//TODO Make it configurable in future if need
	private static final String menuItemTmpl = "<li class=\"page_item\"><a href=\"%s\" class=\"%s\">%s</a></li>";
	
	public MenuItem() {
		super();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getPageMetaKeywords() {
		return pageMetaKeywords;
	}

	public void setPageMetaKeywords(String pageMetaKeywords) {
		this.pageMetaKeywords = pageMetaKeywords;
	}

	public String getPageMetaDescription() {
		return pageMetaDescription;
	}

	public void setPageMetaDescription(String pageMetaDescription) {
		this.pageMetaDescription = pageMetaDescription;
	}

	public String getContentFile() {
		return contentFile;
	}

	public void setContentFile(String contentFile) {
		this.contentFile = contentFile;
	}
	
	public String getTmplLabel() {
		return tmplPageLabel;
	}

	public void setTmplPageLabel(String tmplPageLabel) {
		this.tmplPageLabel = tmplPageLabel;
	}

	@Override
	public String toString() {
		return String.format(menuItemTmpl, href.equals("/")?"/":String.format("/%s/", href), styleClass, label);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contentFile == null) ? 0 : contentFile.hashCode());
		result = prime * result + ((href == null) ? 0 : href.hashCode());
		result = prime * result
				+ ((styleClass == null) ? 0 : styleClass.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MenuItem other = (MenuItem) obj;
		if (contentFile == null) {
			if (other.contentFile != null)
				return false;
		} else if (!contentFile.equals(other.contentFile))
			return false;
		if (href == null) {
			if (other.href != null)
				return false;
		} else if (!href.equals(other.href))
			return false;
		if (styleClass == null) {
			if (other.styleClass != null)
				return false;
		} else if (!styleClass.equals(other.styleClass))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
}
