<?php
error_reporting(E_ALL ^ E_NOTICE);

require_once "utils/pager.php";
require_once "application/models/functions_decode.php";
require_once "application/libraries/parser.php";
require_once "application/plugins/snippets/Google.php";
require_once "application/plugins/snippets/Ukr.php";
require_once "application/plugins/snippets/Tut.php";
require_once "application/plugins/images/ImagesGoogle.php"; 
require_once "utils/ya_news_extractor.php";
require_once "utils/config.php";
require_once "utils/proxy_config.php";
require_once "utils/snippets_dao.php";

$page_title="";
$page_meta_keywords="";
$page_meta_description="";
$is_cached = false;

$function = new Functions;
$snippet_extractor = new Ukr;
#$snippet_extractor = new Google;
#$snippet_extractor = new Tut;
$google_image = new ImagesGoogle;

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

//заводим массивы ключей и городов
$KEY_PER_PAGE=25;
$key_page_number=1;
$current_page="MAIN_PAGE";

$url = $_SERVER["REQUEST_URI"];
#echo "REQUEST_URI: ".$url.'<br>';

$page_key = "";
if(strcmp("/",$url) != 0){
	$request_uri = explode('/', $url);
	if(isset($request_uri[1])){
		$page_key = $request_uri[1];
	}
}

#echo "page_key: ".$page_key.'<br>';
$site_main_domain = $_SERVER["HTTP_HOST"];;

//обрабатываем запрос генерации урлов
$url_for_cache = "";
$key_page_number = "";

if( $page_key && !is_numeric($page_key)){
	$template = file_get_contents("tmpl_key.html");
	$current_page = "KEY_PAGE";
	$url_for_cache = $page_key;
} else{
	//check for main_page paging
	if(is_numeric($page_key)){
		$key_page_number = $page_key;
		if($page_key != 1){
			$url_for_cache = $page_key;
		}else{
			$url_for_cache = "/";
		}
	}
	if(!$key_page_number){
		$key_page_number = 1;
		$url_for_cache = "/";
	}
	$current_page = "MAIN_PAGE_PAGING";
	#echo "key_page_number: ".$key_page_number."<br/>";
	$template = file_get_contents("tmpl_main_new.html");
}

#echo "url_for_cache: ".$url_for_cache."<br/>";
#echo "current_page: ".$current_page."<br/>";

$template=preg_replace("/\[URL\]/",$site_main_domain, $template);
$template=preg_replace("/\[URLMAIN\]/",$site_main_domain, $template);
$template=preg_replace("/\[HEADER_KEYS\]/",HEADER_KEYS, $template);

//fetch regions
$con=mysqli_connect(DB_HOST,DB_USER_NAME,DB_USER_PWD,DB_NAME);
mysqli_query($con,"set character_set_client='utf8'");
mysqli_query($con,"set character_set_results='utf8'");
mysqli_query($con,"set collation_connection='utf8_general_ci'");

//get page info
$page_info = getPageInfo($con,$url_for_cache);
#var_dump($page_info);

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
$title_template = "Кредиты в России, Банки России, Области, Регионы и Округи";

