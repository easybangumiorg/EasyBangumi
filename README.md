纯纯看番 KMP 项目，初步计划支持 Android, Desktop

* app 顶层壳模块，平台特化，依赖 shared 根模块 和 lib 模块
* shared 共享模块，主要业务逻辑所在，树形结构，分为根模块和子模块
* lib 底层库模块，比较独立的工具

其中 shared 为树形结构，并且 shared 子模块只能被其他 shared 子模块或者 shared 根模块依赖

# Ayala 的奇思妙想
## 功能框图

```mermaid
flowchart TD
    subgraph user_interface["用户界面"]
        subgraph main["主页"]
            多视图集合主页
            标签视图
            文件系统视图
            element_only["仅元素，由纯纯看番统一呈现"]
        end
        subgraph search["搜索"]
            年代
            类型
        end
        subgraph repository["媒体库"]
            搜索
            合集
            喜爱
        end
        subgraph media_present["媒体呈现"]
            源/播放列表/视频
            源/章节/文章漫画
            源/专辑/音乐
        end
    end
    subgraph plugin_host["插件主机"]
        JS运行时
        固化插件
        插件数据库
        管理器
        执行器
        调度
    end
    subgraph plugin_asbtract["插件抽象集"]
        目录资源
        元数据资源
        媒体资源
        附加资源
        数据同步
    end
    subgraph local_repository["本地插件"]
        解析器
        封装
        目录监听
        组合器
        虚拟文件系统
    end
    subgraph local_repository_asbtract["本地抽象"]
        本地媒体虚拟目录
        本地状态更新
        虚目录与实库存项绑定
    end
    subgraph repository_host["库存主机"]
        Notefication["库存通知 Notefication"]
        Provider["库存内容提供 Provider"]
        Receiver["库存事件 Receiver"]
        Bridge["插件扩展桥 Bridge"]
        库存数据库
        定时任务
        本地源管理
        聚合
        媒体资源处理
        媒体类型管理
    end
    subgraph universal_media_interface["通用媒体接口"]
        资源交互
        配置项
        插件管理
    end
    subgraph application_host["应用主机"]
        语言
        KV数据库
        关系型数据库
        依赖注入
        文件系统
        下载器
        流处理
        携程
        编解码
    end
    subgraph plugins["外部插件"]
        jellyfin.js
        bangumi.meta.js
    end
    plugins --> plugin_host
    plugin_host --> plugin_asbtract
    plugin_asbtract --> repository_host
    application_host --> plugin_host
    application_host --> repository_host
    application_host --> local_repository
    local_repository --> local_repository_asbtract
    local_repository_asbtract --> repository_host
    Provider --> universal_media_interface
    Receiver --> universal_media_interface
    Bridge --> universal_media_interface
    Notefication --> universal_media_interface
    universal_media_interface --> user_interface
    application_host --> user_interface
```

1. ~~不要做成纯纯屯番~~（屯番好快乐啊）
2. 将本地源做成组件注入JS插件二次抽象是不错的方法，但是限制太大，等同于做了一个万能的第三方客户端
3. 插件是组件形式的，可以声明式，也能注册式

## 架构图

```mermaid
flowchart TD

    subgraph framework ["lib"]
        unifile[unifile <br/> 文件抽象]
        javascript[javascript <br/> JS运行时]
        inject[inject <br/> 依赖注入]
    end

    subgraph app_shared_platform[":app:shared:platform"]
        PlatformInformaiton
        PlatformLogger
        PlatformPath
    end
  
    subgraph base_utils[":base:utils"]
        unInjectable_utils
        Injectable_utils
    end
    
    subgraph base_model[":base:model"]
        system
        io
        provider
        etc.
    end

    subgraph base_compose[":base:compose"]
        BackHandler
    end

    subgraph dataSource ["数据源"]
        direction LR
        subgraph plugin
            api
            core
            dataSource_utils["utils"]
        end
        plugin --> repository
    end

    subgraph app[":app"]
        preference
        subgraph ui ["UI<br/>目前未动工"]
        
        end
        subgraph app_shared[":app:shared"]
            room[(room)]
        end
        ui --> app_shared
    end
    
    app --> base_compose
    app --> base_model
    app_shared --> base_model
    app_shared --> base_utils
    app_shared -->|inject| app_shared_platform
    app_shared_platform --> base_model
    base_utils --> base_model
    dataSource --> base_model
```

是不是很混乱，等何言自己改吧

## 关于~~如何重写~~奇思妙想

