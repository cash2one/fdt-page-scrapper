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

//default offset for moskow
function engdate($d, $format = 'jS \of F h:i:s A', $offset = -8)
{
    $d += 3600 * $offset;
    return date($format, $d);
}

function fillItemInfo($con, $url_region, $url_city, $template)
{
	global $state_name, $state_abbr, $page_title,$page_meta_keywords,$page_meta_description,$page_meta_placename,$page_meta_position;
	global $page_meta_region,$page_meta_icbm,$city_name,$geo_placename,$geo_position,$icbm,$geo_region,$zip_code,$country,$clouds;
	$result_array = array();
	$query_case_list = 	" SELECT c.item_name, item_name_latin, c.geo_placename, c.geo_position, c.geo_region, c.ICBM, c.zip_code, c.country, r.region_name, r.abbr " .
						" FROM item c LEFT JOIN region r ON c.region_id = r.region_id " .
						" WHERE LOWER(r.category_name_latin) = LOWER(?) AND LOWER(c.item_name_latin) = LOWER(?)  "; 
	
	//$query_case_list = "SELECT c.item_name, c.item_name_latin, ek.key_value, ek.key_value_latin, r.region_name, r.category_name_latin, unix_timestamp(cp.posted_time), r.region_id, cp.anchor_name FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND cp.city_page_key = ? AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id AND cp.posted_time <= now()";
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
		$item_name = $city_name_int;
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
		$page_meta_icbm = "<meta name=\"ICBM\" content=\"$ICBM\" />";
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
		$category_name_latin = $v["category_name_latin"];
		
		$citiesListSrt = $citiesListSrt." ".$v["cityName"]." $abbr,";
		
		$citiesListHtml = $citiesListHtml . "<li>•&nbsp;&nbsp;<a href=\"/$category_name_latin/$cityNameLatin/\" title=\"".CITY_CLOSE_LIST_LINK_TITLE."\">$cityName, $abbr</a></li>";
		
		$citiesListHtml = preg_replace("/\[item_name\]/", $cityName , $citiesListHtml);
		$citiesListHtml = preg_replace("/\[CATEGORY_ABBR\]/", $abbr , $citiesListHtml);
		$citiesListHtml = preg_replace("/\[ZIP_CODE\]/", $zipCode , $citiesListHtml);
	}
	
	$template = preg_replace("/\[CLOSE_CITIES\]/", $citiesListHtml , $template);
	
	$clouds = trim($citiesListSrt, ",");
	
	return $template;
}

function getClosestCitiesList($con, $url_region, $url_city)
{
	$result_array = array();
	$query_case_list = " SELECT c.item_name, c.item_name_latin, r.abbr, c.zip_code, r.category_name_latin " .
				" FROM  city c, neighbor_city nc, region r WHERE c.city_id = nc.neighbor_city_id AND r.region_id = c.region_id AND " . 
				" nc.city_id = ( " .
				" SELECT c.city_id " .
				" FROM city c, region r " .
				" WHERE c.region_id = r.region_id  AND LOWER(r.category_name_latin) LIKE LOWER(?) AND LOWER(c.item_name_latin) LIKE LOWER(?)) ";

	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
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
	if(!mysqli_stmt_bind_result($stmt, $cityName, $cityNameLatin, $abbr, $zip_code, $category_name_latin)){
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
									"category_name_latin" => $category_name_latin
		);
		
		$i++;
	}
	
	return $result_array;
}

function fillCategoryList($con, $template)
{
	global $page_title,$page_meta_keywords,$page_meta_description;
	
	$result = mysqli_query($con,"SELECT COUNT(1) city_count, r.category_name_latin, r.category_name, r.abbr FROM item c LEFT JOIN category r ON c.category_id = r.category_id GROUP BY r.category_id ORDER BY r.category_name");

	$categoryName;
	$categoryNameLatin;
	$categories = "";
	
	while($row = mysqli_fetch_array($result))
	{
		global $categoryName, $categoryNameLatin;
		$categoryName = $row['category_name'];
		$categoryNameLatin = $row['category_name_latin'];
		$categories = $categories."<li class=\"page_item\"><a href=\"/$categoryNameLatin/\" title=\"".CATEGORY_LINK_TITLE."\">$categoryName</a> (" . $row['city_count'] . " cities)</li>\r\n";
		$categories = preg_replace("/\[CATEGORY_NAME\]/", $categoryName , $categories);
		$categories = preg_replace("/\[CATEGORY_ABBR\]/", $categoryName , $categories);
	}
	
	//apply template
	$template=preg_replace("/\[STATES_LIST\]/", $categories, $template);
	
	return $template;
}

