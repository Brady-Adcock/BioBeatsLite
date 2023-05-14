package org.impulsewellness.litebest.backend.filters;

import org.impulsewellness.litebest.backend.EMGData;

// Down-sample, drop points
public class DownSamplingFilter extends Filter{
    private int dropNum;

    public DownSamplingFilter(int dropNum, Filter next) {
        super(next);
        this.dropNum = dropNum;
        outputDataLength = inputDataLength/(dropNum + 1);
    }

    @Override
    EMGData filterData(EMGData data) {
        double old_data[][] = data.getData2d();
        double new_data[][] = new double[inputNumChannels][inputDataLength/(dropNum + 1)];

        for(int i=0; i< new_data.length; i++) {
            for(int j=0; j<new_data[i].length; j++) {
                if(j % dropNum+1 == 0) {
                    new_data[i][j/(dropNum+1)] = old_data[i][j];
                }
            }
        }
        data.setData(new_data);
        return data;
    }
}