~~这是一个复杂的系统工程，难以做到尽善尽美，及其容易顾此失彼，究竟是该侧重于数据源测呢还是倾向于元数据源测呢，该设计何种API以提供二级功能呢，这是一个很难回答的问题。~~

本地CMS，带点插件可以提供额外功能，还能在局域网里互相提供资源，没错，就是为了愉快的看番！

--Ayala评

计划是作为一个媒体中间件的概念做设计，该中间件需要有多个输入端和多个输出端。

资源应当予以分类

1. 目录资源：目录资源是指资源的目录结构，比如文件夹、Jellfin的媒体库、某个视频网站的首页、搜索等。
2. 元数据源：元数据源是指资源的元数据，比如视频的标题、封面、简介等，具体可以来自TMDB、豆瓣、Bangumi等。
3. 媒体源：媒体源是指资源的实际内容，比如视频、音频、图片、电子书、RSS等，媒体枚举也包含在该源内。
4. 附加项目源：附加项目是指资源的附加内容，比如字幕、评论、弹幕等。

资源分为目录类型、请求类型以及载入类型：

1. 目录类型可以直接是Jellfin的媒体库（文件夹）或是本地网络上的NAS或者是托管的本体源或者是任意文件夹，由于目录不一定什么情况下都有写权限，关于目录的索引会保存到本地的数据库。
2. 请求类型指的是资源的读权限相对受限的情况，比如远程的Jellfin的媒体库，这种情况下需要通过Jellfin的API来获取资源。
3. 载入类型指的是资源的读取具有延迟的情况，比如无法流式传输的资源、BitTorrent的资源、需要解密的资源等。

对应的获取途径：

1. 文件系统（SMB等）
2. 网络请求（webdev、RESTful API、RPC等）
3. 下载器（bittorrent，csv等）

资源管理与媒体源可以进行组合，比如请求类型的目录资源、载入类型的媒体源、目录类型的元数据源（Jellyfin的媒体库文件夹）等，具体实现逻辑交由插件实现，这里仅提供思路，
**纯纯看番仅对资源进行实现**。

资源的呈现方式并不唯一，但是应该实现一个层级，在这一层级之上可以是用户界面，也可以是web服务器，甚至可以作为封装库被其他APP使用。

### 插件主机

插件主机为每个插件实例化一个plugin接口，插件作者在plugin上注册自己的插件，并实现某一种拦截器。

每种数据源会被拆分为多个步骤，插件开发者可以实现一些步骤使得插件具备某些功能，这些步骤应当以异步拦截器的形式存在，需要完成某些到步骤后，资源才能开始呈现到用户。

比如用js实现插件源可以这样：

