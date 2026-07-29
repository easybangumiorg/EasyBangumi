# 弹弹play开放弹幕网络接入说明

本项目的内置弹幕源接入弹弹play开放弹幕网络。产品界面、关于页或源码说明中应使用完整名称“弹弹play开放弹幕网络”，并标注官网 `www.dandanplay.com`。

## 凭证配置

`AppId` 与 `AppSecret` 不得提交到版本库。Android 构建按以下优先级读取：

1. `DANDANPLAY_APP_ID` 与 `DANDANPLAY_APP_SECRET` 环境变量；
2. Gradle 属性 `dandanplay.appId` 与 `dandanplay.appSecret`；
3. 仅限本机、被 Git 忽略的根目录 `dandanplay.properties`。

当构建没有可用凭证时，应用必须将弹弹play显示为不可用，而不是发起未认证请求。

## 使用边界

- 请求使用签名模式：`base64(sha256(AppId + Timestamp + Path + AppSecret))`。
- 搜索、文件匹配与弹幕加载只在用户发起的播放或匹配操作中执行；应缓存结果并避免批量抓取。
- 弹幕加载使用 `GET /api/v2/comment/{episodeId}?withRelated=true`；不使用已下线的 `related` 或 `extcomment` 接口。
- 不将弹幕功能作为付费卖点；如需商业使用或较高配额，向弹弹play申请授权。
