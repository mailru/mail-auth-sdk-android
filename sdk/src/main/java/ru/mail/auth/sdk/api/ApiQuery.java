package ru.mail.auth.sdk.api;

import android.text.TextUtils;
import android.util.Pair;

import java.util.LinkedList;
import java.util.List;

public class ApiQuery {
    private final String mHost;
    private final String mMethodName;
    private final List<Pair<String, String>> mGetArgs;
    private final List<Pair<String, String>> mPostArgs;

    private ApiQuery(String host,
                     String methodName,
                     List<Pair<String, String>> getArgs,
                     List<Pair<String, String>> postArgs) {
        mHost = host;
        mMethodName = methodName;
        mGetArgs = getArgs;
        mPostArgs = postArgs;
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

    public static class Builder {
        private String mHost;
        private String mMethodName;
        private List<Pair<String, String>> mGetArgs = new LinkedList<>();
        private List<Pair<String, String>> mPostArgs = new LinkedList<>();

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
                mPostArgs.add(new Pair<>(name, value));
            }
            return this;
        }


        public ApiQuery build() {
            return new ApiQuery(mHost, mMethodName, mGetArgs, mPostArgs);
        }
    }
}
