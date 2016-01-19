<?php

class Qip
{
	# Функция парсинга картинок из Qip.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://photo.search.qip.ru/search/?query=$query";
		$html = $F->GetHTML($url, 'photo.search.qip.ru');

		if (!is_bool($html))
		{
			foreach ($html->find('div[id="imgContainer"] div[class="imgBox"]') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('div[class="video"] a img', 0)->src;
					preg_match('|l=(.+?)&|', urldecode($e->find('div[class="video"] a', 0)->href), $large);
					$images[$i]['large'] = 'http://' . urldecode($large[1]);
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check Qip images plugin.");
		}

		return $images;
	}
}