```js
// @key com.ayala.ebplugin.jellfin
// @label Jellyfin
// @versionName 1.6
// @versionCode 7
// @libVersion 11
// @cover https://jellyfin.org/images/favicon.ico

// 为了在不执行插件代码的情况下还能获取插件元数据，需要额外补充一个头部

// 话说为啥不直接在头部塞个json，然后在插件注册阶段注入回来啊？

// 插件注册，不可以异步执行，如果插件加载超过一定时间直接视为加载失败
plugin.register({
  name: 'Jellfin',
  description: '提供对Jellyfin服务器的支持',
  package: 'com.ayala.ebplugin.jellfin',
  keyword: ['jellfin', 'localmedia'],
  version: '1.0.0',
  minHostVersion: 1,
  icon: 'icon.png',
  access: ['network', 'fs-read', 'fs-write'， 'internal-aria', 'notefication'， 'webview'， 'uri', 'pipeline', 'interceptor'],
  preferrence: {
    server: {
      type: 'input',
      name: '服务器地址',
      default: 'http://localhost:8096',
      description: 'Jellyfin服务器的地址，例如http://localhost:8096'
    },
    username: {
      type: 'input',
      name: '用户名',
      default: 'admin',
      description: 'Jellyfin服务器的用户名'
    },
    password: {
      type: 'password',
      name: '密码',
      default: 'password',
      description: 'Jellyfin服务器的密码'
    },
    action_login: {
      type: 'action',
      name: '登录',
      description: '登录到Jellyfin服务器',
      action: async (config) => {
        // 登录到Jellyfin服务器
      }
    }
  },
  repository : {
    url: 'https://xxxx.com/xxxx/xxxx.git',
    branch: 'master'
  }
})

// 插件生命周期实现
plugin.on_init(async () => {
  // 初始化插件
  // 在初始化过程中，插件可以检查自身是否拥有足够的权限
  // 具有权限后纯纯看番才会注入相应的接口，请务必不要脱离生命周期
  if (!plugin.has_access('network'))
    throw new AccessNotEncludedException('没有足够的权限')
})
plugin.on_close(async () => {
  // 关闭纯纯看番前执行
})
plugin.on_install(async () => {
  // 当安装插件后执行
})
plugin.on_uninstall(async () => {
  // 当卸载插件前执行
})

// 数据源相关

// 当注册directory后，插件视为具备目录资源，将会在纯纯看番主页展示该插件的一个标签页
// directory是插件唯一的，不可以与其他插件共享
plugin.directory(async tab => {
    // 你可以异步的获取插件应该展示的标签，标签可以重复定义，但是不能重名
    // 除了仿文件系统的标签，其余标签均具有缓存属性，但是在定义时可以设置每次访问都刷新
    tab.homepage('首页', async block => {
        // 当展示为首页时可以使用一些预制的组件
        block.carousel('热门', async () => {
            // 轮播图组件
        })
        block.horizontal_list('最近更新', async () => {
            // 水平列表组件
        })
        block.grid('', async (context) => {
            // 网格组件，可以用于展示更多
            // 此外，当不指定组件名称时，组件名称将被隐藏
        })
    })
    // 以下兼容老版更好的起始插件，应该是最先支持的
    tab.page('分类'， async subtab => {
        subtab.page('战斗', async (context) => {
            context.has_nextpage()
            return ...
        })
        subtab.page('热血', async (context) => { })
        subtab.page('泡面', async (context) => { })
    })
    // 你也可以直接以文件浏览器的形式展示
    tab.explorer('浏览' async fs => {
        // fs是一个抽象的文件系统对象，用于展示一个虚拟路径
        fs.on_list(async (path) => {
            // 列出目录下的内容
            // 返回虚路径对象以及若干错误
        })
        fs.on_open(async (path) => {
            // 打开文件，获取文件实媒体对象
        })
        fs.on_menu('删除', vfs.File_type, async (virtual_file) => {
            // 右键菜单，'删除'时干什么
        })
    })
    // 当你的插件只有一个tab时，将不会展示tab的名称
    tab.always_show_tab_name = true
})

// 当注册search后，插件视为具备搜索资源
plugin.search(async (context) => {
    context.page
    context.has_nextpage()
    return [] // 当返回空列表时，纯纯看番会认为没有更多的资源
})

// content_type有 bangumi(节目、电影都在这个分类下)，e_book, audio, image
plugin.info_of(content_type.bangumi, async (context) => {
    从 context.summary 获取并返回 info
    // 获取资源的描述信息，将会返回当前源能获取的所有信息
    // 之后也有可能调用相应pipeline获取更加详细的信息(meta_info)
    // 在这一步开始就应该区分资源的类型，比如是目录类型、请求类型还是载入类型，以及资源是否可以访问
    return ...
})

plugin.media_of(content_type.bangumi, async (context) => {
    // 获取资源的实际内容
    return ...
})

plugin.do_update(content_type.bangumi, async (context) => {
    // 检查资源是否有更新
    return ...
})

// Pipeline 相关

plugin.pipeline(pipeline.meta, content_type.bangumi, async (content) => {
    // 处理节目类型资源的元数据
    // 管道是一种后处理的特性，纯纯看番会优先展示已经获取到的元数据
    // 但是会额外调用管道（如果插件具有pipeline权限的话），管道可以对不完整元数据进行处理
    // 如果已经是详细的元数据了，也可以主动返回
    return content
})

// Interceptor 相关

// 附加项目资源由拦截器提供，旨在为具体资源提供弹幕、评论、进度支持
plugin.interceptor(interceptor.danmaku, content_type.bangumi, async (context) => {
    // 返回弹幕列表
})

plugin.interceptor(interceptor.comment, content_type.bangumi, async (context) => {
    // 返回评论列表
})

plugin.interceptor(interceptor.progress, content_type.bangumi, async (context) => {
    // 将进度同步到不知道哪里
})

```

这不一定是最万金油的做法，如果硬要在元数据源和数据源中分一个主次的话，我会更倾向于元数据源，但是考虑到纯纯看番应该会做到干净安装后没有任何插件的情况下，那么对于数据库的设计应该是跟占上风的。

新描述：不再采用拦截器而是直接问插件提供这个功能不，评论字幕弹幕什么的都可以本地提供，再写套插件改为云提供，总之做到就算不在线看也能有内容。


## 精简描述

可以看到以上两节写了大量废话，最终采用何种设计也未知，当发生具体的变化后将会把内容填充到下面。