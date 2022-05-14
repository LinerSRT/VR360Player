package ru.liner.vr360server.utils.background;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 14.05.2022, суббота
 **/
public interface Function<T> {
    T process();
    void onProcessed(T result);
    void onFailed(Exception e);
}
