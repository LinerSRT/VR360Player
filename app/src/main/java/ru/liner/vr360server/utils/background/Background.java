package ru.liner.vr360server.utils.background;


/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 14.05.2022, суббота
 **/
public class Background {
    public static <T> void process(Function<T> function){
        new Thread(() -> {
            try {
                function.onProcessed(function.process());
            } catch (Exception e){
                function.onFailed(e);
            }
        }).start();
    }

}
