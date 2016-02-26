<?php

class Blekko
{
	# Функция парсинга картинок из Blekko.
	public function Start($string, $count, $F)
	{
		$images = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://blekko.com/ws/$query/images";
		$html = $F->GetHTML($url, 'blekko.com');

		if (!is_bool($html))
		{
			foreach ($html->find('div[class="images"] div') as $i => $e)
			{
				if (!empty($e->innertext))
				{
					if ($i < $count)
					{
						$images[$i]['title'] = $F->GetKeyword();
						preg_match("|'(.+?)'|", $e->find('a', 0)->style, $small);
						$images[$i]['small'] = urldecode($small[1]);
						$images[$i]['large'] = urldecode($e->find('a', 0)->href);
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
			$F->Error("Can't create outgoing request. Please check Blekko images plugin.");
		}

		return $images;
	}
}