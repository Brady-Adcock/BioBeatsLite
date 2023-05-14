package org.impulsewellness.litebest.backend.filters;

import org.impulsewellness.litebest.backend.EMGData;

public class RectifyFilter extends Filter {
    public RectifyFilter(Filter next) {
        super(next);
    }

    @Override
    protected EMGData filterData(EMGData data) {
        double[] rec_data = data.getDataFlat();
        for(int i=0; i<rec_data.length; i++) {
            rec_data[i] = (short) Math.abs(rec_data[i]);
        }
        data.setData(rec_data);
        return data;
    }
}
