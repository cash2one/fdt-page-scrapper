var _lg_form_ = _lg_form_ || {};
var initObject = window._lg_form_init_ || window;

window.lmpost = {};
window.lmpost.options = {campaignid: '227458', subid:'', testresult: '', domain: 'https://www.paydaylendersearch.com/forms/paydayv3/', form: '../2page_form_label_v2', leadtypeid: '9'};

var getFromInit = function(a) {
    return (initObject.hasOwnProperty(a)) ? initObject[a] : undefined
};
_lg_form_.affiliateId = getFromInit("aid");
_lg_form_.formName = getFromInit("template");
_lg_form_.source = getFromInit("ref");
_lg_form_.click_id = getFromInit("click_id");
_lg_form_.formData = getFromInit("form_data") || {};
if (getFromInit("conversionCode") != undefined) {
    _lg_form_.conversionCode = getFromInit("conversionCode")
}
if (document.getElementById("_lg_form_").innerHTML == "") {
    document.getElementById("_lg_form_").innerHTML = '<div style="min-height: 493px; position: relative;"><img src="https://leadapi.net/forms/bablo/images/loader.gif" style="position: absolute!important; left: 50%!important; top: 50%!important; margin: -32px 0 0 -25px!important;" /><div style="text-align: center!important; font-size: 20px!important;color:#333!important;padding-top: 170px!important;line-height:normal!important;">Loading. Please wait...</div></div>'
}(function() {
    var f = ["formName", "affiliateId", "source", "click_id"],
        b = {},
        e = "https://www.paydaylendersearch.com/forms/paydayv3/Scripts/forms.core.js",
        //e = "/app/appForm.js",
        j, d, h, a;
    if (typeof _lg_form_ !== "undefined") {
        _lg_form_.leadxCookieName = "_lg_form__leadx"
    }
    if (document.cookie.length != 0) {
        a = document.cookie.match(new RegExp("(^|;)[\\s]*" + _lg_form_.leadxCookieName + "=([^;]*)"));
        if (a !== null) {
            b.leadx = decodeURIComponent(a[2]);
            var g = JSON.parse(b.leadx);
            if (_lg_form_.affiliateId == undefined && g.aid != undefined) {
                _lg_form_.affiliateId = g.aid
            } else {
                if (_lg_form_.affiliateId != g.aid) {
                    g.source = undefined;
                    g.click_id = undefined
                }
            }
            if (_lg_form_.source == undefined && g.source != undefined) {
                _lg_form_.source = g.source
            } else {
                if (_lg_form_.source != g.source) {
                    g.click_id = undefined
                }
            }
            if (_lg_form_.click_id == undefined && g.click_id != undefined) {
                _lg_form_.click_id = g.click_id
            }
        }
    }
    /*for (h = 0; h < f.length; h++) {
        if (typeof _lg_form_[f[h]] !== "undefined") {
            b[f[h]] = _lg_form_[f[h]]
        } else {
            if (f[h] !== "source" && f[h] !== "click_id") {
                throw "Missing parameter: " + f[h]
            }
        }
    }*/
    for (h in b) {
        if (b.hasOwnProperty(h)) {
            e += (e.indexOf("?") === -1 ? "?" : "&") + h + "=" + encodeURIComponent(b[h])
        }
    }
    j = document.createElement("script");
    j.type = "text/javascript";
    j.async = true;
    j.src = e;
    if (j.readyState) {
        j.onreadystatechange = function() {
            if (j.readyState === "loaded" || j.readyState === "complete") {
                j.onreadystatechange = null;
                //c()
            }
        }
    } else {
        j.onload = function() {
            c()
        }
    }

    function c() {
        var i = document.createElement("script");
        i.type = "text/javascript";
        i.async = true;
        i.setAttribute("data-name", "fraud-detector");
        i.src = "https://hashsrv.com/js/hash.js";
        document.getElementsByTagName("head")[0].appendChild(i)
    }
    d = document.getElementsByTagName("script")[0];
    d.parentNode.insertBefore(j, d)
})();