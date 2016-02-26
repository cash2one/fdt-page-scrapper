<?php

class ImagesBing
{
	# Функция парсинга картинок из ImagesBing.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.bing.com/images/search?q=$query";
		$html = $F->GetHTML($url, 'www.bing.com');

		if (!is_bool($html))
		{
			foreach ($html->find('div[id="sg_results"] span[class="sg_pg"] span[class="sg_u"]') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('span[class="sg_cv"] a img', 0)->src;
					preg_match('|imgurl:"(.+?)",|', html_entity_decode($e->outertext), $large);
					$images[$i]['large'] = urldecode($large[1]);
					$i++;
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check ImagesBing images plugin.");
		}

		return $images;
	}
}