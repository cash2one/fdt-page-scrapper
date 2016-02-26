<?php

class Youtube
{
	# Функция парсинга выдачи из Youtube.
	function Start($string, $count, $F)
	{
		$videos = array();

		$query = urlencode($string);
		$url = "http://www.youtube.com/results?search_query=$query";
		$html = $F->GetHTML($url, 'www.youtube.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ol[id="search-results"] li[class="result-item-video"]') as $e)
			{
				if ($i < $count)
				{
					$id = substr($e->find('a[class="result-item-thumb"]', 0)->href, 9);
					$videos[$i]['title'] = $F->GetKeyword();
					$videos[$i]['src'] = "http://img.youtube.com/vi/$id/0.jpg";
					$videos[$i]['url'] = "http://www.youtube.com/embed/$id";
					$i++;
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check Youtube videos plugin.");
		}

		return $videos;
	}
}