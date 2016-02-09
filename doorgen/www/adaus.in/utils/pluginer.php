<?php

	set_time_limit(600);

	require_once "../application/models/functions.php";

	$F = new Functions('../');

	if (!isset($_COOKIE['password']) or $_COOKIE['password'] !== PASSWORD)
	{
		exit('Please login to your dashboard!');
	}

	function check($boolean, $message)
	{
		echo '<div style="display:inline-block; margin: 25px 0 0 25px; font:14px/1em Tahoma">';
		echo $boolean ? '<span style="padding-right:5px;font-weight:bold;color:green;">[OK]</span>  ' . $message : '<span style="padding-right:5px;font-weight:bold;color:red;">[FAIL]</span>  ' . $message;
		echo '</div>';
	}

	$plugins = glob('../application/plugins/snippets/*.php');
	if (!empty($plugins))
	{
		foreach ($plugins as $plugin)
		{
			$stime = microtime();
			$start = implode(array_reverse(explode(' ', substr($stime, 1))));

			$plugin = pathinfo($plugin, PATHINFO_FILENAME);
			$o = $F->GetPluginObject($plugin, 'snippets');
			$snippet = $o->Start('php', 'ru', 1, $F);

			if (!empty($snippet))
				check(true, $plugin);
			else
				check(false, $plugin);

			$etime = microtime();
			$end = implode(array_reverse(explode(' ', substr($etime, 1))));
			$time = $end - $start;

			echo '<pre style="display:inline-block; margin: 25px 0 0 20px;"><span style="color:blue;">'; printf("%.4f seconds", $time); echo '</span></pre><br>';
		}
	}