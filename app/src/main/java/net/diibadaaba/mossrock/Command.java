package net.diibadaaba.mossrock;

public abstract class Command {
    public static final String HEAD = "0,0,6,0,256,";
    public static final String DIM_LEN = "75,0,";
    public static final String LEARN_LEN = "67,0,";
    public static final String HIGH = "1,5,1,1,";
    public static final String LOW = "1,1,1,5,";
    public static final String MID_SYNC = LOW;
    public static final String DIM_P = "1,1,1,1,";
    public static final String STOP = "1,39,";
    public static final String END = "0";
    public static final String SYNC= "1,10,";
    public static final Integer TARGET_GROUP = 0;
    public static final String WHEEL_HEAD = "0,0,6,0,360,";
    public static final String WHEEL_LEN = "26,0,";
    public static final String WHEEL_HIGH = "1,3,3,1,";
    public static final String WHEEL_LOW = "1,3,1,3,";
    public static final String WHEEL_PRE_SYNC = "1,3,1,3,1,3,3,1,1,3,3,1,";
    public static final String WHEEL_SYNC = "1,31,";
    private static final String[] encodedBits = new String[]{LOW, HIGH};
    private static final String[] wheelEncodedBits = new String[]{WHEEL_LOW, WHEEL_HIGH};

    public static String commandDim(String uniqueCode, int moduleCode, int dimLevel) {
        return HEAD +
                DIM_LEN +
                SYNC +
                encodeBits(pwd(uniqueCode), 26) +
                MID_SYNC +
                DIM_P +
                encodeBits(inverse4Bits(moduleCode - 1), 4) +
                encodeBits(dimLevel, 4) +
                STOP +
                END;
    }


    public static String commandLearn(String uniqueCode, int moduleCode, boolean on) {
        int isGroup = toInt(moduleCode == TARGET_GROUP);
        return HEAD +
                LEARN_LEN +
                SYNC +
                encodeBits(pwd(uniqueCode), 26) +
                encodeBits(isGroup, 1) +
                encodeBits(toInt(on), 1) +
                encodeBits(inverse4Bits(moduleCode - 1), 4) +
                STOP +
                END;
    }

    public static String commandWheel(int wheelLetterCode, int wheelNumberCode, boolean on) {
        int code = inverse4Bits(wheelNumberCode - 1) + (inverse4Bits(wheelLetterCode) << 4);
        return WHEEL_HEAD +
                WHEEL_LEN +
                encodeBits(wheelEncodedBits, code, 8) +
                WHEEL_PRE_SYNC +
                encodeBits(wheelEncodedBits, toInt(on), 1) +
                WHEEL_SYNC +
                END;
    }

    protected static String encodeBits(int value, int useBits) {
        return encodeBits(encodedBits, value, useBits);
    }

    protected static String encodeBits(String[] encodedBits, int value, int useBits) {
        String ret = "";
        int shift = useBits - 1;
        for (int i = 0; i < useBits; ++i) {
            int bit = 1 & value >> shift - i;
            ret += encodedBits[bit];
        }
        return ret.toString();
    }

    protected static int inverse4Bits(int value) {
        return (value & 1) << 3 | (value & 2) << 1 | (value & 4) >> 1 | 1 & value >> 3;
    }

    protected static int pwd(String uniqueCode) {
        int ret = 0;
        for (int i = 0; i < Math.min(5, uniqueCode.length()); ++i) {
            ret += pwd_char(uniqueCode.charAt(i), i) << i * 6;
        }
        return ret;
    }

    protected static int pwd_char(char c, int index) {
        if (index < 4) {
            if (c == '#') {
                return 63;
            }
            if (c == '*') {
                return 62;
            }
            if (c < 'A') {
                return 52 + (c - 48);
            }
            if (c < 'a') {
                return c - 65;
            }
            return 26 + (c - 97);
        }
        return c - 49;
    }

    protected static int toInt(boolean bool) {
        return bool ? 1 : 0;
    }
}

