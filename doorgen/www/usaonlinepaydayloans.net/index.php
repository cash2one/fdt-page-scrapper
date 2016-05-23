<?php

error_reporting(E_ALL ^ E_NOTICE);

require_once "utils/pager.php";
require_once "application/models/functions_decode.php";
require_once "application/libraries/parser.php";
require_once "utils/title_generator.php";
require_once "utils/case_value_selector.php";
require_once "utils/config.php";

$page_title="";
$page_meta_keywords="";
$page_meta_description="";

$page_meta_placename = "";
$page_meta_position = "";
$page_meta_region = "";
$page_meta_icbm = "";

$function = new Functions;

function fillCityInfo($con, $url_region, $url_city, $template)
{
	global $state_name, $state_abbr, $page_title,$page_meta_keywords,$page_meta_description,$page_meta_placename,$page_meta_position;
	global $page_meta_region,$page_meta_icbm,$city_name,$geo_placename,$geo_position,$icbm,$geo_region,$zip_code,$country,$clouds;
	$result_array = array();
	$query_case_list = 	" SELECT c.city_name, city_name_latin, c.geo_placename, c.geo_position, c.geo_region, c.ICBM, c.zip_code, c.country, r.region_name, r.abbr " .
						" FROM city c LEFT JOIN region r ON c.region_id = r.region_id " .
						" WHERE LOWER(r.region_name_latin) = LOWER(?) AND LOWER(c.city_name_latin) = LOWER(?)  "; 
	
	//$query_case_list = "SELECT c.city_name, c.city_name_latin, ek.key_value, ek.key_value_latin, r.region_name, r.region_name_latin, unix_timestamp(cp.posted_time), r.region_id, cp.anchor_name FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND cp.city_page_key = ? AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id AND cp.posted_time <= now()";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "ss", $url_region, $url_city)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $city_name_int,$city_name_latin_int,$geo_placename_int, $geo_position_int, $geo_region_int, $ICBM_int, $zip_code_int, $country_int, $region_name_int, $abbr_int)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	
	if(mysqli_stmt_fetch($stmt)) {
		$city_name = $city_name_int;
		$geo_placename = $geo_placename_int;
		$geo_position = $geo_position_int;
		$icbm = $ICBM_int;
		$geo_region = $geo_region_int;
		$zip_code = $zip_code_int;
		$country = $country_int;
		
		$state_name = $region_name_int;
		$state_abbr = $abbr_int;
	
		$page_meta_placename = "<meta name=\"geo.placename\" content=\"$geo_placename\" />";
		$page_meta_position = "<meta name=\"geo.position\" content=\"$geo_position\" />";
		$page_meta_region = "<meta name=\"geo.region\" content=\"$geo_region\" />";
		$page_meta_icbm = "<meta name=\"ICBM\" content=\"$icbm\" />";
	}else{
		return null;
	}
	
	mysqli_stmt_close($stmt);
	
	$citiesListSrt = "";
	$citiesListHtml = "";
	
	$cityList =  getClosestCitiesList($con, $url_region, $url_city);
	
	foreach ($cityList as $k => $v) {
		$abbr = $v["abbr"];
		$cityName = $v["cityName"];
		$zipCode = $v["zip_code"];
		$cityNameLatin = $v["cityNameLatin"];
		$region_name_latin = $v["region_name_latin"];
		
		$citiesListSrt = $citiesListSrt." ".$v["cityName"]." $abbr,";
		
		$citiesListHtml = $citiesListHtml . "<li>•&nbsp;&nbsp;<a href=\"/$region_name_latin/$cityNameLatin/\" title=\"".CITY_CLOSE_LIST_LINK_TITLE."\">$cityName, $abbr</a></li>";
		
		$citiesListHtml = preg_replace("/\[CITY_NAME\]/", $cityName , $citiesListHtml);
		$citiesListHtml = preg_replace("/\[STATE_ABBR\]/", $abbr , $citiesListHtml);
		$citiesListHtml = preg_replace("/\[ZIP_CODE\]/", $zipCode , $citiesListHtml);
	}
	
	$template = preg_replace("/\[CLOSE_CITIES\]/", $citiesListHtml , $template);
	
	$clouds = trim($citiesListSrt, ",");
	
	return $template;
}

