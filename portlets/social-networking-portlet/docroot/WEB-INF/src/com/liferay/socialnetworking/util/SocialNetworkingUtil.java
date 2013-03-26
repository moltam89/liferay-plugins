/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.socialnetworking.util;

import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Borkuti
 */
public class SocialNetworkingUtil {

	public static String replaceVariables(String urlPattern, User user) {
		Map<String, String> variables = new HashMap<String, String>();

		variables.put(
			"liferay:userCompanyId", String.valueOf(user.getCompanyId()));
		variables.put("liferay:userId", String.valueOf(user.getUserId()));
		variables.put(
			"liferay:userScreenName", HtmlUtil.escapeURL(user.getScreenName()));

		return StringUtil.replace(
			urlPattern, StringPool.DOLLAR_AND_OPEN_CURLY_BRACE,
			StringPool.CLOSE_CURLY_BRACE, variables);
	}

}