public enum Commands {

    glxavor_menu (0),
    grancel_nor_meqena (1),
    tesnel_bolor_meqenaner (2),
    heraxosa_hamarov_gtnel (3),
    ;

    Commands(int i ) {
    }


    public static Commands getEnumByValue(int intValue) {
        switch (intValue) {
            case 0:
                return glxavor_menu;
            case 1:
                return grancel_nor_meqena;
            case 2:
                return tesnel_bolor_meqenaner;
            case 3:
                return heraxosa_hamarov_gtnel;
            default:
                return null;
        }
    }
}
