<?php
require_once "config.php";

class YaNewsExtractor
{
	function isNewsUpdateNeed($news_dir){
		// открываем папку
		$dh = opendir($news_dir);
		$result_file_name = "";
		$last_change_date_result_file = 0;
		while($filename = readdir($dh)) {
			if($filename != "." && $filename != ".." && $filename != ".htaccess" && $last_change_date_result_file < filemtime($news_dir.$filename)){
				$last_change_date_result_file = filemtime($news_dir.$filename);
				$result_file_name = $news_dir.$filename;
				#echo $result_file_name;
			}
		}
		closedir($dh);
		
		//check last news update
		#echo "time: ".time()."; last_change_date_result_file: ".$last_change_date_result_file;
		if( $last_change_date_result_file == 0 || ((time()-$last_change_date_result_file) > 600000)){
			//delete all previous news
			#echo "Удаление старых новостей";
			$dh = opendir($news_dir);
			while(false !== ($filename = readdir($dh))){
				#echo "Удаление старого файла...";
                if($filename != "." && $filename != ".." && $filename != ".htaccess" ) unlink($news_dir.$filename);
			}
			closedir($dh);
			#echo "Need_news_update";
			return "";
		}else{
			#echo "NO_need_news_update";
			return $result_file_name;
		}
				
	}
	
	function saveNewsFile($news_dir,$news_content){
		$fp = fopen($news_dir.time(), 'w');
		
		$test = fwrite($fp, $news_content); // Запись в файл
		/*if ($test) {
			echo 'Данные в файл успешно занесены.';
		}else{
			echo 'Ошибка при записи в файл.';
		}*/
		
		fclose($fp); //Закрытие файла
	}
	
	/*function readNewsFile($news_file_name){
		// открываем папку
		$result_file_name = "";
		$last_change_date_result_file = 0;
		$dh = opendir($news_dir);
		while($filename = readdir($dh)) {	
			if($last_change_date_result_file > filectime($filename)){
				$last_change_date_result_file = filectime($filename);
				$result_file_name = $filename;
			}
		}
		closedir($dh);
		//return news content
		return file($result_file_name);
	}*/
	
	function getYandexNewsContent($function)
	{
		$news_message = "<div><style>span.yandex_date {font-size: 85%; margin-right:0.5em;} div.yandex_informer	{font-size: 85%; margin-bottom: 0.3em;} .yandex_title 	{font-size: 100%; margin-bottom: 0.5em; color: #EF6F53 }	.yandex_title a	{ }	div.yandex_allnews	{font-size: 80%; margin-top: 0.3em;} div.yandex_allnews	{font-size: 80%; margin-top: 0.3em;}	div.yandex_annotation		{font-size: 85%; margin-bottom: 0.5em;}</style><div class=yandex_title><b><h3>Последние новости<h3></b></div>";
		#echo "news_message: ".$news_message;
		$news_content = "";
		$needed = "var m_finances = new Array()";
		$lines = array();
		
		preg_match("@^http://news\.yandex\.ru/ru/(.*)5\.utf8\.js@",NEWS_FEED,$request_uri);
		$news_content_field = $request_uri[1];
		
		$attempCnt = 0;
		while(count($lines) != 6 && $attempCnt < 5){
		    $attempCnt = $attempCnt + 1;
			$news_content = $function->GetHTML(NEWS_FEED,NEWS_FEED_DOMAIN);
			$split_expr = '/m\_'.$news_content_field.'\[m\_'.$news_content_field.'\.length\]\=new\ f/';
			$lines = preg_split($split_expr, $news_content);
			#echo var_dump($lines);
		}
		
		//needed lines: 1,2,3,4,5
		for($i=1; $i <=5; $i++){
			#echo "<br/>i: ".$i."<br/>";
			/*
			[0] - title (+3)
			[2] - date (+1)
			[3] - time (+1)
			[4] - description (+1)
			*/
			$news_array = explode("', ",$lines[$i]);
			#echo var_dump($news_array);
			#echo "<br/>";
			$title = substr($news_array[0],3);
			$date = substr($news_array[2],1);
			$time = substr($news_array[3],1);
			$description = substr($news_array[4],1);
			$news_message = $news_message.'<div><span class=yandex_date>'.$date.'&nbsp;'.$time.'</span><span class=yandex_title>'.$title.'</span></div><div class=yandex_annotation>'.$description.'</div>';
		}
		
		return $news_message;
	}
}
?>