<?php
class YaNewsExtractor
{
	function getYandexNewsContent($function)
	{
		$news_message = "<div><style>span.yandex_date {font-size: 85%; margin-right:0.5em;} div.yandex_informer	{font-size: 85%; margin-bottom: 0.3em;} .yandex_title 	{font-size: 100%; margin-bottom: 0.5em; color: #EF6F53 }	.yandex_title a	{ }	div.yandex_allnews	{font-size: 80%; margin-top: 0.3em;} div.yandex_allnews	{font-size: 80%; margin-top: 0.3em;}	div.yandex_annotation		{font-size: 85%; margin-bottom: 0.5em;}</style><div class=yandex_title><b><h3>Последние новости<h3></b></div>";
		#echo "news_message: ".$news_message;
		$news_content = "";
		$needed = "var m_finances = new Array()";
		$lines = array();
		
		while(count($lines) != 6){
			$news_content = $function->GetHTML('http://news.yandex.ru/ru/finances5.utf8.js','news.yandex.ru');
			$lines = preg_split('/m\_finances\[m\_finances\.length\]\=new\ f/', $news_content);
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