<?php

require_once "config.php";

function getTablesRecordCount($con)
{
	$count_array = array();
	$query_case_list = "SELECT count(*) region_count FROM `region` UNION SELECT count(*) city_count FROM `city` UNION SELECT count(*) extra_key_count FROM `extra_key`";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $count)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	while(mysqli_stmt_fetch($stmt)) {
		array_push($count_array,$count);
	}
		
	mysqli_stmt_close($stmt);
	
	return $count_array;
}

function getNewsIdForPostingArray($con, $news_for_posting)
{
	$id_array = array();
	$id_array_for_posting = array();
	$query_case_list = "SELECT cp.city_page_id FROM `city_page` cp where cp.posted_time > now() + INTERVAL 5 MINUTE";
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
	$query_case_list = "UPDATE city_page cp SET cp.posted_time = (now() + INTERVAL ".rand(0,86400)." SECOND) WHERE cp.city_page_id = ?";
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

function getPageInfo($con,$city_page_id)
{
	$result_array = array();
	$query_case_list = "SELECT r.region_name_latin, cp.city_page_key, c.city_name_latin FROM `city` c, `city_page` cp, `region` r WHERE 1 AND cp.city_page_id = ? AND c.city_id = cp.city_id AND c.region_id = r.region_id";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $city_page_id)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $region_name_latin, $city_page_key, $city_name_latin)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	"region_name_latin"=>$region_name_latin,
						"city_page_key"=>$city_page_key,
						"city_name_latin"=>$city_name_latin
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

//читаем из файла значение, в котором храниться кол-во новостей, необходимых для добавления
$news_per_region = 5;
$news_per_region_min = 5;
$news_per_region_max = 5;
$posting_time_table_file_path = "time_table.txt";
if (file_exists($posting_time_table_file_path) and filesize($posting_time_table_file_path) > 0) {
	$time_table_array = file($posting_time_table_file_path, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
	if(isset($time_table_array[0])){
		list($news_per_region_min,$news_per_region_max) = explode('-',$time_table_array[0]);
		if(!$news_per_region_max){
			$news_per_region_max = $news_per_region_min;
		}
	}
	array_splice($time_table_array, 0, 1);
	//save new time table to file
	object2file($time_table_array, $posting_time_table_file_path);
}

//randomize news count
$news_per_region = rand($news_per_region_min,$news_per_region_max);
echo "news_per_region: ".$news_per_region."<br/>";
echo "news_per_region_min: ".$news_per_region_min."<br/>";
echo "news_per_region_max: ".$news_per_region_max."<br/>";

$con=mysqli_connect(DB_HOST,DB_USER_NAME,DB_USER_PWD,DB_NAME);
/*echo "Connecting...";
if (mysqli_connect_errno())
{
  echo "Failed to connect to MySQL: " . mysqli_connect_error();
}*/

//получаем кол-во новостей на один регион


//получаем необходимые параметры из базы
list($region_count,$city_count,$extra_key_count) = getTablesRecordCount($con);
echo "region_count: ".$region_count."<br/>";
echo "city_count: ".$city_count."<br/>";
echo "extra_key_count: ".$extra_key_count."<br/>";

$news_count_for_posting = $region_count * $news_per_region;
echo "news_count_for_posting: ".$news_count_for_posting."<br/>";
//получаем список всех новостей, у которых время постинга больше текущего времени на 5 мин
$news_for_posting_array  = getNewsIdForPostingArray($con,$news_count_for_posting);
echo var_dump($news_for_posting_array);

$flag = true;
$server_name = $_SERVER["SERVER_NAME"];

$ctx = stream_context_create(array(
    'http' => array(
		'method'  => 'GET',
        'timeout' => 5
        )
    )
); 

for($i = 0; $i < count($news_for_posting_array); $i++){
	postNews($con,$news_for_posting_array[$i]);
	$result_array = getPageInfo($con,$news_for_posting_array[$i]);
	$href = "/".str_replace(" ","-",$result_array["region_name_latin"])."/".str_replace(" ","-",$result_array["city_page_key"]).".html";
	$url="http://".$server_name.$href;
	file_get_contents($url, false, $ctx); 
	#$ch = curl_init();
	#curl_setopt( $ch, CURLOPT_TIMEOUT, 5);
	#curl_setopt( $ch, CURLOPT_URL, $url );  
	#curl_exec( $ch );
	echo "curl_exec executed..."."<br/>";
	#curl_close ( $ch );
	echo $url."<br/>";
}
//случайно выбараем

mysqli_close($con);

unset($con);
?>