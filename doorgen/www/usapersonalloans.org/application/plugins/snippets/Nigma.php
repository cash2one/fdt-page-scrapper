<?php

class Nigma
{
	# Функция парсинга выдачи из Nigma.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.nigma.ru/?s=$query&lang=$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.nigma.ru');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[id="results"] ol li') as $e)
			{
				$t = 'div[class="snippet_title"] a';
				$d = 'div[class="snippet_text"]';

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
			$F->Error("Can't create outgoing request. Please check Nigma snippets plugin.");
		}

		return $snippets;
	}
}