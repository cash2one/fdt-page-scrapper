<?php
error_reporting(E_ALL ^ E_NOTICE);

require_once "utils/config.php";

//default offset for moskow
function engdate($d, $format = 'jS \of F h:i:s A', $offset = -8)
{
    $d += 3600 * $offset;
    return date($format, $d);
}

function fillArticleList($con)
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
	
	$mainUrl = $_SERVER["HTTP_HOST"];
	
	while($row = mysqli_fetch_array($result))
	{
		global $regionName, $regionNameLatin;
		$text = $row['text'];
		$title = $row['title'];
		$url= $row['url'];
		$post_dt = $row['posted_time'];
		$upd_flg = $row['upd_flg'];
		$updTitle = "";
		
		/*if($cur_news_posted_time != engdate($post_dt,'jS \of F')){
			$cur_news_posted_time = engdate($post_dt,'jS \of F');
			$atricles = $atricles."<br/><h3>".$cur_news_posted_time."</h3>";
		}
		
		if($upd_flg == 1){
			 $updTitle = "Information updated | ";
		}*/
		
		
		//$atricles = $atricles. "$mainUrl/articles/$url;" . $title .";".engdate($post_dt,'jS \of F, h:i:s A')."\r\n";
		$atricles = $atricles. "https://$mainUrl/articles/$url"."\r\n";
		/*$atricles = preg_replace("/\[STATE_NAME\]/", $regionName , $atricles);
		$atricles = preg_replace("/\[STATE_ABBR\]/", $regionName , $atricles);*/
	}
	
	
	return $atricles;
}

$con=mysqli_connect(DB_HOST,DB_USER_NAME,DB_USER_PWD,DB_NAME);
#echo "Connecting...";
if (mysqli_connect_errno())
{
	#echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

mysqli_query($con,"set character_set_client='utf8'");
mysqli_query($con,"set character_set_results='utf8'");
mysqli_query($con,"set collation_connection='utf8_general_ci'");

$template = fillArticleList($con);

$file = 'articles_posted.txt';

// Пишем содержимое обратно в файл
file_put_contents($file, $template);

mysqli_close($con);

#echo $template;	
?>
