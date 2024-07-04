package tkaxv7s.xposed.sesame.model.task.antSports;

import tkaxv7s.xposed.sesame.hook.ApplicationHook;

public class AntSportsRpcCall {
    private static final String chInfo = "ch_appcenter__chsub_9patch",
            timeZone = "Asia\\/Shanghai", version = "3.0.1.2", alipayAppVersion = "0.0.852",
            cityCode = "330100", appId = "2021002116659397";

    public static String queryCoinBubbleModule() {
        return ApplicationHook.requestString("com.alipay.sportshealth.biz.rpc.sportsHealthHomeRpc.queryCoinBubbleModule",
                "[{\"bubbleId\":\"\",\"canAddHome\":false,\"chInfo\":\"" + chInfo
                        + "\",\"clientAuthStatus\":\"not_support\",\"clientOS\":\"android\",\"distributionChannel\":\"\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_AI\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"]}]");
    }

    public static String receiveCoinAsset(String assetId, int coinAmount) {
        return ApplicationHook.requestString("com.alipay.sportshealth.biz.rpc.SportsHealthCoinCenterRpc.receiveCoinAsset",
                "[{\"assetId\":\"" + assetId
                        + "\",\"chInfo\":\"" + chInfo
                        + "\",\"clientOS\":\"android\",\"coinAmount\":"
                        + coinAmount
                        + ",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"],\"tracertPos\":\"首页金币收集\"}]");
    }

    public static String queryMyHomePage() {
        return ApplicationHook.requestString("alipay.antsports.walk.map.queryMyHomePage", "[{\"alipayAppVersion\":\""
                + alipayAppVersion + "\",\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"],\"pathListUsePage\":true,\"timeZone\":\""
                + timeZone + "\"}]");
    }

    public static String join(String pathId) {
        return ApplicationHook.requestString("alipay.antsports.walk.map.join", "[{\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"],\"pathId\":\""
                + pathId + "\"}]");
    }

    public static String openAndJoinFirst() {
        return ApplicationHook.requestString("alipay.antsports.walk.user.openAndJoinFirst", "[{\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"]}]");
    }

    public static String go(String day, String rankCacheKey, int stepCount) {
        return ApplicationHook.requestString("alipay.antsports.walk.map.go", "[{\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"day\":\"" + day
                + "\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"],\"needAllBox\":true,\"rankCacheKey\":\""
                + rankCacheKey + "\",\"timeZone\":\"" + timeZone + "\",\"useStepCount\":" + stepCount
                + "}]");
    }

    public static String openTreasureBox(String boxNo, String userId) {
        return ApplicationHook.requestString("alipay.antsports.walk.treasureBox.openTreasureBox", "[{\"boxNo\":\"" + boxNo
                + "\",\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"],\"userId\":\""
                + userId + "\"}]");
    }

    public static String queryBaseList() {
        return ApplicationHook.requestString("alipay.antsports.walk.path.queryBaseList", "[{\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"]}]");
    }

    public static String queryProjectList(int index) {
        return ApplicationHook.requestString("alipay.antsports.walk.charity.queryProjectList", "[{\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"],\"index\":"
                + index + ",\"projectListUseVertical\":true}]");
    }

    public static String donate(int donateCharityCoin, String projectId) {
        return ApplicationHook.requestString("alipay.antsports.walk.charity.donate", "[{\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"donateCharityCoin\":" + donateCharityCoin
                + ",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"],\"projectId\":\""
                + projectId + "\"}]");
    }

    public static String queryWalkStep() {
        return ApplicationHook.requestString("alipay.antsports.walk.user.queryWalkStep", "[{\"chInfo\":\"" + chInfo
                + "\",\"clientOS\":\"android\",\"features\":[\"DAILY_STEPS_RANK_V2\",\"STEP_BATTLE\",\"CLUB_HOME_CARD\",\"NEW_HOME_PAGE_STATIC\",\"CLOUD_SDK_AUTH\",\"STAY_ON_COMPLETE\",\"EXTRA_TREASURE_BOX\",\"NEW_HOME_PAGE_STATIC\",\"SUPPORT_TAB3\",\"SUPPORT_FLYRABBIT\",\"PROP\",\"PROPV2\",\"ASIAN_GAMES\"],\"timeZone\":\""
                + timeZone + "\"}]");
    }

    public static String walkDonateSignInfo(int count) {
        return ApplicationHook.requestString("alipay.charity.mobile.donate.walk.walkDonateSignInfo",
                "[{\"needDonateAction\":false,\"source\":\"walkDonateHome\",\"steps\":" + count
                        + ",\"timezoneId\":\""
                        + timeZone + "\"}]");
    }

    public static String donateWalkHome(int count) {
        return ApplicationHook.requestString("alipay.charity.mobile.donate.walk.home",
                "[{\"module\":\"3\",\"steps\":" + count + ",\"timezoneId\":\"" + timeZone + "\"}]");
    }

    public static String exchange(String actId, int count, String donateToken) {
        return ApplicationHook.requestString("alipay.charity.mobile.donate.walk.exchange",
                "[{\"actId\":\"" + actId + "\",\"count\":"
                        + count + ",\"donateToken\":\"" + donateToken + "\",\"timezoneId\":\""
                        + timeZone + "\",\"ver\":0}]");
    }

    /* 这个好像没用 */
    public static String exchangeSuccess(String exchangeId) {
        String args1 = "[{\"exchangeId\":\"" + exchangeId
                + "\",\"timezone\":\"GMT+08:00\",\"version\":\"" + version + "\"}]";
        return ApplicationHook.requestString("alipay.charity.mobile.donate.exchange.success", args1);
    }

    /* 文体中心 */
    public static String userTaskGroupQuery(String groupId) {
        return ApplicationHook.requestString("alipay.tiyubiz.sports.userTaskGroup.query",
                "[{\"cityCode\":\"" + cityCode + "\",\"groupId\":\"" + groupId + "\"}]");
    }

    public static String userTaskComplete(String bizType, String taskId) {
        return ApplicationHook.requestString("alipay.tiyubiz.sports.userTask.complete",
                "[{\"bizType\":\"" + bizType + "\",\"cityCode\":\"" + cityCode + "\",\"completedTime\":"
                        + System.currentTimeMillis() + ",\"taskId\":\"" + taskId + "\"}]");
    }

    public static String userTaskRightsReceive(String taskId, String userTaskId) {
        return ApplicationHook.requestString("alipay.tiyubiz.sports.userTaskRights.receive",
                "[{\"taskId\":\"" + taskId + "\",\"userTaskId\":\"" + userTaskId + "\"}]");
    }

    public static String queryAccount() {
        return ApplicationHook.requestString("alipay.tiyubiz.user.asset.query.account",
                "[{\"accountType\":\"TIYU_SEED\"}]");
    }

    public static String queryRoundList() {
        return ApplicationHook.requestString("alipay.tiyubiz.wenti.walk.queryRoundList",
                "[{}]");
    }

    public static String participate(int bettingPoints, String InstanceId, String ResultId, String roundId) {
        return ApplicationHook.requestString("alipay.tiyubiz.wenti.walk.participate",
                "[{\"bettingPoints\":" + bettingPoints + ",\"guessInstanceId\":\"" + InstanceId
                        + "\",\"guessResultId\":\"" + ResultId
                        + "\",\"newParticipant\":false,\"roundId\":\"" + roundId
                        + "\",\"stepTimeZone\":\"Asia/Shanghai\"}]");
    }

    public static String pathFeatureQuery() {
        return ApplicationHook.requestString("alipay.tiyubiz.path.feature.query",
                "[{\"appId\":\"" + appId
                        + "\",\"features\":[\"USER_CURRENT_PATH_SIMPLE\"],\"sceneCode\":\"wenti_shijiebei\"}]");
    }

    public static String pathMapJoin(String pathId) {
        return ApplicationHook.requestString("alipay.tiyubiz.path.map.join",
                "[{\"appId\":\"" + appId + "\",\"pathId\":\"" + pathId + "\"}]");
    }

    public static String pathMapHomepage(String pathId) {
        return ApplicationHook.requestString("alipay.tiyubiz.path.map.homepage",
                "[{\"appId\":\"" + appId + "\",\"pathId\":\"" + pathId + "\"}]");
    }

    public static String stepQuery(String countDate, String pathId) {
        return ApplicationHook.requestString("alipay.tiyubiz.path.map.step.query",
                "[{\"appId\":\"" + appId + "\",\"countDate\":\"" + countDate
                        + "\",\"pathId\":\""
                        + pathId + "\",\"timeZone\":\"Asia/Shanghai\"}]");
    }

    public static String tiyubizGo(String countDate, int goStepCount, String pathId, String userPathRecordId) {
        return ApplicationHook.requestString("alipay.tiyubiz.path.map.go",
                "[{\"appId\":\"" + appId + "\",\"countDate\":\"" + countDate
                        + "\",\"goStepCount\":"
                        + goStepCount + ",\"pathId\":\"" + pathId
                        + "\",\"timeZone\":\"Asia/Shanghai\",\"userPathRecordId\":\""
                        + userPathRecordId + "\"}]");
    }

    public static String rewardReceive(String pathId, String userPathRewardId) {
        return ApplicationHook.requestString("alipay.tiyubiz.path.map.reward.receive",
                "[{\"appId\":\"" + appId + "\",\"pathId\":\"" + pathId + "\",\"userPathRewardId\":\""
                        + userPathRewardId + "\"}]");
    }

    /* 抢好友大战 */
    public static String queryClubHome() {
        return ApplicationHook.requestString("alipay.antsports.club.home.queryClubHome",
                "[{\"chInfo\":\"healthstep\",\"timeZone\":\"Asia/Shanghai\"}]");
    }

    public static String collectBubble(String bubbleId) {
        return ApplicationHook.requestString("alipay.antsports.club.home.collectBubble",
                "[{\"bubbleId\":\"" + bubbleId + "\",\"chInfo\":\"healthstep\"}]");
    }

    public static String queryTrainItem() {
        return ApplicationHook.requestString("alipay.antsports.club.train.queryTrainItem",
                "[{\"chInfo\":\"healthstep\"}]");
    }

    public static String trainMember(String itemType, String memberId, String originBossId) {
        return ApplicationHook.requestString("alipay.antsports.club.train.trainMember",
                "[{\"chInfo\":\"healthstep\",\"itemType\":\"" + itemType + "\",\"memberId\":\"" + memberId + "\",\"originBossId\":\"" + originBossId + "\"}]");
    }

    public static String queryMemberPriceRanking(String coinBalance) {
        return ApplicationHook.requestString("alipay.antsports.club.ranking.queryMemberPriceRanking",
                "[{\"buyMember\":\"true\",\"chInfo\":\"healthstep\",\"coinBalance\":\"" + coinBalance + "\"}]");
    }

    public static String queryClubMember(String memberId, String originBossId) {
        return ApplicationHook.requestString("alipay.antsports.club.trade.queryClubMember",
                "[{\"chInfo\":\"healthstep\",\"memberId\":\"" + memberId + "\",\"originBossId\":\"" + originBossId + "\"}]");
    }

    public static String buyMember(String currentBossId, String memberId, String originBossId, String priceInfo, String roomId) {
        String requestData = "[{\"chInfo\":\"healthstep\",\"currentBossId\":\"" + currentBossId + "\",\"memberId\":\"" + memberId + "\",\"originBossId\":\"" + originBossId + "\",\"priceInfo\":" + priceInfo + ",\"roomId\":\"" + roomId + "\"}]";
        return ApplicationHook.requestString("alipay.antsports.club.trade.buyMember", requestData);
    }

}