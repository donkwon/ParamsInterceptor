# ParamsInterceptor

## 介绍
OkHttp拦截器，为请求体Body或者请求头Header添加公共参数

### 使用 Gradle 集成

将 `JitPack.io` 仓库添加到工程 `build.gradle` 中

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

然后，在应用 `build.gradle` 中添加依赖。

![OkHttp](https://img.shields.io/badge/OkHttp-3.9.0-yellow)
![OkHttp](https://img.shields.io/badge/Gson-2.8.2-blue)
```
compile 'com.github.donkwon:ParamsInterceptor:1.0.1'
```
或者：
```
compile ('com.github.donkwon:ParamsInterceptor:1.0.1'){
    exclude module: 'gson'
    exclude module: 'okhttp'
}
compile 'com.google.code.gson:gson:2.8.2'
compile 'com.squareup.okhttp3:okhttp:3.9.0'
```

### 用法
```
OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
    //加入公共请求参数
    .addInterceptor(new BodyParamsInterceptor() {
        @Override
        protected Map<String, String> getBodyCommonParameters() {
            Map<String, String> map = new HashMap<>();
            map.put("uid", "123");
            return map;
        }
    })
    //加入公共请求头
    .addInterceptor(new HeaderParamsInterceptor() {
        @Override
        protected Map<String, String> getHeaderParameters() {
            Map<String, String> map = new HashMap<>();
            map.put("token", "token");
            map.put("device", "android");
            return map;
        }
    });
```