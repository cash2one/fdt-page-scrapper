<?php

class ImagesMail
{
	# Функция парсинга картинок из ImagesMail.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://go.mail.ru/search_images?q=$query";
		$html = $F->GetHTML($url, 'go.mail.ru');

		if (!is_bool($html))
		{
			foreach ($html->find('a[class="pic"]') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('img', 0)->src;
					$images[$i]['large'] = $e->find('img', 0)->rel;
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check ImagesMail images plugin.");
		}

		return $images;
	}
}