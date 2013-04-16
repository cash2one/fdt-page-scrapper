<?php
//заводим массивы ключей и городов
$city=file("city.txt");
$keys=file("keys.txt");
$city_news_per_page=1;
$page=1;
echo "Starting...<br>";
$current_page="MAIN_PAGE";


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


//определяем имя домена и сабдомена и записываем номер ключа и номер города
//$url = $_SERVER["HTTP_HOST"];
//echo "HTTP_HOST".$url.'<br>';
//preg_match("/[a-z0-9]*\.[a-z0-9]*$/",$url,$url1);
//preg_match("/[0-9]+-[0-9]+/",$url,$match);
//list($keys_num, $city_num) = split('-', $match[0]);

$url = $_SERVER["REQUEST_URI"];
echo "REQUEST_URI".$url.'<br>';
preg_match("/[\-a-zA-Z0-9]+\/[\-a-zA-Z0-9]*/",$url,$request_uri);
echo "request_uri".$request_uri[0].'<br>';
echo $request_uri[0].'<br>';

if(count($request_uri)>=1){
	list($url_region,$url_city) = explode('/', $request_uri[0]);
}

echo "url_region".$url_region.'<br>';
echo "url_city".$url_city.'<br>';

//обрабатываем запрос генерации урлов

$template=file_get_contents("main_region.html");	

if( $url_city && is_numeric($url_city) && $url_region){
	$template = null;
	$page = $url_city;
	$current_page = "REGION_PAGE_PAGING";
	echo "REGION_PAGE_PAGING";
} elseif ($url_city && $url_region){
	$template = null;
	$current_page = "CITY_PAGE";
	echo "CITY_PAGE";
} elseif(!$url_city && $url_region){
	$template = file_get_contents("tmpl_region.html");
	$current_page = "REGION_PAGE";
	echo "REGION_PAGE";
} elseif($url_city == 'index.php' && !$url_region){
	//TODO Обработка региона
	$template=file_get_contents("tmpl_main.html");
	$current_page = "MAIN_PAGE";
	echo "MAIN_PAGE";
}else{
	$template=file_get_contents("tmpl_main.html");
	$current_page = "MAIN_PAGE";
	echo "MAIN_PAGE";
}
	
//замена макросов в шаблоне с обработкой главной страницы
if ($url==$url1[0])	
{
	$template=preg_replace("/\[CITY\]/", "Москва", $template);
	$template=preg_replace("/\[KEY\]/", "Кредиты и займы онлайн", $template);
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
$template=preg_replace("/\[LINKS\]/", "$random", $template);

//fetch regions
$con=mysqli_connect("localhost","root","hw6cGD6X","doorgen_banks");
echo "Connecting...";
if (mysqli_connect_errno())
{
  echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

//mysql_query("set character_set_client='utf8'");
//mysql_query("set character_set_results='utf8'");
//mysql_query("set collation_connection='utf8_general_ci'");
echo "<rb>".$current_page."<rb>";
if($current_page == "MAIN_PAGE"){
	echo "Main page processing...";
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
		if($posted != 0 && ($posted%$reg_per_section == 0)){
			$template=preg_replace("/\[REGIONS_".$page."\]/", $regions, $template);
			$regions = "";
			$page = $page+1;
		}
		$posted = $posted + 1;
		$regions = $regions."<a href = \"/".str_replace(" ","-",$row['region_name_latin'])."/\">".$row['region_name']."</a>&nbsp;";
	}
}

#http://php.net/manual/en/mysqli.prepare.php
if($current_page == "REGION_PAGE"){
	echo "Region page processing...";
	//prepare statement
	if (!($stmt = mysqli_prepare($con,"SELECT c.city_name, c.city_name_latin, ek.key_value, ek.key_value_latin, r.region_name, r.region_name_latin FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND r.region_name_latin like replace(LOWER(?),'-','_') AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id"))) {
		echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
	}
	
	//set values
	echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $url_region)) {
		echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
	}
	
	echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
	}

    /* instead of bind_result: */
	echo "get result...";
    if(!mysqli_stmt_bind_result($stmt, $city_name,$city_name_latin,$key_value, $key_value_latin, $region_name, $region_name_latin)){
		echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
	}
	
	/* now you can fetch the results into an array - NICE */
	echo "print...";
	$num_rows = mysqli_num_rows($result);
	echo "Num rows: " . $num_rows;
	
    while (mysqli_stmt_fetch($stmt)) {
        // use your $myrow array as you would with any other fetch
        echo "City name: ".$city_name."; key: ".$key_value;
    }
	
	$result = mysqli_query($con,"SELECT count(*) FROM `city` c, `city_page` cp, `region` r, `extra_key` ek WHERE 1 AND r.region_name_latin like replace(LOWER(?),'-','_') AND c.city_id = cp.city_id AND c.region_id = r.region_id AND ek.key_id = cp.key_id");
	
	/* explicit close recommended */
	mysqli_stmt_close($stmt);
}

mysqli_close($con);

$template=preg_replace("/\[REGIONS_".$page."\]/", $regions, $template);

echo $template;	
?>