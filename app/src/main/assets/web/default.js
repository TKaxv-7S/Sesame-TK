var defaultJson = (function (exports) {

  const default_BaseModel = {
    "enable": {
      "name": "启用模块",
      "value": false,
      "type": "BOOLEAN"
    },
    "stayAwake": {
      "name": "保持唤醒",
      "value": true,
      "type": "BOOLEAN"
    },
    "checkInterval": {
      "name": "执行间隔(分钟)",
      "value": 3300000,
      "type": "MULTIPLY_INTEGER"
    },
    "execAtTimeList": {
      "name": "定时执行(关闭:-1)",
      "value": [
        "-1",
        "2400"
      ],
      "type": "LIST"
    },
    "wakenAtTimeList": {
      "name": "定时唤醒(关闭:-1)",
      "value": [
        "0650",
        "2350"
      ],
      "type": "LIST"
    },
    "energyTime": {
      "name": "只收能量时间(范围)",
      "value": [
        "0700-0731"
      ],
      "type": "LIST"
    },
    "timedTaskModel": {
      "name": "定时任务模式",
      "value": 0,
      "type": "CHOICE"
    },
    "timeoutRestart": {
      "name": "超时重启",
      "value": true,
      "type": "BOOLEAN"
    },
    "waitWhenException": {
      "name": "异常等待时间(分钟)",
      "value": 3600000,
      "type": "MULTIPLY_INTEGER"
    },
    "batteryPerm": {
      "name": "为支付宝申请后台运行权限",
      "value": true,
      "type": "BOOLEAN"
    },
    "newRpc": {
      "name": "使用新接口(最低支持v10.3.96.8100)",
      "value": true,
      "type": "BOOLEAN"
    },
    "debugMode": {
      "name": "开启抓包(基于新接口)",
      "value": false,
      "type": "BOOLEAN"
    },
    "recordLog": {
      "name": "记录日志",
      "value": true,
      "type": "BOOLEAN"
    },
    "enableOnGoing": {
      "name": "开启状态栏禁删",
      "value": false,
      "type": "BOOLEAN"
    },
    "languageSimplifiedChinese": {
      "name": "只显示中文并设置时区",
      "value": true,
      "type": "BOOLEAN"
    },
    "showToast": {
      "name": "气泡提示",
      "value": true,
      "type": "BOOLEAN"
    },
    "toastOffsetY": {
      "name": "气泡纵向偏移",
      "value": 0,
      "type": "INTEGER"
    }
  }

  const default_AntForestV2 = {
    "enable": {
      "name": "开启森林",
      "value": false,
      "type": "BOOLEAN"
    },
    "collectEnergy": {
      "name": "收集能量",
      "value": false,
      "type": "BOOLEAN"
    },
    "batchRobEnergy": {
      "name": "一键收取",
      "value": false,
      "type": "BOOLEAN"
    },
    "queryInterval": {
      "name": "查询间隔(毫秒或毫秒范围)",
      "value": "500-1000",
      "type": "STRING"
    },
    "collectInterval": {
      "name": "收取间隔(毫秒或毫秒范围)",
      "value": "1000-1500",
      "type": "STRING"
    },
    "doubleCollectInterval": {
      "name": "双击收取间隔(毫秒或毫秒范围)",
      "value": "50-150",
      "type": "STRING"
    },
    "balanceNetworkDelay": {
      "name": "平衡网络延迟",
      "value": true,
      "type": "BOOLEAN"
    },
    "advanceTime": {
      "name": "提前时间(毫秒)",
      "value": 0,
      "type": "INTEGER"
    },
    "tryCount": {
      "name": "尝试收取(次数)",
      "value": 1,
      "type": "INTEGER"
    },
    "retryInterval": {
      "name": "重试间隔(毫秒)",
      "value": 1000,
      "type": "INTEGER"
    },
    "dontCollectList": {
      "name": "不收取能量列表",
      "value": {
        "key": {},
        "value": false
      },
      "type": "SELECT"
    },
    "doubleCard": {
      "name": "双击卡 | 使用",
      "value": false,
      "type": "BOOLEAN"
    },
    "doubleCountLimit": {
      "name": "双击卡 | 使用次数",
      "value": 6,
      "type": "INTEGER"
    },
    "doubleCardTime": {
      "name": "双击卡 | 使用时间(范围)",
      "value": [
        "0700-0730"
      ],
      "type": "LIST"
    },
    "returnWater10": {
      "name": "返水 | 10克需收能量(关闭:0)",
      "value": 0,
      "type": "INTEGER"
    },
    "returnWater18": {
      "name": "返水 | 18克需收能量(关闭:0)",
      "value": 0,
      "type": "INTEGER"
    },
    "returnWater33": {
      "name": "返水 | 33克需收能量(关闭:0)",
      "value": 0,
      "type": "INTEGER"
    },
    "waterFriendList": {
      "name": "浇水 | 好友列表",
      "value": {
        "key": {},
        "value": true
      },
      "type": "SELECT"
    },
    "waterFriendCount": {
      "name": "浇水 | 克数(10 18 33 66)",
      "value": 66,
      "type": "INTEGER"
    },
    "helpFriendCollect": {
      "name": "复活能量 | 开启",
      "value": false,
      "type": "BOOLEAN"
    },
    "helpFriendCollectType": {
      "name": "复活能量 | 动作",
      "value": 0,
      "type": "CHOICE"
    },
    "helpFriendCollectList": {
      "name": "复活能量 | 好友列表",
      "value": {
        "key": {},
        "value": false
      },
      "type": "SELECT"
    },
    "exchangeEnergyDoubleClick": {
      "name": "活力值 | 兑换限时双击卡",
      "value": false,
      "type": "BOOLEAN"
    },
    "exchangeEnergyDoubleClickCount": {
      "name": "活力值 | 兑换限时双击卡数量",
      "value": 6,
      "type": "INTEGER"
    },
    "exchangeEnergyDoubleClickLongTime": {
      "name": "活力值 | 兑换永久双击卡",
      "value": false,
      "type": "BOOLEAN"
    },
    "exchangeEnergyDoubleClickCountLongTime": {
      "name": "活力值 | 兑换永久双击卡数量",
      "value": 6,
      "type": "INTEGER"
    },
    "collectProp": {
      "name": "收集道具",
      "value": false,
      "type": "BOOLEAN"
    },
    "collectWateringBubble": {
      "name": "收金球",
      "value": false,
      "type": "BOOLEAN"
    },
    "energyRain": {
      "name": "能量雨",
      "value": false,
      "type": "BOOLEAN"
    },
    "giveEnergyRainList": {
      "name": "赠送能量雨列表",
      "value": {
        "key": {},
        "value": false
      },
      "type": "SELECT"
    },
    "animalConsumeProp": {
      "name": "派遣动物",
      "value": false,
      "type": "BOOLEAN"
    },
    "userPatrol": {
      "name": "巡护森林",
      "value": false,
      "type": "BOOLEAN"
    },
    "receiveForestTaskAward": {
      "name": "森林任务",
      "value": false,
      "type": "BOOLEAN"
    },
    "antdodoCollect": {
      "name": "神奇物种开卡",
      "value": false,
      "type": "BOOLEAN"
    },
    "totalCertCount": {
      "name": "记录证书总数",
      "value": false,
      "type": "BOOLEAN"
    },
    "collectGiftBox": {
      "name": "领取礼盒",
      "value": false,
      "type": "BOOLEAN"
    },
    "medicalHealthFeeds": {
      "name": "健康医疗",
      "value": false,
      "type": "BOOLEAN"
    },
    "sendEnergyByAction": {
      "name": "森林集市",
      "value": false,
      "type": "BOOLEAN"
    },
    "sendFriendCard": {
      "name": "送卡片好友列表(当前图鉴所有卡片)",
      "value": {
        "key": {},
        "value": false
      },
      "type": "SELECT"
    },
    "whoYouWantToGiveTo": {
      "name": "赠送道具好友列表（所有可送道具）",
      "value": {
        "key": {},
        "value": false
      },
      "type": "SELECT"
    },
    "ecoLifeTick": {
      "name": "绿色 | 行动打卡",
      "value": false,
      "type": "BOOLEAN"
    },
    "ecoLifeOpen": {
      "name": "绿色 | 自动开通",
      "value": false,
      "type": "BOOLEAN"
    },
    "photoGuangPan": {
      "name": "绿色 | 光盘行动",
      "value": false,
      "type": "BOOLEAN"
    },
    "photoGuangPanBefore": {
      "name": "绿色 | 光盘前图片ID",
      "value": "",
      "type": "TEXT"
    },
    "photoGuangPanAfter": {
      "name": "绿色 | 光盘后图片ID",
      "value": "",
      "type": "TEXT"
    },
    "photoGuangPanClear": {
      "name": "绿色 | 清空图片ID",
      "type": "EMPTY"
    }
  }

  exports.default_BaseModel = default_BaseModel;
  exports.default_AntForestV2 = default_AntForestV2;
  return exports;
})({})