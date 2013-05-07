<?php
error_reporting(E_ALL ^ E_NOTICE);

require_once "pager.php";
require_once "application/models/functions_decode.php";
require_once "application/libraries/parser.php";
require_once "application/plugins/snippets/Google.php";
require_once "utils/title_generator.php";
require_once "utils/case_value_selector.php";

$page_title="";
$page_meta_keywords="";
$page_meta_description="";
$is_cached = false;

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
		echo "&lta href=\"$res\"&gt".trim($fkeys[$i])." ".trim($fcity[$j])."&lt/a&gt<br>";
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
	$query_case_list = "SELECT c.city_name, c.city_name_latin, ek.key_value, ek.key_value_latin, r.region_name, r.region_name_latin, unix_timestamp(cp.posted_time) FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND cp.city_page_key = ? AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $city_page_key)) {
		echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $city_name,$city_name_latin,$key_value, $key_value_latin, $region_name, $region_name_latin, $posted_time)){
		echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	"city_name"=>$city_name,
						"city_name_latin"=>$city_name_latin,
						"key_value"=>$key_value, 
						"key_value_latin"=>$key_value_latin, 
						"region_name"=>$region_name, 
						"region_name_latin"=>$region_name_latin, 
						"posted_time"=>$posted_time
					);	
	}else{
		echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
	
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function getPageInfo($con,$page_url)
{
	$query_case_list = "SELECT cp.cached_page_title, cp.cached_page_meta_keywords, cp.cached_page_meta_description, cp.cached_time FROM `cached_page` cp WHERE 1 AND cp.cached_page_url = ?";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $page_url)) {
		echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $cached_page_title, $cached_page_meta_keywords, $cached_page_meta_description, $cached_time)){
		echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	"cached_page_title"=>$cached_page_title,
						"cached_page_meta_keywords"=>$cached_page_meta_keywords,
						"cached_page_meta_description"=>$cached_page_meta_description, 
						"cached_time"=>$cached_time
					);	
	}else{
		echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
		
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function savePageInfo($con,$page_url, $title, $keywords, $description)
{
	$query_case_list = "INSERT INTO `cached_page` (cached_page_url, cached_page_title, cached_page_meta_keywords, cached_page_meta_description, cached_time) VALUES (?,?,?,?,now())";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "ssss", $page_url,$title,$keywords,$description)) {
		echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		echo "Saving failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	mysqli_stmt_close($stmt);
}

//заводим массивы ключей и городов
$CITY_NEWS_PER_PAGE=1;
$city_news_page_number=1;
$current_page="MAIN_PAGE";

$title_template = "Кредиты в России, Банки России, Области, Регионы и Округи";

$function = new Functions;
$google_snippet = new Google;

print_r(error_get_last());

//определяем имя домена и сабдомена и записываем номер ключа и номер города
$url = $_SERVER["HTTP_HOST"];
#echo "HTTP_HOST: ".$url.'<br>';
//preg_match("/[a-z0-9]*\.[a-z0-9]*$/",$url,$url1);
//preg_match("/[0-9]+-[0-9]+/",$url,$match);
//list($keys_num, $city_num) = split('-', $match[0]);

$url = $_SERVER["REQUEST_URI"];
#echo "REQUEST_URI".$url.'<br>';
preg_match("/[\-a-zA-Z0-9]+\/[\-a-zA-Z0-9]*/",$url,$request_uri);
#echo "request_uri".$request_uri[0].'<br>';
#echo $request_uri[0].'<br>';

if(count($request_uri)>=1){
	list($url_region,$url_city) = explode('/', $request_uri[0]);
}

#echo "url_region".$url_region.'<br>';
#echo "url_city".$url_city.'<br>';

//обрабатываем запрос генерации урлов

