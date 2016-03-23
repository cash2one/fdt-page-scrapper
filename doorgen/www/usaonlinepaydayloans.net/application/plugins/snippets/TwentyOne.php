<?php

class TwentyOne
{
	# Функция парсинга выдачи из TwentyOne.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.21.by/search.php?subject=web&engine=Mixed&query=$query";
		$html = $F->GetHTML($url, 'search.21.by');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('table[class="results"] td') as $e)
			{
				$t = 'div[class="name"]';
				$d = 'div';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($e->find($d, 1)->plaintext))
				{
					$description = $e->find($d, 1)->plaintext;
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
			$F->Error("Can't create outgoing request. Please check TwentyOne snippets plugin.");
		}

		return $snippets;
	}
}