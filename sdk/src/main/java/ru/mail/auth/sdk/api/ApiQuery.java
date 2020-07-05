package ru.mail.auth.sdk.api;

import android.text.TextUtils;
import android.util.Pair;

import java.util.LinkedList;
import java.util.List;

public class ApiQuery {
    private final String mHost;
    private final String mMethodName;
    private final Method mMethod;
    private final List<Pair<String, String>> mGetArgs;
    private final List<Pair<String, String>> mPostArgs;
    private final String mPostBody;
    private final ContentType mContentType;

    private ApiQuery(String host,
                     String methodName,
                     Method method,
                     List<Pair<String, String>> getArgs,
                     List<Pair<String, String>> postArgs,
                     String postBody,
                     ContentType contentType) {
        mHost = host;
        mMethodName = methodName;
        mMethod = method;
        mGetArgs = getArgs;
        mPostArgs = postArgs;
        mPostBody = postBody;
        mContentType = contentType;
    }

    public Method getMethod() {
        return mMethod;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public String getHost() {
        return mHost;
    }

    public List<Pair<String, String>> getGetArgs() {
        return mGetArgs;
    }

    public List<Pair<String, String>> getPostArgs() {
        return mPostArgs;
    }

    public String getPostBody() {
        return mPostBody;
    }

    public ContentType getContentType() {
        return mContentType;
    }

    public static class Builder {
        private String mHost;
        private String mMethodName;
        private Method mMethod = Method.GET;
        private List<Pair<String, String>> mGetArgs = new LinkedList<>();
        private List<Pair<String, String>> mPostArgs = new LinkedList<>();
        private String mPostBody;
        private ContentType mContentType = ContentType.FORM_URL_ENCODED;

        public Builder withMethod(Method method) {
            mMethod = method;
            return this;
        }

        public Builder withHost(String host) {
            mHost = host;
            return this;
        }

        public Builder withMethodName(String methodName) {
            mMethodName = methodName;
            return this;
        }

        public Builder withGetParam(String name, String value) {
            if (!TextUtils.isEmpty(value)) {
                mGetArgs.add(new Pair<>(name, value));
            }
            return this;
        }

        public Builder withPostParam(String name, String value) {
            if (!TextUtils.isEmpty(value)) {
                mMethod = Method.POST;
                mPostArgs.add(new Pair<>(name, value));
            }
            return this;
        }

        public Builder withContentType(ContentType type) {
            mContentType = type;
            return this;
        }

        public Builder withPostBody(String value) {
            mMethod = Method.POST;
            mPostBody = value;
            return this;
        }

        public ApiQuery build() {
            return new ApiQuery(mHost, mMethodName, mMethod, mGetArgs, mPostArgs, mPostBody, mContentType);
        }
    }

    public enum Method {
        POST, GET
    }

    public enum ContentType {
        FORM_URL_ENCODED("application/x-www-form-urlencoded"),
        JSON("application/json");

        private String mRepr;

        ContentType(String repr) {
            mRepr = repr;
        }

        public String getRepr() {
            return mRepr;
        }
    }
}
