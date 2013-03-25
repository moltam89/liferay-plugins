package com.liferay.socialnetworking.util;

import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.User;

import java.util.HashMap;
import java.util.Map;
public class SocialNetworkingUtil {

	public static String getURLWithUserData(String urlPattern, User user) {
		Map<String, String> variables = new HashMap<String, String>();

		variables.put(
			"liferay:user:screenName",
			HtmlUtil.escapeURL(user.getScreenName()));

		variables.put(
			"liferay:user:companyId", String.valueOf(user.getCompanyId()));

		variables.put("liferay:user:id", String.valueOf(user.getUserId()));

		return StringUtil.replace(
			urlPattern, StringPool.DOLLAR_AND_OPEN_CURLY_BRACE,
			StringPool.CLOSE_CURLY_BRACE, variables);
	}

}