<?php
class TitleGenerator
{
	function getRandomTitle($con,$region_name)
	{
		$query_city_list = "select c.case_value from `doorgen_banks`.`case` c where c.location_type_code_value = 2 AND c.case_code_value = 7 and c.location_id = (select region_id from `doorgen_banks`.`region` r where r.region_name_latin = replace(LOWER(?),'-','_')");
		#echo "query_city_list: ".$query_city_list."<br>";
		if (!($stmt = mysqli_prepare($con,$query_city_list))) {
			echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		//set values
		echo "set value...";
		$id=1;
		if (!mysqli_stmt_bind_param($stmt, "s", $region_name)) {
			echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		echo "execute...";
		if (!mysqli_stmt_execute($stmt)){
			echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}

		/* instead of bind_result: */
		echo "get result...";
		if(!mysqli_stmt_bind_result($stmt, $region_name)){
			echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error();
		}
		
		$index = 1;
		while (mysqli_stmt_fetch($stmt)) {
			// use your $myrow array as you would with any other fetch
			echo "City name: ".$city_name."; key: ".$key_value;
			$city_href = "<a href = \"/".str_replace(" ","-",$region_name_latin)."/".str_replace(" ","-",$city_name_latin." ".$key_value_latin).".html\">".$city_name." ".$key_value."</a>&nbsp;";
			$template=preg_replace("/\[CITY_NEWS_".$index."\]/", $city_href, $template);
			$index = $index+1;
		}

		$pager = new Pager;
		$template=preg_replace("/\[PAGER\]/", $pager->getPageNavigation("/".str_replace(" ","-",$region_name_latin)."/",$city_news_page_number, $max_page_number), $template);
		
		/* explicit close recommended */
		mysqli_stmt_close($stmt);
	}
}
?>