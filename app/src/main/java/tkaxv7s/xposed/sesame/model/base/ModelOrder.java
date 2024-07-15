package tkaxv7s.xposed.sesame.model.base;

import tkaxv7s.xposed.sesame.data.Model;
import tkaxv7s.xposed.sesame.model.normal.answerAI.AnswerAI;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;
import tkaxv7s.xposed.sesame.model.task.ancientTree.AncientTree;
import tkaxv7s.xposed.sesame.model.task.antCooperate.AntCooperate;
import tkaxv7s.xposed.sesame.model.task.antFarm.AntFarm;
import tkaxv7s.xposed.sesame.model.task.antForest.AntForestV2;
import tkaxv7s.xposed.sesame.model.task.antMember.AntMember;
import tkaxv7s.xposed.sesame.model.task.antOcean.AntOcean;
import tkaxv7s.xposed.sesame.model.task.antOrchard.AntOrchard;
import tkaxv7s.xposed.sesame.model.task.antSports.AntSports;
import tkaxv7s.xposed.sesame.model.task.antStall.AntStall;
import tkaxv7s.xposed.sesame.model.task.greenFinance.GreenFinance;
import tkaxv7s.xposed.sesame.model.task.reserve.Reserve;
import tkaxv7s.xposed.sesame.model.task.antDodo.AntDodo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModelOrder {

    private static final Class<Model>[] array = new Class[]{
            BaseModel.class
            , AntForestV2.class
            , AntFarm.class
            , AntStall.class
            , AntOrchard.class
            , Reserve.class
            , AntDodo.class
            , AntOcean.class
            , AntCooperate.class
            , AncientTree.class
            , AntSports.class
            , AntMember.class
            , GreenFinance.class
            , AnswerAI.class
    };

    private static final List<Class<Model>> readOnlyClazzList = Collections.unmodifiableList(Arrays.asList(array));

    public static List<Class<Model>> getClazzList() {
        return readOnlyClazzList;
    }

}