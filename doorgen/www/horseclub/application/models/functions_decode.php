<?php


class Functions
{
    function __construct($_63d1c85e = '')
    {
        $this->path = $_63d1c85e;
    }
    
    private function Base($_24c827e28343fa0f7484e4987c25b8b556cbe1a1)
    {
        $_ddc3e8112f079c21b8a7d1ce3a788d71bccd932c4ee489d6ece63eda = array(
            'base',
            'cache'
        );
        foreach ($_ddc3e8112f079c21b8a7d1ce3a788d71bccd932c4ee489d6ece63eda as $_8a104cec3b1b9e82b179cce4cf51ec8ab1f20ee875b34ef8eede74e3) {
            if (!is_dir($_24c827e28343fa0f7484e4987c25b8b556cbe1a1 . "content/$_8a104cec3b1b9e82b179cce4cf51ec8ab1f20ee875b34ef8eede74e3")) {
                if (@!mkdir($_24c827e28343fa0f7484e4987c25b8b556cbe1a1 . "content/$_8a104cec3b1b9e82b179cce4cf51ec8ab1f20ee875b34ef8eede74e3", 0755)) {
                    exit("Невозможно создать папку $_8a104cec3b1b9e82b179cce4cf51ec8ab1f20ee875b34ef8eede74e3. Установите 777 права доступа на папку content.");
                }
            }
        }
        $_0faf0081 = array(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f'
        );
        for ($_1ec0ed674764b534ec639744ad096db49851a83f48b0a2fd = 0; $_1ec0ed674764b534ec639744ad096db49851a83f48b0a2fd < count($_0faf0081); $_1ec0ed674764b534ec639744ad096db49851a83f48b0a2fd++) {
            if (!is_dir($_24c827e28343fa0f7484e4987c25b8b556cbe1a1 . "content/base/$_0faf0081[$_1ec0ed674764b534ec639744ad096db49851a83f48b0a2fd]")) {
                if (mkdir($_24c827e28343fa0f7484e4987c25b8b556cbe1a1 . "content/base/$_0faf0081[$_1ec0ed674764b534ec639744ad096db49851a83f48b0a2fd]", 0755)) {
                    foreach ($_0faf0081 as $_945d185c101b015232adee6625c2a4f0) {
                        touch($_24c827e28343fa0f7484e4987c25b8b556cbe1a1 . "content/base/$_0faf0081[$_1ec0ed674764b534ec639744ad096db49851a83f48b0a2fd]/$_945d185c101b015232adee6625c2a4f0.txt");
                    }
                }
            }
        }
    }
    
