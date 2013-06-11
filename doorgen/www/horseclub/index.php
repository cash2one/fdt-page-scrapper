<?php
error_reporting(E_ALL ^ E_NOTICE);

require_once "utils/pager.php";
require_once "application/models/functions_decode.php";
require_once "application/libraries/parser.php";
require_once "application/plugins/snippets/Google.php";
require_once "application/plugins/snippets/Ukr.php";
require_once "application/plugins/images/ImagesGoogle.php"; 
require_once "utils/title_generator.php";
require_once "utils/case_value_selector.php";
require_once "utils/ya_news_extractor.php";
require_once "utils/config.php";

$page_title="";
$page_meta_keywords="";
$page_meta_description="";
$is_cached = false;

$function = new Functions;
$snippet_extractor = new Ukr;
$google_image = new ImagesGoogle;
$title_generator = new TitleGenerator;

function rusdate($d, $format = 'j %MONTH% Y', $offset = 0)
{
    $montharr = array('января', 'февраля', 'марта', 'апреля', 'мая', 'июня', 'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря');
    $dayarr = array('понедельник', 'вторник', 'среда', 'четверг', 'пятница', 'суббота', 'воскресенье');
 
    $d += 3600 * $offset;
 
    $sarr = array('/%MONTH%/i', '/%DAYWEEK%/i');
    $rarr = array( $montharr[date("m", $d) - 1], $dayarr[date("N", $d) - 1] );
 
    $format = preg_replace($sarr, $rarr, $format); 
    return date($format, $d);
}

//фунцкия генерации урлов
function urlgenerator($fkeys, $fcity, $fdomain) 
{
	for ($i=0;$i<count($fkeys);$i++)
	{for ($j=0;$j<count($fcity);$j++)
	{
		$res=encodestring(trim($fkeys[$i])."-".trim($fcity[$j])."-$i-$j");
		$res="http://".str_replace(" ","-",$res).".$fdomain";
		#echo "&lta href=\"$res\"&gt".trim($fkeys[$i])." ".trim($fcity[$j])."&lt/a&gt<br>";
	}}
	
	
}

  // функция превода текста с кириллицы в траскрипт
function encodestring($str) 
{
    $tr = array(
        "А"=>"A","Б"=>"B","В"=>"V","Г"=>"G",
        "Д"=>"D","Е"=>"E","Ж"=>"J","З"=>"Z","И"=>"I",
        "Й"=>"Y","К"=>"K","Л"=>"L","М"=>"M","Н"=>"N",
        "О"=>"O","П"=>"P","Р"=>"R","С"=>"S","Т"=>"T",
        "У"=>"U","Ф"=>"F","Х"=>"H","Ц"=>"TS","Ч"=>"CH",
        "Ш"=>"SH","Щ"=>"SCH","Ъ"=>"","Ы"=>"YI","Ь"=>"",
        "Э"=>"E","Ю"=>"YU","Я"=>"YA","а"=>"a","б"=>"b",
        "в"=>"v","г"=>"g","д"=>"d","е"=>"e","ж"=>"j",
        "з"=>"z","и"=>"i","й"=>"y","к"=>"k","л"=>"l",
        "м"=>"m","н"=>"n","о"=>"o","п"=>"p","р"=>"r",
        "с"=>"s","т"=>"t","у"=>"u","ф"=>"f","х"=>"h",
        "ц"=>"ts","ч"=>"ch","ш"=>"sh","щ"=>"sch","ъ"=>"y",
        "ы"=>"yi","ь"=>"","э"=>"e","ю"=>"yu","я"=>"ya"
    );
    return strtr($str,$tr);
}

