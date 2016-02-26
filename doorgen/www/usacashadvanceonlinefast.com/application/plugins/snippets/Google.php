<?php

class Google
{
	# Функция парсинга выдачи из Google.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.google.com/search?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.google.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('li[class="g"]') as $e)
			{
				$t = 'h3[class="r"] a';
				$d = 'div[class="s"] span[class="st"]';

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
			$F->Error("Can't create outgoing request. Please check Google snippets plugin.");
		}

		return $snippets;
	}
}