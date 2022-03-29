public enum Commands {

    glxavor_menu (0),
    grancel_nor_meqena (1),
    tesnel_bolor_meqenaner (2),
    heraxosa_hamarov_gtnel (3),
    ;

    Commands(int i ) {
    }

    public static Commands getEnum(String s){
        if(glxavor_menu.name().equals(s)){
            return glxavor_menu;
        }else if(grancel_nor_meqena.name().equals(s)){
            return grancel_nor_meqena;
        }else if(tesnel_bolor_meqenaner.name().equals(s)){
            return tesnel_bolor_meqenaner;
        }else if (heraxosa_hamarov_gtnel.name().equals(s)){
            return heraxosa_hamarov_gtnel;
        }
       return null;
    }
}
