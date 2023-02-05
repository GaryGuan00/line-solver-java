package jline.lang.constant;

public enum SchedStrategy {
    INF,
    FCFS,
    LCFS,
    LCFSPR,
    SIRO,
    SJF,
    LJF,
    PS,
    DPS,
    GPS,
    SEPT,
    LEPT,
    HOL,
    FORK,
    EXT,
    REF;

    public static SchedStrategy fromLINEString(String string) {
        switch(string) {
            case "inf":
                return INF;
            case "fcfs":
                return FCFS;
            case "lcfs":
                return LCFS;
            case "lcfspr":
                 return LCFSPR;
            case "siro":
                return SIRO;
            case "sjf":
                return SJF;
            case "ljf":
                return LJF;
            case "ps":
                return PS;
            case "dps":
                return DPS;
            case "gps":
                return GPS;
            case "sept":
                return SEPT;
            case "lept":
                return LEPT;
            case "hol":
                return HOL;
            case "fork":
                return FORK;
            case "ext":
                return EXT;
            case "ref":
                return REF;
            default:
                throw new RuntimeException("Unable to return a SchedStrategy, check string and try again.");
        }
    }


    public static String toText(SchedStrategy scheduling) {
        switch (scheduling){
            case INF:
                return "inf";
            case FCFS:
                return "fcfs";
            case LCFS:
                return "lcfs";
            case LCFSPR:
                return "lcfspr";
            case SIRO:
                return "siro";
            case PS:
                return "ps";
            case DPS:
                return "dps";
            case GPS:
                return "gps";
            case SEPT:
                return "sept";
            case LEPT:
                return "lept";
            case HOL:
                return "hol";
            case FORK:
                return "fork";
            case EXT:
                return "ext";
            case REF:
                return "ref";
            default:
                return "";
        }
    }

    public static int toID(SchedStrategy scheduling) {
        switch (scheduling){
            case INF:
                return 0;
            case FCFS:
                return 1;
            case LCFS:
                return 2;
            case LCFSPR:
                return 3;
            case SIRO:
                return 4;
            case PS:
                return 5;
            case DPS:
                return 6;
            case GPS:
                return 7;
            case SEPT:
                return 8;
            case LEPT:
                return 9;
            case HOL:
                return 10;
            case FORK:
                return 11;
            case EXT:
                return 12;
            case REF:
                return 13;
            default:
                return -1;
        }
    }
}