    private function BaseWriter($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c, $_af523a2d8918ae60810bbc6b30799223)
    {
        $_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c = $this->Clear($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c);
        $_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8                                                                 = md5($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c);
        $_e49563feca0fe402d064339724899474                                                                                                 = array(
            'md5' => $_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8,
            'query' => $_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c,
            'translit' => $this->Convert($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c)
        );
        $_091db10488a55a1e6d20b517be60b85b345b47cce4d475df91b6325b62aa98cb                                                                 = file("content/base/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[0]/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[1].txt", FILE_IGNORE_NEW_LINES);
        if (!in_array(json_encode($_e49563feca0fe402d064339724899474), $_091db10488a55a1e6d20b517be60b85b345b47cce4d475df91b6325b62aa98cb)) {
            file_put_contents("content/base/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[0]/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[1].txt", json_encode($_e49563feca0fe402d064339724899474) . "", FILE_APPEND | LOCK_EX);
        }
        if (ONLY_EXISTING_KEYWORDS == 'false') {
            foreach ($_af523a2d8918ae60810bbc6b30799223 as $_cac236123dd68b35ed08f1be3b9479a8e2d16980) {
                $_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c = $_cac236123dd68b35ed08f1be3b9479a8e2d16980['words'];
                if (!empty($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c)) {
                    $_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8 = md5($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c);
                    $_e49563feca0fe402d064339724899474                                 = array(
                        'md5' => $_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8,
                        'query' => $_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c,
                        'translit' => $this->Convert($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c)
                    );
                    $_091db10488a55a1e6d20b517be60b85b345b47cce4d475df91b6325b62aa98cb = file("content/base/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[0]/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[1].txt", FILE_IGNORE_NEW_LINES);
                    if (!in_array(json_encode($_e49563feca0fe402d064339724899474), $_091db10488a55a1e6d20b517be60b85b345b47cce4d475df91b6325b62aa98cb)) {
                        file_put_contents("content/base/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[0]/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[1].txt", json_encode($_e49563feca0fe402d064339724899474) . "", FILE_APPEND | LOCK_EX);
                    }
                }
            }
        }
    }
    public function CheckQuery($_a986c8931418c3e11b53dae8b42eec29cef321a0a52500bb, $_d88696a833553e3d75affcbefb72fbbf)
    {
        $_5ce19675f36a08ad9e5f914bbe2e79e92842ccc3afe7b62d49267fc693967eb4 = md5($_a986c8931418c3e11b53dae8b42eec29cef321a0a52500bb);
        $_c7410b428bdb0d8b3be050bf7d7622d6677d5fba                         = array(
            'md5' => $_5ce19675f36a08ad9e5f914bbe2e79e92842ccc3afe7b62d49267fc693967eb4,
            'query' => $_a986c8931418c3e11b53dae8b42eec29cef321a0a52500bb,
            'translit' => $this->Convert($_a986c8931418c3e11b53dae8b42eec29cef321a0a52500bb)
        );
        $_0ceb14a690a0d94a59f362047ea1d6c548b42204                         = file("content/base/$_5ce19675f36a08ad9e5f914bbe2e79e92842ccc3afe7b62d49267fc693967eb4[0]/$_5ce19675f36a08ad9e5f914bbe2e79e92842ccc3afe7b62d49267fc693967eb4[1].txt", FILE_IGNORE_NEW_LINES);
        if (SUPPORT_32 == 'false') {
            if (!in_array(json_encode($_c7410b428bdb0d8b3be050bf7d7622d6677d5fba), $_0ceb14a690a0d94a59f362047ea1d6c548b42204)) {
                $this->View('404', $_d88696a833553e3d75affcbefb72fbbf);
                exit;
            }
        }
    }
    public function CheckUrl($_4cd6fca0168fbc49aece7aa52bcf87d8dd01021f)
    {
        if (strpos($_4cd6fca0168fbc49aece7aa52bcf87d8dd01021f, '?') == 1)
            return true;
        else
            return false;
    }
    public function Clear($_76d4c47e4bbd2ef931fa0b28b6a3b65947eda8dc)
    {
        $_079537787a26759cf6663fc7b5670cc547eec08bbf334835809227f6a2940cbd = array(
            '+',
            '—',
            '.',
            ',',
            '=',
            '?',
            '%',
            ';',
            ':',
            '^',
            '$',
            '#',
            '!',
            '@',
            '№',
            '_',
            '/',
            '|',
            '[',
            ']',
            '{',
            '}',
            '&',
            '*',
            '(',
            ')',
            '<',
            '>',
            '-',
            '"',
            '»',
            '«',
            '~',
            '`',
            "'",
            'amp',
            'nbsp',
            'quot',
            '®',
            '©',
            '™',
            '•',
            '♥',
            '☆',
            '·',
            '›',
            ''
        );
        $_76d4c47e4bbd2ef931fa0b28b6a3b65947eda8dc                         = str_replace($_079537787a26759cf6663fc7b5670cc547eec08bbf334835809227f6a2940cbd, ' ', $_76d4c47e4bbd2ef931fa0b28b6a3b65947eda8dc);
        return preg_replace('|s+|', ' ', trim($_76d4c47e4bbd2ef931fa0b28b6a3b65947eda8dc));
    }
    public function Convert($_0fd42b3f73c448b34940b339f87d07adf116b05c0227aad72e8f0ee90533e699)
    {
        $_0fd42b3f73c448b34940b339f87d07adf116b05c0227aad72e8f0ee90533e699                                                                 = mb_strtolower($this->Clear($_0fd42b3f73c448b34940b339f87d07adf116b05c0227aad72e8f0ee90533e699), 'UTF-8');
        $_45cf72471551f6e7f68688a6f08bf68b12b1f581d0e9a46ed355c373378e2e32e4863e9abe45a9f79fb50194a9ed627ae0d7f38bea317510d991ee05280a9f1c = array(
            'а' => 'a',
            'б' => 'b',
            'в' => 'v',
            'г' => 'g',
            'д' => 'd',
            'е' => 'e',
            'ё' => 'e',
            'ж' => 'zh',
            'з' => 'z',
            'и' => 'i',
            'й' => 'y',
            'к' => 'k',
            'л' => 'l',
            'м' => 'm',
            'н' => 'n',
            'о' => 'o',
            'п' => 'p',
            'р' => 'r',
            'с' => 's',
            'т' => 't',
            'у' => 'u',
            'ф' => 'f',
            'х' => 'h',
            'ц' => 'c',
            'ч' => 'ch',
            'ш' => 'sh',
            'щ' => 'sch',
            'ь' => '',
            'ы' => 'y',
            'ъ' => '',
            'э' => 'e',
            'ю' => 'yu',
            'я' => 'ya'
        );
        return str_replace(' ', '-', strtr($_0fd42b3f73c448b34940b339f87d07adf116b05c0227aad72e8f0ee90533e699, $_45cf72471551f6e7f68688a6f08bf68b12b1f581d0e9a46ed355c373378e2e32e4863e9abe45a9f79fb50194a9ed627ae0d7f38bea317510d991ee05280a9f1c));
    }
    
