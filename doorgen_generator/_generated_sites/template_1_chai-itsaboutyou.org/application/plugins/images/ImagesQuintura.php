<?php

class ImagesQuintura
{
	# Функция парсинга картинок из ImagesQuintura.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.quintura.com/?request=$query&tab=1&page=1";
		$html = $F->GetHTML($url, 'www.quintura.com');

		if (!is_bool($html))
		{
			foreach ($html->find('ol[class="en"] li') as $i => $e)
			{
				if ($i < $count)
				{
					$images[$i]['title'] = $F->GetKeyword();
					$images[$i]['small'] = $e->find('a img', 0)->src;
					$images[$i]['large'] = $e->find('input', 0)->value;
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check ImagesQuintura images plugin.");
		}

		return $images;
	}
}