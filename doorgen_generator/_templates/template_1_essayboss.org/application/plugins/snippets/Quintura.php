<?php

class Quintura
{
	# Функция парсинга выдачи из Quintura.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.quintura.com/?request=$query";
		$html = $F->GetHTML($url, 'www.quintura.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('li') as $e)
			{
				$t = 'p[class="title"] a';
				$d = 'p[class="annotation"]';

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
			$F->Error("Can't create outgoing request. Please check Quintura snippets plugin.");
		}

		return $snippets;
	}
}