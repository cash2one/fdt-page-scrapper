<?php
class TitleGenerator
{
	function getRegionRandomTitle()
	{
		$phrase_array = array("взять кредит", "получить кредит", "оформить кредит", "онлайн кредит", "получить займ", "взять займ");
		$phrase_end = "у нас на сайте";
		
		$size = count($phrase_array);
		$phrase = "";
		while($size > 0) {
			$rand_phrase_index = rand(0,$size-1);
			if($size != 1){
				$phrase = $phrase.$phrase_array[$rand_phrase_index].', ';
			}else{
				$phrase = $phrase.$phrase_array[$rand_phrase_index].' '.$phrase_end;
			}
			
			array_splice($phrase_array, $rand_phrase_index, 1);
			$size = count($phrase_array);
		}
		return $phrase;
	}
	
	function getCityRandomTitle()
	{
		$phrase_array = array("Частный кредит [CITY_CASE_1_7]","Частный кредит в городе [CITY_CASE_1_1]","Частный кредит в г.[CITY_CASE_1_7]","Частный кредит [CITY_CASE_1_1]");
		$phrase_end = "[REGION_NAME]";
		
		$size = count($phrase_array);
		$phrase = "";
		while($size > 0) {
			$rand_phrase_index = rand(0,$size-1);
			if($size != 1){
				$phrase = $phrase.$phrase_array[$rand_phrase_index].', ';
			}else{
				$phrase = $phrase.$phrase_array[$rand_phrase_index].' '.$phrase_end;
			}
			
			array_splice($phrase_array, $rand_phrase_index, 1);
			$size = count($phrase_array);
		}
		return $phrase;
	}
}
?>