<?php

class WebCrawler
{
	# Функция парсинга картинок из WebCrawler.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.webcrawler.com/search/images?q=$query";
		$html = $F->GetHTML($url, 'www.webcrawler.com');

		if (!is_bool($html))
		{
			foreach ($html->find('div[id="resultsMain"] div[class="searchResult imageResult"]') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('div a img', 0)->src;
					preg_match('|du=(.+?)&|', $e->find('div a', 0)->href, $large);
					$images[$i]['large'] = urldecode($large[1]);
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check WebCrawler images plugin.");
		}

		return $images;
	}
}