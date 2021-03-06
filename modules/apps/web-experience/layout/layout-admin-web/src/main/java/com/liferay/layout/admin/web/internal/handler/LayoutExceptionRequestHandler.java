/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.layout.admin.web.internal.handler;

import com.liferay.portal.kernel.exception.LayoutNameException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ResourceBundleLoader;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(immediate = true, service = LayoutExceptionRequestHandler.class)
public class LayoutExceptionRequestHandler {

	public void handlePortalException(
			ActionRequest actionRequest, ActionResponse actionResponse,
			PortalException pe)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		if (pe instanceof LayoutNameException) {
			ResourceBundle resourceBundle =
				_resourceBundleLoader.loadResourceBundle(
					themeDisplay.getLocale());

			jsonObject.put(
				"error",
				LanguageUtil.get(
					resourceBundle, "please-enter-a-valid-name-for-the-page"));
		}
		else {
			jsonObject.put(
				"error",
				LanguageUtil.get(
					themeDisplay.getLocale(), "an-unexpected-error-occurred"));
		}

		JSONPortletResponseUtil.writeJSON(
			actionRequest, actionResponse, jsonObject);
	}

	@Reference(
		target = "(bundle.symbolic.name=com.liferay.layout.admin.web)",
		unbind = "-"
	)
	private ResourceBundleLoader _resourceBundleLoader;

}