function getClosestCitiesList($con, $url_region, $url_city)
{
	$result_array = array();
	$query_case_list = " SELECT c.city_name, c.city_name_latin, r.abbr, c.zip_code, r.region_name_latin " .
				" FROM  city c, neighbor_city nc, region r WHERE c.city_id = nc.neighbor_city_id AND r.region_id = c.region_id AND " . 
				" nc.city_id = ( " .
				" SELECT c.city_id " .
				" FROM city c, region r " .
				" WHERE c.region_id = r.region_id  AND LOWER(r.region_name_latin) LIKE LOWER(?) AND LOWER(c.city_name_latin) LIKE LOWER(?)) ";

	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "ss", $url_region, $url_city)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $cityName, $cityNameLatin, $abbr, $zip_code, $region_name_latin)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	$i = 0;
	while(mysqli_stmt_fetch($stmt))
	{
		#$citiesList = $citiesList.$cityName." ".$abbr.",";
		$result_array[$i] = array(	"cityName"=>$cityName,
									"cityNameLatin"=>$cityNameLatin,
									"abbr"=>$abbr,
									"zip_code"=>$zip_code,
									"region_name_latin" => $region_name_latin
		);
		
		$i++;
	}
	
	return $result_array;
}

function fillStateList($con, $template)
{
	global $page_title,$page_meta_keywords,$page_meta_description;
	
	$result = mysqli_query($con,"SELECT COUNT(1) city_count, r.region_name_latin, r.region_name, r.abbr FROM city c LEFT JOIN region r ON c.region_id = r.region_id GROUP BY r.region_id ORDER BY r.region_name");

	$regionName;
	$regionNameLatin;
	$regions = "";
	
	while($row = mysqli_fetch_array($result))
	{
		global $regionName, $regionNameLatin;
		$regionName = $row['region_name'];
		$regionNameLatin = $row['region_name_latin'];
		$regions = $regions."<li class=\"page_item\"><a href=\"/$regionNameLatin/\" title=\"".STATE_LINK_TITLE."\">$regionName</a> (" . $row['city_count'] . " cities)</li>\r\n";
		$regions = preg_replace("/\[STATE_NAME\]/", $regionName , $regions);
		$regions = preg_replace("/\[STATE_ABBR\]/", $regionName , $regions);
	}
	
	//apply template
	$template=preg_replace("/\[STATES_LIST\]/", $regions, $template);
	
	return $template;
}

function fillCitiesList($con, $stateNameLatin, $template)
{
	global $page_title,$page_meta_keywords,$page_meta_description, $state_name, $state_abbr, $state_city_count;
	
	$count = 0;
	
	$query_case_list = 	" SELECT c.city_name, city_name_latin, r.region_name, r.region_name_latin, r.abbr " .
						" FROM city c LEFT JOIN region r ON c.region_id = r.region_id " .
						" WHERE LOWER(r.region_name_latin) = LOWER(?) ORDER BY c.city_name ";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	if (!mysqli_stmt_bind_param($stmt, "s", $stateNameLatin)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $city_name, $city_name_latin, $region_name, $region_name_latin, $abbr)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$template = preg_replace("/\[STATE_NAME\]/", $region_name, $template);
		$template = preg_replace("/\[STATE_ABBR\]/", $abbr, $template);
	}else{
		return null;
	}
	
	$i = 0;
	do{
		$count++;
		if($i == 0){
			$cities = $cities. "<tr>";
		}elseif($i == 4){
			$cities = $cities. "</tr><tr>";
			$i = 0;
		}
		
		$cities = $cities."<td style=\"line-height:20px; font-size:15px;\" align=\"left\" valign=\"top\">•&nbsp; <a href=\"/"  .$region_name_latin . "/" . $city_name_latin ."\" title=\"".CITY_LIST_LINK_TITLE."\" >". $city_name ."</a><br></td>";
		$cities = preg_replace("/\[CITY_NAME\]/", $city_name , $cities);
		$cities = preg_replace("/\[STATE_NAME\]/", $region_name , $cities);
		$cities = preg_replace("/\[STATE_ABBR\]/", $abbr , $cities);
		
		$i = $i + 1;
	}while(mysqli_stmt_fetch($stmt));
	
	$state_city_count = $count;
	
	$cities = $cities. "</tr>";
	
	$template=preg_replace("/\[CITIES_LIST\]/", $cities, $template);
	
	$state_name = $region_name;
	$state_abbr = $abbr;

	mysqli_stmt_close($stmt);

	return $template;
}

