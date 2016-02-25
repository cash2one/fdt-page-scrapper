<?php

class Community
{
	# Функция формирования комментариев.
	public function Start($F)
	{
		$avatars = glob('txt/avatars/*.*', GLOB_NOSORT);
		$nicks = $F->GetContents('txt/nicknames.txt');
		$texts = $F->GetContents('txt/comments.txt');

		$comments = array();

		if (!empty($avatars) and !empty($nicks) and !empty($texts))
		{
			for ($i = 0; $i < 20; $i++)
			{
				$avatar = array_rand($avatars);
				$nick = array_rand($nicks);
				$text = array_rand($texts);

				$comments[$i]['avatar'] = str_replace(' ', '%20', $avatars[$avatar]);
				$comments[$i]['nick'] = $nicks[$nick];
				$comments[$i]['text'] = $texts[$text];
			}
		}
		else
		{
			$F->Error("Folder txt/avatars and files txt/nicknames.txt, txt/comments.txt can't be empty.");
		}

		return $comments;
	}
}