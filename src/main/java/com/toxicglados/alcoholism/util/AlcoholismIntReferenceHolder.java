package com.toxicglados.alcoholism.util;

import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;

public class AlcoholismIntReferenceHolder extends IntReferenceHolder {
    public Integer lastValue;
    IIntArray intArray;
    int index;
    public AlcoholismIntReferenceHolder(IIntArray intArrayIn, int indexIn){
        this.intArray = intArrayIn;
        this.index = indexIn;
        lastValue = null;
    }

    @Override
    public int get() {
        return intArray.get(index);
    }

    @Override
    public void set(int value) {
        intArray.set(index, value);
    }

    @Override
    public boolean isDirty() {
        if (lastValue != null){
            int i = this.get();
            boolean flag = i != this.lastValue;
            this.lastValue = i;
            return flag;
        }
        else{
            this.lastValue = this.get();
            return true;
        }
    }
}
