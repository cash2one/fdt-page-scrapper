<?php

class ImagesGoogle
{
	# Функция парсинга картинок из ImagesGoogle.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://images.google.com/search?tbm=isch&q=$query&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'images.google.com');

		if (!is_bool($html))
		{
			foreach ($html->find('table[class="images_table"] td') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('a img', 0)->src;
					preg_match('|imgurl=(.+?)&|', $e->find('a', 0)->href, $large);
					$images[$i]['large'] = urldecode($large[1]);
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check ImagesGoogle images plugin.");
		}

		return $images;
	}
}