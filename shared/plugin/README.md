plugin 包含

拓展层 extension - 额外代码加载（js, pkg）

番源层 source - 包括元数据源和播放源，比如（Bangumi，xx看番 等）
    
业务层 component - 番源提供的业务，比如发现页 - 搜索 - 播放 - 配置

* :plugin 入口模块，注册 koin，注入配置
* :plugin:api: 接口和实体
* :plugin:core: plugin 加载管理核心代码
* :plugin:utils: 提供给额外代码的工具，如加解密，无头浏览器嗅探等
* :plugin:inner: 内置番剧源
