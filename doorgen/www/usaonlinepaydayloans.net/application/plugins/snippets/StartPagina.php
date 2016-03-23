<?php

class StartPagina
{
	# Функция парсинга выдачи из StartPagina.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://startgoogle.startpagina.nl/index.php?q=$query";
		$html = $F->GetHTML($url, 'startgoogle.startpagina.nl');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('td p[class="g"]') as $e)
			{
				$t = 'a';
				$d = 'font';

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
			$F->Error("Can't create outgoing request. Please check StartPagina snippets plugin.");
		}

		return $snippets;
	}
}