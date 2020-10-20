package com.indago.data.segmentation;

import net.imglib2.roi.io.labeling.LabelingIOService;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

public class XmlIoLabelingPlusTest {

    Context context;

    @Before
    public void beforeTests() {
        context = new Context();
    }


    @Test
    public void testLoadFromBson() {
        XmlIoLabelingPlus plus = new XmlIoLabelingPlus();
        plus.labelingIOService = context.getService(LabelingIOService.class);
        LabelingPlus p = plus.loadFromBson("src/test/resources/bson/example1.bson");
        System.out.println(p.getFragments().toString());
    }

    @Test
    public void testSaveAsBson() {
        XmlIoLabelingPlus plus = new XmlIoLabelingPlus();
        plus.labelingIOService = context.getService(LabelingIOService.class);
        LabelingPlus p = plus.loadFromBson("src/test/resources/bson/example1.bson");
        plus.saveAsBson(p, "src/test/resources/bson/save_test.bson");
    }
}