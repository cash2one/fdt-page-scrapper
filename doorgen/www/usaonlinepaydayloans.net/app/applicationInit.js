window.lmpost = {};
window.lmpost.options = {campaignid: '227458', subid:'', testresult: '', domain: 'https://www.paydaylendersearch.com/forms/paydayv3/', form: '../2page_form_label_v2', leadtypeid: '9'};
loadScr(
	lmpost.options.domain + 'Scripts/forms.core.js',
	"leadsb2cformsrc", 
	function() {
		function upd_screen() {
			window.document.getElementById('fr_loader').style = 'height: 0px';
			window.document.getElementById('fr_loader').innerHTML = '';
		}
		setTimeout(upd_screen, 3000);
	}
);