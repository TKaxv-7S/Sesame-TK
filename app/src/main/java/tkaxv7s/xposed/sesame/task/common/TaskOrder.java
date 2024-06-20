package tkaxv7s.xposed.sesame.task.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tkaxv7s.xposed.sesame.task.model.ancientTree.AncientTree;
import tkaxv7s.xposed.sesame.task.model.antCooperate.AntCooperate;
import tkaxv7s.xposed.sesame.task.model.antFarm.AntFarm;
import tkaxv7s.xposed.sesame.task.model.antForest.AntForestV2;
import tkaxv7s.xposed.sesame.task.model.antMember.AntMember;
import tkaxv7s.xposed.sesame.task.model.antOcean.AntOcean;
import tkaxv7s.xposed.sesame.task.model.antOrchard.AntOrchard;
import tkaxv7s.xposed.sesame.task.model.antSports.AntSports;
import tkaxv7s.xposed.sesame.task.model.antStall.AntStall;
import tkaxv7s.xposed.sesame.task.model.greenFinance.GreenFinance;
import tkaxv7s.xposed.sesame.task.model.kbMember.KBMember;
import tkaxv7s.xposed.sesame.task.model.reserve.Reserve;

public class TaskOrder {

    private static final Class<ModelTask>[] array = new Class[]{
            AntForestV2.class
            , AntCooperate.class
            , AntFarm.class
            , Reserve.class
            , AntOrchard.class
            , AncientTree.class
            , AntSports.class
            , AntMember.class
            , AntOcean.class
            , AntStall.class
            , GreenFinance.class
            , KBMember.class
    };

    private static final List<Class<ModelTask>> readOnlyClazzList = Collections.unmodifiableList(Arrays.asList(array));

    public static Integer getClazzSize() {
        return array.length;
    }

    public static List<Class<ModelTask>> getClazzList() {
        return readOnlyClazzList;
    }

}
