package tkaxv7s.xposed.sesame.util;

import com.fasterxml.jackson.core.type.TypeReference;
import de.robv.android.xposed.XposedHelpers;
import lombok.Getter;
import tkaxv7s.xposed.sesame.entity.UserEntity;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserIdMap {

    private static final Map<String, UserEntity> userMap = new ConcurrentHashMap<>();

    private static final Map<String, UserEntity> readOnlyUserMap = Collections.unmodifiableMap(userMap);

    @Getter
    private static String currentUid = null;

    public static Map<String, UserEntity> getUserMap() {
        return readOnlyUserMap;
    }

    public static Set<String> getUserIdSet() {
        return userMap.keySet();
    }

    public static Collection<UserEntity> getUserEntityCollection() {
        return userMap.values();
    }

    public synchronized static void initUser(String currentUserId) {
        setCurrentUserId(currentUserId);
        ApplicationHook.getMainHandler().post(() -> {
            ClassLoader loader;
            try {
                loader = ApplicationHook.getClassLoader();
            } catch (Exception e) {
                Log.i("Error getting classloader");
                return;
            }
            try {
                UserIdMap.unload();
                String selfId = ApplicationHook.getUserId();
                Class<?> clsUserIndependentCache = loader.loadClass("com.alipay.mobile.socialcommonsdk.bizdata.UserIndependentCache");
                Class<?> clsAliAccountDaoOp = loader.loadClass("com.alipay.mobile.socialcommonsdk.bizdata.contact.data.AliAccountDaoOp");
                Object aliAccountDaoOp = XposedHelpers.callStaticMethod(clsUserIndependentCache, "getCacheObj", clsAliAccountDaoOp);
                List<?> allFriends = (List<?>) XposedHelpers.callMethod(aliAccountDaoOp, "getAllFriends", new Object[0]);
                if (!allFriends.isEmpty()) {
                    Class<?> friendClass = allFriends.get(0).getClass();
                    Field userIdField = XposedHelpers.findField(friendClass, "userId");
                    Field accountField = XposedHelpers.findField(friendClass, "account");
                    Field nameField = XposedHelpers.findField(friendClass, "name");
                    Field nickNameField = XposedHelpers.findField(friendClass, "nickName");
                    Field remarkNameField = XposedHelpers.findField(friendClass, "remarkName");
                    Field friendStatusField = XposedHelpers.findField(friendClass, "friendStatus");
                    UserEntity selfEntity = null;
                    for (Object userObject : allFriends) {
                        try {
                            String userId = (String) userIdField.get(userObject);
                            String account = (String) accountField.get(userObject);
                            String name = (String) nameField.get(userObject);
                            String nickName = (String) nickNameField.get(userObject);
                            String remarkName = (String) remarkNameField.get(userObject);
                            Integer friendStatus = (Integer) friendStatusField.get(userObject);
                            UserEntity userEntity = new UserEntity(userId, account, friendStatus, name, nickName, remarkName);
                            if (Objects.equals(selfId, userId)) {
                                selfEntity = userEntity;
                            }
                            UserIdMap.add(userEntity);
                        } catch (Throwable t) {
                            Log.i("addUserObject err:");
                            Log.printStackTrace(t);
                        }
                    }
                    UserIdMap.saveSelf(selfEntity);
                }
                UserIdMap.save(selfId);
            } catch (Throwable t) {
                Log.i("checkUnknownId.run err:");
                Log.printStackTrace(t);
            }
        });
    }

    public synchronized static void setCurrentUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            currentUid = null;
            return;
        }
        currentUid = userId;
    }

    public static String getCurrentMaskName() {
        return getMaskName(currentUid);
    }

    public static String getMaskName(String userId) {
        UserEntity userEntity = userMap.get(userId);
        if (userEntity == null) {
            return null;
        }
        return userEntity.getMaskName();
    }

    public static String getFullName(String userId) {
        UserEntity userEntity = userMap.get(userId);
        if (userEntity == null) {
            return null;
        }
        return userEntity.getFullName();
    }

    public static UserEntity get(String userId) {
        return userMap.get(userId);
    }

    public synchronized static void add(UserEntity userEntity) {
        String userId = userEntity.getUserId();
        if (userId == null || userId.isEmpty()) {
            return;
        }
        userMap.put(userId, userEntity);
    }

    public synchronized static void remove(String userId) {
        userMap.remove(userId);
    }

    public synchronized static void load(String userId) {
        userMap.clear();
        try {
            String body = FileUtil.readFromFile(FileUtil.getFriendIdMapFile(userId));
            if (!body.isEmpty()) {
                Map<String, UserEntity.UserDto> dtoMap = JsonUtil.parseObject(body, new TypeReference<Map<String, UserEntity.UserDto>>() {
                });
                for (UserEntity.UserDto dto : dtoMap.values()) {
                    userMap.put(dto.getUserId(), dto.toEntity());
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public synchronized static void unload() {
        userMap.clear();
    }

    public synchronized static boolean save(String userId) {
        return FileUtil.write2File(JsonUtil.toJsonString(userMap), FileUtil.getFriendIdMapFile(userId));
    }

    public synchronized static void loadSelf(String userId) {
        userMap.clear();
        try {
            String body = FileUtil.readFromFile(FileUtil.getSelfIdFile(userId));
            if (!body.isEmpty()) {
                UserEntity.UserDto dto = JsonUtil.parseObject(body, new TypeReference<UserEntity.UserDto>() {
                });
                userMap.put(dto.getUserId(), dto.toEntity());
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public synchronized static boolean saveSelf(UserEntity userEntity) {
        return FileUtil.write2File(JsonUtil.toJsonString(userEntity), FileUtil.getSelfIdFile(userEntity.getUserId()));
    }

}
