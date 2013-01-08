<?php

	# Функция копирования файлов.
	function CopyDir($source, $target)
	{
		if (is_dir($source))
		{
			@mkdir($target);
			$d = dir($source);

			while (FALSE !== ($entry = $d->read()))
			{
				if ($entry == '.' || $entry == '..') continue;

				CopyDir("$source/$entry", "$target/$entry");
			}

			$d->close();
		}
		else
		{
			copy($source, $target);
		}
	}

	CopyDir('base', 'content/base');
	CopyDir('content_cache', 'content/cache');