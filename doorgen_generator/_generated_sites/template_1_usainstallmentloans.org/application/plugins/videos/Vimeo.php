<?php

class Vimeo
{
	# Функция парсинга видео из Vimeo.
	function Start($string, $count, $F)
	{
		$videos = array();

		$query = urlencode($string);
		$url = "http://vimeo.com/search?q=$query";
		$html = $F->GetHTML($url, 'vimeo.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ol[id="browse_list"] li') as $e)
			{
				if ($i < $count)
				{
					$videos[$i]['title'] = $F->GetKeyword();
					$videos[$i]['src'] = $e->find('a img', 0)->src;
					$id = $e->find('a', 0)->href;
					$videos[$i]['url'] = "http://player.vimeo.com/video$id";
					$i++;
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check Vimeo videos plugin.");
		}

		return $videos;
	}
}