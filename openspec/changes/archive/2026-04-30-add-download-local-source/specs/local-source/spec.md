## ADDED Requirements

### Requirement: 本地源注册

系统 SHALL 将本地源注册为 InnerSource，key 为 `"easybangumi_local"`，实现 PlayComponent 和 PrefComponent。

#### Scenario: 本地源出现在源列表
- **WHEN** 应用启动加载所有源
- **THEN** 本地源（LocalInnerSource）出现在已启用的源列表中

#### Scenario: 本地源提供播放能力
- **WHEN** 用户请求播放本地番剧的剧集
- **THEN** LocalPlayComponent 返回 PlayInfo（type=TYPE_NORMAL, url=本地文件路径）

### Requirement: 本地文件夹扫描

LocalCartoonController SHALL 扫描本地根目录下的所有子文件夹，解析 Kodi NFO 元数据。

#### Scenario: 扫描本地文件夹
- **WHEN** 本地根目录存在且包含子文件夹
- **THEN** 控制器遍历所有子文件夹，对每个调用 LocalItemFactory.getItemFromFolder()

#### Scenario: 自动刷新
- **WHEN** 下载完成或路径配置变更
- **THEN** 控制器自动重新扫描文件夹

#### Scenario: 扫描结果
- **WHEN** 扫描完成
- **THEN** 返回 Map<String, LocalCartoonItem>，以 folderUri 为 key

### Requirement: Kodi NFO 解析

LocalItemFactory SHALL 解析 Kodi NFO 格式的元数据文件。

#### Scenario: 解析 tvshow.nfo
- **WHEN** 文件夹中存在 tvshow.nfo
- **THEN** 解析出 title、plot、poster、tag 等元数据

#### Scenario: 解析剧集 NFO
- **WHEN** 媒体文件旁存在同名 .nfo 文件
- **THEN** 从 `<episodedetails>` 中解析 title、episode 编号

#### Scenario: 无 NFO 时的降级
- **WHEN** 媒体文件旁没有 .nfo 文件
- **THEN** 从文件名刮削剧集编号（正则提取 E 后面的数字）

#### Scenario: 支持的媒体格式
- **WHEN** 扫描文件夹中的文件
- **THEN** 仅处理 .mp4 和 .mkv 格式的文件

### Requirement: Kodi NFO 生成

系统 SHALL 生成符合 Kodi 标准的 NFO 文件。

#### Scenario: 生成 tvshow.nfo
- **WHEN** 新建本地番剧条目
- **THEN** 生成包含 `<tvshow>` 根元素的 NFO，包含 title、plot、art/poster、tag

#### Scenario: 生成剧集 NFO
- **WHEN** 下载完成复制视频文件
- **THEN** 生成包含 `<episodedetails>` 根元素的 NFO，包含 title、season、episode

### Requirement: Android 路径双模式

系统 SHALL 支持私有目录和用户自选目录两种本地存储路径。

#### Scenario: 使用私有目录
- **WHEN** 用户选择私有目录模式
- **THEN** 本地文件存储在 app 内部存储的 `local_source` 目录下

#### Scenario: 使用用户自选目录
- **WHEN** 用户通过 SAF 选择外部目录
- **THEN** 系统在该目录下创建 `local_bangumi` 子目录，存储为 `UFD(TYPE_ANDROID_UNI, uri)`

#### Scenario: 权限丢失回退
- **WHEN** 用户自选目录的 SAF 权限丢失
- **THEN** 系统自动回退到私有目录并提示用户

### Requirement: .nomedia 支持

系统 SHALL 支持在本地番剧目录创建 .nomedia 文件，防止系统媒体扫描。

#### Scenario: 自动创建 .nomedia
- **WHEN** 用户启用 .nomedia 选项且本地目录创建时
- **THEN** 系统在 local_bangumi 目录下创建 .nomedia 文件

### Requirement: 本地番剧删除

系统 SHALL 支持删除本地番剧及其所有文件。

#### Scenario: 删除整个番剧
- **WHEN** 用户删除一个本地番剧条目
- **THEN** 系统删除对应的文件夹，刷新本地源，级联删除相关下载请求

#### Scenario: 删除单集
- **WHEN** 用户删除一个本地剧集
- **THEN** 系统删除对应的 .mp4/.mkv 和 .nfo 文件，刷新本地源

### Requirement: PrefComponent 配置

LocalPrefComponent SHALL 提供本地源的配置界面，包括路径选择和 .nomedia 开关。

#### Scenario: 显示路径配置
- **WHEN** 用户打开本地源设置
- **THEN** 显示当前路径模式（私有/用户自选）和实际路径

#### Scenario: 切换路径模式
- **WHEN** 用户切换路径模式
- **THEN** 保存配置，触发本地源重新扫描
