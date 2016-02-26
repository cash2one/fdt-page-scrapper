<?php

class PicSearch
{
	# Функция парсинга картинок из PicSearch.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.picsearch.com/index.cgi?q=$query";
		$html = $F->GetHTML($url, 'www.picsearch.com');

		if (!is_bool($html))
		{
			foreach ($html->find('div[id="results_table"] a') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('img', 0)->src;
					$href = $F->GetHTML("http://www.picsearch.com$e->href", 'www.picsearch.com');
					$images[$i]['large'] = $href->find('div[class="detail-links"] a', 1)->href;
				}
			}

			$html->clear(); $href->clear();
			$html = null; $href = null; $e = null;
			unset($html, $href, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check PicSearch images plugin.");
		}

		return $images;
	}
}