if( $url_city && is_numeric($url_city) && $url_region){
	$template = file_get_contents("tmpl_region.html");
	$city_news_page_number = $url_city;
	$current_page = "REGION_PAGE_PAGING";
	#echo "REGION_PAGE_PAGING";
} elseif ($url_city && $url_region){
	echo "url_city: ".$url_city."<br/>";
	echo "url_region: ".$url_region."<br/>";
	$template = file_get_contents("tmpl_key.html");
	$current_page = "CITY_PAGE";
	#echo "CITY_PAGE";
} elseif(!$url_city && $url_region){
	$template = file_get_contents("tmpl_region.html");
	$current_page = "REGION_PAGE";
	$city_news_page_number = 1;
	#echo "REGION_PAGE";
} elseif($url_city == 'index.php' && !$url_region){
	//TODO Обработка региона
	$template=file_get_contents("tmpl_main.html");
	$current_page = "MAIN_PAGE";
	#echo "MAIN_PAGE";
}else{
	$template=file_get_contents("tmpl_main.html");
	$current_page = "MAIN_PAGE";
	#echo "MAIN_PAGE";
}
	
//замена макросов в шаблоне с обработкой главной страницы
if ($url==$url1[0])	
{
	$template=preg_replace("/<title>.*<\/title>/", "<title>Кредиты в России, Банки России, Области, Регионы и Округи | ".$_SERVER["SERVER_NAME"]."</title>", $template);
	$template=preg_replace("/name=\"keywords\" content=\".*\"/", "name=\"keywords\" content=\"Денежный кредит, кредит без залога, кредит наличными без поручителей, оформление кредита, кредиты малому бизнесу, коммерческий кредит в городе Москва | ".$_SERVER["SERVER_NAME"]."\"", $template);
	$template=preg_replace("/name=\"description\" content=\".*\"/", "name=\"description\" content=\"Займы и кредиты онлайн - Денежный кредит, кредит без залога, кредит наличными без поручителей, оформление кредита, кредиты малому бизнесу, коммерческий кредит в городе Москва | ".$_SERVER["SERVER_NAME"]."\"", $template);
}

else

{
	$template=preg_replace("/\[CITY\]/", trim($city[$city_num]), $template);
	$template=preg_replace("/\[KEY\]/", trim($keys[$keys_num]), $template);
}


$template=preg_replace("/\[RANDKEY\]/e", 'trim($keys[rand(0,$max_k)])', $template);
$template=preg_replace("/\[RANDCITY\]/e", 'trim($city[rand(0,$max_c)])', $template);
$template=preg_replace("/\[URL\]/", "http://$url", $template);
$template=preg_replace("/\[URLMAIN\]/", "http://$url1[0]", $template);

//fetch regions
$con=mysqli_connect("localhost","root","hw6cGD6X","doorgen_banks");
#echo "Connecting...";
if (mysqli_connect_errno())
{
  echo "Failed to connect to MySQL: " . mysqli_connect_error();
}


//get page info
$page_info = getPageInfo($con,$url);
if($page_info){
	$is_cached = true;
	$page_title = $page_info['cached_page_title'];
	$page_meta_keywords = $page_info['cached_page_meta_keywords'];
	$page_meta_description = $page_info['cached_page_meta_description'];
	echo "Page $url is CACHED."."<br/>";
}else{
	echo "Page $url is NOT CACHED."."<br/>";
	$is_cached = false;
}

//mysql_query("set character_set_client='utf8'");
//mysql_query("set character_set_results='utf8'");
//mysql_query("set collation_connection='utf8_general_ci'");
#echo "Processing page: ".$current_page."<br>";


if($current_page == "MAIN_PAGE"){
	#echo "Main page processing...";
	
	$pager = new TitleGenerator; 	
	
	if(!$is_cached){
		$page_title = $pager->getRegionRandomTitle()." | ".$_SERVER[HTTP_HOST];
	}
	$result = mysqli_query($con,"SELECT COUNT(*) as row_count FROM doorgen_banks.region");
	$row = mysqli_fetch_assoc($result);
	$row_count = $row['row_count'];

	$reg_section_count = 4;
	$reg_per_section = ($row_count - $row_count % $reg_section_count) / $reg_section_count;

	$result = mysqli_query($con,"SELECT region_name, region_name_latin FROM doorgen_banks.region");

	$regions = "";
	$posted = 0;
	$page = 1;
	while($row = mysqli_fetch_array($result))
	{
		//print result to page
		if($posted != 0 && ($posted%$reg_per_section == 0)){
			$template=preg_replace("/\[REGIONS_".$page."\]/", $regions, $template);
			$regions = "";
			$page = $page+1;
		}
		//fill result with region list
		$posted = $posted + 1;
		$regions = $regions."<a href = \"/".str_replace(" ","-",$row['region_name_latin'])."/\">".$row['region_name']."</a>&nbsp;";
	}
	
	$bread_crumbs = "<a href =\"/\">".Главная."</a>";
}