function fillArticleList($con, $template)
{
	global $page_title,$page_meta_keywords,$page_meta_description;
	
	$result = mysqli_query($con," SELECT ac.*, at.title, at.url, t1.posted_cnt, unix_timestamp(ac.post_dt) posted_time " .
								" FROM article_content ac, article_tmpl at," .
								" (SELECT ac.tmpl_id, MAX(ac.post_dt) max_post_dt, COUNT(1) posted_cnt " .
								" FROM article_content ac" .
								" WHERE ac.post_dt < now() GROUP BY ac.tmpl_id) AS t1" .
								" WHERE ac.tmpl_id = t1.tmpl_id AND at.tmpl_id = ac.tmpl_id  AND ac.post_dt = t1.max_post_dt ORDER BY ac.post_dt DESC"
							);

	$text;
	$title;
	$url;
	$post_dt;
	$upd_flg;
	
	$atricles = "";

	$cur_news_posted_time = "";
	while($row = mysqli_fetch_array($result))
	{
		global $categoryName, $categoryNameLatin;
		$text = $row['text'];
		$title = $row['title'];
		$url= $row['url'];
		$post_dt = $row['posted_time'];
		$upd_flg = $row['upd_flg'];
		$updTitle = "";
		
		if($cur_news_posted_time != engdate($post_dt,'jS \of F')){
			$cur_news_posted_time = engdate($post_dt,'jS \of F');
			$atricles = $atricles."<br/><h3>".$cur_news_posted_time."</h3>";
		}
		
		if($upd_flg == 1){
			 $updTitle = "Information updated | ";
		}
		
		$atricles = $atricles."<a href=\"/articles/$url/\">".$updTitle. " " . $title ." (".engdate($post_dt,'jS \of F, h:i:s A').")</a></br>\r\n";
		/*$atricles = preg_replace("/\[CATEGORY_NAME\]/", $categoryName , $atricles);
		$atricles = preg_replace("/\[CATEGORY_ABBR\]/", $categoryName , $atricles);*/
	}
	
	//apply template
	$template=preg_replace("/\[ATRICLES_LIST\]/", $atricles, $template);
	
	return $template;
}

function fillArticle($con, $url, $template)
{
	global $page_title,$page_meta_keywords,$page_meta_description;
	
	#echo "URL:" . $url . "; ";
	
	$result_array = array();
	$query_case_list = 	" SELECT ac.text, at.title FROM article_tmpl at LEFT JOIN article_content ac ON at.tmpl_id = ac.tmpl_id " .
						" WHERE LOWER(at.url) = LOWER(?) AND ac.post_dt < now() ORDER BY ac.post_dt DESC LIMIT 1";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	$main_key = "/";
	if (!mysqli_stmt_bind_param($stmt, "s", $url)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $text, $title)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	if(mysqli_stmt_fetch($stmt)) {
		$page_title = $title;
		$page_meta_keywords = $title;
		$page_meta_description = $title;

		#echo " " . $title . " - " .$text;
		
		$template=preg_replace("/\[ARTICLE_TITLE\]/", $title, $template);
		$template=preg_replace("/\[ARTICLE_BODY\]/", $text, $template);
	}else{
		#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
		mysqli_stmt_close($stmt);
		return null;
	}

	mysqli_stmt_close($stmt);

	return $template;
}

