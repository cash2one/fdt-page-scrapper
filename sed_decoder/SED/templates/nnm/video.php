<!doctype html>
<html>
	<head>
		<title></title>
		<meta charset="utf-8">
		<?php echo $T->getCSS('style.css'); ?>
	</head>
	<body>
		<iframe width="640" height="480" src="<?php echo $T->GetVideo($_GET['v'])->url; ?>" frameborder="0"></iframe>
	</body>
</html>