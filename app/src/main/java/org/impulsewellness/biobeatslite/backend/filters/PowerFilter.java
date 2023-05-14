package org.impulsewellness.litebest.backend.filters;

import org.impulsewellness.litebest.backend.EMGData;

public class PowerFilter extends Filter {
    int power;

    public PowerFilter(int power, Filter next) {
        super(next);
        this.power = power;
    }

    @Override
    protected EMGData filterData(EMGData data) {
        double[] rec_data = data.getDataFlat();
        for(int i=0; i<rec_data.length; i++) {
            rec_data[i] = (short) Math.pow(rec_data[i], power);
        }
        data.setData(rec_data);
        return data;
    }
}
