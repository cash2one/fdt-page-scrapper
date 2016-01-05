<?php
class TitleGenerator
{
	/*function getRandomString($elements_array)
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
	}*/

	function getRandomTitle()
	{
		$phrase_array = array("Взять кредит", "Получить кредит", "Оформить кредит", "Онлайн кредит", "Получить займ", "Взять займ");
		$phrase_end = "у нас на сайте";

		$phrase = "";
		$size = count($phrase_array);
		$rand_phrase_index = rand(0,$size-1);
		$phrase = $phrase_array[$rand_phrase_index].' '.$phrase_end;
		return $phrase;
	}

	function getCityRandomTitle($mix_region, $anchor_value)
	{
		$phrase_end = "| [REGION_NAME] - ".$mix_region;
		$phrase =  $anchor_value.' '.$phrase_end;
		return $phrase;
	}
}
?>