    private function CreateCache($_bce54819, $_04d4ebf41f750ecc954f20b5a40037124c27570162678b90dd4fb5f54cb2cf3f9286bb2296c4a155c8b92e9ef77d120f720f64ba901ec6847912c5e7691e6581)
    {
        if (!file_exists($_bce54819)) {
            file_put_contents($_bce54819, json_encode($_04d4ebf41f750ecc954f20b5a40037124c27570162678b90dd4fb5f54cb2cf3f9286bb2296c4a155c8b92e9ef77d120f720f64ba901ec6847912c5e7691e6581));
        }
    }
    public function CreateUrl($_6e9bd5804d79ab41904a7ceec2ff0457f1bea6701a885b88)
    {
        if (isset($_POST['referer']) and !empty($_POST['referer'])) {
            $_469aa1d0feda03efad9c67f4487e3db635f6cdfe                 = $_POST['referer'];
            $_01432255a2039d84a4dcbb82135e73d87798c6db3e1ad9927c20bb4b = $this->GetContents('txt/kids.txt');
            if (in_array($_469aa1d0feda03efad9c67f4487e3db635f6cdfe, $_01432255a2039d84a4dcbb82135e73d87798c6db3e1ad9927c20bb4b)) {
                $_50ced2b2a9b5561721c65554bf9231a37429662f932b47d6 = "http://$_469aa1d0feda03efad9c67f4487e3db635f6cdfe";
            }
        } else {
            $_50ced2b2a9b5561721c65554bf9231a37429662f932b47d6 = $this->host;
        }
        $_c1b2d5ca812288348925331707a306d7688ff8ba3ace6d22786afa5b = $this->GetQuery($_6e9bd5804d79ab41904a7ceec2ff0457f1bea6701a885b88);
        $_87319e1ceb28e02d31264e1d7f561ece3f17a35d59b647a8e466a1f6 = $this->GetController($_6e9bd5804d79ab41904a7ceec2ff0457f1bea6701a885b88);
        switch (FU_STATUS) {
            case 'ON':
                if (!empty($_c1b2d5ca812288348925331707a306d7688ff8ba3ace6d22786afa5b)) {
                    switch (substr(FU_TYPE, 0, 4)) {
                        case 'FULL':
                            $_50ced2b2a9b5561721c65554bf9231a37429662f932b47d6 .= "/$_87319e1ceb28e02d31264e1d7f561ece3f17a35d59b647a8e466a1f6/" . $this->Convert($_c1b2d5ca812288348925331707a306d7688ff8ba3ace6d22786afa5b) . "-" . substr(md5($_c1b2d5ca812288348925331707a306d7688ff8ba3ace6d22786afa5b), 0, 2) . substr(FU_TYPE, 4);
                            break;
                        case 'RAND':
                            $_50ced2b2a9b5561721c65554bf9231a37429662f932b47d6 .= "/$_87319e1ceb28e02d31264e1d7f561ece3f17a35d59b647a8e466a1f6/" . md5($_c1b2d5ca812288348925331707a306d7688ff8ba3ace6d22786afa5b) . substr(FU_TYPE, 4);
                            break;
                    }
                } else {
                    $_50ced2b2a9b5561721c65554bf9231a37429662f932b47d6 .= "/$_87319e1ceb28e02d31264e1d7f561ece3f17a35d59b647a8e466a1f6";
                }
                break;
            case 'OFF':
                if (!empty($_c1b2d5ca812288348925331707a306d7688ff8ba3ace6d22786afa5b)) {
                    $_50ced2b2a9b5561721c65554bf9231a37429662f932b47d6 .= !empty($_87319e1ceb28e02d31264e1d7f561ece3f17a35d59b647a8e466a1f6) ? "/?" . CONTROLLER . "=$_87319e1ceb28e02d31264e1d7f561ece3f17a35d59b647a8e466a1f6&" . QUERY . "=" . urlencode($_c1b2d5ca812288348925331707a306d7688ff8ba3ace6d22786afa5b) : "";
                } else {
                    $_50ced2b2a9b5561721c65554bf9231a37429662f932b47d6 .= !empty($_87319e1ceb28e02d31264e1d7f561ece3f17a35d59b647a8e466a1f6) ? "/?" . CONTROLLER . "=$_87319e1ceb28e02d31264e1d7f561ece3f17a35d59b647a8e466a1f6" : "";
                }
                break;
        }
        return $_50ced2b2a9b5561721c65554bf9231a37429662f932b47d6;
    }
    public function Dublier($_0b1dff4d63eba63737e7c5623c3884389416cdce736a31c91b87a309780145c0458f6c8fd9b42cc1)
    {
        foreach ($_0b1dff4d63eba63737e7c5623c3884389416cdce736a31c91b87a309780145c0458f6c8fd9b42cc1 as $_e456821cbc62b1a87f6e6c8a96e0e3e4) {
            $_3771ab040630eed7161ba000bcc361e04f4e5edf8a17d7015c6e086edc0f7e6372411effce8f7d0f66dd96d70849ef714e45dfafebb2a7a7801e67d70bbcf5aa[] = $_e456821cbc62b1a87f6e6c8a96e0e3e4['title'];
        }
        $_1372a58ef375e7947c1879169593de04739c009049d958a48bf1d7a2 = array_keys(array_unique($_3771ab040630eed7161ba000bcc361e04f4e5edf8a17d7015c6e086edc0f7e6372411effce8f7d0f66dd96d70849ef714e45dfafebb2a7a7801e67d70bbcf5aa));
        $_a98235b1e79b5558fa8f96dfb57bb12b                         = array();
        foreach ($_0b1dff4d63eba63737e7c5623c3884389416cdce736a31c91b87a309780145c0458f6c8fd9b42cc1 as $_f1d403294c64c19ce0eba621719c15a0 => $_e456821cbc62b1a87f6e6c8a96e0e3e4) {
            if (in_array($_f1d403294c64c19ce0eba621719c15a0, $_1372a58ef375e7947c1879169593de04739c009049d958a48bf1d7a2)) {
                $_a98235b1e79b5558fa8f96dfb57bb12b[] = $_e456821cbc62b1a87f6e6c8a96e0e3e4;
            }
        }
        return array_values($_a98235b1e79b5558fa8f96dfb57bb12b);
    }
    public function Error($_0573b907732c058ed854711ddc56e4955c6818fe)
    {
        $_d88a5792aad8ffc0c9cb2668b70d947bda0d94ac45476735914d485e394fd12c86e01137470da3c8efb382949e7f8b706c0968e916a2ed5d1d250208f1c6611a = $this->path . 'content/errors.log';
        if (file_exists($_d88a5792aad8ffc0c9cb2668b70d947bda0d94ac45476735914d485e394fd12c86e01137470da3c8efb382949e7f8b706c0968e916a2ed5d1d250208f1c6611a)) {
            $_db3f5f980935a420f80ad7328c9b1e0e10a5a0e3f4811bec43cc1ff8823a69aa6cfe0301ede43416 = file($_d88a5792aad8ffc0c9cb2668b70d947bda0d94ac45476735914d485e394fd12c86e01137470da3c8efb382949e7f8b706c0968e916a2ed5d1d250208f1c6611a);
            if (count($_db3f5f980935a420f80ad7328c9b1e0e10a5a0e3f4811bec43cc1ff8823a69aa6cfe0301ede43416) >= 1000) {
                unlink($_d88a5792aad8ffc0c9cb2668b70d947bda0d94ac45476735914d485e394fd12c86e01137470da3c8efb382949e7f8b706c0968e916a2ed5d1d250208f1c6611a);
            }
        }
        $_c6335a93d067f3a1e8118da3273cd88f = gmdate("d.m.y H:i:s", time() + 10800);
        file_put_contents($_d88a5792aad8ffc0c9cb2668b70d947bda0d94ac45476735914d485e394fd12c86e01137470da3c8efb382949e7f8b706c0968e916a2ed5d1d250208f1c6611a, "$_c6335a93d067f3a1e8118da3273cd88f - $_0573b907732c058ed854711ddc56e4955c6818fe", FILE_APPEND | LOCK_EX);
    }
    private function Filter($_48a53cf6b064de94dc2cb47968394dc6)
    {
        $_28e112792272d388bb6d573dc22a057f = $this->GetContents('txt/stop-words.txt');
        if (!empty($_28e112792272d388bb6d573dc22a057f)) {
            foreach ($_28e112792272d388bb6d573dc22a057f as $_020600ce => $_9667f0087c3b892e3e24c3db1653e092) {
                if (preg_match('|^[*]|', $_9667f0087c3b892e3e24c3db1653e092))
                    $_28e112792272d388bb6d573dc22a057f[$_020600ce] = str_replace('*', '', $_9667f0087c3b892e3e24c3db1653e092) . '$';
                if (preg_match('|[*]$|', $_9667f0087c3b892e3e24c3db1653e092))
                    $_28e112792272d388bb6d573dc22a057f[$_020600ce] = '^' . str_replace('*', '', $_9667f0087c3b892e3e24c3db1653e092);
            }
            $_9667f0087c3b892e3e24c3db1653e092 = implode('|', $_28e112792272d388bb6d573dc22a057f);
        }
        foreach ($_48a53cf6b064de94dc2cb47968394dc6 as $_c771d53a60957f9483ecd2f19406c4efcf8b77b3a47ff22d => $_b5802d64dc3214e02f00716b3f1460df2840456628713ae4c6dadf2fb247c91f) {
            $_23dabaf7bf78d57dd387f3b980ca0857968cb6b5                                                      = str_replace('...', '', $_b5802d64dc3214e02f00716b3f1460df2840456628713ae4c6dadf2fb247c91f['title']);
            $_28e112792272d388bb6d573dc22a057f                                                              = explode(' ', $this->Clear($_23dabaf7bf78d57dd387f3b980ca0857968cb6b5));
            $_28e112792272d388bb6d573dc22a057f                                                              = array_slice($_28e112792272d388bb6d573dc22a057f, 0, WORDS);
            $_28e112792272d388bb6d573dc22a057f                                                              = implode(' ', $_28e112792272d388bb6d573dc22a057f);
            $_48a53cf6b064de94dc2cb47968394dc6[$_c771d53a60957f9483ecd2f19406c4efcf8b77b3a47ff22d]['title'] = $_23dabaf7bf78d57dd387f3b980ca0857968cb6b5;
            $_48a53cf6b064de94dc2cb47968394dc6[$_c771d53a60957f9483ecd2f19406c4efcf8b77b3a47ff22d]['words'] = $_28e112792272d388bb6d573dc22a057f;
            if (!empty($_9667f0087c3b892e3e24c3db1653e092)) {
                $_77ae380258ac12b2c117cc78c99eed17 = mb_strtolower($_9667f0087c3b892e3e24c3db1653e092, 'UTF-8');
                $_c921d1aa8e5e8c89848aff9d136fb9c9 = mb_strtolower($_28e112792272d388bb6d573dc22a057f, 'UTF-8');
                if (preg_match("/$_77ae380258ac12b2c117cc78c99eed17/", $_c921d1aa8e5e8c89848aff9d136fb9c9)) {
                    $_48a53cf6b064de94dc2cb47968394dc6[$_c771d53a60957f9483ecd2f19406c4efcf8b77b3a47ff22d]['words'] = '';
                }
            }
        }
        return $_48a53cf6b064de94dc2cb47968394dc6;
    }
    public function GetCache($_deb5b9c7613e92fb7b56e86004292aa5)
    {
        if (file_exists($_deb5b9c7613e92fb7b56e86004292aa5)) {
            touch($_deb5b9c7613e92fb7b56e86004292aa5);
            $_deb5b9c7613e92fb7b56e86004292aa5                                 = file_get_contents($_deb5b9c7613e92fb7b56e86004292aa5);
            $_aa629748189aaa5a1bdb99dfc2f1c23bf1698147da42c4138964bd6f2842e5ca = json_decode($_deb5b9c7613e92fb7b56e86004292aa5, true);
            return $_aa629748189aaa5a1bdb99dfc2f1c23bf1698147da42c4138964bd6f2842e5ca;
        }
    }
    public function GetContent($_481e3772098759cdfd15bb7f566871354aa2f813100289ee)
    {
        $_d4d5318bf7654f828ad43a8013b98372 = $this->getContents($_481e3772098759cdfd15bb7f566871354aa2f813100289ee);
        if (!empty($_d4d5318bf7654f828ad43a8013b98372)) {
            $_e918203bc64978b94158287160722b564b5ea63526e81bc42964a4fae63ded97 = array_rand($_d4d5318bf7654f828ad43a8013b98372);
            return $_d4d5318bf7654f828ad43a8013b98372[$_e918203bc64978b94158287160722b564b5ea63526e81bc42964a4fae63ded97];
        }
    }
    public function GetContents($_526d34745e1edb26983173332c579c2d)
    {
        $_b7b03d2e46ed926ebe1bdf4dac23ea790f5b945b7706535cca7a1b9757ffde26 = $this->path . $_526d34745e1edb26983173332c579c2d;
        if (file_exists($_b7b03d2e46ed926ebe1bdf4dac23ea790f5b945b7706535cca7a1b9757ffde26) and filesize($_b7b03d2e46ed926ebe1bdf4dac23ea790f5b945b7706535cca7a1b9757ffde26) > 0) {
            $_3f5b474d12c9595d516fcdfcea45add901f845c74d045a17c53c50612ab986bf7ba7ee825d411f360b23ee09424327d5 = file($_b7b03d2e46ed926ebe1bdf4dac23ea790f5b945b7706535cca7a1b9757ffde26, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
            return array_values(array_unique($_3f5b474d12c9595d516fcdfcea45add901f845c74d045a17c53c50612ab986bf7ba7ee825d411f360b23ee09424327d5));
        }
    }
   
    public function GetHTML($url_for_extract, $host)
    {
		//TODO save into config
		$proxy_user = "VIPUAEURx0kv2UoXOB";
		$proxy_pass = "MIXaMe2YRd";
        $user_agent = $this->GetContent('txt/browsers.txt');
        if (in_array('curl', get_loaded_extensions())) {
            $curl_engine = curl_init();
            curl_setopt($curl_engine, CURLOPT_URL, $url_for_extract);
            curl_setopt($curl_engine, CURLOPT_RETURNTRANSFER, 1);
            curl_setopt($curl_engine, CURLOPT_USERAGENT, $user_agent);
            $proxy_ip = $this->GetContent('txt/proxies.txt');
            if ($proxy_ip) {
				echo "USING PROXY: ".$proxy_ip."<br/>";
                curl_setopt($curl_engine, CURLOPT_PROXY, $proxy_ip);
                curl_setopt($curl_engine, CURLOPT_PROXYTYPE, CURLPROXY_SOCKS5);
				curl_setopt($curl_engine, CURLOPT_PROXYUSERPWD, $proxy_user.':'.$proxy_pass);
            }
            curl_setopt($curl_engine, CURLOPT_TIMEOUT, 15);
            $_04dd72e973847607c7d4e6cd938f170c49d14a9e78bedb7c2158d4c2 = curl_exec($curl_engine);
            curl_close($curl_engine);
            $_9b9540098da4f542604041ec8927619e4d00f84cf84223de20e7a88d18c0752fe525178a650f9f6e3818a80714305e53 = str_get_html($_04dd72e973847607c7d4e6cd938f170c49d14a9e78bedb7c2158d4c2);
            if (is_bool($_9b9540098da4f542604041ec8927619e4d00f84cf84223de20e7a88d18c0752fe525178a650f9f6e3818a80714305e53)) {
                $this->Error("Proxy $proxy_ip does not work. Please remove it.");
            }
        } else {
            $_cf9bbb7ed922b875cad68bfc1bae3f950a75a194985c8c1fcaf3c76a954f6f1a3eb1e50021cb5a187fe615b6eb27ff17ebaf0990546c441edd13f1447a1f952b = array(
                'http' => array(
                    'method' => "GET",
                    'header' => "Host: $host" . "User-Agent: $user_agent"
                )
            );
            $_f7afe154ad13c034c053d8ff862e861eaa953834827fd725ee6c9303e7e82cdc                                                                 = stream_context_create($_cf9bbb7ed922b875cad68bfc1bae3f950a75a194985c8c1fcaf3c76a954f6f1a3eb1e50021cb5a187fe615b6eb27ff17ebaf0990546c441edd13f1447a1f952b);
            $_9b9540098da4f542604041ec8927619e4d00f84cf84223de20e7a88d18c0752fe525178a650f9f6e3818a80714305e53                                 = file_get_html($url_for_extract, false, $_f7afe154ad13c034c053d8ff862e861eaa953834827fd725ee6c9303e7e82cdc);
        }
        return $_9b9540098da4f542604041ec8927619e4d00f84cf84223de20e7a88d18c0752fe525178a650f9f6e3818a80714305e53;
    }
	
	public function GetKeyword()
    {
        $_01f300c5 = implode($this->GetQueries(1));
        return $_01f300c5;
    }
	
	public function GetQueries($_895dcdee271bdae8ce1ea437591499fe20fa31813c21f814473d6c6bbf2b33db)
    {
        $_db3156b54d7cee17c9a848bf7c01b6a617f94c82 = glob('content/base/*/*', GLOB_NOSORT);
        if (!empty($_db3156b54d7cee17c9a848bf7c01b6a617f94c82)) {
            $_109786267d6aff856324e181b83a403c0cf28a7b06fe07f5 = array();
            foreach ($_db3156b54d7cee17c9a848bf7c01b6a617f94c82 as $_7f93ae6489d2f836d740ed69c95f52a8c2ab4f64514dea9e) {
                if (filesize($_7f93ae6489d2f836d740ed69c95f52a8c2ab4f64514dea9e) > 0) {
                    $_109786267d6aff856324e181b83a403c0cf28a7b06fe07f5[] = $_7f93ae6489d2f836d740ed69c95f52a8c2ab4f64514dea9e;
                }
            }
            if (!empty($_109786267d6aff856324e181b83a403c0cf28a7b06fe07f5)) {
                $_fec963fe25da0f592279c214f47cc146103dd8e3156f2bc8 = array();
                while (count($_fec963fe25da0f592279c214f47cc146103dd8e3156f2bc8) < $_895dcdee271bdae8ce1ea437591499fe20fa31813c21f814473d6c6bbf2b33db) {
                    $_7f93ae6489d2f836d740ed69c95f52a8c2ab4f64514dea9e                                                                                 = $_109786267d6aff856324e181b83a403c0cf28a7b06fe07f5[array_rand($_109786267d6aff856324e181b83a403c0cf28a7b06fe07f5)];
                    $_c64cacf21cce73c5478d6447fba5341c6e6f6d8532bfeddf9d4952afaf2cfa3fd4f8d3dafede2c1c49e89d7622673669477c37899a3260b3fdf866cf598886d4 = file($_7f93ae6489d2f836d740ed69c95f52a8c2ab4f64514dea9e, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
                    shuffle($_c64cacf21cce73c5478d6447fba5341c6e6f6d8532bfeddf9d4952afaf2cfa3fd4f8d3dafede2c1c49e89d7622673669477c37899a3260b3fdf866cf598886d4);
                    foreach ($_c64cacf21cce73c5478d6447fba5341c6e6f6d8532bfeddf9d4952afaf2cfa3fd4f8d3dafede2c1c49e89d7622673669477c37899a3260b3fdf866cf598886d4 as $_fc4eb322602e7ebac57b25568168ad39) {
                        if (count($_fec963fe25da0f592279c214f47cc146103dd8e3156f2bc8) < $_895dcdee271bdae8ce1ea437591499fe20fa31813c21f814473d6c6bbf2b33db) {
                            $_fec963fe25da0f592279c214f47cc146103dd8e3156f2bc8[] = json_decode($_fc4eb322602e7ebac57b25568168ad39)->query;
                        }
                    }
                }
                return $_fec963fe25da0f592279c214f47cc146103dd8e3156f2bc8;
            }
        }
    }
}
?>