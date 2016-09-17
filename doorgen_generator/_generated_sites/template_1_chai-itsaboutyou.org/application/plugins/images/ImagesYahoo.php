<?php

class ImagesYahoo
{
	# Функция парсинга картинок из ImagesYahoo.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://images.search.yahoo.com/search/images?p=$query&fl=1&ei=utf-8";
		$html = $F->GetHTML($url, 'images.search.yahoo.com');

		if (!is_bool($html))
		{
			foreach ($html->find('li[class="ld"]') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('a img', 0)->src;
					preg_match('|imgurl=(.+?)&|', $e->find('a', 0)->href, $large);
					$images[$i]['large'] = 'http://' . urldecode($large[1]);
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check ImagesYahoo images plugin.");
		}

		return $images;
	}
}