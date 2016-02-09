<?php

class Sweetim
{
	# Функция парсинга выдачи из Sweetim.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.sweetim.com/search.asp?q=$query&ln=$language";
		$html = $F->GetHTML($url, 'search.sweetim.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[id="result1_ltr"] div div[class="resP"]') as $e)
			{
				$t = 'a';
				$d = 'div';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($e->find($d, 0)->innertext))
				{
					preg_match('|</div>(.+?)$|', $e->find($d, 0)->innertext, $matches);
					$description = (!empty($matches[1])) ? strip_tags($matches[1]) : '';
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
			$F->Error("Can't create outgoing request. Please check Sweetim snippets plugin.");
		}

		return $snippets;
	}
}