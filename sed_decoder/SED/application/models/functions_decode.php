<?php
class Functions
{
    public $_abfbd198884bed65b698b74d62c54cd47f44e048;
    
    public $_22b5520d76531e17f66ecd8a4c3c3980a5464e65570c2ae698cd887d;
    function __construct($_63d1c85e = '')
    {
        require_once $_63d1c85e . 'config.php';
        require_once $_63d1c85e . 'application/libraries/parser.php';
        $this->Base($_63d1c85e);
        $this->host = 'http://' . $_SERVER['HTTP_HOST'] . ROOT;
        $this->path = $_63d1c85e;
    }
    
    
    private function __clone()
    {
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
    public function GetController($_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0)
    {
        if (strlen(ROOT) > 0 AND strstr($_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0, ROOT)) {
            $_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0 = substr($_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0, strlen(ROOT));
        }
        $_c664bf14d074e1e853aab333954ca37b2dd9935d = '';
        if ($this->CheckUrl($_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0) == true)
            $_d2c90c806c279c9556583e7582a5b041 = array_values(explode('&', $_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0));
        else
            $_d2c90c806c279c9556583e7582a5b041 = array_values(array_filter(explode('/', $_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0)));
        if (!empty($_d2c90c806c279c9556583e7582a5b041[0])) {
            if (strrchr($_d2c90c806c279c9556583e7582a5b041[0], '=')) {
                $_d2c90c806c279c9556583e7582a5b041[0] = substr(strrchr($_d2c90c806c279c9556583e7582a5b041[0], '='), 1);
            }
            $_c664bf14d074e1e853aab333954ca37b2dd9935d = $_d2c90c806c279c9556583e7582a5b041[0];
        }
        return $_c664bf14d074e1e853aab333954ca37b2dd9935d;
    }
    public function GetFile($_2a63ed6a7953b76bfde59bf048629621a88a175cc5f8ca7e)
    {
        if (!empty($_2a63ed6a7953b76bfde59bf048629621a88a175cc5f8ca7e)) {
            $_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808 = md5($_2a63ed6a7953b76bfde59bf048629621a88a175cc5f8ca7e);
            switch (CACHE) {
                case 'index':
                    $_830c64617d808c22cbac79542c109a508a7e41a1 = "content/cache/$_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808.json";
                    break;
                case 'disc':
                    $_acb85238                                                                                                                         = implode('/', str_split($_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808, 1));
                    $_b8cb84225212fa57a73e186766644e9369e02c9925d03b656e0e2103c7e73ae36735d33fa1b8cceefe78afa8064a882c6e1cab3608a9cf1bb6ae427eec04f3e7 = pathinfo($_acb85238, PATHINFO_DIRNAME);
                    $_646d8196                                                                                                                         = "content/cache/$_b8cb84225212fa57a73e186766644e9369e02c9925d03b656e0e2103c7e73ae36735d33fa1b8cceefe78afa8064a882c6e1cab3608a9cf1bb6ae427eec04f3e7";
                    if (!is_dir($_646d8196)) {
                        if (!mkdir($_646d8196, 0755, true)) {
                            $this->Error("Can't create directory $_646d8196.");
                        }
                    }
                    $_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808 = pathinfo($_acb85238, PATHINFO_FILENAME);
                    $_830c64617d808c22cbac79542c109a508a7e41a1                 = "$_646d8196/$_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808.json";
                    break;
            }
            return $_830c64617d808c22cbac79542c109a508a7e41a1;
        }
    }
    public function GetFolders()
    {
        $_c7c341277cb0c87d85dc4fd8e04ce54974794f6975543e7cac75ed3657798016 = array();
        $_9e2b176976724abfe5b274f25d23e05a11006e2d8315c54b9fde1293cab62bbe = array(
            'userinc',
            'img',
            'css',
            'js'
        );
        $_2bf9b23157be3163a731e06cf344f3fe9775ad7b87a4cfcb                 = glob("templates/" . TEMPLATE . "/*", GLOB_ONLYDIR);
        foreach ($_2bf9b23157be3163a731e06cf344f3fe9775ad7b87a4cfcb as $_1e4d5021803ec7f87cbc31bcea7d7949b29487dd25d8a3899d6ebb8d0f91aa56a811c33cc4b6a0c5bef173aacc0fc49413c0d079861fc783645828f3984389c9) {
            $_be41fef6bae4f85b74de16000ba02ddf = pathinfo($_1e4d5021803ec7f87cbc31bcea7d7949b29487dd25d8a3899d6ebb8d0f91aa56a811c33cc4b6a0c5bef173aacc0fc49413c0d079861fc783645828f3984389c9, PATHINFO_FILENAME);
            if (!in_array($_be41fef6bae4f85b74de16000ba02ddf, $_9e2b176976724abfe5b274f25d23e05a11006e2d8315c54b9fde1293cab62bbe)) {
                $_c7c341277cb0c87d85dc4fd8e04ce54974794f6975543e7cac75ed3657798016[] = $_be41fef6bae4f85b74de16000ba02ddf;
            }
        }
        return $_c7c341277cb0c87d85dc4fd8e04ce54974794f6975543e7cac75ed3657798016;
    }
    public function GetHTML($_d5d95599389f1efbadea727fd5354be542e9a22d0d2e39034bd21552, $_22e3f969)
    {
        $_12f5e7547880cda6b32fbf0592aba5425584ae1fb81ad963186a09ecf6eb04125f544e3b2cb42303be9a3fd41c4c56a94634a61e337807f2e5b0c659ec278308 = $this->GetContent('txt/browsers.txt');
        if (in_array('curl', get_loaded_extensions())) {
            $_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a = curl_init();
            curl_setopt($_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a, CURLOPT_URL, $_d5d95599389f1efbadea727fd5354be542e9a22d0d2e39034bd21552);
            curl_setopt($_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a, CURLOPT_RETURNTRANSFER, 1);
            curl_setopt($_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a, CURLOPT_USERAGENT, $_12f5e7547880cda6b32fbf0592aba5425584ae1fb81ad963186a09ecf6eb04125f544e3b2cb42303be9a3fd41c4c56a94634a61e337807f2e5b0c659ec278308);
            $_d40546bd122c921a2182b4e0e2ea5ee6 = $this->GetContent('txt/proxies.txt');
            if ($_d40546bd122c921a2182b4e0e2ea5ee6) {
                curl_setopt($_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a, CURLOPT_PROXY, $_d40546bd122c921a2182b4e0e2ea5ee6);
                curl_setopt($_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a, CURLOPT_PROXYTYPE, CURLPROXY_SOCKS5);
            }
            curl_setopt($_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a, CURLOPT_TIMEOUT, 15);
            $_04dd72e973847607c7d4e6cd938f170c49d14a9e78bedb7c2158d4c2 = curl_exec($_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a);
            curl_close($_7b294f11adad1fec1f148fcfbb02deec25f8c1d129ccd266eb856b0c61cb7bc3e06bcd53b145686a);
            $_9b9540098da4f542604041ec8927619e4d00f84cf84223de20e7a88d18c0752fe525178a650f9f6e3818a80714305e53 = str_get_html($_04dd72e973847607c7d4e6cd938f170c49d14a9e78bedb7c2158d4c2);
            if (is_bool($_9b9540098da4f542604041ec8927619e4d00f84cf84223de20e7a88d18c0752fe525178a650f9f6e3818a80714305e53)) {
                $this->Error("Proxy $_d40546bd122c921a2182b4e0e2ea5ee6 does not work. Please remove it.");
            }
        } else {
            $_cf9bbb7ed922b875cad68bfc1bae3f950a75a194985c8c1fcaf3c76a954f6f1a3eb1e50021cb5a187fe615b6eb27ff17ebaf0990546c441edd13f1447a1f952b = array(
                'http' => array(
                    'method' => "GET",
                    'header' => "Host: $_22e3f969" . "User-Agent: $_12f5e7547880cda6b32fbf0592aba5425584ae1fb81ad963186a09ecf6eb04125f544e3b2cb42303be9a3fd41c4c56a94634a61e337807f2e5b0c659ec278308"
                )
            );
            $_f7afe154ad13c034c053d8ff862e861eaa953834827fd725ee6c9303e7e82cdc                                                                 = stream_context_create($_cf9bbb7ed922b875cad68bfc1bae3f950a75a194985c8c1fcaf3c76a954f6f1a3eb1e50021cb5a187fe615b6eb27ff17ebaf0990546c441edd13f1447a1f952b);
            $_9b9540098da4f542604041ec8927619e4d00f84cf84223de20e7a88d18c0752fe525178a650f9f6e3818a80714305e53                                 = file_get_html($_d5d95599389f1efbadea727fd5354be542e9a22d0d2e39034bd21552, false, $_f7afe154ad13c034c053d8ff862e861eaa953834827fd725ee6c9303e7e82cdc);
        }
        return $_9b9540098da4f542604041ec8927619e4d00f84cf84223de20e7a88d18c0752fe525178a650f9f6e3818a80714305e53;
    }
    public function GetImage($_2bb2d272f5ece767095f2ae4ed4540ec)
    {
        $_1f5f5b265100daad35b3a491e1c55351 = $this->host . '/image.php?' . base64_encode($_2bb2d272f5ece767095f2ae4ed4540ec);
        return $_1f5f5b265100daad35b3a491e1c55351;
    }
    public function GetKeyword()
    {
        $_01f300c5 = implode($this->GetQueries(1));
        return $_01f300c5;
    }
    public function GetLinks($_af331011bd23561cfa616cc345bf104468315b5b, $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc)
    {
        $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc = array_reverse($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc);
        $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc = array_slice($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc, 0, COUNT_SNIPPETS);
        if (STOP_GROWING == 'false') {
            switch (SUBJECT_LIMITATION) {
                case 'true':
                    $_2b55ce0a9dc42f0e103a7146cdcd8b8b = explode(' ', $_af331011bd23561cfa616cc345bf104468315b5b);
                    foreach ($_2b55ce0a9dc42f0e103a7146cdcd8b8b as $_4eff36155b347beceb090be634ed874dce1a1738a95d3b1c22bf7437b80af3974c2656ac6fcb04d5 => $_2c8b871e52d4e5f5db5ff84a82a45327e20df77edef961c4b6fa0e9c3d97ce5b) {
                        if (strlen($_2c8b871e52d4e5f5db5ff84a82a45327e20df77edef961c4b6fa0e9c3d97ce5b) < 4) {
                            unset($_2b55ce0a9dc42f0e103a7146cdcd8b8b[$_4eff36155b347beceb090be634ed874dce1a1738a95d3b1c22bf7437b80af3974c2656ac6fcb04d5]);
                        }
                    }
                    $_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d = 0;
                    foreach ($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc as $_f272a53b0ef1c576ff83d5cbdb2278970c863fb8 => $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3) {
                        if ($_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d <= COUNT_LINKS) {
                            foreach ($_2b55ce0a9dc42f0e103a7146cdcd8b8b as $_2c8b871e52d4e5f5db5ff84a82a45327e20df77edef961c4b6fa0e9c3d97ce5b) {
                                if (preg_match('|' . mb_strtolower($_2c8b871e52d4e5f5db5ff84a82a45327e20df77edef961c4b6fa0e9c3d97ce5b, 'UTF-8') . '|', mb_strtolower($_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'], 'UTF-8'))) {
                                    if (ONLY_EXISTING_KEYWORDS == 'false') {
                                        if (!empty($_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['words'])) {
                                            $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['link'] = '<a href="' . $this->CreateUrl("/" . RESULT . "/" . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['words']) . '" title="' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '">' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '</a>';
                                        }
                                    } else {
                                        $_5fcf2c9c13847fbe85391de6c646fa24                                                                      = $this->GetKeyword();
                                        $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['link']  = '<a href="' . $this->CreateUrl("/" . RESULT . "/$_5fcf2c9c13847fbe85391de6c646fa24") . '" title="' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '">' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '</a>';
                                        $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['words'] = $_5fcf2c9c13847fbe85391de6c646fa24;
                                    }
                                    $_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d++;
                                }
                            }
                        } else {
                            if (ONLY_EXISTING_KEYWORDS == 'true') {
                                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['words'] = '';
                            }
                        }
                    }
                    break;
                case 'false':
                    $_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d = 0;
                    foreach ($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc as $_f272a53b0ef1c576ff83d5cbdb2278970c863fb8 => $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3) {
                        if ($_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d < COUNT_LINKS) {
                            if (ONLY_EXISTING_KEYWORDS == 'false') {
                                if (!empty($_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['words'])) {
                                    $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['link'] = '<a href="' . $this->CreateUrl("/" . RESULT . "/" . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['words']) . '" title="' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '">' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '</a>';
                                }
                            } else {
                                $_5fcf2c9c13847fbe85391de6c646fa24                                                                      = $this->GetKeyword();
                                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['link']  = '<a href="' . $this->CreateUrl("/" . RESULT . "/$_5fcf2c9c13847fbe85391de6c646fa24") . '" title="' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '">' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '</a>';
                                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['words'] = $_5fcf2c9c13847fbe85391de6c646fa24;
                            }
                            $_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d++;
                        } else {
                            if (ONLY_EXISTING_KEYWORDS == 'true') {
                                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['words'] = '';
                            }
                        }
                    }
                    break;
            }
        }
        $_f1ea666ca26a11a5e7854267b5e642adf79b94df1a720349 = $this->GetContent('txt/external_linking.txt');
        if (EXTERNAL_LINKING == 'true' and !empty($_f1ea666ca26a11a5e7854267b5e642adf79b94df1a720349)) {
            if (ONLY_EXISTING_KEYWORDS == 'false') {
                if (!empty($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['words'])) {
                    $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['link'] = '<a href="' . $_f1ea666ca26a11a5e7854267b5e642adf79b94df1a720349 . '?' . CONTROLLER . '=' . RESULT . '&amp;' . QUERY . '=' . urlencode($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['words']) . '" title="' . $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['title'] . '">' . $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['title'] . '</a>';
                }
            } else {
                $_5fcf2c9c13847fbe85391de6c646fa24                             = $this->GetKeyword();
                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['link']  = '<a href="' . $_f1ea666ca26a11a5e7854267b5e642adf79b94df1a720349 . '?' . CONTROLLER . '=' . RESULT . '&amp;' . QUERY . '=' . urlencode($_5fcf2c9c13847fbe85391de6c646fa24) . '" title="' . $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['title'] . '">' . $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['title'] . '</a>';
                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['words'] = $_5fcf2c9c13847fbe85391de6c646fa24;
            }
        }
        return array_reverse($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc);
    }
    public function GetPage($_0d16bb215d8dc885d3d525a314af36ae72730c782a3616e67df4b97a410364db)
    {
        $_b4e4afecb1a4ad3d4700fa8088e85617acd4fdceea05ae70 = $this->GetFolders();
        if (in_array($_0d16bb215d8dc885d3d525a314af36ae72730c782a3616e67df4b97a410364db, $_b4e4afecb1a4ad3d4700fa8088e85617acd4fdceea05ae70)) {
            $_3e1c1c3c88a4b996390eafbc3720be28 = glob("templates/" . TEMPLATE . "/$_0d16bb215d8dc885d3d525a314af36ae72730c782a3616e67df4b97a410364db/*.php", GLOB_NOSORT);
            if (empty($_3e1c1c3c88a4b996390eafbc3720be28)) {
                $_3e1c1c3c88a4b996390eafbc3720be28                                 = glob("templates/" . TEMPLATE . "/result/*.php", GLOB_NOSORT);
                $_0d16bb215d8dc885d3d525a314af36ae72730c782a3616e67df4b97a410364db = 'result';
            }
        } else {
            $_3e1c1c3c88a4b996390eafbc3720be28                                 = glob("templates/" . TEMPLATE . "/result/*.php", GLOB_NOSORT);
            $_0d16bb215d8dc885d3d525a314af36ae72730c782a3616e67df4b97a410364db = 'result';
        }
        $_a350da74412ca741d07ab86e847badc1 = array();
        foreach ($_3e1c1c3c88a4b996390eafbc3720be28 as $_eb04716e063f60d082bc85a23c86141f1266a326f71cd6e5) {
            $_2436d80a0d005509e23101ca51db99483569686d = pathinfo($_eb04716e063f60d082bc85a23c86141f1266a326f71cd6e5, PATHINFO_FILENAME);
            if ($_2436d80a0d005509e23101ca51db99483569686d !== 'index') {
                $_a350da74412ca741d07ab86e847badc1[] = $_2436d80a0d005509e23101ca51db99483569686d;
            }
        }
        $_2436d80a0d005509e23101ca51db99483569686d = $_a350da74412ca741d07ab86e847badc1[array_rand($_a350da74412ca741d07ab86e847badc1)];
        return "$_0d16bb215d8dc885d3d525a314af36ae72730c782a3616e67df4b97a410364db/$_2436d80a0d005509e23101ca51db99483569686d";
    }
    public function GetPages()
    {
        $_3ce553f1b6cb49103bf55f430a933ac0999f27df                 = array();
        $_f78b913cbaa45d46af1cc61c6f436acf405f706bf60ed736e8f260a1 = array(
            'base',
            'home'
        );
        $_055f5d97                                                 = glob('templates/' . TEMPLATE . '/*.php', GLOB_NOSORT);
        foreach ($_055f5d97 as $_d7d8bc782ae1643b6fabf261332ee4a32683713c) {
            $_d497205f9bbb4ff229717ce2cc831599b2afd711b2457309fa95d556 = pathinfo($_d7d8bc782ae1643b6fabf261332ee4a32683713c, PATHINFO_FILENAME);
            if (!in_array($_d497205f9bbb4ff229717ce2cc831599b2afd711b2457309fa95d556, $_f78b913cbaa45d46af1cc61c6f436acf405f706bf60ed736e8f260a1)) {
                $_3ce553f1b6cb49103bf55f430a933ac0999f27df[] = $_d497205f9bbb4ff229717ce2cc831599b2afd711b2457309fa95d556;
            }
        }
        return $_3ce553f1b6cb49103bf55f430a933ac0999f27df;
    }
    public function GetPluginObject($_6ffd3bb3dc9612915eefa0d60b27921aed811474, $_5866db2005a6e7ce2e3073c8bf09e05c)
    {
        require_once $this->path . "application/plugins/$_5866db2005a6e7ce2e3073c8bf09e05c/$_6ffd3bb3dc9612915eefa0d60b27921aed811474.php";
        $_243d57c7adf74a735417366273a728ad7573d74b = new ReflectionClass($_6ffd3bb3dc9612915eefa0d60b27921aed811474);
        if ($_243d57c7adf74a735417366273a728ad7573d74b->getConstructor() === NULL) {
            $_95196c95ae919ba5a83d3717fddb8e28 = new $_6ffd3bb3dc9612915eefa0d60b27921aed811474();
            return $_95196c95ae919ba5a83d3717fddb8e28;
        }
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
    public function GetQuery($_ceda46bee2c569e571611d5db5f7efd6)
    {
        if (strlen(ROOT) > 0 AND strstr($_ceda46bee2c569e571611d5db5f7efd6, ROOT)) {
            $_ceda46bee2c569e571611d5db5f7efd6 = substr($_ceda46bee2c569e571611d5db5f7efd6, strlen(ROOT));
        }
        $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = '';
        if ($this->CheckUrl($_ceda46bee2c569e571611d5db5f7efd6) == true)
            $_4681ef5daa26f8844d1f99e2573cdaec198fd2fc5ee06c96562c883733185b5d = array_values(explode('&', $_ceda46bee2c569e571611d5db5f7efd6));
        else
            $_4681ef5daa26f8844d1f99e2573cdaec198fd2fc5ee06c96562c883733185b5d = array_values(array_filter(explode('/', $_ceda46bee2c569e571611d5db5f7efd6)));
        if (!empty($_4681ef5daa26f8844d1f99e2573cdaec198fd2fc5ee06c96562c883733185b5d[1])) {
            $_ceda46bee2c569e571611d5db5f7efd6 = urldecode($_4681ef5daa26f8844d1f99e2573cdaec198fd2fc5ee06c96562c883733185b5d[1]);
            if (strrchr($_ceda46bee2c569e571611d5db5f7efd6, '=')) {
                $_ceda46bee2c569e571611d5db5f7efd6                         = substr($_ceda46bee2c569e571611d5db5f7efd6, strpos($_ceda46bee2c569e571611d5db5f7efd6, '=') + 1);
                $_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768 = md5($_ceda46bee2c569e571611d5db5f7efd6);
                $_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569         = file("content/base/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[0]/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[1].txt", FILE_IGNORE_NEW_LINES);
                foreach ($_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569 as $_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a) {
                    if ($_ceda46bee2c569e571611d5db5f7efd6 == json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->query) {
                        $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->query;
                    }
                }
            }
            if (strrchr($_ceda46bee2c569e571611d5db5f7efd6, '.')) {
                $_ceda46bee2c569e571611d5db5f7efd6 = substr($_ceda46bee2c569e571611d5db5f7efd6, 0, strrpos($_ceda46bee2c569e571611d5db5f7efd6, '.'));
            }
            if (preg_match('|[a-z0-9]{32}|', $_ceda46bee2c569e571611d5db5f7efd6)) {
                if (strrchr($_ceda46bee2c569e571611d5db5f7efd6, '-')) {
                    $_ceda46bee2c569e571611d5db5f7efd6 = substr(strrchr($_ceda46bee2c569e571611d5db5f7efd6, '-'), 1);
                }
                $_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768 = $_ceda46bee2c569e571611d5db5f7efd6;
                $_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569         = file("content/base/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[0]/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[1].txt", FILE_IGNORE_NEW_LINES);
                foreach ($_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569 as $_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a) {
                    if ($_ceda46bee2c569e571611d5db5f7efd6 == json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->md5) {
                        $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->query;
                    }
                }
            } else {
                if (strrchr($_ceda46bee2c569e571611d5db5f7efd6, '-')) {
                    $_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768 = substr(strrchr($_ceda46bee2c569e571611d5db5f7efd6, '-'), 1);
                    $_ceda46bee2c569e571611d5db5f7efd6                         = substr($_ceda46bee2c569e571611d5db5f7efd6, 0, -3);
                    $_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569         = file("content/base/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[0]/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[1].txt", FILE_IGNORE_NEW_LINES);
                    foreach ($_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569 as $_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a) {
                        if ($_ceda46bee2c569e571611d5db5f7efd6 == json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->translit) {
                            $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->query;
                        }
                    }
                } else {
                    $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = $_ceda46bee2c569e571611d5db5f7efd6;
                }
            }
        }
        return $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca;
    }
    private function GetSnippets($_22541c8c0e7d3f47a1e88ff50882af5a03f132135bbf4a5866aee36e6a9a9a31889fed51a512e7466b8a242c3ad153a1fa21d55b45a9c0d64b4d511e7280bd57, $_2333641c600b77573d1a03df3234ba6ba06d650280d9310de18b8efaed64f76ae79fdbdc316b2ddaabe87e0024e2e2e6838e8372020b945bb4a673ac09ba5333, $_644e9393b50247621f2ac8b742d82bca)
    {
        foreach (glob('application/plugins/snippets/*.php') as $_b15b341b28412c5c2d989d4c9f4e8a4c61887e28e4822c54) {
            $_b15b341b28412c5c2d989d4c9f4e8a4c61887e28e4822c54 = pathinfo($_b15b341b28412c5c2d989d4c9f4e8a4c61887e28e4822c54, PATHINFO_FILENAME);
            if (!in_array($_b15b341b28412c5c2d989d4c9f4e8a4c61887e28e4822c54, $_22541c8c0e7d3f47a1e88ff50882af5a03f132135bbf4a5866aee36e6a9a9a31889fed51a512e7466b8a242c3ad153a1fa21d55b45a9c0d64b4d511e7280bd57)) {
                $_766825cd89dbaf318b1f82205b9c0b5223ec2acc4ce7aef4b5f754c422380710[] = $_b15b341b28412c5c2d989d4c9f4e8a4c61887e28e4822c54;
            }
        }
        $_b15b341b28412c5c2d989d4c9f4e8a4c61887e28e4822c54         = $_766825cd89dbaf318b1f82205b9c0b5223ec2acc4ce7aef4b5f754c422380710[array_rand($_766825cd89dbaf318b1f82205b9c0b5223ec2acc4ce7aef4b5f754c422380710)];
        $_ff4fb8014b021536959088600fb82fbd11099d4ecd924b57c37296f8 = $this->GetPluginObject($_b15b341b28412c5c2d989d4c9f4e8a4c61887e28e4822c54, 'snippets');
        $_644e9393b50247621f2ac8b742d82bca                         = COUNT_SNIPPETS - $_644e9393b50247621f2ac8b742d82bca;
        $_ab5718577dba7411e6360ad11e69a886c7783262e4ecd7e61b4528d7 = $_ff4fb8014b021536959088600fb82fbd11099d4ecd924b57c37296f8->Start($_2333641c600b77573d1a03df3234ba6ba06d650280d9310de18b8efaed64f76ae79fdbdc316b2ddaabe87e0024e2e2e6838e8372020b945bb4a673ac09ba5333, LANGUAGE, $_644e9393b50247621f2ac8b742d82bca, $this);
        return $_ab5718577dba7411e6360ad11e69a886c7783262e4ecd7e61b4528d7;
    }
    public function Ping($_7a9e800f5606ae4fca16e95ce4220d39f81308cf3fa83bf582846da8caba62ab601106ccfdf3db99f204d223a70bcda4172b305ecd3c674f8d264ceaf1d8dca0, $_b12a34b13e42d833b95cdecf1ed08f4f7dd817348d5ce26b)
    {
        if (strlen(PINGERS) > 0) {
            $_e4663075aeb7735b7f820ea0c37c0067015e2616fe72c23ca3442aab0418ac53 = explode(' | ', PINGERS);
            foreach ($_e4663075aeb7735b7f820ea0c37c0067015e2616fe72c23ca3442aab0418ac53 as $_957b4de48fd8d8a83cc5be1ca9d34338fd748a3b16f96ba286894f220eb67a63) {
                $_9244c78f80042674d2bc2a5d8d1fccf3fa6c463800c590eb99552cff = $this->GetPluginObject($_957b4de48fd8d8a83cc5be1ca9d34338fd748a3b16f96ba286894f220eb67a63, 'pingers');
                $_9244c78f80042674d2bc2a5d8d1fccf3fa6c463800c590eb99552cff->Start($_7a9e800f5606ae4fca16e95ce4220d39f81308cf3fa83bf582846da8caba62ab601106ccfdf3db99f204d223a70bcda4172b305ecd3c674f8d264ceaf1d8dca0, $_b12a34b13e42d833b95cdecf1ed08f4f7dd817348d5ce26b, $this);
            }
        }
    }
    public function Query()
    {
        switch (CACHE) {
            case 'index':
                $_a908108927b64fb6ffd67df098c977ec32c8cbfd6f6b394c = glob("content/cache/*.json", GLOB_NOSORT);
                if (!empty($_a908108927b64fb6ffd67df098c977ec32c8cbfd6f6b394c)) {
                    $_d3fec7709e289ab2feaec2443e030e882773a6e087505ae29150c0509cc6d16b = $_a908108927b64fb6ffd67df098c977ec32c8cbfd6f6b394c[array_rand($_a908108927b64fb6ffd67df098c977ec32c8cbfd6f6b394c)];
                    $_d8473e479b1342d802bc2c984cd9b005f94ba7e5e04364fc2bcb3db6         = pathinfo($_d3fec7709e289ab2feaec2443e030e882773a6e087505ae29150c0509cc6d16b, PATHINFO_FILENAME);
                    $_4cc66e33b18797d3d3858fd8be8d95886d747e95d23925d52229d681         = $this->GetQuery("/result/$_d8473e479b1342d802bc2c984cd9b005f94ba7e5e04364fc2bcb3db6");
                    return $_4cc66e33b18797d3d3858fd8be8d95886d747e95d23925d52229d681;
                }
                break;
            case 'disc':
                $_d8473e479b1342d802bc2c984cd9b005f94ba7e5e04364fc2bcb3db6 = $this->GetContent('content/cache/disc.txt');
                if (!empty($_d8473e479b1342d802bc2c984cd9b005f94ba7e5e04364fc2bcb3db6)) {
                    $_4cc66e33b18797d3d3858fd8be8d95886d747e95d23925d52229d681 = $this->GetQuery("/result/$_d8473e479b1342d802bc2c984cd9b005f94ba7e5e04364fc2bcb3db6");
                    return $_4cc66e33b18797d3d3858fd8be8d95886d747e95d23925d52229d681;
                }
                break;
        }
    }
    
    public function RSS($_b135f9591e66b399d6a9b1bcbb1f9150527760a5, $_1dfceae0a1cd7543e410f34db3b0b65d44c39e95745e0dd33c010be5) 	
    { 		
    	$_8a88c4a1ce3156189a97f170038ed0b7cf58a16284ec24ed69118866b1811cdba98a2466f2892917775399b29b0fa8ff = $this->CreateUrl("/rss/$_b135f9591e66b399d6a9b1bcbb1f9150527760a5"); 		
    	$_8a88c4a1ce3156189a97f170038ed0b7cf58a16284ec24ed69118866b1811cdba98a2466f2892917775399b29b0fa8ff = htmlspecialchars($_8a88c4a1ce3156189a97f170038ed0b7cf58a16284ec24ed69118866b1811cdba98a2466f2892917775399b29b0fa8ff); 		
    	$_08335878eca8e1419deaf97039c57339 = TITLE;  		
    	$_09c0b1a8eeef438163f6b502752f38378698a831e742f8f7c2237dbb = <<<CONTENT><?xml version="1.0" encoding="UTF-8"?><rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom"><channel><title>$_b135f9591e66b399d6a9b1bcbb1f9150527760a5</title><link>$this->host</link><atom:link type="application/rss+xml" rel="self" href="$_8a88c4a1ce3156189a97f170038ed0b7cf58a16284ec24ed69118866b1811cdba98a2466f2892917775399b29b0fa8ff"/><description>$_08335878eca8e1419deaf97039c57339</description><language>ru-ru</language>
CONTENT; 		
	foreach ($_1dfceae0a1cd7543e410f34db3b0b65d44c39e95745e0dd33c010be5 as $_16caf1ac00028d20199526210e6b0fcc => $_020900cf) 		
	{ 			
		$_808920afa560ce4bc492a5965526b3dc6c2ae8c8 = htmlspecialchars($_020900cf['title']); 			
		$_eb23bf1a1c14db64e38470ba72858a6265079a0674d8a680 = $this->GetKeyword(); 			
		$_a21b7b546f5a845ef2da69a0d873f07a = $this->CreateUrl("/" . RESULT . "/$_eb23bf1a1c14db64e38470ba72858a6265079a0674d8a680"); 			
		$_77f82a67 = htmlspecialchars($_a21b7b546f5a845ef2da69a0d873f07a); 			
		$_08335878eca8e1419deaf97039c57339 = htmlspecialchars($_020900cf['description']); 			
		$_b2ccea514538dfca259ddf4e365bfed7 = $this->host . "/#$_16caf1ac00028d20199526210e6b0fcc";  			
		$_09c0b1a8eeef438163f6b502752f38378698a831e742f8f7c2237dbb .= <<<CONTENT<item><title>$_808920afa560ce4bc492a5965526b3dc6c2ae8c8</title><link>$_77f82a67</link><description>$_08335878eca8e1419deaf97039c57339</description><guid>$_b2ccea514538dfca259ddf4e365bfed7</guid></item>
CONTENT; 		
	}  		
	$_09c0b1a8eeef438163f6b502752f38378698a831e742f8f7c2237dbb .= <<<CONTENT</channel></rss>
CONTENT; 		
	return $_09c0b1a8eeef438163f6b502752f38378698a831e742f8f7c2237dbb; 	
	}  		
    
    public function Redirect($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904)
    {
        if ($this->CheckUrl($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904) == true AND FU_STATUS == 'ON') {
            header('HTTP/1.1 301 Moved Permanently');
            header('Location: ' . $this->CreateUrl($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904));
            exit();
        } elseif ($this->CheckUrl($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904) == false AND FU_STATUS == 'OFF') {
            header('HTTP/1.1 301 Moved Permanently');
            header('Location: ' . $this->CreateUrl($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904));
            exit();
        }
    }
    private function RemoveCache()
    {
        if (CACHE == 'index') {
            $_e6dde855d475ea028531318458dbff203af2979082229273a0c64646ab188d8d = glob("content/cache/*.json", GLOB_NOSORT);
            if (count($_e6dde855d475ea028531318458dbff203af2979082229273a0c64646ab188d8d) > COUNT_FILES) {
                $_a8ccde601ab45a4e801148ef3b682d62b3afe39c6ef8d8ef2da2646110eb8015 = array();
                foreach ($_e6dde855d475ea028531318458dbff203af2979082229273a0c64646ab188d8d as $_de6c23dc90b9221fe0150f8a68ec733b796edddca14b69d2fff8f472a4ec797f416319202533dcbdef69a4ee654fab8d => $_107efea6c91609d2ad416bddba15a8a8) {
                    $_a8ccde601ab45a4e801148ef3b682d62b3afe39c6ef8d8ef2da2646110eb8015[$_de6c23dc90b9221fe0150f8a68ec733b796edddca14b69d2fff8f472a4ec797f416319202533dcbdef69a4ee654fab8d]['time'] = time() - filemtime($_107efea6c91609d2ad416bddba15a8a8);
                    $_a8ccde601ab45a4e801148ef3b682d62b3afe39c6ef8d8ef2da2646110eb8015[$_de6c23dc90b9221fe0150f8a68ec733b796edddca14b69d2fff8f472a4ec797f416319202533dcbdef69a4ee654fab8d]['name'] = $_107efea6c91609d2ad416bddba15a8a8;
                }
                arsort($_a8ccde601ab45a4e801148ef3b682d62b3afe39c6ef8d8ef2da2646110eb8015);
                $_de6c23dc90b9221fe0150f8a68ec733b796edddca14b69d2fff8f472a4ec797f416319202533dcbdef69a4ee654fab8d = 1;
                $_f7b2383990085a67de60b6ea18137d4c                                                                 = count($_e6dde855d475ea028531318458dbff203af2979082229273a0c64646ab188d8d) - COUNT_FILES;
                foreach ($_a8ccde601ab45a4e801148ef3b682d62b3afe39c6ef8d8ef2da2646110eb8015 as $_705dfcb39e717508f1841d6907e2b4da) {
                    if ($_de6c23dc90b9221fe0150f8a68ec733b796edddca14b69d2fff8f472a4ec797f416319202533dcbdef69a4ee654fab8d <= $_f7b2383990085a67de60b6ea18137d4c) {
                        unlink($_705dfcb39e717508f1841d6907e2b4da['name']);
                        $_de6c23dc90b9221fe0150f8a68ec733b796edddca14b69d2fff8f472a4ec797f416319202533dcbdef69a4ee654fab8d++;
                    }
                }
            }
        }
    }
    public function RemoveDirectory($_69f106769fa8487f68cd1f61fd9f189802df3325904a2a256ba5b559e132dc6fdbb7074b0a4ccf576435982b56af0464bd5e31a36a01c036ccc30295f91b88d9)
    {
        if ($_b792a60520c778f0b55c8f5ddeb3a1b4031fecbb337e68a6966f2ae7 = glob($_69f106769fa8487f68cd1f61fd9f189802df3325904a2a256ba5b559e132dc6fdbb7074b0a4ccf576435982b56af0464bd5e31a36a01c036ccc30295f91b88d9 . "/*")) {
            foreach ($_b792a60520c778f0b55c8f5ddeb3a1b4031fecbb337e68a6966f2ae7 as $_29708e134ec9079f1d5f191db42c1b6a933efe72eed057b0fef463212fbeda7b7ebaba03f6b709f9ee29c75ec5a612fc58879badacc843c6b4391ad3382cd460) {
                if (file_exists("$_69f106769fa8487f68cd1f61fd9f189802df3325904a2a256ba5b559e132dc6fdbb7074b0a4ccf576435982b56af0464bd5e31a36a01c036ccc30295f91b88d9/.htaccess"))
                    unlink("$_69f106769fa8487f68cd1f61fd9f189802df3325904a2a256ba5b559e132dc6fdbb7074b0a4ccf576435982b56af0464bd5e31a36a01c036ccc30295f91b88d9/.htaccess");
                is_dir($_29708e134ec9079f1d5f191db42c1b6a933efe72eed057b0fef463212fbeda7b7ebaba03f6b709f9ee29c75ec5a612fc58879badacc843c6b4391ad3382cd460) ? $this->RemoveDirectory($_29708e134ec9079f1d5f191db42c1b6a933efe72eed057b0fef463212fbeda7b7ebaba03f6b709f9ee29c75ec5a612fc58879badacc843c6b4391ad3382cd460) : unlink($_29708e134ec9079f1d5f191db42c1b6a933efe72eed057b0fef463212fbeda7b7ebaba03f6b709f9ee29c75ec5a612fc58879badacc843c6b4391ad3382cd460);
            }
        }
        rmdir($_69f106769fa8487f68cd1f61fd9f189802df3325904a2a256ba5b559e132dc6fdbb7074b0a4ccf576435982b56af0464bd5e31a36a01c036ccc30295f91b88d9);
    }
    public function Run($_db8a5b5600156c1efdeaeb38444c4e9c)
    {
        $_c11943b9eebf20348fb9bfb1d493ad21885d1c3c8fc75cb8bcef45da07099027f8412110800e76cda35a95201fed095087e4bb5b04be73691d706285b95b6828 = $this->GetFile($_db8a5b5600156c1efdeaeb38444c4e9c);
        if (!file_exists($_c11943b9eebf20348fb9bfb1d493ad21885d1c3c8fc75cb8bcef45da07099027f8412110800e76cda35a95201fed095087e4bb5b04be73691d706285b95b6828)) {
            $_fcf767872b692923db759586ca0e32c8 = array();
            if (strlen(SNIPPETS) > 0 AND !array_key_exists('snippets', $_fcf767872b692923db759586ca0e32c8)) {
                $_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d = array();
                $_2e1a433c4685ebc30c75f7aaacae940c                                                                                                 = explode(' | ', SNIPPETS);
                $_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c = array_rand($_2e1a433c4685ebc30c75f7aaacae940c);
                $_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c                                                                 = $this->GetPluginObject($_2e1a433c4685ebc30c75f7aaacae940c[$_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c], 'snippets');
                foreach ($_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c->Start($_db8a5b5600156c1efdeaeb38444c4e9c, LANGUAGE, COUNT_SNIPPETS, $this) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                    $_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d[] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                }
                while (COUNT_SNIPPETS > count($_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d)) {
                    foreach ($this->GetSnippets($_2e1a433c4685ebc30c75f7aaacae940c, $_db8a5b5600156c1efdeaeb38444c4e9c, count($_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d)) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                        $_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d[] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                    }
                }
                $_fcf767872b692923db759586ca0e32c8['snippets'] = $this->Dublier($_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d);
                if (!empty($_fcf767872b692923db759586ca0e32c8['snippets'])) {
                    shuffle($_fcf767872b692923db759586ca0e32c8['snippets']);
                    $_fcf767872b692923db759586ca0e32c8['snippets'] = $this->Filter($_fcf767872b692923db759586ca0e32c8['snippets']);
                    $this->BaseWriter($_db8a5b5600156c1efdeaeb38444c4e9c, $_fcf767872b692923db759586ca0e32c8['snippets']);
                }
            }
            if (strlen(IMAGES) > 0 AND !array_key_exists('images', $_fcf767872b692923db759586ca0e32c8)) {
                $_2e1a433c4685ebc30c75f7aaacae940c                                 = explode(' | ', IMAGES);
                $_9e142ce28152156494bcd6f604effd85512fdb98ec8b1788f5e2aaf376a3e56b = floor(20 / count($_2e1a433c4685ebc30c75f7aaacae940c));
                foreach ($_2e1a433c4685ebc30c75f7aaacae940c as $_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c) {
                    $_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c = $this->GetPluginObject($_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c, 'images');
                    foreach ($_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c->Start($_db8a5b5600156c1efdeaeb38444c4e9c, $_9e142ce28152156494bcd6f604effd85512fdb98ec8b1788f5e2aaf376a3e56b, $this) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                        $_fcf767872b692923db759586ca0e32c8['images'][] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                    }
                }
                if (!empty($_fcf767872b692923db759586ca0e32c8['images'])) {
                    shuffle($_fcf767872b692923db759586ca0e32c8['images']);
                }
            }
            if (strlen(VIDEOS) > 0 AND !array_key_exists('videos', $_fcf767872b692923db759586ca0e32c8)) {
                $_2e1a433c4685ebc30c75f7aaacae940c                                 = explode(' | ', VIDEOS);
                $_9e142ce28152156494bcd6f604effd85512fdb98ec8b1788f5e2aaf376a3e56b = floor(20 / count($_2e1a433c4685ebc30c75f7aaacae940c));
                foreach ($_2e1a433c4685ebc30c75f7aaacae940c as $_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c) {
                    $_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c = $this->GetPluginObject($_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c, 'videos');
                    foreach ($_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c->Start($_db8a5b5600156c1efdeaeb38444c4e9c, $_9e142ce28152156494bcd6f604effd85512fdb98ec8b1788f5e2aaf376a3e56b, $this) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                        $_fcf767872b692923db759586ca0e32c8['videos'][] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                    }
                }
                if (!empty($_fcf767872b692923db759586ca0e32c8['videos'])) {
                    shuffle($_fcf767872b692923db759586ca0e32c8['videos']);
                }
            }
            if (strlen(COMMENTS) > 0 AND !array_key_exists('comments', $_fcf767872b692923db759586ca0e32c8)) {
                $_2e1a433c4685ebc30c75f7aaacae940c = explode(' | ', COMMENTS);
                foreach ($_2e1a433c4685ebc30c75f7aaacae940c as $_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c) {
                    $_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c = $this->GetPluginObject($_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c, 'comments');
                    foreach ($_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c->Start($this) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                        $_fcf767872b692923db759586ca0e32c8['comments'][] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                    }
                }
            }
            if (!empty($_fcf767872b692923db759586ca0e32c8)) {
                $this->RemoveCache();
                $this->CreateCache($_c11943b9eebf20348fb9bfb1d493ad21885d1c3c8fc75cb8bcef45da07099027f8412110800e76cda35a95201fed095087e4bb5b04be73691d706285b95b6828, $_fcf767872b692923db759586ca0e32c8);
                $this->SaveCache($_db8a5b5600156c1efdeaeb38444c4e9c);
                if (!empty($_fcf767872b692923db759586ca0e32c8['snippets'])) {
                    $_fcf767872b692923db759586ca0e32c8['snippets'] = $this->GetLinks($_db8a5b5600156c1efdeaeb38444c4e9c, $_fcf767872b692923db759586ca0e32c8['snippets']);
                }
            }
            return $_fcf767872b692923db759586ca0e32c8;
        } else {
            $_fcf767872b692923db759586ca0e32c8 = $this->GetCache($_c11943b9eebf20348fb9bfb1d493ad21885d1c3c8fc75cb8bcef45da07099027f8412110800e76cda35a95201fed095087e4bb5b04be73691d706285b95b6828);
            if (!empty($_fcf767872b692923db759586ca0e32c8['snippets'])) {
                $_fcf767872b692923db759586ca0e32c8['snippets'] = $this->GetLinks($_db8a5b5600156c1efdeaeb38444c4e9c, $_fcf767872b692923db759586ca0e32c8['snippets']);
            }
            return $_fcf767872b692923db759586ca0e32c8;
        }
    }
    private function SaveCache($_114bd151f8fb0c58642d2170da4ae7d7c57977260ac2cc8905306cab6b2acabc)
    {
        if (CACHE == 'disc') {
            $_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc = 'content/cache/disc.txt';
            $_021a00d8                                                         = $this->GetContents($_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc);
            if (!empty($_021a00d8)) {
                if (count($_021a00d8) >= 100) {
                    unset($_021a00d8[0]);
                    file_put_contents($_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc, implode("", $_021a00d8) . "");
                }
                file_put_contents($_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc, md5($_114bd151f8fb0c58642d2170da4ae7d7c57977260ac2cc8905306cab6b2acabc) . "", FILE_APPEND | LOCK_EX);
            } else {
                $_021a00d8 = glob("content/cache/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*.json", GLOB_NOSORT);
                foreach ($_021a00d8 as $_972fb81a3b9e6076bb06207a2687f1f1d167ec845852aa6a33e11c1cf1f282f7 => $_f53908a6763f1539d91c2c5cc99173dc) {
                    $_021a00d8[$_972fb81a3b9e6076bb06207a2687f1f1d167ec845852aa6a33e11c1cf1f282f7] = str_replace('/', '', substr($_f53908a6763f1539d91c2c5cc99173dc, 14, -5));
                }
                file_put_contents($_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc, implode("", $_021a00d8) . "");
            }
        }
    }
    public function Url($_93eb41f1d63972e544bc719e8bfcc395)
    {
        $_4fe64b3ba974b24c973080fd951538364ad24a81 = $this->GetContent('txt/urls.txt');
        if (!empty($_4fe64b3ba974b24c973080fd951538364ad24a81)) {
            if (preg_match('|#KEYWORD#|', $_4fe64b3ba974b24c973080fd951538364ad24a81)) {
                $_4fe64b3ba974b24c973080fd951538364ad24a81 = str_replace('#KEYWORD#', $_93eb41f1d63972e544bc719e8bfcc395, $_4fe64b3ba974b24c973080fd951538364ad24a81);
            }
            return $_4fe64b3ba974b24c973080fd951538364ad24a81;
        }
    }
    public function View($_f7f580e11d00a75814d2ded41fe8e8fe, $_3cc7e66bd2bf4b7a0773db81d1d9dd60279115e1da1081830750eb5a140dddd8 = array())
    {
        foreach ($_3cc7e66bd2bf4b7a0773db81d1d9dd60279115e1da1081830750eb5a140dddd8 as $_bb55e73870b8452738bbd58e0318ff43268c583270f64978fa666b5fc4e676ca => $_769ac34a4012ab69c069de0bab7d9e81) {
            $$_bb55e73870b8452738bbd58e0318ff43268c583270f64978fa666b5fc4e676ca = $_769ac34a4012ab69c069de0bab7d9e81;
        }
        if (file_exists("templates/" . TEMPLATE . "/$_f7f580e11d00a75814d2ded41fe8e8fe.php")) {
            require "templates/" . TEMPLATE . "/$_f7f580e11d00a75814d2ded41fe8e8fe.php";
        } else {
            echo "<h1>Error 404 - File not found.</h1>$_f7f580e11d00a75814d2ded41fe8e8fe.php file does not exist in the directory templates/" . TEMPLATE . "<br>";
        }
    }
}
?>