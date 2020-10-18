package com.chm.headlines.util;

import java.util.HashSet;
import java.util.Set;

public class Numbers {
    public boolean duplicate(int numbers[],int length,int [] duplication) {
        if(numbers == null)
            return false;
        for(int num : numbers)
            if(num < 0 || num > length - 1)
                return false;
        Set nums = new HashSet();
        for(int a : numbers){
            if(nums.contains(a)){
                duplication[0] = a;
                return true;
            }else
                nums.add(a);
        }
        return false;

    }
}
