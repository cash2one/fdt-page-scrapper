eval(function (param1, param2, param3, param4, param5, param6) {
    param5 = function (param3) {
        return param3.toString(36);
    };
    if (!'' ['replace'](/^/, String)) {
        while (param3--) {
            param6[param3.toString(param2)] = param4[param3] || param3.toString(param2);
        };
        param4 = [function (param5) {
            return param6[param5];
        }];
        param5 = function () {
            return '\w+';
        };
        param3 = 1;
    };
    while (param3--) {
        if (param4[param3]) {
            param1 = param1['replace'](new RegExp('\b' + param5(param3) + '\b', 'g'), param4[param3]);
        };
    };
    return param1;
}('$(8).c(3(){$(\'.b\').a(3(){5($(1).0(\'2\')){6 2=$(1).0(\'2\');d.e(2)}g{5($(1).0(\'7\')){6 4=$(\'f\').0(\'h\')+\'/i/\'+$(1).0(\'7\');8.9=4}}})});', 19, 19, 'attr|this|url|function|page|if|var|title|document|location|click|without|ready|window|open|base|else|href|search' ['split']('|'), 0, {}));