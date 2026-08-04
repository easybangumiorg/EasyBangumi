# 2026-07-31 品牌资产归档

本目录保存本次静态引用、Android Manifest/source set 和生成脚本审计中确认不参与
当前运行时的资产。所有文件均为可恢复移动，没有永久删除。

| 归档内容 | 原路径 | 原因 |
| --- | --- | --- |
| `runtime-duplicates/app_logo.png` | `app/src/main/res/mipmap-xxhdpi/app_logo.png` | 与正式 `logo_new.png` 字节相同且零引用 |
| `legacy-character/ayala.png` | `app/src/main/res/drawable/ayala.png` | 旧角色图片，零引用且不属于 Arcana 体系 |
| `legacy-verification/overview_verification.png` | `app/src/main/res/drawable/overview_verification.png` | 已由 Arcana 黄发女孩校验资产替换 |
| `false-logo-investigation/*` | `design-audit/overview-verification/` | 为辨认真正 Logo 临时提取的历史/设备图片，结论已否定 |
| `obscured-evidence/01-current-popup-obscured.png` | `design-audit/overview-verification/01-current.png` | 被弹窗遮挡的重复审计截图 |
| `generation-inputs/search-verification-chroma.png` | ImageGen 输出 | 仅用于生成透明母版的绿幕过程文件 |
| `redundant-evidence/07-final-build-verification-not-returned.png` | 最终重装后的重复截图 | 远端来源未再返回校验异常，已有 `06-final-build-source-resolved.png` 记录同类状态 |

恢复时将文件移回“原路径”即可。`runtime-duplicates/app_logo.png` 若恢复，还需要同时
恢复 `tools/process_brand_assets.py` 的对应输出目标，否则下次生成不会更新它。
