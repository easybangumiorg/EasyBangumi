暂时先不考虑 ios，只考虑 Android 和 Jvm  
这里模块都是纯净的模块，不包含跨平台。  
可以让 AndroidUtils 依赖 JvmUtils 并且没有编译的一堆坑 

```
AndroidApp -> utils-Android
                    ↓
DesktopApp -> utils-Jvm 
```


