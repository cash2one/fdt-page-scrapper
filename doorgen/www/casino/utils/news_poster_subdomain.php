<?php

require_once "config.php";

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
	$query_case_list = "SELECT key_value_latin FROM page WHERE page_id = ?";
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
	if(!mysqli_stmt_bind_result($stmt, $key_value_latin)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	"key_value_latin"=>$key_value_latin	);	
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

for($i = 0; $i < count($news_for_posting_array); $i++){
	postNews($con,$news_for_posting_array[$i]);
	$result_array = getPageInfo($con,$news_for_posting_array[$i]);
	$href = $result_array["key_value_latin"];
	$url="http://".$href.".".$server_name;
	$ch = curl_init();
	curl_setopt( $ch, CURLOPT_TIMEOUT, 1);
	curl_setopt( $ch, CURLOPT_URL, $url );  
	curl_exec( $ch );
	echo "curl_exec executed..."."<br/>";
	curl_close ( $ch );
	echo $url."<br/>";
}

mysqli_close($con);

unset($con);
?>