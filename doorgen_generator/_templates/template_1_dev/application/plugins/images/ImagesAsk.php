<?php

class ImagesAsk
{
	# Функция парсинга картинок из ImagesAsk.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.ask.com/pictures?q=$query";
		$html = $F->GetHTML($url, 'www.ask.com');

		if (!is_bool($html))
		{
			foreach ($html->find('div[id="imagegrid"] li') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('a img', 0)->src;
					preg_match('|imagesrc=(.+?)&|', urldecode($e->find('a', 0)->href), $large);
					$images[$i]['large'] = urldecode($large[1]);
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check ImagesAsk images plugin.");
		}

		return $images;
	}
}