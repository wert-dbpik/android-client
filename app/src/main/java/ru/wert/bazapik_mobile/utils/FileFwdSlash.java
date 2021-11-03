package ru.wert.bazapik_mobile.utils;

import java.io.File;

public class FileFwdSlash extends File {

    public FileFwdSlash(String pathname) {
        super(pathname);
    }

    public String toStrong(){
        return super.toString().replace("\\", "/");
    }
}
