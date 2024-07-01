package tkaxv7s.xposed.sesame.util;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import tkaxv7s.xposed.sesame.entity.UserEntity;
import tkaxv7s.xposed.sesame.hook.FriendManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

    public synchronized static void initUser(String userId) {
        setCurrentUserId(userId);
        FriendManager.fillUser();
    }

    public synchronized static void setCurrentUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            currentUid = null;
            return;
        }
        currentUid = userId;
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

    public synchronized static boolean save(String userId) {
        return FileUtil.write2File(JsonUtil.toNoFormatJsonString(userMap), FileUtil.getFriendIdMapFile(userId));
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
        return FileUtil.write2File(JsonUtil.toNoFormatJsonString(userEntity), FileUtil.getSelfIdFile(userEntity.getUserId()));
    }

    public synchronized static void clear() {
        userMap.clear();
    }

}