function getMainPageInfo($con)
{
	$result_array = array();
	$query_case_list = 	" SELECT p.title, p.meta_keywords, p.meta_description " .
						" FROM pages p, door_keys k " .
						" WHERE 1 " .
						" AND p.key_id = k.id " .
						" AND k.key_value= ? ";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	$main_key = "/";
	if (!mysqli_stmt_bind_param($stmt, "s", $main_key)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
	#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $title, $meta_keywords, $meta_description)){
	#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(
				"title"=>$title,
				"meta_keywords"=>$meta_keywords,
				"meta_description"=>$meta_description,
		);
	}else{
			#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}

	mysqli_stmt_close($stmt);

	return $result_array;
}

//заводим массивы ключей и городов
$CITY_NEWS_PER_PAGE=10;
$current_page="MAIN_PAGE";

//определяем имя домена и сабдомена и записываем номер ключа и номер города
#$url = $_SERVER["HTTP_HOST"];
#echo "HTTP_HOST: ".$url.'<br>';
//preg_match("/[a-z0-9]*\.[a-z0-9]*$/",$url,$url1);
//preg_match("/[0-9]+-[0-9]+/",$url,$match);
//list($keys_num, $city_num) = split('-', $match[0]);

$url = $_SERVER["REQUEST_URI"];
#$url = "/altayskiy-kray/";
#echo "REQUEST_URI: ".$url.'<br>';
preg_match("/[\-a-zA-Z0-9]+\/[\-a-zA-Z0-9]*/",$url,$request_uri);
#echo "request_uri".$request_uri[0].'<br>';
#echo $request_uri[0].'<br>';

$url_region = "";
$url_city = "";
if(count($request_uri)>=1){
	list($url_region,$url_city) = explode('/', $request_uri[0]);
}

#echo "url_region: ".$url_region.'<br>';
#echo "url_city: ".$url_city.'<br>';

#$url_region = "Alaska";
#$url_city = "Adak";

$template = file_get_contents("tmpl_main.html");

$template=preg_replace("/\[URL\]/",$_SERVER["HTTP_HOST"], $template);
$template=preg_replace("/\[URLMAIN\]/",$_SERVER["HTTP_HOST"], $template);
$template=preg_replace("/\[HEADER_KEYS\]/",HEADER_KEYS, $template);