function getKeyInfo($con,$city_page_key)
{
	$result_array = array();
	$query_case_list = "SELECT c.city_name, c.city_name_latin, ek.key_value, ek.key_value_latin, r.region_name, r.region_name_latin, unix_timestamp(cp.posted_time), r.region_id FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND cp.city_page_key = ? AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id AND cp.posted_time <= now()";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $city_page_key)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $city_name,$city_name_latin,$key_value, $key_value_latin, $region_name, $region_name_latin, $posted_time, $region_id)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	"city_name"=>$city_name,
						"city_name_latin"=>$city_name_latin,
						"key_value"=>$key_value, 
						"key_value_latin"=>$key_value_latin, 
						"region_name"=>$region_name, 
						"region_name_latin"=>$region_name_latin, 
						"posted_time"=>$posted_time, 
						"region_id"=>$region_id
					);	
	}else{
		#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
	
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function getPageInfo($con,$page_url)
{
	$result_array = array();
	$query_case_list = "SELECT cp.cached_page_id, cp.cached_page_title, cp.cached_page_meta_keywords, cp.cached_page_meta_description, cp.cached_time FROM `cached_page` cp WHERE 1 AND cp.cached_page_url = ?";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $page_url)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $cached_page_id, $cached_page_title, $cached_page_meta_keywords, $cached_page_meta_description, $cached_time)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	
						"cached_page_id"=>$cached_page_id,
						"cached_page_title"=>$cached_page_title,
						"cached_page_meta_keywords"=>$cached_page_meta_keywords,
						"cached_page_meta_description"=>$cached_page_meta_description, 
						"cached_time"=>$cached_time
					);	
	}else{
		#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
		
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function savePageInfo($conn,$page_url, $title, $keywords, $description)
{
	#echo "Saving page info..<br/>";
	$query_case_list = "INSERT INTO `cached_page` (cached_page_url, cached_page_title, cached_page_meta_keywords, cached_page_meta_description, cached_time) VALUES (?,?,?,?,now())";
	if (!($stmt = mysqli_prepare($conn,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "ssss", $page_url,$title,$keywords,$description)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Saving failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	mysqli_stmt_close($stmt);
	
	$conn->commit();
	
	unset($query_case_list);
}

function fillSnippetsContent($template, $key_value, $conn, $page_url){

	global $function, $google_image, $snippet_extractor;
	$snippets_array = getPageSnippets($conn,$page_url);
	
	if(!$snippets_array){
		#echo "Saving snippets.";
		$rand_index_array = array();
		$index = 0;
		while(count($rand_index_array) < 3){
			$rand_value = rand(0,8);
			if(!in_array($rand_value,$rand_index_array)){
				$rand_index_array[$index] = $rand_value;
				$index++;
			}
		}
		
		$snippet_image_array = $google_image->Start($key_value,count($rand_index_array),$function);
		$snippet_array = array();
		while(!$snippet_array){
			$snippet_array = $snippet_extractor->Start($key_value,'ru',count($rand_index_array),$function);
		}
		
		for($i=0; $i < count($rand_index_array); $i++){
			$snippets_array[$rand_index_array[$i]]['title'] = preg_replace('/ {0,}\.{2,}/','.',$snippet_array[$i]['title']);
			$snippets_array[$rand_index_array[$i]]['description'] = preg_replace('/ {0,}\.{2,}/','.',$snippet_array[$i]['description']);
			if($snippet_image_array && $snippet_image_array[$i]){
				$snippets_array[$rand_index_array[$i]]['small'] = $snippet_image_array[$i]['small'];
				$snippets_array[$rand_index_array[$i]]['large'] = $snippet_image_array[$i]['large'];
			}
		}
		
		savePageSnippets($conn, $page_url, $snippets_array);
	}
	
	$SNIPPET_BLOCK_1 = "<div class='wrap border-bot-1'><img src='[SNIPPET_IMG_SMALL_[INDEX]]' alt=''><p class='text-1 top-2 p3'><h2>[SNIPPET_TITLE_[INDEX]]</h2></p><p>[SNIPPET_CONTENT_[INDEX]]</p><br/></div>";
	$start_block_index = 1;
	for($i=1; $i <=3; $i++){
		if(isset($snippets_array[$i-1])){
			$template=preg_replace("/\[SNIPPET_BLOCK_1_".$start_block_index."\]/", preg_replace("/\[INDEX\]/", $i, $SNIPPET_BLOCK_1), $template);
			$start_block_index++;
		}
	}
	
	$SNIPPET_BLOCK_2 = "<div class='wrap'><div class='number'>[NUMBER]</div><p class='extra-wrap border-bot-1'><span class='clr-1'><h2>[SNIPPET_TITLE_[INDEX]]</h2></span><br>[SNIPPET_CONTENT_[INDEX]]</p></div>";
	$start_block_index = 1;
	for($i=4; $i <=6; $i++){
		if(isset($snippets_array[$i-1])){
			$template=preg_replace("/\[SNIPPET_BLOCK_2_".$start_block_index."\]/", preg_replace("/\[INDEX\]/", $i, preg_replace("/\[NUMBER\]/", $start_block_index, $SNIPPET_BLOCK_2)), $template);
			$start_block_index++;
		}
	}
	
	$SNIPPET_BLOCK_3 = "<div class='wrap border-bot-1'><img src='[SNIPPET_IMG_SMALL_[INDEX]]' alt='' class='img-indent'><p class='extra-wrap'><span class='clr-1'><h2>[SNIPPET_TITLE_[INDEX]]</h2></span><br>[SNIPPET_CONTENT_[INDEX]]</p></div>";
	$start_block_index = 1;
	for($i=7; $i <=9; $i++){
		if(isset($snippets_array[$i-1])){
			$template=preg_replace("/\[SNIPPET_BLOCK_3_".$start_block_index."\]/", preg_replace("/\[INDEX\]/", $i, $SNIPPET_BLOCK_3), $template);
			$start_block_index++;
		}
	}
	
	//clear empty blocks
	for($i=1; $i <=3; $i++){
		for($j=1; $j <=3; $j++){
			$template=preg_replace("/\[SNIPPET_BLOCK_".$i."_".$j."\]/", "", $template);
		}
	}
	
	for($i=0; $i < 9; $i++){
		if(isset($snippets_array[$i])){
			$template=preg_replace("/\[SNIPPET_TITLE_".($i+1)."\]/", $snippets_array[$i]["title"], $template);
			$template=preg_replace("/\[SNIPPET_CONTENT_".($i+1)."\]/", $snippets_array[$i]["description"], $template);
			$template=preg_replace("/\[SNIPPET_IMG_LARGE_".($i+1)."\]/", isset($snippets_array[$i]["large"])?$snippets_array[$i]["large"]:"", $template);
			$template=preg_replace("/\[SNIPPET_IMG_SMALL_".($i+1)."\]/", isset($snippets_array[$i]["small"])?$snippets_array[$i]["small"]:"", $template);
		}else{
			$template=preg_replace("/\[SNIPPET_TITLE_".($i+1)."\]/", "", $template);
			$template=preg_replace("/\[SNIPPET_CONTENT_".($i+1)."\]/", "", $template);
			$template=preg_replace("/\[SNIPPET_IMG_LARGE_".($i+1)."\]/", "", $template);
			$template=preg_replace("/\[SNIPPET_IMG_SMALL_".($i+1)."\]/", "", $template);
		}
	}
	
	unset($snippets_array);
	
	return $template;
}

function savePageSnippets($conn, $page_url, $snippets_array)
{	
	for($i = 0; $i < 9; $i++){
		if(isset($snippets_array[$i])){
			$query_case_list = "INSERT INTO `snippets` (cached_page_id, snippets_index, snippets_title, snippets_content, snippets_image_large, snippets_image_small, created_time) SELECT cp.cached_page_id,?,?,?,?,?,now() FROM cached_page cp WHERE cp.cached_page_url = ?";
			if (!($stmt = mysqli_prepare($conn,$query_case_list))) {
				#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
			}
			//set values
			#echo "set value...";
			if (!mysqli_stmt_bind_param($stmt, "dsssss", $i, $snippets_array[$i]["title"],$snippets_array[$i]["description"],$snippets_array[$i]["large"],$snippets_array[$i]["small"], $page_url)) {
				#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
				print_r(error_get_last());
			}
			
			#echo "execute...";
			if (!mysqli_stmt_execute($stmt)){
				#echo "Saving failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
				print_r(error_get_last());
			}

			mysqli_stmt_close($stmt);
			
			$conn->commit();
		}
	}
}

function getPageSnippets($conn,$page_url)
{
	$snippets_array = array();
	$query_case_list = "select snp.snippets_index, snp.cached_page_id, snp.snippets_title, snp.snippets_content, snp.snippets_image_large, snp.snippets_image_small from snippets snp where snp.cached_page_id IN (select cp.cached_page_id from cached_page cp where cp.cached_page_url = ?)";
	if (!($stmt = mysqli_prepare($conn,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	if (!mysqli_stmt_bind_param($stmt, "s", $page_url)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $snippets_index, $cached_page_id, $snippets_title, $snippets_content, $snippets_image_large, $snippets_image_small)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	while(mysqli_stmt_fetch($stmt)) {
		$snippets_array[$snippets_index] = array(	
						"cached_page_id"=>$cached_page_id,
						"title"=>$snippets_title,
						"description"=>$snippets_content, 
						"large"=>$snippets_image_large,
						"small"=>$snippets_image_small
					);	
	}
		
	mysqli_stmt_close($stmt);
	
	return $snippets_array;
}

function getRegionPageRandomTitle($region_id){
	$titles_array = array("Регионы и Округи, Области, Банки России, Кредиты в России","Кредиты в России, Области, Банки России, Регионы и Округи","Регионы и Округи, Банки России, Области, Кредиты в России","Кредиты в России, Регионы и Округи, Области, Банки России","Области, Регионы и Округи, Банки России, Кредиты в России","Кредиты в России, Области, Регионы и Округи, Банки России","Банки России, Регионы и Округи, Области, Кредиты в России","Области, Кредиты в России, Регионы и Округи, Банки России","Области, Кредиты в России, Банки России, Регионы и Округи","Банки России, Регионы и Округи, Кредиты в России, Области","Регионы и Округи, Области, Кредиты в России, Банки России","Кредиты в России, Банки России, Области, Регионы и Округи","Регионы и Округи, Банки России, Кредиты в России, Области","Области, Регионы и Округи, Кредиты в России, Банки России","Кредиты в России, Банки России, Регионы и Округи, Области","Области, Банки России, Кредиты в России, Регионы и Округи","Области, Банки России, Регионы и Округи, Кредиты в России","Банки России, Кредиты в России, Области, Регионы и Округи","Банки России, Области, Регионы и Округи, Кредиты в России","Банки России, Кредиты в России, Регионы и Округи, Области","Банки России, Области, Кредиты в России, Регионы и Округи","Кредиты в России, Регионы и Округи, Банки России, Области","Регионы и Округи, Кредиты в России, Области, Банки России","Регионы и Округи, Кредиты в России, Банки России, Области");
	$title_id = $region_id % count($titles_array);
	return $titles_array[$title_id];
}

//заводим массивы ключей и городов
$CITY_NEWS_PER_PAGE=10;
$city_news_page_number=1;
$current_page="MAIN_PAGE";

//определяем имя домена и сабдомена и записываем номер ключа и номер города
#$url = $_SERVER["HTTP_HOST"];
#echo "HTTP_HOST: ".$url.'<br>';
//preg_match("/[a-z0-9]*\.[a-z0-9]*$/",$url,$url1);
//preg_match("/[0-9]+-[0-9]+/",$url,$match);
//list($keys_num, $city_num) = split('-', $match[0]);

$url = $_SERVER["REQUEST_URI"];
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
#echo "url_city".$url_city.'<br>';

//обрабатываем запрос генерации урлов
$url_for_cache = "";

if( $url_city && is_numeric($url_city) && $url_region){
	$template = file_get_contents("tmpl_region_new.html");
	$city_news_page_number = $url_city;
	$current_page = "REGION_PAGE_PAGING";
	$url_for_cache = "/".$url_region."/";
	#echo "REGION_PAGE_PAGING";
} elseif ($url_city && $url_region){
	#echo "url_city: ".$url_city."<br/>";
	#echo "url_region: ".$url_region."<br/>";
	$template = file_get_contents("tmpl_key.html");
	$current_page = "CITY_PAGE";
	$url_for_cache = $url;
	#echo "CITY_PAGE";
} elseif(!$url_city && $url_region){
	$template = file_get_contents("tmpl_region_new.html");
	$current_page = "REGION_PAGE";
	$city_news_page_number = 1;
	$url_for_cache = $url;
	#echo "REGION_PAGE";
} elseif(($url_city == 'index.php' || $url_city == '') && !$url_region){
	//TODO Обработка региона
	$template=file_get_contents("tmpl_main_new.html");
	$current_page = "MAIN_PAGE";
	$url_for_cache = $url;
	#echo "MAIN_PAGE";
}

$template=preg_replace("/\[RANDKEY\]/e", 'trim($keys[rand(0,$max_k)])', $template);
$template=preg_replace("/\[RANDCITY\]/e", 'trim($city[rand(0,$max_c)])', $template);
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


//get page info
$page_info = getPageInfo($con,$url_for_cache);
if($page_info){
	$is_cached = true;
	$page_title = $page_info['cached_page_title'];
	$page_meta_keywords = $page_info['cached_page_meta_keywords'];
	$page_meta_description = $page_info['cached_page_meta_description'];
	#echo "Page $url is CACHED."."<br/>";
}else{
	#echo "Page $url is NOT CACHED."."<br/>";
	$is_cached = false;
}

//mysql_query("set character_set_client='utf8'");
//mysql_query("set character_set_results='utf8'");
//mysql_query("set collation_connection='utf8_general_ci'");
#echo "Processing page: ".$current_page."<br>";



if($current_page == "MAIN_PAGE"){
	#echo "Main page processing...";
	if(!$is_cached){
		$page_title = $title_generator->getRegionRandomTitle();
	}
	$result = mysqli_query($con,"SELECT COUNT(*) as row_count FROM doorgen_banks.region");
	$row = mysqli_fetch_assoc($result);
	$row_count = $row['row_count'];

	$reg_section_count = 4;
	$reg_per_section = ($row_count - $row_count % $reg_section_count) / $reg_section_count;

	$result = mysqli_query($con,"SELECT region_name, region_name_latin, region_id FROM doorgen_banks.region");

	$regions = "";
	$posted = 0;
	$page = 1;
	$firstRegNmChr = "";
	while($row = mysqli_fetch_array($result))
	{
		//print result to page
		/*if($posted != 0 && ($posted%$reg_per_section == 0)){
			$template=preg_replace("/\[REGIONS_".$page."\]/", $regions, $template);
			$regions = "";
			$page = $page+1;
		}
		//fill result with region list
		$posted = $posted + 1;
		$regions = $regions."<a href = \"/".str_replace(" ","-",$row['region_name_latin'])."/\">".$row['region_name']."</a>&nbsp;";*/
		//print result to page
		$curentFrstChr = mb_substr($row['region_name'],0,1, 'UTF-8');
		if($firstRegNmChr != $curentFrstChr){
			$firstRegNmChr = $curentFrstChr;
			$regions = $regions."<br><h3>$firstRegNmChr</h3>";
		}
		//fill result with region list
		$posted = $posted + 1;
		$regions = $regions."<a href = \"/".str_replace(" ","-",$row['region_name_latin'])."/\">".$row['region_name']."</a>&nbsp;<br/>";
	}
	//apply template
	$template=preg_replace("/\[REGIONS_1\]/", $regions, $template);
	
	$bread_crumbs = "<a href =\"#\">Главная</a>";
}

$title_template = "Кредиты в России, Банки России, Области, Регионы и Округи";

if($current_page == "REGION_PAGE" || $current_page == "REGION_PAGE_PAGING"){
	//get region names
	$result = mysqli_query($con,"SELECT region_name, region_id FROM doorgen_banks.region r where r.region_name_latin like replace(LOWER('".$url_region."'),'-','_')");
	$row = mysqli_fetch_assoc($result);
	$region_name = $row['region_name'];
	
	//generate header menu
	$caseSelector = new CaseValueSelector;
	$region_cases = array();
	$region_cases = $caseSelector->getCaseTitle($con,2,$url_region);
	
	if(!$is_cached){
		$page_title = $region_name." - ".getRegionPageRandomTitle($row['region_id']);
	}
	#echo "region_name: " . $region_name . "<br>";
	//getting city news count
	$query_count = "SELECT count(*) as row_count FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND r.region_name_latin like replace(LOWER('".$url_region."'),'-','_') AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id AND cp.posted_time < now()";
	$result = mysqli_query($con,$query_count);
	
	$row = mysqli_fetch_assoc($result);
	$city_news_count = $row['row_count'];
	#echo "city_news_count: " . $city_news_count . "<br>";
	
	if($city_news_count>0){
		//вычисляем последнюю страницы
		$max_page_number = floor($city_news_count/$CITY_NEWS_PER_PAGE);
		if($city_news_count%$CITY_NEWS_PER_PAGE != 0){
			$max_page_number = $max_page_number + 1;
		}
		
		if($city_news_page_number > $max_page_number){
			$city_news_page_number = $max_page_number;
		}
		
		#echo "max_page_number: ".$max_page_number."<br>";
		#echo "final city_news_page_number: ".$city_news_page_number."<br>";
		
		$start_position = $CITY_NEWS_PER_PAGE*($city_news_page_number-1);
		#echo "start_position: ".$start_position."<br>";

		#echo "Region page processing...";
		//prepare statement
		$query_city_list = "SELECT cp.anchor_name, c.city_name, c.city_name_latin, ek.key_value, ek.key_value_latin, r.region_name, r.region_name_latin, unix_timestamp(cp.posted_time), r.region_id FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND r.region_name_latin like replace(LOWER(?),'-','_') AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id AND cp.posted_time < now() ORDER BY cp.posted_time DESC LIMIT ".$start_position.",".$CITY_NEWS_PER_PAGE;
		#echo "query_city_list: ".$query_city_list."<br>";
		if (!($stmt = mysqli_prepare($con,$query_city_list))) {
			#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		//set values
		#echo "set value...";
		$id=1;
		if (!mysqli_stmt_bind_param($stmt, "s", $url_region)) {
			#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		#echo "execute...";
		if (!mysqli_stmt_execute($stmt)){
			#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}

		/* instead of bind_result: */
		#echo "get result...";
		if(!mysqli_stmt_bind_result($stmt, $anchor_name, $city_name, $city_name_latin, $key_value, $key_value_latin, $region_name, $region_name_latin, $posted_time, $region_id)){
			#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		$news_block = "";
		$cur_news_posted_time = "";
		while (mysqli_stmt_fetch($stmt)) {
			if($cur_news_posted_time != rusdate($posted_time,'j %MONTH% Y')){
				$cur_news_posted_time = rusdate($posted_time,'j %MONTH% Y');
				$news_block = $news_block."<br/><h3>".$cur_news_posted_time."</h3>";
			}
			// use your $myrow array as you would with any other fetch
			#echo "City name: ".$city_name."; key: ".$key_value;
			
			//generate link name
			
			
			$city_href = "<a href = \"/".str_replace(" ","-",$region_name_latin)."/".str_replace(" ","-",$city_name_latin." ".$key_value_latin).".html\">".$anchor_name." (".rusdate($posted_time,'j %MONTH% Y, G:i').")</a><br/>";
			$news_block = $news_block.$city_href;
		}
		$template=preg_replace("/\[CITY_NEWS_1\]/", $news_block, $template);

		$pager = new Pager;
		$template=preg_replace("/\[PAGER\]/", $pager->getPageNavigation("/".str_replace(" ","-",$region_name_latin)."/",$city_news_page_number, $max_page_number), $template);
		
		/* explicit close recommended */
		mysqli_stmt_close($stmt);
	}else{
		$template=preg_replace("/\[CITY_NEWS_1\]/", "Новостей по данному региону не найдео", $template);
		$template=preg_replace("/\[PAGER\]/","", $template);
	}
	//fill [BREAD_CRUMBS]
	$bread_crumbs = "<a href =\"/\">Главная</a>&nbsp;>&nbsp;<a href =\"#\">".$region_name."</a>&nbsp;";
	
	//
	
}

$key_info = array();
if($current_page == "CITY_PAGE"){
	//get city page info
	$key_info = getKeyInfo($con,$url_city);
	
	#echo "url_city: ".$url_city."<br/>";
	
	/*"city_name"=>$city_name,
						"city_name_latin"=>$city_name_latin,
						"key_value"=>$key_value, 
						"key_value_latin"=>$key_value_latin, 
						"region_name"=>$region_name, 
						"region_name_latin"=>$region_name_latin, 
						"posted_time"=>$posted_time*/
						
	#echo var_dump($key_info);

	//if page exist
	if($key_info){
		$region_name = $key_info['region_name'];
		
		//generate header menu
		$caseSelector = new CaseValueSelector;
		$city_cases = array();
		$city_cases = $caseSelector->getCaseTitle($con,1,$caseSelector->getCityValueByNewsKey($con,$url_city));
		
		if(!$is_cached){
			$page_title = $title_generator->getCityRandomTitle(getRegionPageRandomTitle($key_info['region_id']));
		}
		//fill [BREAD_CRUMBS]
		$bread_crumbs = "<a href =\"/\">Главная</a>&nbsp;>&nbsp;<a href =\"/".$url_region."/\">".$region_name."</a>&nbsp;>&nbsp;<a href =\"#\">".$key_info['city_name']." ".$key_info['key_value']."</a>";
	}else{
		#PAGE NOT FOUND REDIRECT
		header('HTTP/1.1 404 Not Found');
		#header('Location: /');
		$_GET['e'] = 404;
		include ($_SERVER['DOCUMENT_ROOT'] . '/404.html');
		exit;
	}
}

for($i=1; $i <= 9; $i++){
	$page_title=preg_replace("/\[REGION_CASE_".$i."\]/", isset($region_cases["$i"])?$region_cases["$i"]:"", $page_title);
	$page_title=preg_replace("/\[CITY_CASE_".$i."\]/", isset($city_cases["$i"])?$city_cases["$i"]:"", $page_title);
}

$page_title=preg_replace("/\[REGION_NAME\]/", $region_name, $page_title);

if(!$is_cached){
	$iterator_i = 0;	
	while(!$page_meta_description){
		#echo "Page_title: ".$page_title."<br/>";
		#echo "Iterator index: ".$iterator_i."<br/>";
		$iterator_i++;
		$snippet_array = $snippet_extractor->Start(preg_replace('/\|/',' ',$page_title),'ru',1,$function);
		$page_meta_description = preg_replace('/ {0,}\.{2,}/','.',$snippet_array[0]["description"]);
	}
	$page_title = $page_title." | ".$_SERVER['HTTP_HOST'];
	savePageInfo($con,$url_for_cache, $page_title, $page_title, $page_meta_description);
}

if($current_page == "CITY_PAGE"){
	$template = fillSnippetsContent($template,$key_info['city_name']." ".$key_info['key_value'],$con, $url);

	//delete all unnecessary templates anchors
	for($i=0; $i < 9; $i++){
		$template=preg_replace("/\[SNIPPET_TITLE_".($i+1)."\]/", '', $template);
		$template=preg_replace("/\[SNIPPET_CONTENT_".($i+1)."\]/", '', $template);
		$template=preg_replace("/\[SNIPPET_IMG_LARGE_".($i+1)."\]/", '', $template);
		$template=preg_replace("/\[SNIPPET_IMG_SMALL_".($i+1)."\]/", '', $template);
	}
	
	unset($key_info);
}

$template=preg_replace("/\[BREAD_CRUMBS\]/", $bread_crumbs, $template);

$template=preg_replace("/\[REGION_NAME\]/", $region_name, $template);
$template=preg_replace("/\[TITLE\]/", $page_title, $template);
$template=preg_replace("/\[DESCRIPTION\]/", $page_meta_description, $template);

for($i=1; $i <= 9; $i++){
	$template=preg_replace("/\[REGION_CASE_".$i."\]/", isset($region_cases["$i"])?$region_cases["$i"]:"", $template);
	$template=preg_replace("/\[CITY_CASE_".$i."\]/", isset($city_cases["$i"])?$city_cases["$i"]:"", $template);
}

//print last news
if($current_page == "REGION_PAGE" || $current_page == "REGION_PAGE_PAGING" || $current_page == "MAIN_PAGE"){
	$news_content_folder = './news_content/';
	$news_extractor = new YaNewsExtractor;
	$news_file_name = $news_extractor->isNewsUpdateNeed($news_content_folder);
	#echo "news_file_name: ".$news_file_name;
	if($news_file_name == ""){
		$extractd_news = $news_extractor->getYandexNewsContent($function);
		$news_extractor->saveNewsFile($news_content_folder, $extractd_news);
	}else{
		$extractd_news = file_get_contents($news_file_name);
	}
	$template=preg_replace("/\[LAST_NEWS\]/", $extractd_news, $template);
}

unset($city_cases, $region_cases, $page_meta_description, $page_title, $bread_crumbs, $region_name, $function, $snippet_extractor, $google_image, $title_generator, $extractd_news, $news_extractor, $url_for_cache);
mysqli_close($con);

echo $template;	
?>