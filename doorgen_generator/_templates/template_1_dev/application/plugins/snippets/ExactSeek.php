<?php

class ExactSeek
{
	# Функция парсинга выдачи из ExactSeek.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://web1.exactseek.com/webclient/?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'web1.exactseek.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="results"] ol li p') as $e)
			{
				$t = 'a[class="title"]';
				$d = 'arr[name="content"] str';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = html_entity_decode($e->find($t, 0)->plaintext, ENT_QUOTES, 'UTF-8');
				}

				if (isset($e->find($d, 0)->plaintext))
				{
					$description = html_entity_decode($e->find($d, 0)->plaintext, ENT_QUOTES, 'UTF-8');
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
			$F->Error("Can't create outgoing request. Please check ExactSeek snippets plugin.");
		}

		return $snippets;
	}
}