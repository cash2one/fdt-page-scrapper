<?php

	define("DB_HOST", "localhost");

	#Параметр md5 пароля для доступа к панели управления.
	#password
	define("DB_USER_NAME", "usapersonalloans");

	#Параметр корня сайта.
	define("DB_USER_PWD", "lol200");

	#Параметр названия сайта.
	define("DB_NAME", "usapersonalloansorg");
	
	#Параметр названия сайта.
	define("SITE_NAME", "USAPersonalLoans.org");
	
	/*
	
	-------------STATE-------------
	STATE_ABBR			'PR'
	STATE_NAME			'Puerto Rico'
	-------------------------------
	
	
	-------------CITY--------------
	CITY_NAME			'Adak'
	GEO_PLACENAME		'Adak, Aleutians West county, AK 99546, USA'
	GEO_POSITION		'51.8777;-176.659'
	ICBM				'51.8777,-176.659'
	GEO_REGION			'US-AK'
	ZIP_CODE			'99546'
	COUNTRY				'Aleutians West county'
	-------------------------------
	
	*/
	
	#Параметр тайтла линка штата для списка штатов
	define("STATE_LINK_TITLE", "USA Personal Loans [STATE_NAME]");
	
	#Параметр тайтла линка города для списка городов на странице штата
	define("CITY_LIST_LINK_TITLE", "USA Personal Loans [CITY_NAME] [STATE_ABBR]");
	
	#Параметр тайтла линка города для списка ближайщих городов на странице город
	define("CITY_CLOSE_LIST_LINK_TITLE", "USA Personal Loans [CITY_NAME] [STATE_ABBR] [ZIP_CODE]");
	
	
	#Main page title
	define("MAIN_PAGE_TITLE", "Personal Loans in the USA | Quick Cash Online");
	#Main page meta description
	define("MAIN_PAGE_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#Main page meta keywords
	define("MAIN_PAGE_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	#Контент для тэга H1
	define("MAIN_PAGE_H1", "USA Personal Loans");
	#Контент для тэга H2
	define("MAIN_PAGE_H2", "[SITE_NAME] About Us");
	
	
	#State page title
	define("STATE_PAGE_TITLE", "Online Personal [STATE_NAME] Loans | USA Cash");
	#State meta description
	define("STATE_META_DESCRIPTION", "Need some Personal Loans within the state of [STATE_NAME]? Looking for Instant Approval Personal Loans Online? No credit check direct lenders companies in [CITY_COUNT] cities of [STATE_ABBR] USA. Get Your unsecured/secured Cash loan Now!");
	#State meta keywords
	define("STATE_META_KEYWORDS", "[STATE_ABBR] Personal Loans, bad credit ok, private bank loans [STATE_NAME], small personal credit, best direct lenders [STATE_ABBR], instant approval in [STATE_NAME] USA");
	#Контент для тэга H1
	define("STATE_PAGE_H1", "Personal Loans in [STATE_NAME] USA");
	#Контент для тэга H2
	define("STATE_PAGE_H2", "Apply For Your Money Today!, [STATE_ABBR] USA");

	
	#City page title
	define("CITY_PAGE_TITLE", "Personal Loans [CITY_NAME] [COUNTRY] [STATE_ABBR] | USA Personal Loans");
	#City meta description
	define("CITY_META_DESCRIPTION", "Need some quick Guaranteed Personal Loans Cash within 50 miles of [CITY_NAME] [STATE_NAME] near you? Looking for a small no credit check payday direct lenders companies with low interest rates Online? Fast Instant Approval Personal Loans in [STATE_ABBR]. Get Your secured/unsecured MONEY loan Today!");
	#City meta keywords
	define("CITY_META_KEYWORDS", "Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in [STATE_ABBR], [CITY_NAME], [CLOUDS]");
	#Контент для тэга H1
	define("CITY_PAGE_H1", "Best Place To Get A Personal Loan [CITY_NAME], [STATE_ABBR]");
	#Контент для тэга H2
	define("CITY_PAGE_H2", "Apply For Your Cash NOW! in [CITY_NAME], [STATE_NAME] [ZIP_CODE] Guaranteed Payment");
	
	#FAQ page title
	define("FAQ_PAGE_TITLE", "Frequently Asked Questions about Personal Loans in the USA");
	#FAQ meta description
	define("FAQ_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#FAQ meta keywords
	define("FAQ_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	
	#Policy page title
	define("CONTACT_PAGE_TITLE", "Contact US in the USA");
	#Policy meta description
	define("CONTACT_META_DESCRIPTION", "Contact us in the USA");
	#Policy meta keywords
	define("CONTACT_META_KEYWORDS", "Contact us in the USA");

	#Apply now page title
	define("APPLY_PAGE_TITLE", "Apply Now for Your Personal Loan in the USA");
	#Policy meta description
	define("APPLY_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#Policy meta keywords
	define("APPLY_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	
	#Policy page title
	define("POLICY_PAGE_TITLE", "Policy on Responsible Lending");
	#Policy meta description
	define("POLICY_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#Policy meta keywords
	define("POLICY_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	
	#Articles page title
	define("ARTICLES_LIST_PAGE_TITLE", "Articles");
	#Articles meta description
	define("ARTICLES_LIST_META_DESCRIPTION", "Articles");
	#Articles meta keywords
	define("ARTICLES_LIST_META_KEYWORDS", "Articles");
		
	#Privacy Policy page title
	define("PRIVACY_PAGE_TITLE", "Privacy Policy | Quick Cash Online");
	#Privacy meta description
	define("PRIVACY_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#Privacy meta keywords
	define("PRIVACY_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	
	#Terms of Use page title
	define("TERMS_PAGE_TITLE", "Terms of Use | Quick Cash Online");
	#Terms of Use meta description
	define("TERMS_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#Terms of Use meta keywords
	define("TERMS_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	
	#Rates and Fees page title
	define("RATES_PAGE_TITLE", "Rates and Fees | Quick Cash Online");
	#Rates and Fees Use meta description
	define("RATES_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#Rates and Fees Use meta keywords
	define("RATES_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	
	#E-Consent page title
	define("ECONSENT_PAGE_TITLE", "Consent for Electronic Signatures, Records, Disclosures, Communication | Quick Cash Online");
	#E-Consent Use meta description
	define("ECONSENT_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#E-Consent Use meta keywords
	define("ECONSENT_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	
	#Disclaimer page title
	define("DISCLAIMER_PAGE_TITLE", "Disclaimer and APR Representative | Quick Cash Online");
	#Rates and Fees Use meta description
	define("DISCLAIMER_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#Rates and Fees Use meta keywords
	define("DISCLAIMER_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
	
	#Marketing Practices page title
	define("MARKETING_PAGE_TITLE", "Marketing Practices | Quick Cash Online");
	#Marketing Practices Use meta description
	define("MARKETING_META_DESCRIPTION", "Need extra money in USA? Looking For Personal Loans? Get Your loan!");
	#Marketing Practices Use meta keywords
	define("MARKETING_META_KEYWORDS", "USA Personal Loans, bad credit ok, private bank loans, small personal credit, best direct lenders, instant approval in USA");
		
?>