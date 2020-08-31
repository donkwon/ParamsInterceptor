package com.github.donkwon.paramsinterceptor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import okhttp3.*;
import okio.Buffer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 请求体加参数
 * 支持GET\DELETE\PUT\POST
 * 支持：
 * 表单 FormBody application/x-www-form-urlencoded
 * 文件 MultipartBody multipart/form-data
 * json
 */
public abstract class BodyParamsInterceptor implements Interceptor {

    protected abstract Map<String, String> getBodyCommonParameters();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //获取到方法
        String method = request.method();

        RequestBody requestBody = request.body();

        //公共参数，后面可以通过配置的方式将这一块提取出来
        Map<String, String> commonParameters = getBodyCommonParameters();

        switch (method) {
            case "GET":
            case "DELETE":
                //Get请求的时候，公共参数加在URL上面
                request = insertParameters(request, request.url().newBuilder(), commonParameters);
                break;
            case "PUT":
                request = request.newBuilder()
                        .put(insertParameters(requestBody, commonParameters))
                        .build();
                break;
            case "POST":
                request = request.newBuilder()
                        .post(insertParameters(requestBody, commonParameters))
                        .build();
                break;
        }
        return chain.proceed(request);
    }


    /**
     * Url插入查询数据
     *
     * @param parameters 需要添加的参数集合
     */
    private Request insertParameters(Request request, HttpUrl.Builder builder, Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            //遍历加入需要添加的参数
            builder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        return request.newBuilder()
                .url(builder.build())
                .build();
    }


    private RequestBody insertParameters(RequestBody requestBody, Map<String, String> parameters) throws IOException {
        if (requestBody instanceof FormBody) {
            return insertParameters((FormBody) requestBody, parameters);
        }
        if (requestBody instanceof MultipartBody) {
            return insertParameters((MultipartBody) requestBody, parameters);
        }
        MediaType mediaType = null;
        if (requestBody != null) {
            mediaType = requestBody.contentType();
        }
        if (mediaType == null) {
            //如果为空的话，以json的形式直接传公共参数
            Buffer buffer = new Buffer();
            Writer writer = new OutputStreamWriter(buffer.outputStream(), Charset.forName("UTF-8"));
            JsonWriter jsonWriter = new Gson().newJsonWriter(writer);
            Gson mGson = new Gson();
            mGson.getAdapter(TypeToken.get(Map.class)).write(jsonWriter, parameters);
            jsonWriter.close();
            return RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), buffer.readByteString());
        }
        if (mediaType.subtype() != null
                && "json".equalsIgnoreCase(mediaType.subtype())) {
            //如果使用的@Body注解，这里的contentType来源为retrofit.addConverterFactory内部里面对RequestBody.setContentType
            Buffer sink = new Buffer();
            requestBody.writeTo(sink);
            Charset charset = mediaType.charset();
            if (charset == null) {
                charset = Charset.forName("UTF-8");
            }
            //读出原请求内容
            String jsonStr = sink.readString(charset);
            JsonObject jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                //加入公共参数
                jsonObject.addProperty(entry.getKey(), entry.getValue());
            }
            return RequestBody.create(requestBody.contentType(), jsonObject.toString());
        }
        return requestBody;
    }

    /**
     * MultipartBody插入需要添加的参数
     *
     * @param parameters 需要添加的参数集合
     */
    private MultipartBody insertParameters(MultipartBody multipartBody, Map<String, String> parameters) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (multipartBody != null) {
            for (MultipartBody.Part part : multipartBody.parts()) {
                //遍历加入原有参数
                builder.addPart(part);
            }
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            //遍历加入需要添加的参数
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    /**
     * FormBody插入需要添加的参数
     *
     * @param parameters 需要添加的参数集合
     */
    private FormBody insertParameters(FormBody formBody, Map<String, String> parameters) {
        FormBody.Builder builder = new FormBody.Builder();
        if (formBody != null) {
            for (int i = 0; i < formBody.size(); i++) {
                //遍历加入原有参数
                builder.add(formBody.name(i), formBody.value(i));
            }
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            //遍历加入需要添加的参数
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

}