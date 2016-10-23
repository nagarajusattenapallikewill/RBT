package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.ProgressiveCIDPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.Hunter;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;

public class CopyHunter extends Hunter
{
    private static final String CopyHunter = "copy";

    public CopyHunter()
    {
        initHunter();
    }

    private void initHunter()
    {
        ProgressiveCIDPublisher publisher = new ProgressiveCIDPublisher(CopyHunter,5);
        QueueContainer cidContainer = new QueueContainer(publisher);
    }
}