//fetch regions
$con=mysqli_connect(DB_HOST,DB_USER_NAME,DB_USER_PWD,DB_NAME);
#echo "Connecting...";
if (mysqli_connect_errno())
{
	#echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

mysqli_query($con,"set character_set_client='utf8'");
mysqli_query($con,"set character_set_results='utf8'");
mysqli_query($con,"set collation_connection='utf8_general_ci'");

$page_title = "";
$page_meta_description = "";
$page_meta_keywords = "";
$page_meta_placename = "";
$page_meta_position = "";
$page_meta_region = "";
$page_meta_icbm = "";

$page_h1 = "";
$page_h2 = "";

$state_name = "";
$state_abbr = "";
$state_city_count = "";

$city_name = "";
$geo_placename = "";
$geo_position = "";
$icbm = "";
$geo_region = "";
$zip_code = "";
$country = "";
$clouds = "";

$page_title = MAIN_PAGE_TITLE;
$page_meta_description = MAIN_PAGE_META_DESCRIPTION;
$page_meta_keywords = MAIN_PAGE_META_KEYWORDS;

$page_h1 = MAIN_PAGE_H1;
$page_h2 = MAIN_PAGE_H2;

if($url_region == 'apply-now'){
	$current_page = "APPLY_NOW_PAGE";
	$tmpl_file_name="tmpl_apply_now.html";
	
	$page_title = APPLY_PAGE_TITLE;
	$page_meta_description = APPLY_META_DESCRIPTION;
	$page_meta_keywords = APPLY_META_KEYWORDS;
}
elseif($url_region == 'faq'){
	$current_page = "FAQ_PAGE";
	$tmpl_file_name="tmpl_faq.html";
	
	$page_title = FAQ_PAGE_TITLE;
	$page_meta_description = FAQ_META_DESCRIPTION;
	$page_meta_keywords = FAQ_META_KEYWORDS;	
}
elseif($url_region == 'policy'){
	$current_page = "POLICY_PAGE";
	$tmpl_file_name="tmpl_policy.html";
	
	$page_title = POLICY_PAGE_TITLE;
	$page_meta_description = POLICY_META_DESCRIPTION;
	$page_meta_keywords = POLICY_META_KEYWORDS;	
}
elseif($url_region == 'contact'){
	$current_page = "CONTACT_PAGE";
	$tmpl_file_name="tmpl_contact.html";
	
	$page_title = CONTACT_PAGE_TITLE;
	$page_meta_description = CONTACT_META_DESCRIPTION;
	$page_meta_keywords = CONTACT_META_KEYWORDS;	
}
elseif($url_city && $url_region){
	$current_page = "CITY_PAGE";
	$tmpl_file_name="tmpl_city.html";
} 
elseif(!$url_city && $url_region){
	$current_page = "REGION_PAGE";
	$tmpl_file_name="tmpl_region.html";
} 
elseif($url_region && $url_city){
	$current_page = "MAIN_PAGE";
}
elseif(($url_city == 'index.php' || $url_city == '') && !$url_region){
	$current_page = "MAIN_PAGE";
	$tmpl_file_name="tmpl_main_block.html";
}else{
	$current_page = "MAIN_PAGE";
	$tmpl_file_name="tmpl_main_block.html";
}


$tmpl_inner = file_get_contents($tmpl_file_name);
$template  = fillStateList($con, $template);

if($current_page == "REGION_PAGE"){
	
	$tmpl_inner = fillCitiesList($con, $url_region, $tmpl_inner);
	if($tmpl_inner == null){
		$tmpl_inner = file_get_contents("tmpl_main_block.html");
	}
	
	$page_title = STATE_PAGE_TITLE;
	$page_meta_description = STATE_META_DESCRIPTION;
	$page_meta_keywords = STATE_META_KEYWORDS;
	
	$page_h1 = STATE_PAGE_H1;
	$page_h2 = STATE_PAGE_H2;
}elseif($current_page == "CITY_PAGE"){
	//get city page info
	$tmpl_inner = fillCityInfo($con,$url_region, $url_city, $tmpl_inner);
	
	if($tmpl_inner == null){
		$tmpl_inner = file_get_contents("tmpl_main_block.html");
	}
	
	$page_title = CITY_PAGE_TITLE;
	$page_meta_description = CITY_META_DESCRIPTION;
	$page_meta_keywords = CITY_META_KEYWORDS;
	
	$page_h1 = CITY_PAGE_H1;
	$page_h2 = CITY_PAGE_H2;
}

$template=preg_replace("/\[MAIN_BLOCK\]/",$tmpl_inner, $template);


$key_info = array();

$template=preg_replace("/\[PAGE_H1\]/", $page_h1, $template);
$template=preg_replace("/\[PAGE_H2\]/", $page_h2, $template);

$template=preg_replace("/\[TITLE\]/", $page_title, $template);
$template=preg_replace("/\[DESCRIPTION\]/", $page_meta_description, $template);
$template=preg_replace("/\[KEYWORDS\]/", $page_meta_keywords, $template);

$template=preg_replace("/\[CITY_PLACENAME\]/", $page_meta_placename, $template);
$template=preg_replace("/\[CITY_PPOSITION\]/", $page_meta_position, $template);
$template=preg_replace("/\[CITY_REGION\]/", $page_meta_region, $template);
$template=preg_replace("/\[META_ICBM\]/", $page_meta_icbm, $template);

$template=preg_replace("/\[STATE_NAME\]/", $state_name, $template);
$template=preg_replace("/\[STATE_ABBR\]/", $state_abbr, $template);
$template=preg_replace("/\[CITY_NAME\]/", $city_name, $template);
$template=preg_replace("/\[GEO_PLACENAME\]/", $geo_placename, $template);
$template=preg_replace("/\[GEO_POSITION\]/", $geo_position, $template);
$template=preg_replace("/\[ICBM\]/", $icbm, $template);
$template=preg_replace("/\[GEO_REGION\]/", $geo_region, $template);
$template=preg_replace("/\[ZIP_CODE\]/", $zip_code, $template);
$template=preg_replace("/\[COUNTRY\]/", $country, $template);
$template=preg_replace("/\[CLOUDS\]/", $clouds, $template);
$template=preg_replace("/\[CITY_COUNT\]/", $state_city_count, $template);

$template=preg_replace("/\[SITE_NAME\]/", SITE_NAME, $template);

$template=preg_replace("/\[CANONICAL_LINK\]/", "http://" . $_SERVER["HTTP_HOST"] . $_SERVER["REQUEST_URI"], $template);

unset($city_cases, $region_cases, $page_meta_description, $page_title, $bread_crumbs, $region_name, $function, $snippet_extractor, $google_image, $title_generator, $extractd_news, $news_extractor, $url_for_cache);
mysqli_close($con);

echo $template;	
?>