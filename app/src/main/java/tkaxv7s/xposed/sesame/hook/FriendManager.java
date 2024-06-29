package tkaxv7s.xposed.sesame.hook;

import de.robv.android.xposed.XposedHelpers;
import org.json.JSONException;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.util.*;

import java.util.Calendar;
import java.util.List;

public class FriendManager {
    private static final String TAG = FriendManager.class.getSimpleName();

    public static void fillUser() {
        new Thread() {

            @Override
            public void run() {
                ClassLoader loader;
                try {
                    loader = ApplicationHook.getClassLoader();
                } catch (Exception e) {
                    Log.i(TAG, "Error getting classloader");
                    return;
                }
                try {
                    Class<?> clsUserIndependentCache = loader.loadClass("com.alipay.mobile.socialcommonsdk.bizdata.UserIndependentCache");
                    Class<?> clsAliAccountDaoOp = loader.loadClass("com.alipay.mobile.socialcommonsdk.bizdata.contact.data.AliAccountDaoOp");
                    Object aliAccountDaoOp = XposedHelpers.callStaticMethod(clsUserIndependentCache, "getCacheObj", clsAliAccountDaoOp);
                    List<?> allFriends = (List<?>) XposedHelpers.callMethod(aliAccountDaoOp, "getAllFriends", new Object[0]);
                    for (Object friend : allFriends) {
                        try {
                            String userId = (String) XposedHelpers.findField(friend.getClass(), "userId").get(friend);
                            String account = (String) XposedHelpers.findField(friend.getClass(), "account").get(friend);
                            String name = (String) XposedHelpers.findField(friend.getClass(), "name").get(friend);
                            String nickName = (String) XposedHelpers.findField(friend.getClass(), "nickName").get(friend);
                            String remarkName = (String) XposedHelpers.findField(friend.getClass(), "remarkName").get(friend);
                            if (StringUtil.isEmpty(remarkName)) {
                                remarkName = nickName;
                            }
                            remarkName += "|" + name;
                            UserIdMap.putIdMap(userId, remarkName + "(" + account + ")");
                        } catch (Throwable t) {
                            Log.i(TAG, "checkUnknownId.for err:");
                            Log.printStackTrace(TAG, t);
                        }
                    }
                    UserIdMap.saveIdMap();
                } catch (Throwable t) {
                    Log.i(TAG, "checkUnknownId.run err:");
                    Log.printStackTrace(TAG, t);
                }
            }
        }.start();
    }

    public static boolean needUpdateAll(long last) {
        if (last == 0L) {
            return true;
        }
        Calendar cLast = Calendar.getInstance();
        cLast.setTimeInMillis(last);
        Calendar cNow = Calendar.getInstance();
        if (cLast.get(Calendar.DAY_OF_YEAR) == cNow.get(Calendar.DAY_OF_YEAR)) {
            return false;
        }
        return cNow.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    private static JSONObject joFriendWatch;

    public static void friendWatch(String id, int collectedEnergy) {
        if (id.equals(UserIdMap.getCurrentUid())) {
            return;
        }
        try {
            if (joFriendWatch == null) {
                String strFriendWatch = FileUtil.readFromFile(FileUtil.getFriendWatchFile());
                if (!"".equals(strFriendWatch)) {
                    joFriendWatch = new JSONObject(strFriendWatch);
                } else {
                    joFriendWatch = new JSONObject();
                }
            }
            if (needUpdateAll(FileUtil.getFriendWatchFile().lastModified())) {
                friendWatchNewWeek();
            }
            friendWatchSingle(id, collectedEnergy);
        } catch (Throwable th) {
            Log.i(TAG, "friendWatch err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static void friendWatchSingle(String id, int collectedEnergy) throws JSONException {
        JSONObject joSingle = joFriendWatch.optJSONObject(id);
        if (joSingle == null) {
            joSingle = new JSONObject();
            joSingle.put("name", UserIdMap.getNameById(id));
            joSingle.put("allGet", 0);
            joSingle.put("startTime", TimeUtil.getDateStr());
            joFriendWatch.put(id, joSingle);
        }
        joSingle.put("weekGet", joSingle.optInt("weekGet", 0) + collectedEnergy);
        FileUtil.write2File(joFriendWatch.toString(), FileUtil.getFriendWatchFile());
    }

    private static void friendWatchNewWeek() {
        JSONObject joSingle;
        try {
            String dateStr = TimeUtil.getDateStr();
            List<String> friendIds = UserIdMap.getFriendIds();
            for (String id : friendIds) {
                if (joFriendWatch.has(id)) {
                    joSingle = joFriendWatch.getJSONObject(id);
                } else {
                    joSingle = new JSONObject();
                }
                joSingle.put("name", UserIdMap.getNameById(id));
                joSingle.put("allGet", joSingle.optInt("allGet", 0) + joSingle.optInt("weekGet", 0));
                joSingle.put("weekGet", 0);
                if (!joSingle.has("startTime")) {
                    joSingle.put("startTime", dateStr);
                }
                joFriendWatch.put(id, joSingle);
            }
            FileUtil.write2File(joFriendWatch.toString(), FileUtil.getFriendWatchFile());
        } catch (Throwable th) {
            Log.i(TAG, "friendWatchNewWeek err:");
            Log.printStackTrace(TAG, th);
        }
    }
}
