/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dr
 */
public abstract class ReExp {

    private int retryfreq = 0;

    /** 重试的睡眠时间 */
    private int sleepTime = 0;

    private boolean isException = true;

    private Class<Exception> exception = Exception.class;

    private final Map<Class, String> ClassExpResult = new ConcurrentHashMap<>();

    public ReExp setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    public ReExp setRetryFreq(int retryfreq) {
        this.retryfreq = retryfreq;
        return this;
    }

    public ReExp setException(Class<Exception> exception) {
        this.isException = true;
        this.exception = exception;
        return this;
    }

    public ReExp addException(Class exception,String result) {
        ClassExpResult.put(exception,result);
        return this;
    }

    /**
     * 重试
     * @return ?
     */
    protected abstract Object runs() throws Exception;

    /**
     * 默认
     * @return ?
     */
    protected abstract Object defruns();


    public Object execute() {
        for (int i = 0; i < retryfreq; i++) {
            try {
                return runs();
            } catch (Exception e) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
        }
        return defruns();
    }

    public Object countExecute(String name) {
        ResultData data = new ResultData(name,retryfreq);
        for (int i = 0; i < retryfreq; i++) {
            try {
                data.result = runs();
                return data;
            } catch (Exception e) {
                if (isException && exception.isInstance(e)) {
                    data.failures++;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException interruptedException) {
                    }
                } else {
                    for (Entry<Class, String> classStringEntry : ClassExpResult.entrySet()) {
                        Class classdata = (Class) ((Entry) classStringEntry).getKey();
                        if (classdata.isInstance(e)) {
                            data.result = ((Entry) classStringEntry).getValue();
                            return data;
                        }
                    }
                    return defruns();
                }
            }
        }
        return defruns();
    }

    public static class ResultData {
        public Object result = null;
        public final int cout;
        public int failures = 0;
        public ResultData(String name,int rq) {
            cout = rq;
        }
    }

}