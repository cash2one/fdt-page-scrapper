<?php

class TitleGenerator
{
	
	#return "<a href =\"/\">".Главная."</a>&nbsp;>&nbsp;<a href =\"#\">".$region_name."</a>&nbsp;";
	function getRandomTitle($url,$page_number, $max_page_number)
	{
		$phrase_array = array("взять кредит", "получить кредит", "оформить кредит", "онлайн кредит", "получить займ", "взять займ");
		$phrase_end = "у нас на сайте";
		
		if($page_number == 1 && $max_page_number == 1){
			return "";
		}elseif($page_number == $max_page_number){
			return "<a href =\"".$url.($page_number-1)."/\">".$prev_page_label."</a>";
		}elseif($page_number < $max_page_number && $page_number != 1){
			return "<a href =\"".$url.($page_number-1)."/\">".$prev_page_label."</a>&nbsp;<a href =\"".$url.($page_number+1)."/\">".$next_page_label."</a>";
		}elseif($page_number < $max_page_number && $page_number == 1){
			return "<a href =\"".$url.($page_number+1)."/\">".$next_page_label."</a>";
		}
	}
}
#$pager = new Pager; 
#echo $pager->getPageNavigation("http://url/",1,1)."\n";
#echo $pager->getPageNavigation("http://url/",3,3)."\n";
#echo $pager->getPageNavigation("http://url/",2,3)."\n";
#echo $pager->getPageNavigation("http://url/",1,3)."\n";
?>