function fillCategoriesList($con, $categoryNameLatin, $template)
{
	global $page_title,$page_meta_keywords,$page_meta_description, $state_name, $state_abbr, $state_city_count;
	
	$count = 0;
	
	$query_case_list = 	" SELECT c.item_name, item_name_latin, r.category_name, r.category_name_latin, r.abbr " .
						" FROM item c LEFT JOIN category r ON c.category_id = r.category_id " .
						" WHERE LOWER(r.category_name_latin) = LOWER(?) ORDER BY c.item_name ";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	if (!mysqli_stmt_bind_param($stmt, "s", $categoryNameLatin)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $item_name, $item_name_latin, $category_name, $category_name_latin, $abbr)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$template = preg_replace("/\[CATEGORY_NAME\]/", $category_name, $template);
		$template = preg_replace("/\[CATEGORY_ABBR\]/", $abbr, $template);
	}else{
		return null;
	}
	
	$i = 0;
	do{
		$count++;
		if($i == 0){
			$cities = $cities. "<tr>";
		}elseif($i == 3){
			$cities = $cities. "</tr><tr>";
			$i = 0;
		}
		
		$cities = $cities."<td style=\"line-height:20px; font-size:15px;\" align=\"left\" valign=\"top\">•&nbsp; <a href=\"/"  .$category_name_latin . "/" . $item_name_latin ."\" title=\"".CITY_LIST_LINK_TITLE."\" >". $item_name ."</a><br></td>";
		$cities = preg_replace("/\[item_name\]/", $item_name , $cities);
		$cities = preg_replace("/\[CATEGORY_NAME\]/", $category_name , $cities);
		$cities = preg_replace("/\[CATEGORY_ABBR\]/", $abbr , $cities);
		
		$i = $i + 1;
	}while(mysqli_stmt_fetch($stmt));
	
	$state_city_count = $count;
	
	$cities = $cities. "</tr>";
	
	$template=preg_replace("/\[CITIES_LIST\]/", $cities, $template);
	
	$state_name = $category_name;
	$state_abbr = $abbr;

	mysqli_stmt_close($stmt);

	return $template;
}

function fillCategoryPageContent($con, $categoryNameLatin, $template)
{	
	global $page_title, $page_meta_description, $page_meta_keywords, $page_h1, $page_h2;

	$query_case_list = 	" SELECT r.category_name, r.category_name_latin, r.abbr, r.generated_text, r.title, r.meta_description, r. 	meta_keywords " .
						" FROM category r " .
						" WHERE LOWER(r.category_name_latin) = LOWER(?) ";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	if (!mysqli_stmt_bind_param($stmt, "s", $categoryNameLatin)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $category_name, $category_name_latin, $abbr, $generated_text, $title, $meta_description, $meta_keywords)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$template=preg_replace("/\[CATEGORY_GEN_TEXT\]/", $generated_text, $template);
		
		$page_title=preg_replace("/\[CATEGORY_PAGE_TITLE\]/", $title, $page_title);
		$page_meta_description=preg_replace("/\[CATEGORY_META_DESCRIPTION\]/", $meta_description, $page_meta_description);
		$page_meta_keywords=preg_replace("/\[CATEGORY_META_KEYWORDS\]/", $meta_keywords, $page_meta_keywords);
	}else{
		return null;
	}

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

