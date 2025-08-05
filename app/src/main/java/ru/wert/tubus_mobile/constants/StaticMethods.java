package ru.wert.tubus_mobile.constants;

import java.io.File;
import java.util.Objects;

public class StaticMethods {

    /**
     * Удаление всех загруженных чертежей в папку Consts.TEMP_DIR
     */
    public static void clearAppCash() {
        File tempFolder = new File(Consts.TEMP_DIR.getPath());
        if(tempFolder.exists()){
            for (File myFile : Objects.requireNonNull(tempFolder.listFiles()))
                if (myFile.isFile()) myFile.delete();
        }

    }

}
