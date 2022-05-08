package ru.liner.vr360server.utils.pagetransformer;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

/*******************************************************************
 *    * * * *   * * * *   *     *       Created by OCN.Yang
 *    *     *   *         * *   *       Time:2017/12/6 17:59.
 *    *     *   *         *   * *       Email address:ocnyang@gmail.com
 *    * * * *   * * * *   *     *.Yang  Web site:www.ocnyang.com
 *******************************************************************/


public class ParallaxVerticalTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        int height = page.getHeight();
        if (position < -1) {
            page.setScrollY((int) (height * 0.75));
        } else if (position <= 1) {
            if (position < 0) {
                page.setScrollY((int) (height * 0.75 * position));
            } else {
                page.setScrollY((int) (height * 0.75 * position));
            }
        } else {
            page.setScrollY((int) (height * 0.75 * -1));
        }
    }
}
