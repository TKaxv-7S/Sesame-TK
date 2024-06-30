package tkaxv7s.xposed.sesame.entity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tkaxv7s.xposed.sesame.util.FileUtil;
import tkaxv7s.xposed.sesame.util.UserIdMap;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.StringUtil;

/**
 * @author Constanline
 * @since 2023/08/08
 */
public class FriendWatch extends IdAndName {

    private static final String TAG = FriendWatch.class.getSimpleName();

    public String startTime;

    public int allGet;

    public int weekGet;

    public FriendWatch(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(IdAndName o) {
        FriendWatch another = (FriendWatch) o;
        if (this.weekGet > another.weekGet) {
            return -1;
        } else if (this.weekGet < another.weekGet) {
            return 1;
        }
        return super.compareTo(o);
    }

    public static List<FriendWatch> getList() {
        ArrayList<FriendWatch> list = new ArrayList<>();
        String strFriendWatch = FileUtil.readFromFile(FileUtil.getFriendWatchFile());
        try {
            JSONObject joFriendWatch;
            if (StringUtil.isEmpty(strFriendWatch)) {
                joFriendWatch = new JSONObject();
            } else {
                joFriendWatch = new JSONObject(strFriendWatch);
            }
            for (String id : UserIdMap.getUserIdSet()) {
                JSONObject friend = joFriendWatch.optJSONObject(id);
                if (friend == null) {
                    friend = new JSONObject();
                }
                String name = UserIdMap.getFullName(id);
                FriendWatch friendWatch = new FriendWatch(id, name);
                friendWatch.startTime = friend.optString("startTime", "无");
                friendWatch.weekGet = friend.optInt("weekGet", 0);
                friendWatch.allGet = friend.optInt("allGet", 0) + friendWatch.weekGet;
                String showText = name + "(开始统计时间:" + friendWatch.startTime + ")\n\n";
                showText = showText + "周收:" + friendWatch.weekGet + " 总收:" + friendWatch.allGet;
                friendWatch.name = showText;
                list.add(friendWatch);
            }
        } catch (Throwable t) {
            Log.i(TAG, "FriendWatch getList: ");
            Log.printStackTrace(TAG, t);
            try {
                FileUtil.write2File(new JSONObject().toString(), FileUtil.getFriendWatchFile());
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }

        return list;
    }
}