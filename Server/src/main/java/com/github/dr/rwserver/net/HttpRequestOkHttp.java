/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net;

import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.log.Log;
import okhttp3.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;


/**
 * @author Dr
 */
public class HttpRequestOkHttp {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36";
	private static final OkHttpClient CLIENT = new OkHttpClient();


	public static String doGet(final String url) {
		if (IsUtil.isBlank(url)) {
			Log.error("[GET URL] NULL");
			return "";
		}
		Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent",USER_AGENT)
				.build();
		try (Response response = CLIENT.newCall(request).execute()) {
			return Objects.requireNonNull(response.body()).string();
		} catch (IOException e) {
			Log.error(e);
		}
		return "";
	}

	public static String doPost(String url, String param) {
		FormBody.Builder formBody = new FormBody.Builder();
		final String[] paramArray = param.split("&");
		for (String pam : paramArray) {
			final String[] keyValue = pam.split("=");
			formBody.add(keyValue[0],keyValue[1]);
		}
		return doPost(url,formBody);
	}

	public static String doPost(String url, FormBody.Builder data) {
		Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent",USER_AGENT)
				.post(data.build())
				.build();
		return getHttpResultString(request);
	}

	public static String doPostJson(String url, String param) {
		RequestBody body = RequestBody.create(param, MediaType.parse("application/json; charset=utf-8"));
		Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent",USER_AGENT)
				.post(body)
				.build();
		return getHttpResultString(request);
	}


	private static String getHttpResultString(Request request) {
		String result = "";
		try (Response response = CLIENT.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				Log.error("Unexpected code",new IOException());
			}
			result = Objects.requireNonNull(response.body()).string();
			Objects.requireNonNull(response.body()).close();
		} catch (IOException e) {
			Log.error(e);
		}
		return result;
	}

	public static String doPostRw(String url, String param) {
		FormBody.Builder formBody = new FormBody.Builder();
		final String[] paramArray = param.split("&");
		for (String pam : paramArray) {
			final String[] keyValue = pam.split("=");
			formBody.add(keyValue[0],keyValue[1]);
		}
		Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent","rw android 151 zh")
				.addHeader("Language","zh")
				.addHeader("Connection","close")
				.post(formBody.build())
				.build();
		return getHttpResultString(request);
	}

	public static boolean downUrl(final String url,final File file) {
		FileOutputStream output = null;
			Request request   = new Request.Builder()
					.url(url)
					.addHeader("User-Agent",USER_AGENT)
					.build();
		try (Response response = CLIENT.newCall(request).execute()) {
			if ( !response.isSuccessful() ) {
				throw new FileNotFoundException();
			}
			output  = new FileOutputStream(file);
			output.write(Objects.requireNonNull(response.body()).bytes());
			output.flush();
			return true;
		} catch (Exception e) {
			Log.error(e);
		} finally {
			if ( output != null ) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}