if($current_page == "MAIN_PAGE_PAGING"){
	#echo "Main page processing...";
	if(!$is_cached){
		$page_title = MAIN_TITLE;
	}

	$result = mysqli_query($con,"SELECT key_value, key_value_latin, unix_timestamp(posted_time) posted_time FROM page WHERE posted_time < now() ORDER BY posted_time DESC LIMIT 50");
	
	//getting city news count
	$query_count = "SELECT count(*) row_count FROM page WHERE posted_time < now()";
	$result = mysqli_query($con,$query_count);
	
	$row = mysqli_fetch_assoc($result);
	$page_key_count = $row['row_count'];
	#echo "page_key_count: " . $page_key_count . "<br>";
	
	if($page_key_count>0){
		//вычисляем последнюю страницы
		$max_page_number = floor($page_key_count/$KEY_PER_PAGE);
		if($page_key_count%$KEY_PER_PAGE != 0){
			$max_page_number = $max_page_number + 1;
		}
		
		if($key_page_number > $max_page_number){
			$key_page_number = $max_page_number;
		}
		
		#echo "max_page_number: ".$max_page_number."<br>";
		#echo "final key_page_number: ".$key_page_number."<br>";
		
		$start_position = $KEY_PER_PAGE*($key_page_number-1);
		#echo "start_position: ".$start_position."<br>";

		#echo "Page processing...";
		//prepare statement
		$query_key_page_list = "SELECT key_value, key_value_latin, unix_timestamp(posted_time) posted_time FROM page WHERE posted_time < now() ORDER BY posted_time DESC LIMIT ".$start_position.",".$KEY_PER_PAGE;
		#echo "query_city_list: ".$query_key_page_list."<br>";
		if (!($stmt = mysqli_prepare($con,$query_key_page_list))) {
			#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		#echo "execute...";
		if (!mysqli_stmt_execute($stmt)){
			#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}

		/* instead of bind_result: */
		#echo "get result...";
		if(!mysqli_stmt_bind_result($stmt, $key_value, $key_value_latin, $posted_time )){
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
			
			
			$city_href = "<a href = \"http://".$site_main_domain."/".$key_value_latin."/\">".$key_value." (".rusdate($posted_time,'j %MONTH% Y, G:i').")</a><br/>";
			$news_block = $news_block.$city_href;
		}
		$template=preg_replace("/\[CITY_NEWS_1\]/", $news_block, $template);
		#echo "news_block: ".$news_block."<br>";

		$pager = new Pager;
		$template=preg_replace("/\[PAGER\]/", $pager->getPageNavigation("/",$key_page_number, $max_page_number), $template);
		
		/* explicit close recommended */
		mysqli_stmt_close($stmt);
	}else{
		$template=preg_replace("/\[CITY_NEWS_1\]/", "<br/>Новостей по данному региону не найдено", $template);
		$template=preg_replace("/\[PAGER\]/","", $template);
	}
	//fill [BREAD_CRUMBS]
	$bread_crumbs = "<a href =\"/#\">Главная</a>&nbsp;";
	
	//
}

$key_info = array();

if($current_page == "KEY_PAGE"){
	//get city page info
	#echo "page_key: ".$page_key."<br/>";
	
	$key_info = getKeyInfo($con,$page_key);

	/*
	$result_array = array(	"key_value"=>$key_value,
						"key_value_latin"=>$key_value_latin,
						"posted_time"=>$posted_time
					);	
	*/
						
	#var_dump($key_info);
	
	//if page exist
	if($key_info){
		if(!$is_cached){
			$page_title = $key_info['key_value']." | ".MAIN_TITLE;
		}
		//fill [BREAD_CRUMBS]
		$bread_crumbs = "<a href =\"http://".$site_main_domain."\">Главная</a>&nbsp;>&nbsp;<a href =\"#\">".$key_info['key_value']."</a>";
	}else{
		#PAGE NOT FOUND REDIRECT
		header('HTTP/1.1 404 Not Found');
		#header('Location: /');
		$_GET['e'] = 404;
		include ($_SERVER['DOCUMENT_ROOT'] . '/404.html');
		exit;
	}
}

#echo "Snippet extraction....";
if(!$is_cached){
	$page_meta_description = false;
	while(!$page_meta_description){
		#echo "Page_title: ".$page_title."<br/>";
		$snippet_array = $snippet_extractor->Start(preg_replace('/\|/',' ',$page_title),'ru',1,$function);
		#var_dump($snippet_array);
		if(isset($snippet_array[0])){
			$page_meta_description = preg_replace('/ {0,}\.{2,}/','.',$snippet_array[0]["description"]);
		}
	}
	$page_title = $page_title." | ".$site_main_domain;
	savePageInfo($con,$url_for_cache, $page_title, $page_title, $page_meta_description);
}

if($current_page == "KEY_PAGE"){
	$template = fillSnippetsContent($template,$key_info['key_value'],$con, $page_key);

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

$template=preg_replace("/\[TITLE\]/", $page_title, $template);
$template=preg_replace("/\[MAIN_TITLE\]/", MAIN_TITLE, $template);
$template=preg_replace("/\[DESCRIPTION\]/", $page_meta_description, $template);

//print last news
if($current_page == "MAIN_PAGE_PAGING"){
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

unset($page_meta_description, $page_title, $bread_crumbs, $region_name, $function, $snippet_extractor, $google_image, $extractd_news, $news_extractor, $url_for_cache);
mysqli_close($con);

echo $template;	
?>