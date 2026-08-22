# EasyBangumi 品牌资产

## 当前品牌结论

- 正式 Logo 是黄发魔法少女（Arcana）头像。
- `sources/` 保存设计母版或可恢复的最高质量来源，不参与 Android 资源打包。
- `app/src/main/res` 与 `app/src/main/assets` 只保存运行时派生物。
- `archive/` 保存已确认无运行时引用、被替代或误判的资产，可按归档说明恢复。

当前没有找到生成正式 Logo 时使用的更高分辨率原图，因此
`sources/arcana-v1/identity/app-logo-master.png` 是从当前 512×512 正式图标冻结的
`recovered-derivative` 母版。后续若找回原始母版，应升级来源并重新生成派生物。

## 运行时资产清单

| 角色 | 运行时路径 | 状态 |
| --- | --- | --- |
| 正式 Logo | `app/src/main/res/mipmap-xxhdpi/logo_new.png` | 正在使用 |
| Debug Logo | `app/src/debug/res/mipmap-xxhdpi/logo_debug.png` | 正在使用，由 debug Manifest 覆盖 |
| 空态 | `app/src/main/res/drawable/empty_bocchi.png` | 正在使用 |
| 加载态 | `app/src/main/assets/loading_ryo.gif` | 正在使用 |
| 错误态 | `app/src/main/res/drawable/error_ikuyo.png` | 正在使用 |
| 人机校验 | `app/src/main/res/drawable-nodpi/search_verification.png` | 正在使用，供所有搜索模式复用 |

## 视觉不变量

- 角色：金黄色长发、粉紫色眼睛、紫金魔法饰品、Q 版比例。
- 主色：暖黄、宝石紫、粉紫；深色只用于服装和轮廓。
- Logo 必须保持面部、发色和紫金法杖可识别，不能换成其他角色。
- Debug 版本只能增加调试标识，不得替换角色主体。
- 状态插画可以更换动作与道具，但必须延续同一角色、线条和配色体系。
- 人机校验使用紫金盾牌与发光对勾；小尺寸优先保证脸部和盾牌轮廓可辨识。

## 生成

```bash
python3 tools/process_brand_assets.py \
  --logo design/brand/sources/arcana-v1/identity/app-logo-master.png \
  --empty <empty-source> \
  --loading <loading-source> \
  --error <error-source> \
  --verification design/brand/sources/arcana-v1/verification/search-verification-master.png \
  --project-root .
```

脚本只生成被 Android 实际使用的运行时文件，不再生成无引用的
`res/mipmap-xxhdpi/app_logo.png` 或根目录副本。

## 规则

1. 新资产先登记到 `manifest.yaml`，再进入运行时目录。
2. 归档前必须同时检查源码引用、Manifest/source set 覆盖和工具脚本输出。
3. 仍被运行时引用的资产不得归档；风格不一致时先标记为待迁移。
4. 归档是可恢复移动，不直接删除二进制资产。
