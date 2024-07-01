package tkaxv7s.xposed.sesame.entity;

import lombok.Data;
import lombok.Getter;
import tkaxv7s.xposed.sesame.util.StringUtil;

@Getter
public class UserEntity {

    private final String userId;

    private final String account;

    private final String realName;

    private final String nickName;

    private final String remarkName;

    private final String showName;

    private final String maskName;

    private final String fullName;

    public UserEntity(String userId, String account, String realName, String nickName, String remarkName) {
        this.userId = userId;
        this.account = account;
        this.realName = realName;
        this.nickName = nickName;
        this.remarkName = remarkName;
        String showNameTmp;
        if (StringUtil.isEmpty(remarkName)) {
            showNameTmp = nickName;
        } else {
            showNameTmp = remarkName;
        }
        String maskNameTmp;
        if (realName != null && realName.length() > 1) {
            maskNameTmp = "*" + realName.substring(1);
        } else {
            maskNameTmp = realName;
        }
        /*if (isMaskAccount) {
            int length = account.length();
            if (length > 5) {
                int prefixIndex = Math.min(3, Math.max(1, length - 3));
                String prefix = account.substring(0, prefixIndex);
                int tmpIndex = prefixIndex + 3;
                int suffixIndex = length - 4;
                if (suffixIndex < tmpIndex) {
                    suffixIndex = tmpIndex;
                }
                String suffix = account.substring(suffixIndex);
                account = prefix + "***" + suffix;
            }
        }*/
        this.showName = showNameTmp;
        this.maskName = showNameTmp + "|" + maskNameTmp;
        this.fullName = showNameTmp + "|" + realName + "(" + account + ")";
    }

    @Data
    public static class UserDto {

        private String userId;

        private String account;

        private String realName;

        private String nickName;

        private String remarkName;

        public UserEntity toEntity() {
            return new UserEntity(userId, account, realName, nickName, remarkName);
        }

    }
}
