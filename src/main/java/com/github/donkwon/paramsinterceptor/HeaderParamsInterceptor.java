package com.github.donkwon.paramsinterceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public abstract class HeaderParamsInterceptor implements Interceptor {

    protected abstract Map<String, String> getHeaderParameters();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        Map<String, String> headers = getHeaderParameters();
        if (headers != null) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                    builder.addHeader(key, value);
                }
            }
        }
        return chain.proceed(builder.build());
    }
}
