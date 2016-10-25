package com.fdt.smsreg.enums;

public enum SERVICE 
{
	_4game("4game","принять смс от 4game.ru;"),
	_gmail("gmail","принять смс от Gmail.com;"),
	_facebook("facebook","принять смс от Facebook.com;"),
	_mailru("mailru","принять смс от Mail.ru;"),
	_vk("vk","принять смс от Вконтакте;"),
	_classmates("classmates","принять смс от Одноклассники;"),
	_twitter("twitter","принять смс от Twitter;"),
	_mamba("mamba","принять смс от Мамба;"),
	_loveplanet("loveplanet","принять смс от LovePlanet;"),
	_telegram("telegram","принять смс от telegram.org;"),
	_badoo("badoo","принять смс от Badoo;"),
	_drugvokrug("drugvokrug","принять смс от другвокруг;"),
	_avito("avito","принять смс от avito.ru;"),
	_olx("olx","принять смс от OLX;"),
	_steam("steam","принять смс от STEAM;"),
	_fotostrana("fotostrana","принять смс от Фотострана.ру;"),
	_microsoft("microsoft","принять смс от Microsoft;"),
	_viber("viber","принять смс от Viber;"),
	_whatsapp("whatsapp","принять смс от whatsapp.com;"),
	_wechat("wechat","принять смс от wechat.com;"),
	_seosprint("seosprint","принять смс от SEOsprint.net;"),
	_instagram("instagram","принять смс от Instagram;"),
	_yahoo("yahoo","принять смс от Yahoo;"),
	_other("other","принять смс от сервиса, которого нету в списке выше;");


	private String description;
	private String value;
	
	private SERVICE(final String value, final String description){
		this.value = value;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getValue() {
		return value;
	}
	
	
}
