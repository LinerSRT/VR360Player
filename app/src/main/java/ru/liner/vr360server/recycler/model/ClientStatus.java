package ru.liner.vr360server.recycler.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 28.04.2022, четверг
 **/

@Retention(RetentionPolicy.SOURCE)
public @interface ClientStatus {
    int UNKNOWN = -1;
    int WAITING = 0;
    int DOWNLOADING_VIDEO = 1;
    int READY = 2;
}
