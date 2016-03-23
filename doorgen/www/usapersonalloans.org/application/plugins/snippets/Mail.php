<?php

class Mail
{
	# Функция парсинга выдачи из Mail.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://go.mail.ru/search?q=$query";
		$html = $F->GetHTML($url, 'go.mail.ru');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('li[class="res"]') as $e)
			{
				$t = 'h3[class="res-head"] a';
				$d = 'div[class="snip"]';

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
			$F->Error("Can't create outgoing request. Please check Mail snippets plugin.");
		}

		return $snippets;
	}
}