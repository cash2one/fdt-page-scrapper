<?php

class ImagesRambler
{
	# Функция парсинга картинок из ImagesRambler.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://images.rambler.ru/srch?query=$query";
		$html = $F->GetHTML($url, 'images.rambler.ru');

		if (!is_bool($html))
		{
			foreach ($html->find('ul["pix_1"] li[class="b-serp__list_item"]') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('span a img', 0)->src;
					$images[$i]['large'] = $e->find('span a', 0)->href;
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check ImagesRambler images plugin.");
		}

		return $images;
	}
}