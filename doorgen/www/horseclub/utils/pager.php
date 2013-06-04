<?php

class Pager
{
	#return "<a href =\"/\">".Главная."</a>&nbsp;>&nbsp;<a href =\"#\">".$region_name."</a>&nbsp;";
	function getPageNavigation($url,$page_number, $max_page_number)
	{
		$next_page_label = "Следующая";
		$prev_page_label = "Предыдущая";
		
		if($page_number == 1 && $max_page_number == 1){
			//если только одна страница
			return "";
		}elseif($page_number == $max_page_number){
			//только предыдущая
			return "<a href =\"".(($page_number-1==1)?$url:$url.($page_number-1)."/")."\">".$prev_page_label."</a>";
		}elseif($page_number < $max_page_number && $page_number != 1){
			return "<a href =\"".(($page_number-1==1)?$url:$url.($page_number-1)."/")."\">".$prev_page_label."</a>&nbsp;<a href =\"".$url.($page_number+1)."/\">".$next_page_label."</a>";
		}elseif($page_number < $max_page_number && $page_number == 1){
			//если страница первая
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