function loadMapping($filePath){
	$lines = file($filePath, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
	
	$menu_mapping = array();
	
	foreach ($lines as $line_num => $line) {
		list($url,$data) = explode('=', $line, 2);
		list($file,$title,$meta_keywords,$meta_description)=explode('^', $data);
		#echo "УРЛ #<b>".$url."</b> : соответсвует файл " . $data . "<br />\n";
		$menu_mapping[$url] = array(
				"file"=>$file,
				"page_title"=>$title,
				"page_meta_keywords"=>$meta_keywords,
				"page_meta_description"=>$meta_description
		);
	}
	
	#var_dump($menu_mapping);
	return  $menu_mapping;
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
preg_match("/[\-a-zA-Z0-9_]+\/[\-a-zA-Z0-9_]*/",$url,$request_uri);
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
#$template=preg_replace("/\[HEADER_KEYS\]/",HEADER_KEYS, $template);

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

$item_name = "";
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

$menu_mapping = loadMapping("./menu_map/menu_map.ini");

#var_dump($menu_mapping);

if($menu_mapping[$url_region] || (!$url_region && $menu_mapping["/"])){
	
	if(!$url_region){
		$url_region = "/";
	}
	
	$tmpl_file_name=$menu_mapping[$url_region]["file"];
	
	$page_title = constant($menu_mapping[$url_region]["page_title"]);
	$page_meta_description = constant($menu_mapping[$url_region]["page_meta_description"]);
	$page_meta_keywords = constant($menu_mapping[$url_region]["page_meta_keywords"]);

	if($url_region == "/"){
		$url_region = "";
	}
}
elseif($url_region == 'articles' && $url_city){
	$current_page = "ARTICLE_PAGE";
	$tmpl_file_name="tmpl_article_page.html";	
}
elseif($url_region == 'articles' && !$url_city){
	$current_page = "ARTICLES_LIST_PAGE";
	$tmpl_file_name="tmpl_articles.html";	
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
$template  = fillCategoryList($con, $template);

if($current_page == "REGION_PAGE"){
	#echo "Region page";
	$page_title = CATEGORY_PAGE_TITLE;
	$page_meta_description = CATEGORY_META_DESCRIPTION;
	$page_meta_keywords = CATEGORY_META_KEYWORDS;
	
	$page_h1 = CATEGORY_PAGE_H1;
	$page_h2 = CATEGORY_PAGE_H2;
	
	$tmpl_inner = fillCategoriesList($con, $url_region, $tmpl_inner);
	$tmpl_inner = fillCategoryPageContent($con, $url_region, $tmpl_inner);
	
	if($tmpl_inner == null){
		$tmpl_inner = file_get_contents("tmpl_main_block.html");
	}
}
elseif($current_page == "CITY_PAGE"){
	//get city page info
	$tmpl_inner = fillItemInfo($con,$url_region, $url_city, $tmpl_inner);
	
	if($tmpl_inner == null){
		$tmpl_inner = file_get_contents("tmpl_main_block.html");
	}
	
	$page_title = CITY_PAGE_TITLE;
	$page_meta_description = CITY_META_DESCRIPTION;
	$page_meta_keywords = CITY_META_KEYWORDS;
	
	$page_h1 = CITY_PAGE_H1;
	$page_h2 = CITY_PAGE_H2;
}
elseif($current_page == "ARTICLES_LIST_PAGE"){
	//get city page info
	$tmpl_inner = fillArticleList($con, $tmpl_inner);
	
	if($tmpl_inner == null){
		$tmpl_inner = file_get_contents("tmpl_main_block.html");
	}
	
	$page_title = ARTICLES_LIST_PAGE_TITLE;
	$page_meta_description = ARTICLES_LIST_META_DESCRIPTION;
	$page_meta_keywords = ARTICLES_LIST_META_KEYWORDS;
}
elseif($current_page == "ARTICLE_PAGE"){
	//get city page info
	$tmpl_inner = fillArticle($con, $url_city, $tmpl_inner);
	
	if($tmpl_inner == null){
		$tmpl_inner = file_get_contents("tmpl_main_block.html");
	}
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

$template=preg_replace("/\[CATEGORY_NAME\]/", $state_name, $template);
$template=preg_replace("/\[CATEGORY_ABBR\]/", $state_abbr, $template);
$template=preg_replace("/\[item_name\]/", $item_name, $template);
$template=preg_replace("/\[GEO_PLACENAME\]/", $geo_placename, $template);
$template=preg_replace("/\[GEO_POSITION\]/", $geo_position, $template);
$template=preg_replace("/\[ICBM\]/", $icbm, $template);
$template=preg_replace("/\[GEO_REGION\]/", $geo_region, $template);
$template=preg_replace("/\[ZIP_CODE\]/", $zip_code, $template);
$template=preg_replace("/\[COUNTRY\]/", $country, $template);
$template=preg_replace("/\[CLOUDS\]/", $clouds, $template);
$template=preg_replace("/\[CITY_COUNT\]/", $state_city_count, $template);

$template=preg_replace("/\[CANONICAL_LINK\]/", "http://" . $_SERVER["HTTP_HOST"] . $_SERVER["REQUEST_URI"], $template);

$template=preg_replace("/\[SITE_NAME\]/", SITE_NAME, $template);

unset($city_cases, $region_cases, $page_meta_description, $page_title, $bread_crumbs, $category_name, $function, $snippet_extractor, $google_image, $title_generator, $extractd_news, $news_extractor, $url_for_cache);
mysqli_close($con);

echo $template;	
?>