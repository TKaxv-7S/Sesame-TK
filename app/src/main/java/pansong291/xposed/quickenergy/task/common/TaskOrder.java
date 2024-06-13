package pansong291.xposed.quickenergy.task.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pansong291.xposed.quickenergy.task.model.ancientTree.AncientTree;
import pansong291.xposed.quickenergy.task.model.antCooperate.AntCooperate;
import pansong291.xposed.quickenergy.task.model.antFarm.AntFarm;
import pansong291.xposed.quickenergy.task.model.antForest.AntForestV2;
import pansong291.xposed.quickenergy.task.model.antMember.AntMember;
import pansong291.xposed.quickenergy.task.model.antOcean.AntOcean;
import pansong291.xposed.quickenergy.task.model.antOrchard.AntOrchard;
import pansong291.xposed.quickenergy.task.model.antSports.AntSports;
import pansong291.xposed.quickenergy.task.model.antStall.AntStall;
import pansong291.xposed.quickenergy.task.model.greenFinance.GreenFinance;
import pansong291.xposed.quickenergy.task.model.reserve.Reserve;

public class TaskOrder {

    private static final Class<Task>[] array = new Class[]{
            AntForestV2.class
            , AntCooperate.class
            , AntFarm.class
            , Reserve.class
            , AncientTree.class
            , AntSports.class
            , AntMember.class
            , AntOcean.class
            , AntOrchard.class
            , AntStall.class
            , GreenFinance.class
    };

    private static final List<Class<Task>> readOnlyClazzList = Collections.unmodifiableList(Arrays.asList(array));

    public static Integer getClazzSize() {
        return array.length;
    }

    public static List<Class<Task>> getClazzList() {
        return readOnlyClazzList;
    }

}
