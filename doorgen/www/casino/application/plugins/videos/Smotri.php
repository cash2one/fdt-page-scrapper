<?php

class Smotri
{
	# Функция парсинга видео из Smotri.
	function Start($string, $count, $F)
	{
		$videos = array();

		$query = urlencode($string);
		$url = "http://smotri.com/search/quick/?type_search=1&q=$query";
		$html = $F->GetHTML($url, 'smotri.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="video-list-item"]') as $e)
			{
				if ($i < $count)
				{
					$videos[$i]['title'] = $F->GetKeyword();
					$videos[$i]['src'] = $e->find('div[class="frameImg-video"] a img', 0)->src;
					preg_match('|=(.+?)#|', $e->find('div[class="frameImg-video"] a', 0)->href, $id);
					$videos[$i]['url'] = "http://pics.smotri.com/player.swf?file=$id[1]";
					$i++;
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check Smotri videos plugin.");
		}

		return $videos;
	}
}