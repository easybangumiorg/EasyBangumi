package com.zane.androidupnpdemo.util;

import java.util.Collection;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/28 16:54
 */

public class ListUtils {

    public static boolean isEmpty(Collection list) {
        return !(list != null && list.size() != 0);
    }

}
