/*var script = document.createElement('script');
script.src = "/app/script.js";
document.body.appendChild(script);

script.onload = function() {
    // после выполнения скрипта становится доступна функция _
    alert( "Loaded" ); // её код
	var scriptNew = document.getElementsByTagName('script')[0];
	eval()
	alert( scriptNew.text ); // её код
}*/
window.lmpost = {};
window.lmpost.options = {campaignid: "227458", subid:"", testresult: "", domain: "https://www.paydaylendersearch.com/forms/paydayv3/", form: "../2page_form_label_v2", leadtypeid: "9"};
document.write('<script id="leadsb2cformsrc" async="async" type="text/javascript" src="" + lmpost.options.domain + "Scripts/forms.core.js"><\/script>');