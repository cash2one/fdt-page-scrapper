<?php

class Lexxe
{
	# Функция парсинга выдачи из Lexxe.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.lexxe.com/search?sstring=$query";
		$html = $F->GetHTML($url, 'www.lexxe.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('p[class="result"]') as $e)
			{
				$t = 'span[class="resTitle"] a';
				$d = 'span[class="resTeaser"]';

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
			$F->Error("Can't create outgoing request. Please check Lexxe snippets plugin.");
		}

		return $snippets;
	}
}