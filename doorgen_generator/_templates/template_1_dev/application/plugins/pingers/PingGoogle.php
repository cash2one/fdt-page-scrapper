<?php

class PingGoogle
{
	# Функция пингования страницы PingGoogle.
	function Start($url, $query, $F)
	{
		$xml = <<<XML
<?xml version="1.0" encoding="UTF-8"?>
<methodCall>
    <methodName>weblogUpdates.ping</methodName>
    <params>
        <param>
            <value>$query</value>
        </param>
        <param>
            <value>$url</value>
        </param>
    </params>
</methodCall>
XML;
		$response = file_get_contents("http://blogsearch.google.ru/ping/RPC2", true, stream_context_create(array(
			'http' => array(
				'method' => "POST",
				'header' => "Content-type: text/xml\r\n" . "Content-length: " . strlen($xml),
				'content' => $xml
				)
			)));

		if (!strstr($response, "Thanks for the ping."))
		{
			$F->Error("Url $url is not added. Please check PingGoogle pingers plugin.");
		}
	}
}