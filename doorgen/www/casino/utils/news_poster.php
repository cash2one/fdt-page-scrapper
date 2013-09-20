<?php

require_once "config.php";
require_once "snippets_dao.php";
require_once "../application/models/functions_decode.php";
require_once "../application/libraries/parser.php";
require_once "../application/plugins/snippets/Google.php";
require_once "../application/plugins/snippets/Ukr.php";
require_once "../application/plugins/snippets/Tut.php";
require_once "../application/plugins/images/ImagesGoogle.php"; 

function getNewsIdForPostingArray($con, $news_for_posting)
{
	$id_array = array();
	$id_array_for_posting = array();
	$query_case_list = "SELECT page_id FROM `page` where posted_time > now() + INTERVAL 5 MINUTE";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $id)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	while(mysqli_stmt_fetch($stmt)) {
		array_push($id_array,$id);
	}
		
	mysqli_stmt_close($stmt);
	
	//getting random $news_for_posting records
	if(count($id_array) < $news_for_posting){
		return $id_array;
	}
	
	for($i=0; $i < $news_for_posting; $i++){
		$rand_index = rand(0,count($id_array));
		array_push($id_array_for_posting,$id_array[$rand_index]);
		//remove element from id_array
		array_splice($id_array, $rand_index, 1);
	}
	
	return $id_array_for_posting;
}

function postNews($con,$news_id)
{
	$count_array = array();
	$query_case_list = "UPDATE page SET posted_time = (now() + INTERVAL ".rand(0,86400)." SECOND) WHERE page_id = ?";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if (!mysqli_stmt_bind_param($stmt, "d", $news_id)) {
		echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
		
	mysqli_stmt_close($stmt);
}

function getPageInfo($con,$page_id)
{
	$result_array = array();
	$query_case_list = "SELECT key_value_latin, key_value FROM page WHERE page_id = ?";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $page_id)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $key_value_latin, $key_value)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	"key_value_latin"=>$key_value_latin,
								"key_value"=>$key_value
							);	
	}else{
		#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		print_r(error_get_last());
	}
	
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function object2file($value, $filename)
{
    $f = fopen($filename, 'w');
	foreach ($value as $output)
	{
		fwrite($f, $output."\r\n");
	}
    fclose($f);
}

function getDescriptionByKey($key){
	$page_meta_description = false;
	while(!$page_meta_description){
		#echo "Page_title: ".$page_title."<br/>";
		$snippet_array = $snippet_extractor->Start(preg_replace('/\|/',' ',$page_title),'ru',1,$function);
		#var_dump($snippet_array);
		if(isset($snippet_array[0])){
			$page_meta_description = preg_replace('/ {0,}\.{2,}/','.',$snippet_array[0]["description"]);
		}
	}
	return $page_meta_description;
}

//читаем из файла значение, в котором храниться кол-во новостей, необходимых для добавления
$news_per_day = 5;
$news_per_day_min = 5;
$news_per_day_max = 5;
$posting_time_table_file_path = "time_table.txt";
if (file_exists($posting_time_table_file_path) and filesize($posting_time_table_file_path) > 0) {
	$time_table_array = file($posting_time_table_file_path, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
	if(isset($time_table_array[0])){
		list($news_per_day_min,$news_per_day_max) = explode('-',$time_table_array[0]);
		if(!$news_per_day_max){
			$news_per_day_max = $news_per_day_min;
		}
	}
	array_splice($time_table_array, 0, 1);
	//save new time table to file
	object2file($time_table_array, $posting_time_table_file_path);
}

//randomize news count
$news_per_day = rand($news_per_day_min,$news_per_day_max);
echo "news_per_day: ".$news_per_day."<br/>";
echo "news_per_day_min: ".$news_per_day_min."<br/>";
echo "news_per_day_max: ".$news_per_day_max."<br/>";

$con=mysqli_connect(DB_HOST,DB_USER_NAME,DB_USER_PWD,DB_NAME);
/*echo "Connecting...";
if (mysqli_connect_errno())
{
  echo "Failed to connect to MySQL: " . mysqli_connect_error();
}*/

$news_count_for_posting = $news_per_day;
echo "news_count_for_posting: ".$news_count_for_posting."<br/>";
//получаем список всех новостей, у которых время постинга больше текущего времени на 5 мин
$news_for_posting_array  = getNewsIdForPostingArray($con,$news_count_for_posting);
echo var_dump($news_for_posting_array);

$server_name = $argv[1];
$site_main_domain = $argv[1];

//get connection
$conn=mysqli_connect(DB_HOST,DB_USER_NAME,DB_USER_PWD,DB_NAME);
mysqli_query($conn,"set character_set_client='utf8'");
mysqli_query($conn,"set character_set_results='utf8'");
mysqli_query($conn,"set collation_connection='utf8_general_ci'");

for($i = 0; $i < count($news_for_posting_array); $i++){
	postNews($conn,$news_for_posting_array[$i]);
	$key_info = getPageInfo($conn,$news_for_posting_array[$i]);
	//TODO GET conn,title,keywords,description
	$page_title = $key_info['key_value']." | ".MAIN_TITLE;
	$page_url = $key_info['key_value_latin'];
	$keywords = $page_url;
	$description = getDescriptionByKey($page_title);
	$page_title = $page_title." | ".$site_main_domain;
	
	$key_value = $key_info['key_value'];
	$snippets_array = array();]
	
	echo "title: ".$title."<br/>";
	echo "page_url: ".$page_url."<br/>";
	echo "keywords: ".$keywords."<br/>";
	echo "description: ".$description."<br/>";
	echo "page_title: ".$page_title."<br/>";
	echo "key_value: ".$key_value."<br/>";
	
	savePageInfo($conn,$page_url, $page_title, $keywords, $description);
	
	//getting snippets array
	scrapPageSnippets($snippets_array, $key_value, $conn, $page_url);
}

mysqli_close($conn);

unset($conn);
?>