#http://php.net/manual/en/mysqli.prepare.php

if($current_page == "REGION_PAGE" || $current_page == "REGION_PAGE_PAGING"){
	//get region names
	$result = mysqli_query($con,"SELECT region_name FROM doorgen_banks.region r where r.region_name_latin like replace(LOWER('".$url_region."'),'-','_')");
	$row = mysqli_fetch_assoc($result);
	$region_name = $row['region_name'];
	
	//generate header menu
	$caseSelector = new CaseValueSelector;
	$region_cases = $caseSelector->getCaseTitle($con,2,$url_region);
	
	if(!$is_cached){
		$page_title = $region_name." - ".$title_template." | ".$_SERVER[HTTP_HOST];
	}
	#echo "region_name: " . $region_name . "<br>";
	//getting city new count
	$query_count = "SELECT count(*) as row_count FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND r.region_name_latin like replace(LOWER('".$url_region."'),'-','_') AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id";
	$result = mysqli_query($con,$query_count);
	
	$row = mysqli_fetch_assoc($result);
	$city_news_count = $row['row_count'];
	#echo "city_news_count: " . $city_news_count . "<br>";
	
	if($city_news_count>0){
		//вычисляем последнюю страницы
		$max_page_number = floor($city_news_count/$CITY_NEWS_PER_PAGE);
		if(city_news_count%$CITY_NEWS_PER_PAGE != 0){
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
		$query_city_list = "SELECT c.city_name, c.city_name_latin, ek.key_value, ek.key_value_latin, r.region_name, r.region_name_latin, unix_timestamp(cp.posted_time) FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND r.region_name_latin like replace(LOWER(?),'-','_') AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id LIMIT ".$start_position.",".$CITY_NEWS_PER_PAGE;
		#echo "query_city_list: ".$query_city_list."<br>";
		if (!($stmt = mysqli_prepare($con,$query_city_list))) {
			echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		//set values
		#echo "set value...";
		$id=1;
		if (!mysqli_stmt_bind_param($stmt, "s", $url_region)) {
			echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		#echo "execute...";
		if (!mysqli_stmt_execute($stmt)){
			echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}

		/* instead of bind_result: */
		#echo "get result...";
		if(!mysqli_stmt_bind_result($stmt, $city_name,$city_name_latin,$key_value, $key_value_latin, $region_name, $region_name_latin, $posted_time)){
			echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		$index = 1;
		while (mysqli_stmt_fetch($stmt)) {
			// use your $myrow array as you would with any other fetch
			#echo "City name: ".$city_name."; key: ".$key_value;
			$city_href = "<a href = \"/".str_replace(" ","-",$region_name_latin)."/".str_replace(" ","-",$city_name_latin." ".$key_value_latin).".html\">".$city_name." ".$key_value." (".rusdate($posted_time,'j %MONTH% Y, G:i').")</a>&nbsp;";
			$template=preg_replace("/\[CITY_NEWS_".$index."\]/", $city_href, $template);
			$index = $index+1;
		}

		$pager = new Pager;
		$template=preg_replace("/\[PAGER\]/", $pager->getPageNavigation("/".str_replace(" ","-",$region_name_latin)."/",$city_news_page_number, $max_page_number), $template);
		
		/* explicit close recommended */
		mysqli_stmt_close($stmt);
	}
	//fill [BREAD_CRUMBS]
	$bread_crumbs = "<a href =\"/\">".Главная."</a>&nbsp;>&nbsp;<a href =\"#\">".$region_name."</a>&nbsp;";
	
	//
	
}

if($current_page == "CITY_PAGE"){
	//get region names
	$key_info = getKeyInfo($con,$url_city);
	#echo "var_dump: ". var_dump($key_info)."<br/>";

	if($key_info){
		$region_name = $key_info['region_name'];
		
		//generate header menu
		$caseSelector = new CaseValueSelector;
		$city_cases = $caseSelector->getCaseTitle($con,1,$caseSelector->getCityValueByNewsKey($con,$url_city));
		
		if(!$is_cached){
			$pager = new TitleGenerator; 	
			$page_title = $pager->getCityRandomTitle();
		}
		#echo "region_name: " . $region_name . "<br>";
		//getting city new count
		$query_count = "SELECT count(*) as row_count FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND r.region_name_latin like replace(LOWER('".$url_region."'),'-','_') AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id";
		$result = mysqli_query($con,$query_count);
		
		$row = mysqli_fetch_assoc($result);
		$city_news_count = $row['row_count'];
		#echo "city_news_count: " . $city_news_count . "<br>";
		
		if($city_news_count>0){
			//вычисляем последнюю страницы
			$max_page_number = floor($city_news_count/$CITY_NEWS_PER_PAGE);
			if(city_news_count%$CITY_NEWS_PER_PAGE != 0){
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
			$query_city_list = "SELECT c.city_name, c.city_name_latin, ek.key_value, ek.key_value_latin, r.region_name, r.region_name_latin, unix_timestamp(cp.posted_time) FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND r.region_name_latin like replace(LOWER(?),'-','_') AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id LIMIT ".$start_position.",".$CITY_NEWS_PER_PAGE;
			#echo "query_city_list: ".$query_city_list."<br>";
			if (!($stmt = mysqli_prepare($con,$query_city_list))) {
				echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
			}
			
			//set values
			#echo "set value...";
			$id=1;
			if (!mysqli_stmt_bind_param($stmt, "s", $url_region)) {
				echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
			}
			
			#echo "execute...";
			if (!mysqli_stmt_execute($stmt)){
				echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
			}

			/* instead of bind_result: */
			#echo "get result...";
			if(!mysqli_stmt_bind_result($stmt, $city_name,$city_name_latin,$key_value, $key_value_latin, $region_name, $region_name_latin, $posted_time)){
				echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
			}
			
			$index = 1;
			while (mysqli_stmt_fetch($stmt)) {
				// use your $myrow array as you would with any other fetch
				#echo "City name: ".$city_name."; key: ".$key_value;
				$city_href = "<a href = \"/".str_replace(" ","-",$region_name_latin)."/".str_replace(" ","-",$city_name_latin." ".$key_value_latin).".html\">".$city_name." ".$key_value." (".rusdate($posted_time,'j %MONTH% Y, G:i').")</a>&nbsp;";
				$template=preg_replace("/\[CITY_NEWS_".$index."\]/", $city_href, $template);
				$index = $index+1;
			}

			$pager = new Pager;
			$template=preg_replace("/\[PAGER\]/", $pager->getPageNavigation("/".str_replace(" ","-",$region_name_latin)."/",$city_news_page_number, $max_page_number), $template);
			
			/* explicit close recommended */
			mysqli_stmt_close($stmt);
		}
		//fill [BREAD_CRUMBS]
		$bread_crumbs = "<a href =\"/\">".Главная."</a>&nbsp;>&nbsp;<a href =\"#\">".$region_name."</a>&nbsp;";
	}else{
		#TODO PAGE NOT FOUND REDIRECT
	}
	//
	
}

for($i=1; $i <= 9; $i++){
	$page_title=preg_replace("/\[REGION_CASE_".$i."\]/", $region_cases["$i"], $page_title);
	$page_title=preg_replace("/\[CITY_CASE_".$i."\]/", $city_cases["$i"], $page_title);
}

if(!$is_cached){
	while(!$page_meta_description){
		$snippet_array = $google_snippet->Start($page_title,'ru',1,$function);
		$page_meta_description = $snippet_array[0]["description"];
	}
	savePageInfo($con,$url,$page_title,$page_title,$page_meta_description);
}

mysqli_close($con);
$template=preg_replace("/\[BREAD_CRUMBS\]/", $bread_crumbs, $template);

$template=preg_replace("/\[TITLE\]/", $page_title, $template);
$template=preg_replace("/\[REGION_NAME\]/", $region_name, $template);
$template=preg_replace("/\[DESCRIPTION\]/", $page_meta_description, $template);

for($i=1; $i <= 9; $i++){
	$template=preg_replace("/\[REGION_CASE_".$i."\]/", $region_cases["$i"], $template);
	$template=preg_replace("/\[CITY_CASE_".$i."\]/", $city_cases["$i"], $template);
}

echo $template;	
?>