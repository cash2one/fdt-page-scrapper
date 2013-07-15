<?php  
session_start();
require_once '../application/models/functions.php';
$my_function = new Functions('../');
if (!isset ($_COOKIE['password']) or $_COOKIE['password'] !== PASSWORD)
{
	if (isset ($_POST['password']))
	{
		$password_in_md5 = md5($_POST['password']);
		if ($password_in_md5 == PASSWORD)
		{
			$session_life_time = time() + 86400;
			setcookie('password', $password_in_md5, $session_life_time, '/');
			header('Location: dashboard.php');
		}
	}
}
$config_params = array ();
$config_file = file('../config.php');
foreach ($config_file as $config_file_element)
{
	preg_match_all('|"(.+?)"|', $config_file_element, $matches_array);
	if (!empty ($matches_array[1]))
	{
		$config_params[$matches_array[1][0]] = !empty ($matches_array[1][1]) ? $matches_array[1][1] : '';
	}
}
function Parameter($config_param_label, $_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a)
{
	$_f1344667fbc0e8e5cbf8715bd22e24ca5801bc99b1c59b6a11abafa0 = '<a href="http://autosed.com/manual/#' . strtolower($config_param_label) . '" target="_blank"><img src="data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAALEgAACxIB0t1+/AAAABZ0RVh0Q3JlYXRpb24gVGltZQAwNy8xNi8xMjRk/+8AAAAcdEVYdFNvZnR3YXJlAEFkb2JlIEZpcmV3b3JrcyBDUzVxteM2AAABG0lEQVQokZ2SoW6DUBSGP9gVICrIFBUTCLJkEosoCbJJVe0kdnXlEZBgeYumciTrC2ArJhAVJZkhS5u0WZowsV7SUliW/fK/57v3nP8epa5rpMIwGwEzYMK1FkAcRf5KGooEwzCLgRd+VxJF/qwB25CuC0xzAEBRVJ2wMp+/joA36bruA+Ox3VSV5Y40zTkcTpewd+e6zzHwKB0hVKrqSJYVgIJt37Pff7HZfF6ChmgHURRV056uCxzHRNNEu92J2pfCcDhgOn3ieDyR5+XNeS8YBA4AaZpTVYe/gZZloGmC5fKd7XbXebHKz+deSSa5Xn/0NbRQgbjrxSBwsCyjD4zV8xolfRUdSqLIX/175ZpwzoZHx8xnz5MQwDf542taOuWMsAAAAABJRU5ErkJggg=="></a>';
	switch ($config_param_label)
	{
		case 'LANGUAGE' :
			$xml =<<<XML<?xml version="1.0" encoding="UTF-8"?><methodCall><methodName>weblogUpdates.ping</methodName><params><param><value>$query</value></param><param><value>$url</value></param></params></methodCall> 
XML;
			$_ef88d01f391a9f53244d58c4facd028fc8717591 =<<<HTML<td><span>$config_param_label $_f1344667fbc0e8e5cbf8715bd22e24ca5801bc99b1c59b6a11abafa0</span></td><td><select name="$config_param_label"><option value="$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a">$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a</option>
HTML;
			$_0c681102171ef4e7866515ea28a20fb38ce683f6f519bbb62d70850502c17ad7dab00f9d01c12e472455f283a9652cbabf6182884feaff6352026dcb4fac7e2a = array (
				'ar',
				'bg',
				'id',
				'ca',
				'cs',
				'sr',
				'da',
				'de',
				'en',
				'es',
				'fr',
				'it',
				'lv',
				'lt',
				'hu',
				'nl',
				'no',
				'pl',
				'br',
				'hr',
				'ro',
				'sk',
				'sl',
				'fi',
				'sv',
				'ru',
				'th',
				'tl',
				'tr',
				'uk',
				'el',
				'iw',
				'hi',
				'vi',
				'cn',
				'b5',
				'jp',
				'kr'
			);
			unset ($_0c681102171ef4e7866515ea28a20fb38ce683f6f519bbb62d70850502c17ad7dab00f9d01c12e472455f283a9652cbabf6182884feaff6352026dcb4fac7e2a[array_search($_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a, $_0c681102171ef4e7866515ea28a20fb38ce683f6f519bbb62d70850502c17ad7dab00f9d01c12e472455f283a9652cbabf6182884feaff6352026dcb4fac7e2a)]);
			foreach ($_0c681102171ef4e7866515ea28a20fb38ce683f6f519bbb62d70850502c17ad7dab00f9d01c12e472455f283a9652cbabf6182884feaff6352026dcb4fac7e2a as $_be838e05)
			{
				$_ef88d01f391a9f53244d58c4facd028fc8717591 .=<<<HTML<option value="$_be838e05">$_be838e05</option>
HTML;
			}
			$_ef88d01f391a9f53244d58c4facd028fc8717591 .=<<<HTML </select></td>
HTML;
			return $_ef88d01f391a9f53244d58c4facd028fc8717591;
			break;
		case 'TEMPLATE' :
			foreach (glob('../templates/*', GLOB_ONLYDIR) as $_403a10fe6b60c5723057ad99b56c71531937dc7e200e4b0d11561000cb6d5e9e)
			{
				$_403a10fe6b60c5723057ad99b56c71531937dc7e200e4b0d11561000cb6d5e9e = pathinfo($_403a10fe6b60c5723057ad99b56c71531937dc7e200e4b0d11561000cb6d5e9e, PATHINFO_FILENAME);
				if ($_403a10fe6b60c5723057ad99b56c71531937dc7e200e4b0d11561000cb6d5e9e !== $_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a)
				{
					$_d5aca39dd5a5513e857577b1b1ad4f8bd9e1b722[] = $_403a10fe6b60c5723057ad99b56c71531937dc7e200e4b0d11561000cb6d5e9e;
				}
			}
			$_ef88d01f391a9f53244d58c4facd028fc8717591 =<<<HTML <td><span>$config_param_label $_f1344667fbc0e8e5cbf8715bd22e24ca5801bc99b1c59b6a11abafa0</span></td><td><select name="$config_param_label"><option value="$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a">$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a</option>
HTML;
			if (!empty ($_d5aca39dd5a5513e857577b1b1ad4f8bd9e1b722))
			{
				foreach ($_d5aca39dd5a5513e857577b1b1ad4f8bd9e1b722 as $_403a10fe6b60c5723057ad99b56c71531937dc7e200e4b0d11561000cb6d5e9e)
				{
					$_ef88d01f391a9f53244d58c4facd028fc8717591 .=<<<HTML <option value="$_403a10fe6b60c5723057ad99b56c71531937dc7e200e4b0d11561000cb6d5e9e">$_403a10fe6b60c5723057ad99b56c71531937dc7e200e4b0d11561000cb6d5e9e</option>
HTML;
				}
			}
			$_ef88d01f391a9f53244d58c4facd028fc8717591 .=<<<HTML </select></td>
HTML;
			return $_ef88d01f391a9f53244d58c4facd028fc8717591;
			break;
		case in_array($config_param_label, array (
				'SNIPPETS',
				'IMAGES',
				'VIDEOS',
				'PINGERS',
				'COMMENTS'
			)) :
			$_8662a077fa77c35e47dde5702d6eeef077b45b5e = explode(' | ', $_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a);
			foreach (glob('../application/plugins/' . strtolower($config_param_label) . '/*.php') as $_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4)
			{
				$_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4 = pathinfo($_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4, PATHINFO_FILENAME);
				if (!in_array($_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4, $_8662a077fa77c35e47dde5702d6eeef077b45b5e))
				{
					$_55dac98276c003764d486bf4476867a62772ffe64d10f81d01802dd7[] = $_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4;
				}
			}
			$_1a26c4802573853c0947945bb6c5bb611b06e05d5a309b85db987948 = $config_param_label . '[]';
			$_ef88d01f391a9f53244d58c4facd028fc8717591 =<<<HTML <td><span>$config_param_label $_f1344667fbc0e8e5cbf8715bd22e24ca5801bc99b1c59b6a11abafa0</span></td><td><p>
HTML;
			if (!empty ($_8662a077fa77c35e47dde5702d6eeef077b45b5e[0]))
			{
				foreach ($_8662a077fa77c35e47dde5702d6eeef077b45b5e as $_88360f1b54c8535feef4cc7f6ca7e7b19b48cce43674c934388fe396)
				{
					$_ef88d01f391a9f53244d58c4facd028fc8717591 .=<<<HTML <input id="$_88360f1b54c8535feef4cc7f6ca7e7b19b48cce43674c934388fe396" type="checkbox" name="$_1a26c4802573853c0947945bb6c5bb611b06e05d5a309b85db987948" value="$_88360f1b54c8535feef4cc7f6ca7e7b19b48cce43674c934388fe396" checked><label for="$_88360f1b54c8535feef4cc7f6ca7e7b19b48cce43674c934388fe396">$_88360f1b54c8535feef4cc7f6ca7e7b19b48cce43674c934388fe396</label><br>
HTML;
				}
			}
			if (!empty ($_55dac98276c003764d486bf4476867a62772ffe64d10f81d01802dd7))
			{
				foreach ($_55dac98276c003764d486bf4476867a62772ffe64d10f81d01802dd7 as $_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4)
				{
					$_ef88d01f391a9f53244d58c4facd028fc8717591 .=<<<HTML <input id="$_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4" type="checkbox" name="$_1a26c4802573853c0947945bb6c5bb611b06e05d5a309b85db987948" value="$_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4"><label for="$_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4">$_f389806657737af74ef14d2dac7d6312b78672f9965ff2f4</label><br>
HTML;
				}
			}
			$_ef88d01f391a9f53244d58c4facd028fc8717591 .=<<<HTML </p></td>
HTML;
			return $_ef88d01f391a9f53244d58c4facd028fc8717591;
			break;
		case in_array($config_param_label, array (
				'STOP_GROWING',
				'SUBJECT_LIMITATION',
				'ONLY_EXISTING_KEYWORDS',
				'EXTERNAL_LINKING',
				'SUPPORT_32'
			)) :
			$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5 = ($_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a == 'true') ? 'false' : 'true';
			$_ef88d01f391a9f53244d58c4facd028fc8717591 =<<<HTML <td><span>$config_param_label $_f1344667fbc0e8e5cbf8715bd22e24ca5801bc99b1c59b6a11abafa0</span></td><td><select name="$config_param_label"><option value="$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a">$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a</option><option value="$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5">$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5</option></select></td>
HTML;
			return $_ef88d01f391a9f53244d58c4facd028fc8717591;
			break;
		case 'FU_STATUS' :
			$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5 = ($_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a == 'ON') ? 'OFF' : 'ON';
			$_ef88d01f391a9f53244d58c4facd028fc8717591 =<<<HTML <td><span>$config_param_label $_f1344667fbc0e8e5cbf8715bd22e24ca5801bc99b1c59b6a11abafa0</span></td><td><select name="$config_param_label"><option value="$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a">$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a</option><option value="$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5">$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5</option></select></td>
HTML;
			return $_ef88d01f391a9f53244d58c4facd028fc8717591;
			break;
		case 'CACHE' :
			$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5 = ($_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a == 'index') ? 'disc' : 'index';
			$_ef88d01f391a9f53244d58c4facd028fc8717591 =<<<HTML<td><span>$config_param_label $_f1344667fbc0e8e5cbf8715bd22e24ca5801bc99b1c59b6a11abafa0</span></td><td><select name="$config_param_label"><option value="$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a">$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a</option><option value="$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5">$_1a787afe36e8b07f7babe1a50aacc851dbef48cc0f913d66ad771bb46563ccd5</option></select></td>
HTML;
			return $_ef88d01f391a9f53244d58c4facd028fc8717591;
			break;
		default :
			$_ef88d01f391a9f53244d58c4facd028fc8717591 =<<<HTML <td><span>$config_param_label $_f1344667fbc0e8e5cbf8715bd22e24ca5801bc99b1c59b6a11abafa0</span></td><td><input type="text" name="$config_param_label" value="$_d60f6c1d98e6f90c314d39beb47c3cd93ace40bc99ab296a"></td>
HTML;
			return $_ef88d01f391a9f53244d58c4facd028fc8717591;
	}
}
if (isset ($_COOKIE['password']) and $_COOKIE['password'] == PASSWORD)
{
	if (isset ($_GET['directory']) and !empty ($_GET['directory']))
	{
		$my_function->RemoveDirectory('../content/' . $_GET['directory']);
		header('Location: dashboard.php');
	}
}
if (isset ($_COOKIE['password']) and $_COOKIE['password'] == PASSWORD)
{
	if (isset ($_POST) and !empty ($_POST) and !isset ($_POST['password']))
	{
		foreach ($config_file as $_77b3c5a1b77023997edf6b6c87620835591078a6da97d68a71413dbb => $config_file_element)
		{
			if (!empty ($config_file_element))
			{
				foreach ($config_params as $_3bbcaca1801da2103ea0fd65696191bcad03fe392f5b0ae6d3d54660ef565fc69a0e8f5bee32cc8ab2351f95c05227fd913407a510f298247458d540640cb6dd => $_c209fc0acaa6247bc09fe3478defcc083c60f001a403b7a26522c52f3355a5a5fbe71405ab98cec5bcfb9513b161c9fe8cf7edfd5ff18166a0c751b3186e7ee1)
				{
					if (preg_match("|$_3bbcaca1801da2103ea0fd65696191bcad03fe392f5b0ae6d3d54660ef565fc69a0e8f5bee32cc8ab2351f95c05227fd913407a510f298247458d540640cb6dd|", $config_file_element))
					{
						$_3a183bec0fd356f0ce413bb087095b4e8714879372b05e817d8982d21d30b3bcbcd1823c7b67cf57ab02bf61e791ff90ffd4c6685a93ca19e4a02c1a1f0cebd8 = (isset ($_POST[$_3bbcaca1801da2103ea0fd65696191bcad03fe392f5b0ae6d3d54660ef565fc69a0e8f5bee32cc8ab2351f95c05227fd913407a510f298247458d540640cb6dd])) ? $_POST[$_3bbcaca1801da2103ea0fd65696191bcad03fe392f5b0ae6d3d54660ef565fc69a0e8f5bee32cc8ab2351f95c05227fd913407a510f298247458d540640cb6dd] : '';
						if (is_array($_3a183bec0fd356f0ce413bb087095b4e8714879372b05e817d8982d21d30b3bcbcd1823c7b67cf57ab02bf61e791ff90ffd4c6685a93ca19e4a02c1a1f0cebd8))
						{
							$config_file[$_77b3c5a1b77023997edf6b6c87620835591078a6da97d68a71413dbb] = 'define("' . $_3bbcaca1801da2103ea0fd65696191bcad03fe392f5b0ae6d3d54660ef565fc69a0e8f5bee32cc8ab2351f95c05227fd913407a510f298247458d540640cb6dd . '", "' . implode(' | ', $_3a183bec0fd356f0ce413bb087095b4e8714879372b05e817d8982d21d30b3bcbcd1823c7b67cf57ab02bf61e791ff90ffd4c6685a93ca19e4a02c1a1f0cebd8) . '");' . "";
						}
						else
						{
							$config_file[$_77b3c5a1b77023997edf6b6c87620835591078a6da97d68a71413dbb] = 'define("' . $_3bbcaca1801da2103ea0fd65696191bcad03fe392f5b0ae6d3d54660ef565fc69a0e8f5bee32cc8ab2351f95c05227fd913407a510f298247458d540640cb6dd . '", "' . $_3a183bec0fd356f0ce413bb087095b4e8714879372b05e817d8982d21d30b3bcbcd1823c7b67cf57ab02bf61e791ff90ffd4c6685a93ca19e4a02c1a1f0cebd8 . '");' . "";
						}
					}
				}
			}
		}
		if (file_put_contents('../config.php', $config_file))
		{
			header('Location: dashboard.php');
		}
	}
}
?> 
<!doctype html> 
<html> 
<head> 
<title>Панель управления</title> 
<meta charset="utf-8"> 
<style type="text/css"> #content { width: 960px; margin: auto; } fieldset { border: 1px solid black; border-radius: 4px; font: 16px/19px "Verdana"; } fieldset.auth { width: 330px; margin: auto; } fieldset.auth form td span { display: inline-block; width: 90px; margin-right: 5px; font: 700 14px/14px "Verdana"; text-align: right; } fieldset.auth form td input[type=text] { display: inline-block; width: 196px; border: 1px solid #cccccc; border-radius: 4px; margin: 5px 0; padding: 5px; } fieldset.utils { display: inline-block; width: 225px; vertical-align: top; } fieldset.utils span { display: inline-block; width: 55px; margin-right: 10px; font: 700 14px/14px "Verdana"; text-align: right; } fieldset.utils a { display: inline-block; width: 50px; margin: 10px auto; border: 1px solid black; border-radius: 4px; padding: 5px 10px; background-color: black; font: 600 14px/14px "Verdana"; color: white; text-align: center; text-decoration: none; cursor: pointer; } fieldset.dashboard { display: inline-block; width: 560px; margin: auto; vertical-align: top; } fieldset.dashboard form td p { max-height: 150px; border: 1px solid #cccccc; border-radius: 4px; margin: 5px 0; font: 14px/14px "Verdana"; overflow-y: auto; box-shadow: 2px 2px 8px #cccccc; } fieldset.dashboard form td label { cursor: pointer; } fieldset.dashboard form td span { display: inline-block; width: 260px; margin-right: 5px; font: 700 14px/14px "Verdana"; text-align: right; } fieldset.dashboard form td input[type=text] { display: inline-block; width: 275px; border: 1px solid #cccccc; border-radius: 4px; margin: 5px 0; padding: 5px; font: 14px/14px "Verdana"; box-shadow: 2px 2px 8px #cccccc; } fieldset.dashboard form td input[type=checkbox] { margin: 5px; } fieldset.dashboard form td select { display: inline-block; border: 1px solid #cccccc; border-radius: 4px; margin: 5px 0; padding: 5px; background-color: white; font: 14px/14px "Verdana"; box-shadow: 2px 2px 8px #cccccc; } fieldset form button { display: block; width: 120px; margin: auto; border: 1px solid black; border-radius: 4px; padding: 5px 10px; background-color: black; font: 600 14px/14px "Verdana"; color: white; cursor: pointer; } </style> 
</head> 
<body> 
<div id="content"> 
<?php if (!isset($_COOKIE['password']) or $_COOKIE['password'] !== PASSWORD): ?> 
<fieldset class="auth"> <legend>Авторизация</legend> <form action="dashboard.php" method="post"> 
<table> 
<tr> 
<td> 
<span>Password</span> 
</td> 
<td> 
<input type="text" name="password"> 
</td> 
</tr> 
</table> 
<button type="submit">Login</button> 
</form> 
</fieldset> 
<?php else: ?> 
<fieldset class="utils"> 
<legend>Утилиты</legend> 
<span>CLEAR</span> 
<a href="?directory=base">Base</a> 
<a href="?directory=cache">Cache</a> 
</fieldset> <fieldset class="dashboard"> 
<legend>Панель управления</legend> 
<form action="dashboard.php" method="post"> 
<table> 
<?php foreach ($config_params as $_3bbcaca1801da2103ea0fd65696191bcad03fe392f5b0ae6d3d54660ef565fc69a0e8f5bee32cc8ab2351f95c05227fd913407a510f298247458d540640cb6dd => $_3a183bec0fd356f0ce413bb087095b4e8714879372b05e817d8982d21d30b3bcbcd1823c7b67cf57ab02bf61e791ff90ffd4c6685a93ca19e4a02c1a1f0cebd8): ?> 
<tr> 
<?php echo Parameter($_3bbcaca1801da2103ea0fd65696191bcad03fe392f5b0ae6d3d54660ef565fc69a0e8f5bee32cc8ab2351f95c05227fd913407a510f298247458d540640cb6dd, $_3a183bec0fd356f0ce413bb087095b4e8714879372b05e817d8982d21d30b3bcbcd1823c7b67cf57ab02bf61e791ff90ffd4c6685a93ca19e4a02c1a1f0cebd8); ?> 
</tr> 
<?php endforeach; ?> 
</table> 
<button type="submit">Save</button> 
</form> 
</fieldset> 
<?php endif; ?> 
</div> 
</body> 
</html>
?>