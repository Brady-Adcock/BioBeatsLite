package org.impulsewellness.litebest.backend.filters;

import org.impulsewellness.litebest.backend.EMGData;

public interface FilterInterface {
    public EMGData getFilteredData();
    public void queFilterData(EMGData data);
}
