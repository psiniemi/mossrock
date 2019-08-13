package net.diibadaaba.mossrock;

import static com.github.mob41.blapi.HexUtil.hex2bytes;

public class IRCommands {
    public static final byte[] HK_AUX           = hex2bytes("260058000001289314111411151015101510151114111436141114111510151015351536143614111535151015361436141114111535153614111436141114111535153614111411140005a70001284914000c3c0001284915000d05");
    public static final byte[] HK_GAME          = hex2bytes("2600580000012893160f1510151015111411151015101634151015101510151016351535153515101510153516101510153515351510163416351510153515351510151115351510150005a60001284915000c3b0001284915000d05");
    public static final byte[] HK_SERVER        = hex2bytes("260058000001269512131213121312131312131213121338121312131213121313371337133812131436143614111412143618321510153514111412143614361411180d15351412140005a70001264b15000c3b0001274a16000d05");
    public static final byte[] HK_STB           = hex2bytes("260058000001279415101510151015101511141114111436141115101510151015361434173515101535153614111411143615101535153614111411143615351510153614111411150005a60001264b15000c3b0001274a15000d05");
    public static final byte[] HK_VOL_DOWN_5DB  = hex2bytes("2600b0000001289315101510151015101510151015111535151015101510151015351536153515101a0b1510151015361410161015331735153614361535150d1835153614111510150005a60001284915000c3c0001284915000c3c0001284914000c3c0001284915000c3c0001284915000c3c0001284914000c3c0001284915000c3c0001284914000c3c0001284915000c3c0001294815000c3c0001284915000c3c0001284915000c3c0001294815000d050000000000000000");
    public static final byte[] HK_VOL_DOWN_10DB = hex2bytes("2600e80000012794131213131213121313121213130d1936131213131213121313371337143613131213121312131436131213121436143713371436143613131337143613121411140005a60001284a18000c390001264b13000c3d0001274a14000c3d0001264b13000c3d0001274a13000c3e0001264a14000c3d0001264b14000c3c0001274a14000c3d0001264b14000c3c0001274a13000c3e0001264b14000c3c0001274a14000c3d0001264b14000c3c0001274a14000c3d0001264a14000c3d0001274a14000c3d0001264b13000c3d0001274a14000c3c0001274a14000c3d0001294814000d05");
    public static final byte[] HK_VOL_DOWN_20DB = hex2bytes("260050010001269513121411151015101510151015101635141114111510160f163416341635141115101510160f16341a0c1510153516341634163515351510163416301a101510160005a50001264b16000c3a0001294816000c3b0001294815000c3600012e4819000c380001294814000c3c0001294815000c3c0001294813000c3d00012a4715000c3c0001294815000c3c0001294815000c3b0001274a16000c3b0001294815000c3b0001294816000c3b0001294815000c3b0001294816000c3b0001264b16000c3a00012a4716000c3b0001294717000c3a00012a4716000c3b0001294816000c3b00012d4415000c3b0001274a16000c3b0001294816000c3a0001274a16000c3b0001264b16000c3a0001294816000c3700012d4816000c3a00012a4716000c3b0001274a16000c3b00012d4417000c390001274a16000c3b0001264b16000c3a0001294816000d050000000000000000");
    public static final byte[] HK_VOL_DOWN_30DB = hex2bytes("2600bc010001299214111411141114111411151015101338141114111411141114361337153614111410151114111337151114111436133714361437183214111337153515111411140005a70001294812000c3e0001294815000c3c0001294815000c3c0001294814000c3c0001294815000c3c00012d4415000c3c0001294814000c3c0001294815000c3c0001294813000c3b00012c4812000c3e0001294814000c3d0001294813000c3e0001294817000c390001294813000c3e0001294814000c3d0001294814000c3c0001294813000c3e0001294516000c3c00012b4812000c3e0001294813000c3e0001294813000c3e0001294818000c380001294813000c3e0001264b13000c3e0001284912000c3e0001274a13000c3e0001294813000c3e0001294713000c3e0001294813000c3e0001264b13000c3e0001284912000c3e0001294813000c3e0001294813000c3e0001294416000c3e0001294813000c3e0001294813000c3e0001294812000c3e0001294813000c3e0001294813000c3e0001294812000c3e0001294813000c3e000129481300017e0a000ab300012c4812000c3e0001294813000c3e0001294813000c3e0001264b15000d05000000000000000000000000");
    public static final byte[] HK_VOL_UP_5DB    = hex2bytes("2600b80000012893160f160f160f160f160f161015101634180d160f160f160f163416351634160f163416341734160f160f160f16341635160f160f160f163416341635160f160f160005a50001284916000c3a0001294816000c3b0001284916000c3a0001294816000c3b0001284916000c3a0001294718000c3a0001284916000c3a0001294817000c3a0001284916000c3a0001294817000c3a0001284916000c3a0001294817000c3a0001284916000c3a0001294817000d05");
    public static final byte[] HK_VOL_UP_10DB   = hex2bytes("26000c0114111411141115101510151015101536141114111411141119311535153614111535153515361411141114111535153515111411141114361535153614111411140005a70001284914000c3c0001284915000c3b0001274a15000c3800012c4914000c3c0001284915000c3b0001284915000c3800012c4914000c3c0001284915000c3b0001284915000c3c0001284914000c3c0001284915000c3b0001284816000c3800012c4914000c3c0001284915000c3c0001284815000c3c0001284915000c3b0001284915000c3c0001284914000c3c0001284915000c3b0001284915000d05000000000000000000000000");
    public static final byte[] HK_VOL_UP_20DB   = hex2bytes("2600500100012a911510160f160f160f160f170e161015351510160f160f160f163416351535160f16341a30163515101510160f1634163416101510151016341634163515101510160005a50001264b1a000c360001274a16000c3b0001264b16000c3a0001274a16000c3b0001264b16000c3a0001294717000c3b0001264b15000c390001294a16000c3b0001294815000c3b0001294816000c3b0001264b15000c3b0001294816000c3a0001274a16000c3b0001264b16000c3a00012a4716000c3b0001294717000c3a0001274a16000c3b00012b4616000c3a0001294816000c3b0001264b16000c3a0001274a16000c3b0001264b16000c3a0001294816000c3b0001264b16000c3a0001274a16000c3b0001264b16000c3a0001294816000c3b0001294816000c3a0001294816000c3b0001294815000c3a0001284a16000c3b0001264b15000c3b0001274a15000d050000000000000000");
    public static final byte[] HK_VOL_UP_30DB   = hex2bytes("2600b8010001269512131212141213121312131213131238121312131213131213371437123812131337133713381213121312131337153513131213121312381436143712131213120005a90001264b14000c3c0001274a14000c3d0001264b12000c3e0001274a13000c3e0001264b12000c3e0001274a13000c3d0001274b12000c3e0001274a13000c3e0001264b12000c3e0001274a13000c3e0001264b13000c3d0001274a13000c3e0001264b13000c3d0001274a13000c3e0001264b13000c3a00012a4a13000c3e0001274a13000c3d0001274a13000c3e0001274a13000c3e0001264b12000c3e0001274a13000c3e0001264b12000c3e0001274a13000c3e0001264b13000c3d0001274a13000c3e0001264b12000c3e0001274a13000c3e0001264b12000c3e0001274a14000c3d0001264b13000c3d0001274a14000c3d0001264b13000c3700012d4716000c3e0001264b14000c3c0001274a13000c3e0001264b1a000c360001274a13000c3e0001264b14000c3c0001274a14000c3d0001264b14000c3c0001274a13000c3e0001274a14000c3d0001264b13000c3d0001274a13000c3e00012b4613000c3d0001274a14000d05");
    public static final byte[] LG_ON_OFF        = hex2bytes("2600600000012992140f1513143513131312121314111213133712371313123712381237133712371313121312131238121312131213121313371237133712131337123713371237130005260001274a13000c5c0001274a12000c5c0001284a12000d050000000000000000");
}
