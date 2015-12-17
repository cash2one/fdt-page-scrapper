<?php
error_reporting(E_ALL ^ E_NOTICE);

require_once "utils/pager.php";
require_once "application/models/functions_decode.php";
require_once "application/libraries/parser.php";
require_once "utils/title_generator.php";
require_once "utils/ya_news_extractor.php";
require_once "utils/config.php";
require_once "utils/proxy_config.php";

$page_title="";
$page_meta_keywords="";
$page_meta_description="";

$function = new Functions;
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

function getKeyInfo($con,$page_key)
{
	$result_array = array();
	$query_case_list = "SELECT key_value, key_value_latin, unix_timestamp(post_dt) posted_time FROM pages p, door_keys k ,page_content pc WHERE k.id = p.key_id AND pc.page_id = p.id AND pc.post_dt < now() AND key_value_latin = ?";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	if (!mysqli_stmt_bind_param($stmt, "s", $page_key)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $key_value,$key_value_latin,$posted_time)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	"key_value"=>$key_value,
        						"key_value_latin"=>$key_value_latin,
        						"posted_time"=>$posted_time
					);	
	}else{
		#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}
	
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function getPageInfo($con,$page_url)
{
	$result_array = array();
	$query_case_list = "SELECT p.id, p.title, p.meta_keywords, p.meta_description, pc.post_dt FROM pages p, door_keys k, page_content pc WHERE k.id = p.key_id AND pc.page_id = p.id AND k.key_value_latin = ?";
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
	if(!mysqli_stmt_bind_result($stmt, $id, $title, $meta_keywords, $meta_description, $post_dt)){
		echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	
						"id"=>$id,
						"title"=>$title,
						"meta_keywords"=>$meta_keywords,
						"meta_description"=>$meta_description, 
						"post_dt"=>$post_dt
					);	
	}else{
		#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
		
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function fillSnippetsContent($template, $key_value, $con, $page_url){

	$snippets_array = array();
	$snippets_array = getPageSnippets($con,$page_url);
	
	$SNIPPET_BLOCK_1 = "<div><img src='[SNIPPET_IMG_SMALL_[INDEX]]' alt=''><p class='text-1 top-2 p3'><h2>[SNIPPET_TITLE_[INDEX]]</h2></p><p>[SNIPPET_CONTENT_[INDEX]]</p><br/></div>";
	$start_block_index = 1;
	for($i=1; $i <=3; $i++){
		if(isset($snippets_array[$i-1])){
			$template=preg_replace("/\[SNIPPET_BLOCK_1_".$start_block_index."\]/", preg_replace("/\[INDEX\]/", $i, $SNIPPET_BLOCK_1), $template);
			$start_block_index++;
		}
	}
	
	$SNIPPET_BLOCK_2 = "<div><div class='number'>[NUMBER]</div><p class='extra-wrap border-bot-1'><span class='clr-1'><h2>[SNIPPET_TITLE_[INDEX]]</h2></span><br>[SNIPPET_CONTENT_[INDEX]]</p></div>";
	$start_block_index = 1;
	for($i=4; $i <=6; $i++){
		if(isset($snippets_array[$i-1])){
			$template=preg_replace("/\[SNIPPET_BLOCK_2_".$start_block_index."\]/", preg_replace("/\[INDEX\]/", $i, preg_replace("/\[NUMBER\]/", $start_block_index, $SNIPPET_BLOCK_2)), $template);
			$start_block_index++;
		}
	}
	
	$SNIPPET_BLOCK_3 = "<div><img src='[SNIPPET_IMG_SMALL_[INDEX]]' alt='' class='img-indent'><p class='extra-wrap'><span class='clr-1'><h2>[SNIPPET_TITLE_[INDEX]]</h2></span><br>[SNIPPET_CONTENT_[INDEX]]</p></div>";
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
			$template=preg_replace("/\[SNIPPET_TITLE_".($i+1)."\]/", $snippets_array[$i][0]["title"], $template);
			$description = "";
			for($j=0; $j < 3; $j++){
			    if(isset($snippets_array[$i][$j])){
			        if(!isset($description)){
			            $description =$snippets_array[$i][$j]["description"];
			        }else{
			             $description = $description."<p class=\"extra-wrap border-bot-1\">".$snippets_array[$i][$j]["description"];
			        }
			    }
			}
			$template=preg_replace("/\[SNIPPET_CONTENT_".($i+1)."\]/", $description, $template);
			$template=preg_replace("/\[SNIPPET_IMG_LARGE_".($i+1)."\]/", isset($snippets_array[$i][0]["large"])?$snippets_array[$i][0]["large"]:"", $template);
			$template=preg_replace("/\[SNIPPET_IMG_SMALL_".($i+1)."\]/", isset($snippets_array[$i][0]["small"])?$snippets_array[$i][0]["small"]:"", $template);
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

function getPageSnippets($con,$page_url)
{
    global $stmt;
	$snippets_array = array();
	$query_case_list = " SELECT snp.title, snp.description, cd.snippets_index, snp.image_large, snp.image_small ".
	                   " FROM page_content pc LEFT JOIN content_detail cd ON pc.id = cd.page_content_id, snippets snp , pages p, door_keys k ".
	                   " WHERE k.id = p.key_id AND snp.id = cd.snippet_id AND pc.page_id = p.id AND k.key_value_latin = ? ORDER BY cd.snippets_index ASC, cd.main_flg DESC";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}

	//set values
	#echo "set value...";
	if (!mysqli_stmt_bind_param($stmt, "s", $page_url)) {
		echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $title, $description, $snippets_index, $image_large, $image_small)){
		echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
	
	$lastIdx = 0;
	$shift=0;
	while(mysqli_stmt_fetch($stmt)) {
	    if($snippets_index != $lastIdx){
	        $lastIdx = $snippets_index;
	        $shift = 0;
	    }else{
	        $shift = $shift + 1;
	    }
		$snippets_array[$snippets_index-1][$shift] = array(	
						"title"=>$title,
						"description"=>$description,
						"image_large"=>$image_large,
						"image_small"=>$image_small
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
$KEY_PER_PAGE=25;
$key_page_number=1;
$current_page_type="MAIN_PAGE";

$url = $_SERVER["REQUEST_URI"];
#echo "REQUEST_URI: ".$url.'<br>';

$page_key = "";

#$url = "/alfa-bank-v-nijnem-novgorode/";
#$url = "/";
#$site_main_domain = "vtopax.ru";

if(strcmp("/",$url) != 0){
	$request_uri = explode('/', $url);
	if(isset($request_uri[1])){
		$page_key = $request_uri[1];
	}
}

#echo "page_key: ".$page_key.'<br>';
$site_main_domain = $_SERVER["HTTP_HOST"];

//обрабатываем запрос генерации урлов
$url_for_request = "";
$key_page_number = "";

if( $page_key && !is_numeric($page_key))
{
	$template = file_get_contents("tmpl_key.html");
	$current_page_type = "KEY_PAGE";
	$url_for_request = $page_key;
} 
else{
	//check for main_page paging
	if(is_numeric($page_key)){
		$key_page_number = $page_key;
	}
	
	if(!$key_page_number){
		$key_page_number = 1;
	}
	
	$url_for_request = "/";
	$current_page_type = "MAIN_PAGE_PAGING";
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
#$con=mysqli_connect("192.240.96.222:3306","vtopax","lol200","vtopax");

mysqli_query($con,"set character_set_client='utf8'");
mysqli_query($con,"set character_set_results='utf8'");
mysqli_query($con,"set collation_connection='utf8_general_ci'");

//get page info
$page_info = getPageInfo($con,$url_for_request);
#var_dump($page_info);

if($page_info){
	$is_cached = true;
	$page_title = $page_info['title'];
	$page_meta_keywords = $page_info['meta_keywords'];
	$page_meta_description = $page_info['meta_description'];
	#echo "Page $url is CACHED."."<br/>";
}

#TODO Get random title
$title_template = "Кредиты в России, Банки России, Области, Регионы и Округи";

if($current_page_type == "MAIN_PAGE_PAGING"){
	#echo "Main page processing...";

	#$result = mysqli_query($con,"SELECT k.key_value, k.key_value_latin, unix_timestamp(p.post_dt) posted_time FROM pages p, door_keys k WHERE k.id = p.key_id AND p.post_dt < now() ORDER BY post_dt DESC LIMIT 50");
	
	//getting city news count
	$query_count = " SELECT count(t.key_value) row_count ".
	               " FROM (SELECT DISTINCT k.key_value FROM door_keys k, pages p LEFT JOIN page_content pc ON p.id=pc.page_id ".
	               " WHERE k.id = p.key_id AND pc.post_dt < now() AND k.key_value <> '/' AND pc.page_id IS NOT NULL) as t";
	$result = mysqli_query($con,$query_count);
	
	$row = mysqli_fetch_assoc($result);
	$key_count = $row['row_count'];
	#echo "page_key_count: " . $key_count . "<br>";
	
	if($key_count>0){
		//вычисляем последнюю страницы
		$max_page_number = floor($key_count/$KEY_PER_PAGE);
		if($key_count%$KEY_PER_PAGE != 0){
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
		$query_key_page_list =      " SELECT DISTINCT k.key_value, k.key_value_latin, unix_timestamp(MAX(pc.post_dt)) posted_time, pc.upd_flg, pc.page_id ".
                                    " FROM door_keys k, pages p, page_content pc ".
                                    " WHERE p.id=pc.page_id AND k.id = p.key_id AND pc.post_dt < now() AND k.key_value <> '/' GROUP BY pc.page_id ORDER BY post_dt DESC LIMIT ".$start_position.",".$KEY_PER_PAGE;
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
		if(!mysqli_stmt_bind_result($stmt, $key_value, $key_value_latin, $posted_time, $upd_flg, $page_id )){
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
			$updTitle = "";
			if($upd_flg){
			    $updTitle = "Обновлена информация по ";
			}
			
			$city_href = "<a href = \"http://".$site_main_domain."/".$key_value_latin."/\">".$updTitle.$key_value." (".rusdate($posted_time,'j %MONTH% Y, G:i').")</a><br/>";
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

if($current_page_type == "KEY_PAGE"){
	//get city page info
	#echo "page_key: ".$page_key."<br/>";
	
	$key_info = getKeyInfo($con,$page_key);
	
	//if page exist
	if($key_info){
		//fill [BREAD_CRUMBS]
		$bread_crumbs = "<a href =\"http://".$site_main_domain."\">Главная</a>&nbsp;>&nbsp;<a href =\"#\">".$key_info['key_value']." ".rusdate($key_info['posted_time'],'j %MONTH% Y')."</a>";
	}else{
		#PAGE NOT FOUND REDIRECT
		header('HTTP/1.1 404 Not Found');
		#header('Location: /');
		$_GET['e'] = 404;
		include ($_SERVER['DOCUMENT_ROOT'] . '/404.html');
		exit;
	}
}

if($current_page_type == "KEY_PAGE"){
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
if($current_page_type == "MAIN_PAGE_PAGING"){
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

unset($page_meta_description, $page_title, $bread_crumbs, $region_name, $function, $snippet_extractor, $google_image, $title_generator, $extractd_news, $news_extractor, $url_for_request);
mysqli_close($con);

echo $template;	
?>