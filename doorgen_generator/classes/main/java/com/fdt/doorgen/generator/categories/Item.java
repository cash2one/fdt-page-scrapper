package com.fdt.doorgen.generator.categories;

import java.math.BigDecimal;

public class Item {
	
	private int category_id;
	private String item_name;
	//url string
	private String item_name_latin;
	private String geo_placename;
	private String geo_position;
	private String geo_category;
	private String ICBM;
	private BigDecimal lat;
	private BigDecimal lng;
	private String zip_code;
	private String country;
	private String tmpl_text;
	private String generated_text;
	private Double ratingCount;
	private int reviewCount;
	private int voteCount;
	
	public static Item parseItem(String key){
		Item newItem = new Item();
		
		String[] values = key.split("~",16);
		
		newItem.item_name = values[0];
		newItem.item_name_latin = values[1];
		newItem.category_id = 0;
		newItem.geo_placename = values[3];
		newItem.geo_position = values[4];
		newItem.geo_category = values[5];
		newItem.ICBM = values[6];
		newItem.lat = new BigDecimal(values[7]);
		newItem.lng = new BigDecimal(values[8]);
		newItem.zip_code = values[9];
		newItem.country = values[10];
		newItem.tmpl_text = values[11];
		newItem.generated_text = values[12];
		newItem.ratingCount = Double.valueOf(values[13]);
		newItem.reviewCount = Integer.valueOf(values[14]);
		newItem.voteCount = Integer.valueOf(values[15]);
		
		//category_id, item_name, item_name_latin, geo_placename, geo_position, geo_category, ICBM, lat, lng, zip_code, country, tmpl_text, generated_text, ratingCount, reviewCount, voteCount
		
		
		return newItem;
	}

	public int getCategory_id() {
		return category_id;
	}

	public String getItem_name() {
		return item_name;
	}

	public String getItem_name_latin() {
		return item_name_latin;
	}

	public String getGeo_placename() {
		return geo_placename;
	}

	public String getGeo_position() {
		return geo_position;
	}

	public String getGeo_category() {
		return geo_category;
	}

	public String getICBM() {
		return ICBM;
	}

	public BigDecimal getLat() {
		return lat;
	}

	public BigDecimal getLng() {
		return lng;
	}

	public String getZip_code() {
		return zip_code;
	}

	public String getCountry() {
		return country;
	}

	public String getTmpl_text() {
		return tmpl_text;
	}

	public String getGenerated_text() {
		return generated_text;
	}

	public Double getRatingCount() {
		return ratingCount;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	public int getVoteCount() {
		return voteCount;
	} 
}
