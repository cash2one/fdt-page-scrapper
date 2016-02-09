<?php

class VideoMail
{
	# Функция парсинга видео из VideoMail.
	function Start($string, $count, $F)
	{
		$videos = array();

		$query = urlencode($string);
		$url = "http://video.mail.ru/cgi-bin/photo/lvsearch?q=$query";
		$html = $F->GetHTML($url, 'video.mail.ru');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('li[class="preview-i"]') as $e)
			{
				if ($i < $count)
				{
					$videos[$i]['title'] = $F->GetKeyword();
					$videos[$i]['src'] = $e->find('div[class="preview"] img', 0)->src;
					$href = $e->find('a[class="preview_name"]', 0)->href;
					$url = preg_replace('|\/\$|', '/',substr(str_replace('/', '/$', $href), 0, -5), 5);
					$videos[$i]['url'] = "http://img.mail.ru/r/video2/uvpv3.swf?par=$url&autoplay=0";
					$i++;
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check VideoMail videos plugin.");
		}

		return $videos;
	}
}