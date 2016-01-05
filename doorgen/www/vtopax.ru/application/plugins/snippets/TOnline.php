<?php

class TOnline
{
	# Функция парсинга выдачи из TOnline.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.suche.t-online.de/fast-cgi/tsc?q=$query&language=$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.suche.t-online.de');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('li[class="tsc_Content"]') as $e)
			{
				$t = 'div a span';
				$d = 'span[class="tsc_result_teaser"]';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($e->find($d, 0)->plaintext))
				{
					$description = $e->find($d, 0)->plaintext;
				}

				if ($i < $count)
				{
					if (!empty($title) and !empty($description))
					{
						$snippets[$i]['title'] = trim($title);
						$snippets[$i]['description'] = trim($description);
						$i++;
					}
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check TOnline snippets plugin.");
		}

		return $snippets;
	}
}