/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.orangelabs.rcs.widget;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.LauncherUtils;
import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.service.api.client.ClientApiIntents;
import com.orangelabs.rcs.utils.logger.Logger;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Widget provider
 * 
 * @author jexa7410
 */
public class RcsWidgetProvider extends AppWidgetProvider{

	/**
	 * Widget related intent actions
	 */
	public static final String WIDGET_ACTIVATE_SERVICE = "com.orangelabs.rcs.widget.ACTIVATE";
	public static final String WIDGET_DEACTIVATE_SERVICE = "com.orangelabs.rcs.widget.DEACTIVATE";

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		// Create RCS settings instance
		RcsSettings.createInstance(context);
		if (intent.getAction().equalsIgnoreCase(ClientApiIntents.SERVICE_STATUS)) {
			// Service status broadcasts received from the service
			switch (intent.getIntExtra("status", -1)) {
			case ClientApiIntents.SERVICE_STATUS_STARTING:
				setOnButNotRegisteredMode(context);
				break;
			case ClientApiIntents.SERVICE_STATUS_STARTED:
				setOnButNotRegisteredMode(context);
				break;
			case ClientApiIntents.SERVICE_STATUS_STOPPING:
				break;
			case ClientApiIntents.SERVICE_STATUS_STOPPED:
				setOffMode(context);
				break;
			case ClientApiIntents.SERVICE_STATUS_FAILED:
				setOffMode(context);
				break;
			default:
				if (logger.isActivated()) {
					logger.warn("Unknown service status");
				}
			}
		} else if (intent.getAction().equalsIgnoreCase(ClientApiIntents.SERVICE_REGISTRATION)) {
			if (intent.getBooleanExtra("status", false)){
				setOnAndRegisteredMode(context);
			}else {
				setOnButNotRegisteredMode(context);
			}
		}else if (intent.getAction().equalsIgnoreCase(WIDGET_ACTIVATE_SERVICE)) {
			// Widget was OFF and has been clicked
			if (logger.isActivated()) {
				logger.debug("Activate service asked");
			}
			if (ClientApi.isServiceStarted(context)) {
				// Service is already running, no need to start it, just set
				// widget to ON mode
				if (intent.getBooleanExtra("registered", false)){
					setOnAndRegisteredMode(context);
				}else {
					setOnButNotRegisteredMode(context);
				}
			} else {
				// Service not running, start it and set widget to pending mode
				setPendingMode(context);
				RcsSettings.getInstance().setServiceActivationState(true);
                LauncherUtils.launchRcsService(context, false);
			}
		} else if (intent.getAction().equalsIgnoreCase(WIDGET_DEACTIVATE_SERVICE)) {
			// Widget was ON and has been clicked
			if (logger.isActivated()) {
				logger.debug("Deactivate service asked");
			}
			if (ClientApi.isServiceStarted(context)) {
				// Service is running, stop it and set widget to pending mode
				setPendingMode(context);
                LauncherUtils.stopRcsService(context);
				RcsSettings.getInstance().setServiceActivationState(false);
			} else {
				// Service is already stopped, no need to stop it, just set
				// widget to OFF mode
				setOffMode(context);
			}
		}
	}

	/**
	 * Called the first time the widget is created
	 */
	@Override
	public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		// Widget initialization
		if (ClientApi.isServiceStarted(context)) {
			if (isImsConnected()) {
				setOnAndRegisteredMode(context);
			} else {
				setOnButNotRegisteredMode(context);
			}
		} else {
			setOffMode(context);
		}
	}
	
	/**
	 * Is IMS connected
	 * 
	 * @return IMS connection state
	 */
	private boolean isImsConnected(){
		return ((Core.getInstance() != null) &&
				(Core.getInstance().getImsModule().getCurrentNetworkInterface() != null) &&
				(Core.getInstance().getImsModule().getCurrentNetworkInterface().isRegistered()));
	}

	/**
	 * Set flower PENDING and bar PENDING
	 * 
	 * @param context Context
	 */
	private void setPendingMode(Context context) {
		if (logger.isActivated()) {
			logger.debug("Widget mode set to PENDING");
		}
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),	R.layout.rcs_widget);
		remoteViews.setViewVisibility(R.id.image_off, View.GONE);
		remoteViews.setViewVisibility(R.id.widget_flipper, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.image_on, View.GONE);
		remoteViews.setViewVisibility(R.id.background_off, View.GONE);
		remoteViews.setViewVisibility(R.id.background_on, View.GONE);
		remoteViews.setViewVisibility(R.id.background_flipper, View.VISIBLE);
		// Pending intent does nothing
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, getResultCode(), new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		ComponentName componentName = new ComponentName(context, RcsWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}

	/**
	 * Set flower OFF and bar OFF
	 * 
	 * @param context Context
	 */
	private void setOffMode(Context context) {
		if (logger.isActivated()) {
			logger.debug("Widget mode set to OFF");
		}
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.rcs_widget);
		remoteViews.setViewVisibility(R.id.image_off, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.widget_flipper, View.GONE);
		remoteViews.setViewVisibility(R.id.image_on, View.GONE);
		remoteViews.setViewVisibility(R.id.background_off, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.background_on, View.GONE);
		remoteViews.setViewVisibility(R.id.background_flipper, View.GONE);

		Intent newIntent = new Intent(WIDGET_ACTIVATE_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, getResultCode(), newIntent, PendingIntent.FLAG_ONE_SHOT);
		remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		ComponentName componentName = new ComponentName(context, RcsWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}

	/**
	 * Set flower OFF and bar ON
	 * 
	 * @param context Context
	 */
	private void setOnButNotRegisteredMode(Context context) {
		if (logger.isActivated()) {
			logger.debug("Widget mode set to ON but not registered");
		}
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),	R.layout.rcs_widget);
		remoteViews.setViewVisibility(R.id.image_off, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.widget_flipper, View.GONE);
		remoteViews.setViewVisibility(R.id.image_on, View.GONE);
		remoteViews.setViewVisibility(R.id.background_off, View.GONE);
		remoteViews.setViewVisibility(R.id.background_on, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.background_flipper, View.GONE);
		Intent newIntent = new Intent(WIDGET_DEACTIVATE_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, getResultCode(), newIntent, PendingIntent.FLAG_ONE_SHOT);
		remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		ComponentName componentName = new ComponentName(context, RcsWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}
	
	/**
	 * Set flower ON and bar ON
	 * 
	 * @param context Context
	 */
	private void setOnAndRegisteredMode(Context context) {
		if (logger.isActivated()) {
			logger.debug("Widget mode set to ON and registered");
		}
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),	R.layout.rcs_widget);
		remoteViews.setViewVisibility(R.id.image_off, View.GONE);
		remoteViews.setViewVisibility(R.id.widget_flipper, View.GONE);
		remoteViews.setViewVisibility(R.id.image_on, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.background_off, View.GONE);
		remoteViews.setViewVisibility(R.id.background_on, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.background_flipper, View.GONE);
		Intent newIntent = new Intent(WIDGET_DEACTIVATE_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, getResultCode(), newIntent, PendingIntent.FLAG_ONE_SHOT);
		remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		ComponentName componentName = new ComponentName(context, RcsWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}
}
