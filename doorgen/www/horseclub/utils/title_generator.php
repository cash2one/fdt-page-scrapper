<?php
class TitleGenerator
{
	function getRandomString($elements_array)
	{
		$phrase_array = $elements_array;
		
		$size = count($phrase_array);
		$phrase = "";
		while($size > 0) {
			$rand_phrase_index = rand(0,$size-1);
			if($size != 1){
				$phrase = $phrase.$phrase_array[$rand_phrase_index].', ';
			}else{
				$phrase = $phrase.$phrase_array[$rand_phrase_index];
			}
			
			array_splice($phrase_array, $rand_phrase_index, 1);
			$size = count($phrase_array);
		}
		return $phrase;
	}
	
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
		$phrase_array = array("Частный кредит [CITY_CASE_7]","Частный кредит в городе [CITY_CASE_1]","Частный кредит в г.[CITY_CASE_6]","Частный кредит [CITY_CASE_1]");
		$mix_region = $this->getRandomString(array("Кредиты в России","Банки России","Области","Регионы и Округи"));
		$phrase_end = "| [REGION_NAME] - ".$mix_region;
		
		$size = count($phrase_array);
		$phrase = "";
		$rand_phrase_index = rand(0,$size-1);
		$phrase = $phrase.$phrase_array[$rand_phrase_index].' '.$phrase_end;
		return $phrase;
	}
}
?>