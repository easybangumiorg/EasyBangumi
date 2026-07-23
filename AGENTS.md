# EasyBangumi 项目规则

## Git 规范

以下规范适用于整个仓库。历史分支、提交和 Tag 中不符合本规范的内容仅视为历史遗留，不作为新工作的命名示例。

### 分支

- 需求分支统一命名为 `feat/<meaningful-name>`。
- Git 分支名不能以 `/` 开头，因此 `/feat/XX` 仅表示命名结构，实际名称必须写成 `feat/XX`。
- `<meaningful-name>` 必须直接表达需求内容，优先使用小写英文 kebab-case；可选地以前置 Issue 编号增强可追溯性。
- 推荐示例：`feat/danmaku-settings`、`feat/123-source-search-migration`。
- 禁止使用无实际含义或只表示版本的名称，例如 `feat/xx`、`feat/test`、`feat/6.0`。
- 缺陷修复分支使用 `bugfix/<meaningful-name>`，并遵守相同的语义化命名规则。

### Commit message

- 普通开发提交按变更类型分模块描述，只保留实际存在的模块，并按 `[feat]`、`[bugfix]` 的顺序书写。
- 每个模块内的事项从 `1.` 开始连续编号，事项之间用空格分隔；描述应明确说明实际完成的功能或修复的问题。
- 同时包含功能和修复时使用以下格式：

  ```text
  [feat] 1. 新增 xxx 2. 支持 xxx
  [bugfix] 1. 修复 xxx 2. 修复 xxx
  ```

- 仅有一个模块时省略另一个模块，例如：

  ```text
  [bugfix] 1. 修复搜索结果重复问题
  ```

- 不使用 `[fix]`、`feat:` 等同义但不统一的类型标记。
- `[release] <versionName>` 仅用于包含 `versionCode` / `versionName` 版本变更、并将被对应版本 Tag 标记的发布提交；不得用于普通提交，也不得与 `[feat]` 或 `[bugfix]` 混写。
- 发布提交示例：`[release] 5.7.0`。

### Tag 与发布

- 每个正式版本只创建一个 Tag，Tag 名必须是三段纯数字版本号：`<major>.<minor>.<patch>`，例如 `5.7.0`。
- Tag 名不得带 `v` 前缀、后缀或其他说明文字，并且必须与 `buildSrc/src/main/java/com/heyanle/buildsrc/Android.kt` 中的 `versionName` 完全一致。
- Tag 必须是 annotated tag，并指向同版本的 `[release] <versionName>` 提交。
- 推送任意 Tag 都会触发 `.github/workflows/release.yml`，因此禁止用临时 Tag 测试或绕过版本号规范。
- 涉及 `versionCode`、`versionName`、发布提交、Tag 或推送发布时，必须使用 `.agents/skills/easybangumi-release/SKILL.md` 执行完整发布流程。
- 在该 skill 要求的版本、更新日志、应用数据迁移、Room 迁移、内置源检查、验证和人工确认全部完成前，不得创建发布提交、Tag 或执行推送。
- 发布前只暂存已经审核的明确文件路径，保留工作区中的无关改动。
- 创建 Tag 前必须确认同名 Tag 在本地和远端均不存在；除非得到明确指示，不得删除、移动或重建已经发布的 Tag。
