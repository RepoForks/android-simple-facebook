package com.sromku.simple.fb;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.sromku.simple.fb.actions.GetAlbumsAction;
import com.sromku.simple.fb.actions.GetAppRequestsAction;
import com.sromku.simple.fb.actions.GetFriendsAction;
import com.sromku.simple.fb.actions.GetProfileAction;
import com.sromku.simple.fb.actions.GetScoresAction;
import com.sromku.simple.fb.actions.PublishFeedAction;
import com.sromku.simple.fb.actions.PublishScoreAction;
import com.sromku.simple.fb.entities.Album;
import com.sromku.simple.fb.entities.Feed;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.entities.Score;
import com.sromku.simple.fb.entities.Story;
import com.sromku.simple.fb.entities.Video;
import com.sromku.simple.fb.listeners.OnAlbumsRequestListener;
import com.sromku.simple.fb.listeners.OnAppRequestsListener;
import com.sromku.simple.fb.listeners.OnDeleteRequestListener;
import com.sromku.simple.fb.listeners.OnFriendsRequestListener;
import com.sromku.simple.fb.listeners.OnInviteListener;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.sromku.simple.fb.listeners.OnPermissionListener;
import com.sromku.simple.fb.listeners.OnPostScoreListener;
import com.sromku.simple.fb.listeners.OnProfileRequestListener;
import com.sromku.simple.fb.listeners.OnPublishListener;
import com.sromku.simple.fb.listeners.OnReopenSessionListener;
import com.sromku.simple.fb.listeners.OnScoresRequestListener;
import com.sromku.simple.fb.utils.Errors;
import com.sromku.simple.fb.utils.Errors.ErrorMsg;
import com.sromku.simple.fb.utils.Logger;

/**
 * Simple Facebook SDK which wraps original Facebook SDK 3.5
 * 
 * <br>
 * <br>
 * <b>Features:</b>
 * <ul>
 * <li>Simple configuration</li>
 * <li>No need to use LoginButton view</li>
 * <li>Login/logout</li>
 * <li>Publish feed</li>
 * <li>Publish open graph story</li>
 * <li>Publish photo</li>
 * <li>Invite friends</li>
 * <li>Fetch my profile</li>
 * <li>Fetch friends</li>
 * <li>Fetch albums</li>
 * <li>Predefined all possible permissions. See {@link Permissions}</li>
 * <li>No need to care for correct sequence logging with READ and PUBLISH
 * permissions</li>
 * </ul>
 * 
 * @author sromku
 */
public class SimpleFacebook {
    private static SimpleFacebook mInstance = null;
    private static SimpleFacebookConfiguration mConfiguration = new SimpleFacebookConfiguration.Builder().build();

    private static Activity mActivity;
    private static SessionManager mSessionManager = null;

    private WebDialog mDialog = null;

    private SimpleFacebook() {
    }

    /**
     * Initialize the library and pass an {@link Activity}. This kind of
     * initialization is good in case you have a one base activity and many
     * fragments. In this case you just initialize this library and then just
     * get an instance of this library by {@link SimpleFacebook#getInstance()}
     * in any other place.
     * 
     * @param activity
     *            Activity
     */
    public static void initialize(Activity activity) {
	if (mInstance == null) {
	    mInstance = new SimpleFacebook();
	    mSessionManager = new SessionManager(mActivity, mConfiguration);
	}
	mActivity = activity;
	SessionManager.activity = activity;
    }

    /**
     * Get the instance of {@link SimpleFacebook}. This method, not only returns
     * a singleton instance of {@link SimpleFacebook} but also updates the
     * current activity with the passed activity. <br>
     * If you have more than one <code>Activity</code> in your application. And
     * more than one activity do something with facebook. Then, call this method
     * in {@link Activity#onResume()} method
     * 
     * <pre>
     * &#064;Override
     * protected void onResume() {
     *     super.onResume();
     *     mSimpleFacebook = SimpleFacebook.getInstance(this);
     * }
     * </pre>
     * 
     * @param activity
     * @return {@link SimpleFacebook} instance
     */
    public static SimpleFacebook getInstance(Activity activity) {
	if (mInstance == null) {
	    mInstance = new SimpleFacebook();
	    mSessionManager = new SessionManager(activity, mConfiguration);
	}
	mActivity = activity;
	SessionManager.activity = activity;
	return mInstance;
    }

    /**
     * Get the instance of {@link SimpleFacebook}. <br>
     * <br>
     * <b>Important:</b> Use this method only after you initialized this library
     * or by: {@link #initialize(Activity)} or by {@link #getInstance(Activity)}
     * 
     * @return The {@link SimpleFacebook} instance
     */
    public static SimpleFacebook getInstance() {
	return mInstance;
    }

    /**
     * Set facebook configuration. <b>Make sure</b> to set a configuration
     * before first actual use of this library like (login, getProfile, etc..).
     * 
     * @param configuration
     *            The configuration of this library
     */
    public static void setConfiguration(SimpleFacebookConfiguration configuration) {
	mConfiguration = configuration;
	SessionManager.configuration = configuration;
    }
    
    /**
     * Get configuration
     * @return
     */
    public static SimpleFacebookConfiguration getConfiguration() {
	return mConfiguration;
    }

    /**
     * Login to Facebook
     * 
     * @param onLoginListener
     */
    public void login(OnLoginListener onLoginListener) {
	mSessionManager.login(onLoginListener);
    }

    /**
     * Logout from Facebook
     */
    public void logout(OnLogoutListener onLogoutListener) {
	mSessionManager.logout(onLogoutListener);
    }

    /**
     * Get my profile from facebook.<br>
     * This method will return profile with next default properties depends on
     * permissions you have:<br>
     * <em>id, name, first_name, middle_name, last_name, gender, locale, languages, link, username, timezone, updated_time, verified, bio, birthday, education, email, 
     * hometown, location, political, favorite_athletes, favorite_teams, quotes, relationship_status, religion, website, work</em>
     * 
     * <br>
     * <br>
     * If you need additional or other profile properties like:
     * <em>age_range, picture and more</em>, then use this method:
     * {@link #getProfile(Properties, OnProfileRequestListener)} <br>
     * <br>
     * <b>Note:</b> If you need only few properties for your app, then it is
     * recommended <b>not</b> to use this method, since getting unnecessary
     * properties is time consuming task from facebook side.<br>
     * It is recommended in this case, to use
     * {@link #getProfile(Properties, OnProfileRequestListener)} and mention
     * only needed properties.
     * 
     * @param onProfileRequestListener
     */
    public void getProfile(OnProfileRequestListener onProfileRequestListener) {
	getProfile(null, onProfileRequestListener);
    }

    /**
     * Get my profile from facebook by mentioning specific parameters. <br>
     * For example, if you need: <em>square picture 500x500 pixels</em>
     * 
     * @param onProfileRequestListener
     * @param properties
     *            The {@link Properties}. <br>
     *            To create {@link Properties} instance use:
     * 
     *            <pre>
     * // define the profile picture we want to get
     * PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
     * pictureAttributes.setType(PictureType.SQUARE);
     * pictureAttributes.setHeight(500);
     * pictureAttributes.setWidth(500);
     * 
     * // create properties
     * Properties properties = new Properties.Builder().add(Properties.ID).add(Properties.FIRST_NAME).add(Properties.PICTURE, attributes).build();
     * </pre>
     */
    public void getProfile(Properties properties, OnProfileRequestListener onProfileRequestListener) {
	GetProfileAction getProfileAction = new GetProfileAction(mSessionManager);
	getProfileAction.setProperties(properties);
	getProfileAction.setOnProfileRequestListener(onProfileRequestListener);
	getProfileAction.execute();
    }

    /**
     * Get my friends from facebook.<br>
     * This method will return profile with next default properties depends on
     * permissions you have:<br>
     * <em>id, name</em>
     * 
     * <br>
     * <br>
     * If you need additional or other friend properties like:
     * <em>education, location and more</em>, then use this method:
     * {@link #getFriends(Properties, OnFriendsRequestListener)} <br>
     * <br>
     * 
     * @param onFriendsRequestListener
     */
    public void getFriends(OnFriendsRequestListener onFriendsRequestListener) {
	getFriends(null, onFriendsRequestListener);
    }

    /**
     * Get my friends from facebook by mentioning specific parameters. <br>
     * For example, if you need: <em>id, last_name, picture, birthday</em>
     * 
     * @param onFriendsRequestListener
     * @param properties
     *            The {@link Properties}. <br>
     *            To create {@link Properties} instance use:
     * 
     *            <pre>
     * // define the friend picture we want to get
     * PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
     * pictureAttributes.setType(PictureType.SQUARE);
     * pictureAttributes.setHeight(500);
     * pictureAttributes.setWidth(500);
     * 
     * // create properties
     * Properties properties = new Properties.Builder().add(Properties.ID).add(Properties.LAST_NAME).add(Properties.PICTURE, attributes).add(Properties.BIRTHDAY).build();
     * </pre>
     */
    public void getFriends(Properties properties, OnFriendsRequestListener onFriendsRequestListener) {
	GetFriendsAction getFriendsAction = new GetFriendsAction(mSessionManager);
	getFriendsAction.setProperties(properties);
	getFriendsAction.setOnFriendsRequestListener(onFriendsRequestListener);
	getFriendsAction.execute();
    }

    /**
     * Get albums
     * 
     * <b>Permission:</b><br>
     * {@link Permissions#USER_PHOTOS}
     */
    public void getAlbums(OnAlbumsRequestListener onAlbumsRequestListener) {
	GetAlbumsAction getAlbumsAction = new GetAlbumsAction(mSessionManager);
	getAlbumsAction.setOnAlbumsRequestListener(onAlbumsRequestListener);
	getAlbumsAction.execute();
    }

    /**
     * Get app requests
     * 
     * @param onAppRequestsListener
     */
    public void getAppRequests(OnAppRequestsListener onAppRequestsListener) {
	GetAppRequestsAction getAppRequestsAction = new GetAppRequestsAction(mSessionManager);
	getAppRequestsAction.setOnAppRequestsListener(onAppRequestsListener);
	getAppRequestsAction.execute();
    }

    /**
     * 
     * Gets scores using Scores API for games. <br>
     * <br>
     * 
     * 
     * @param onScoresRequestListener
     *            The listener for getting scores
     * @see https://developers.facebook.com/docs/games/scores/
     */
    public void getScores(OnScoresRequestListener onScoresRequestListener) {
	GetScoresAction getScoresAction = new GetScoresAction(mSessionManager);
	getScoresAction.setOnScoresRequestListener(onScoresRequestListener);
	getScoresAction.execute();
    }

    /**
     * 
     * Posts a score using Scores API for games. If missing publish_actions
     * permission, we do not ask again for it.<br>
     * <br>
     * 
     * <b>Permission:</b><br>
     * {@link Permissions#PUBLISH_ACTION}
     * 
     * 
     * @param score
     *            Score to be posted. <code>int</code>
     * @param onPostScoreListener
     *            The listener for posting score
     * @see https://developers.facebook.com/docs/games/scores/
     */
    public void publish(Score score, OnPostScoreListener onPostScoreListener) {
	PublishScoreAction publishScoreAction = new PublishScoreAction(mSessionManager);
	publishScoreAction.setScore(score);
	publishScoreAction.setOnPostScoreListener(onPostScoreListener);
	publishScoreAction.execute();
    }

    /**
     * 
     * Publish {@link Feed} on the wall.<br>
     * <br>
     * 
     * <b>Permission:</b><br>
     * {@link Permissions#PUBLISH_ACTION}
     * 
     * @param feed
     *            The feed to publish. Use {@link Feed.Builder} to create a new
     *            <code>Feed</code>
     * @param onPublishListener
     *            The listener for publishing action
     * @see https
     *      ://developers.facebook.com/docs/howtos/androidsdk/3.0/publish-to
     *      -feed/
     */
    public void publish(final Feed feed, final OnPublishListener onPublishListener) {
	PublishFeedAction publishFeedAction = new PublishFeedAction(mSessionManager);
	publishFeedAction.setFeed(feed);
	publishFeedAction.setOnPublishListener(onPublishListener);
	publishFeedAction.execute();
    }

    /**
     * Share to feed by using dialog or do it silently without dialog. <br>
     * If you use dialog for sharing, you don't have to configure
     * {@link Permissions#PUBLISH_ACTION} since user does the share by himself.
     * 
     * @param feed
     *            The feed to post
     * @param withDialog
     *            Set <code>True</code> if you want to use dialog.
     * @param onPublishListener
     */
    public void publish(final Feed feed, boolean withDialog, final OnPublishListener onPublishListener) {
	if (!withDialog) {
	    // make it silently
	    publish(feed, onPublishListener);
	} else {
	    if (mSessionManager.isLogin()) {
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(mActivity, Session.getActiveSession(), feed.getBundle())).setOnCompleteListener(new OnCompleteListener() {
		    @Override
		    public void onComplete(Bundle values, FacebookException error) {
			if (error == null) {
			    // When the story is posted, echo the
			    // success
			    // and the post Id.
			    final String postId = values.getString("post_id");
			    if (postId != null) {
				onPublishListener.onComplete(postId);
			    } else {
				onPublishListener.onFail("Canceled by user");
			    }
			} else if (error instanceof FacebookOperationCanceledException) {
			    // User clicked the "x" button
			    onPublishListener.onFail("Canceled by user");
			} else {
			    onPublishListener.onException(error);
			}
		    }

		}).build();

		// show the dialog
		feedDialog.show();
	    } else {
		// callback with 'fail' due to not being loged
		if (onPublishListener != null) {
		    String reason = Errors.getError(ErrorMsg.LOGIN);
		    logError(reason, null);

		    onPublishListener.onFail(reason);
		}
	    }
	}
    }

    /**
     * Publish open graph story.<br>
     * <br>
     * 
     * <b>Permission:</b><br>
     * {@link Permissions#PUBLISH_ACTION}
     * 
     * @param openGraph
     * @param onPublishListener
     */
    public void publish(final Story story, final OnPublishListener onPublishListener) {
	if (mSessionManager.isLogin()) {

	    // if we defined the publish permission
	    if (mConfiguration.getPublishPermissions().contains(Permissions.PUBLISH_ACTION.getValue())) {
		// callback with 'thinking'
		if (onPublishListener != null) {
		    onPublishListener.onThinking();
		}

		/*
		 * Check if session to facebook has 'publish_action' permission.
		 * If not, we will ask user for this permission.
		 */
		if (!mSessionManager.getOpenSessionPermissions().contains(Permissions.PUBLISH_ACTION.getValue())) {
		    mSessionManager.getSessionStatusCallback().mOnReopenSessionListener = new OnReopenSessionListener() {
			@Override
			public void onSuccess() {
			    publishImpl(story, onPublishListener);
			}

			@Override
			public void onNotAcceptingPermissions() {
			    // this fail can happen when user doesn't accept the
			    // publish permissions
			    String reason = Errors.getError(ErrorMsg.CANCEL_PERMISSIONS_PUBLISH, String.valueOf(mConfiguration.getPublishPermissions()));
			    logError(reason, null);

			    if (onPublishListener != null) {
				onPublishListener.onFail(reason);
			    }
			}
		    };

		    // extend publish permissions automatically
		    mSessionManager.extendPublishPermissions();
		} else {
		    publishImpl(story, onPublishListener);
		}
	    } else {
		// callback with 'fail' due to insufficient permissions
		if (onPublishListener != null) {
		    String reason = Errors.getError(ErrorMsg.PERMISSIONS_PUBLISH, Permissions.PUBLISH_ACTION.getValue());
		    logError(reason, null);

		    onPublishListener.onFail(reason);
		}
	    }
	} else {
	    // callback with 'fail' due to not being loged
	    if (onPublishListener != null) {
		String reason = Errors.getError(ErrorMsg.LOGIN);
		logError(reason, null);

		onPublishListener.onFail(reason);
	    }
	}
    }

    /**
     * Publish photo to specific album. You can use
     * {@link #getAlbums(OnAlbumsRequestListener)} to retrieve all user's
     * albums.<br>
     * <br>
     * 
     * <b>Permission:</b><br>
     * {@link Permissions#PUBLISH_STREAM}<br>
     * <br>
     * 
     * <b>Important:</b><br>
     * - The user must own the album<br>
     * - The album should not be full (Max: 200 photos). Check it by
     * {@link Album#getCount()}<br>
     * - The app can add photos to the album<br>
     * - The privacy setting of the app should be at minimum as the privacy
     * setting of the album ( {@link Album#getPrivacy()}
     * 
     * @param photo
     *            The photo to upload
     * @param albumId
     *            The album to which the photo should be uploaded
     * @param onPublishListener
     *            The callback listener
     */
    public void publish(final Photo photo, final String albumId, final OnPublishListener onPublishListener) {
	if (mSessionManager.isLogin()) {
	    // if we defined the publish permission
	    if (mConfiguration.getPublishPermissions().contains(Permissions.PUBLISH_STREAM.getValue())) {
		// callback with 'thinking'
		if (onPublishListener != null) {
		    onPublishListener.onThinking();
		}

		/*
		 * Check if session to facebook has 'publish_action' permission.
		 * If not, we will ask user for this permission.
		 */
		if (!mSessionManager.getOpenSessionPermissions().contains(Permissions.PUBLISH_STREAM.getValue())) {
		    mSessionManager.getSessionStatusCallback().mOnReopenSessionListener = new OnReopenSessionListener() {
			@Override
			public void onSuccess() {
			    publishImpl(photo, albumId, onPublishListener);
			}

			@Override
			public void onNotAcceptingPermissions() {
			    // this fail can happen when user doesn't accept the
			    // publish permissions
			    String reason = Errors.getError(ErrorMsg.CANCEL_PERMISSIONS_PUBLISH, String.valueOf(mConfiguration.getPublishPermissions()));
			    logError(reason, null);

			    if (onPublishListener != null) {
				onPublishListener.onFail(reason);
			    }
			}
		    };

		    // extend publish permissions automatically
		    mSessionManager.extendPublishPermissions();
		} else {
		    publishImpl(photo, albumId, onPublishListener);
		}
	    } else {
		// callback with 'fail' due to insufficient permissions
		if (onPublishListener != null) {
		    String reason = Errors.getError(ErrorMsg.PERMISSIONS_PUBLISH, Permissions.PUBLISH_STREAM.getValue());
		    logError(reason, null);

		    onPublishListener.onFail(reason);
		}
	    }
	} else {
	    // callback with 'fail' due to not being loged
	    if (onPublishListener != null) {
		String reason = Errors.getError(ErrorMsg.LOGIN);
		logError(reason, null);

		onPublishListener.onFail(reason);
	    }
	}
    }

    /**
     * Publish photo to application default album.<br>
     * <br>
     * 
     * <b>Permission:</b><br>
     * {@link Permissions#PUBLISH_STREAM}<br>
     * <br>
     * 
     * <b>Important:</b><br>
     * - The album should not be full (Max: 200 photos). Check it by
     * {@link Album#getCount()}<br>
     * {@link Album#getPrivacy()}
     * 
     * @param photo
     *            The photo to upload
     * @param onPublishListener
     *            The callback listener
     */
    public void publish(final Photo photo, final OnPublishListener onPublishListener) {
	publish(photo, "me", onPublishListener);
    }

    /**
     * Publish video to "Videos" album. <br>
     * 
     * <b>Permission:</b><br>
     * {@link Permissions#PUBLISH_STREAM}<br>
     * <br>
     * 
     * @param video
     *            The video to upload
     * @param onPublishListener
     *            The callback listener
     */
    public void publish(final Video video, final OnPublishListener onPublishListener) {
	if (mSessionManager.isLogin()) {
	    // if we defined the publish permission
	    if (mConfiguration.getPublishPermissions().contains(Permissions.PUBLISH_STREAM.getValue())) {
		// callback with 'thinking'
		if (onPublishListener != null) {
		    onPublishListener.onThinking();
		}

		/*
		 * Check if session to facebook has 'publish_action' permission.
		 * If not, we will ask user for this permission.
		 */
		if (!mSessionManager.getOpenSessionPermissions().contains(Permissions.PUBLISH_STREAM.getValue())) {
		    mSessionManager.getSessionStatusCallback().mOnReopenSessionListener = new OnReopenSessionListener() {
			@Override
			public void onSuccess() {
			    publishImpl(video, onPublishListener);
			}

			@Override
			public void onNotAcceptingPermissions() {
			    // this fail can happen when user doesn't accept the
			    // publish permissions
			    String reason = Errors.getError(ErrorMsg.CANCEL_PERMISSIONS_PUBLISH, String.valueOf(mConfiguration.getPublishPermissions()));
			    logError(reason, null);

			    if (onPublishListener != null) {
				onPublishListener.onFail(reason);
			    }
			}
		    };

		    // extend publish permissions automatically
		    mSessionManager.extendPublishPermissions();
		} else {
		    publishImpl(video, onPublishListener);
		}
	    } else {
		// callback with 'fail' due to insufficient permissions
		if (onPublishListener != null) {
		    String reason = Errors.getError(ErrorMsg.PERMISSIONS_PUBLISH, Permissions.PUBLISH_STREAM.getValue());
		    logError(reason, null);

		    onPublishListener.onFail(reason);
		}
	    }
	} else {
	    // callback with 'fail' due to not being loged
	    if (onPublishListener != null) {
		String reason = Errors.getError(ErrorMsg.LOGIN);
		logError(reason, null);

		onPublishListener.onFail(reason);
	    }
	}
    }

    /**
     * Open invite dialog and can add multiple friends
     * 
     * @param message
     *            The message inside the dialog. It could be <code>null</code>
     * @param onInviteListener
     *            The listener. It could be <code>null</code>
     */
    public void invite(String message, final OnInviteListener onInviteListener) {
	if (mSessionManager.isLogin()) {

	    Bundle params = new Bundle();
	    params.putString("message", message);
	    openInviteDialog(mActivity, params, onInviteListener);
	} else {
	    String reason = Errors.getError(ErrorMsg.LOGIN);
	    logError(reason, null);

	    onInviteListener.onFail(reason);
	}
    }

    /**
     * Open invite dialog and invite only specific friend
     * 
     * @param to
     *            The id of the friend profile
     * @param message
     *            The message inside the dialog. It could be <code>null</code>
     * @param onInviteListener
     *            The listener. It could be <code>null</code>
     */
    public void invite(String to, String message, final OnInviteListener onInviteListener) {
	if (mSessionManager.isLogin()) {

	    Bundle params = new Bundle();
	    if (message != null) {
		params.putString("message", message);
	    }
	    params.putString("to", to);
	    openInviteDialog(mActivity, params, onInviteListener);
	} else {
	    String reason = Errors.getError(ErrorMsg.LOGIN);
	    logError(reason, null);

	    onInviteListener.onFail(reason);
	}
    }

    /**
     * Open invite dialog and invite several specific friends
     * 
     * @param suggestedFriends
     *            The ids of friends' profiles
     * @param message
     *            The message inside the dialog. It could be <code>null</code>
     * @param onInviteListener
     *            The error listener. It could be <code>null</code>
     */
    public void invite(String[] suggestedFriends, String message, final OnInviteListener onInviteListener) {
	if (mSessionManager.isLogin()) {

	    Bundle params = new Bundle();
	    if (message != null) {
		params.putString("message", message);
	    }
	    params.putString("suggestions", TextUtils.join(",", suggestedFriends));
	    openInviteDialog(mActivity, params, onInviteListener);
	} else {
	    String reason = Errors.getError(ErrorMsg.LOGIN);
	    logError(reason, null);

	    onInviteListener.onFail(reason);
	}
    }

    /**
     * 
     * Deletes an apprequest.<br>
     * <br>
     * 
     * @param inRequestId
     *            Input request id to be deleted. Note that it should have the
     *            form {USERID}_{REQUESTID} <code>String</code>
     * @param onDeleteRequestListener
     *            The listener for deletion action
     * @see https
     *      ://developers.facebook.com/docs/android/app-link-requests/#step3
     */
    public void deleteRequest(String inRequestId, final OnDeleteRequestListener onDeleteRequestListener) {
	if (mSessionManager.isLogin()) {
	    // Create a new request for an HTTP delete with the
	    // request ID as the Graph path.
	    Session session = mSessionManager.getOpenSession();
	    Request request = new Request(session, inRequestId, null, HttpMethod.DELETE, new Request.Callback() {
		@Override
		public void onCompleted(Response response) {
		    FacebookRequestError error = response.getError();
		    if (error != null) {
			// log
			logError("failed to delete requests", error.getException());

			// callback with 'exception'
			if (onDeleteRequestListener != null) {
			    onDeleteRequestListener.onException(error.getException());
			}
		    } else {
			// callback with 'complete'
			if (onDeleteRequestListener != null) {
			    onDeleteRequestListener.onComplete();
			}
		    }
		}
	    });
	    // Execute the request asynchronously.
	    Request.executeBatchAsync(request);
	} else {
	    String reason = Errors.getError(ErrorMsg.LOGIN);
	    logError(reason, null);

	    // callback with 'fail' due to not being logged in
	    if (onDeleteRequestListener != null) {
		onDeleteRequestListener.onFail(reason);
	    }
	}
    }

    /**
     * 
     * Requests {@link Permissions#PUBLISH_ACTION} and nothing else. Useful when
     * you just want to request the action and won't be publishing at the time,
     * but still need the updated <b>access token</b> with the permissions
     * (possibly to pass back to your backend). You must add
     * {@link Permissions#PUBLISH_ACTION} to your SimpleFacebook configuration
     * before calling this.
     * 
     * <b>Must be logged to use.</b>
     * 
     * @param onPermissionListener
     *            The listener for the request permission action
     */
    public void requestPublish(final OnPermissionListener onPermissionListener) {
	if (mSessionManager.isLogin()) {
	    if (mConfiguration.getPublishPermissions().contains(Permissions.PUBLISH_ACTION.getValue())) {
		if (onPermissionListener != null) {
		    onPermissionListener.onThinking();
		}
		/*
		 * Check if session to facebook has 'publish_action' permission.
		 * If not, we will ask user for this permission.
		 */
		if (!mSessionManager.getOpenSessionPermissions().contains(Permissions.PUBLISH_ACTION.getValue())) {
		    mSessionManager.getSessionStatusCallback().mOnReopenSessionListener = new OnReopenSessionListener() {
			@Override
			public void onSuccess() {
			    if (onPermissionListener != null) {
				onPermissionListener.onSuccess(mSessionManager.getAccessToken());
			    }
			}

			@Override
			public void onNotAcceptingPermissions() {
			    // this fail can happen when user doesn't accept the
			    // publish permissions
			    String reason = Errors.getError(ErrorMsg.CANCEL_PERMISSIONS_PUBLISH, String.valueOf(mConfiguration.getPublishPermissions()));
			    logError(reason, null);
			    if (onPermissionListener != null) {
				onPermissionListener.onFail(reason);
			    }
			}
		    };
		    // extend publish permissions automatically
		    mSessionManager.extendPublishPermissions();
		} else {
		    // We already have the permission.
		    if (onPermissionListener != null) {
			onPermissionListener.onSuccess(mSessionManager.getAccessToken());
		    }
		}
	    }
	} else {
	    // callback with 'fail' due to not being loged
	    if (onPermissionListener != null) {
		String reason = Errors.getError(ErrorMsg.LOGIN);
		logError(reason, null);
		onPermissionListener.onFail(reason);
	    }
	}
    }

    /**
     * Call this inside your activity in {@link Activity#onActivityResult}
     * method
     * 
     * @param activity
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */
    public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
	if (Session.getActiveSession() != null) {
	    return Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
	} else {
	    return false;
	}
    }

    /**
     * Clean all references like Activity to prevent memory leaks
     */
    public void clean() {
	mActivity = null;
    }

    private static void publishImpl(Story story, final OnPublishListener onPublishListener) {
	Session session = mSessionManager.getOpenSession();
	String appNamespace = mConfiguration.getNamespace();

	Request request = new Request(session, story.getGraphPath(appNamespace), story.getActionBundle(), HttpMethod.POST, new Request.Callback() {
	    @Override
	    public void onCompleted(Response response) {
		GraphObject graphObject = response.getGraphObject();
		if (graphObject != null) {
		    JSONObject graphResponse = graphObject.getInnerJSONObject();
		    String postId = null;
		    try {
			postId = graphResponse.getString("id");
		    } catch (JSONException e) {
			// log
			logError("JSON error", e);
		    }

		    FacebookRequestError error = response.getError();
		    if (error != null) {
			// log
			logError("Failed to publish", error.getException());

			// callback with 'exception'
			if (onPublishListener != null) {
			    onPublishListener.onException(error.getException());
			}
		    } else {
			// callback with 'complete'
			if (onPublishListener != null) {
			    onPublishListener.onComplete(postId);
			}
		    }
		} else {
		    // log
		    logError("The GraphObject in Response of publish action has null value. Response=" + response.toString(), null);

		    if (onPublishListener != null) {
			onPublishListener.onComplete("0");
		    }
		}
	    }
	});

	RequestAsyncTask task = new RequestAsyncTask(request);
	task.execute();
    }

    private static void publishImpl(Photo photo, String albumId, final OnPublishListener onPublishListener) {
	Session session = mSessionManager.getOpenSession();
	Request request = new Request(session, albumId + "/photos", photo.getBundle(), HttpMethod.POST, new Request.Callback() {
	    @Override
	    public void onCompleted(Response response) {
		GraphObject graphObject = response.getGraphObject();
		if (graphObject != null) {
		    JSONObject graphResponse = graphObject.getInnerJSONObject();
		    String postId = null;
		    try {
			postId = graphResponse.getString("id");
		    } catch (JSONException e) {
			// log
			logError("JSON error", e);
		    }

		    FacebookRequestError error = response.getError();
		    if (error != null) {
			// log
			logError("Failed to publish", error.getException());

			// callback with 'exception'
			if (onPublishListener != null) {
			    onPublishListener.onException(error.getException());
			}
		    } else {
			// callback with 'complete'
			if (onPublishListener != null) {
			    onPublishListener.onComplete(postId);
			}
		    }
		} else {
		    // log
		    logError("The GraphObject in Response of publish action has null value. Response=" + response.toString(), null);

		    if (onPublishListener != null) {
			onPublishListener.onComplete("0");
		    }
		}
	    }
	});

	RequestAsyncTask task = new RequestAsyncTask(request);
	task.execute();
    }

    private static void publishImpl(Video video, final OnPublishListener onPublishListener) {
	Session session = mSessionManager.getOpenSession();
	Request request = new Request(session, "me/videos", video.getBundle(), HttpMethod.POST, new Request.Callback() {
	    @Override
	    public void onCompleted(Response response) {
		GraphObject graphObject = response.getGraphObject();
		if (graphObject != null) {
		    JSONObject graphResponse = graphObject.getInnerJSONObject();
		    String postId = null;
		    try {
			postId = graphResponse.getString("id");
		    } catch (JSONException e) {
			// log
			logError("JSON error", e);
		    }

		    FacebookRequestError error = response.getError();
		    if (error != null) {
			// log
			logError("Failed to publish", error.getException());

			// callback with 'exception'
			if (onPublishListener != null) {
			    onPublishListener.onException(error.getException());
			}
		    } else {
			// callback with 'complete'
			if (onPublishListener != null) {
			    onPublishListener.onComplete(postId);
			}
		    }
		} else {
		    // log
		    logError("The GraphObject in Response of publish action has null value. Response=" + response.toString(), null);

		    if (onPublishListener != null) {
			onPublishListener.onComplete("0");
		    }
		}
	    }
	});

	RequestAsyncTask task = new RequestAsyncTask(request);
	task.execute();
    }

    private void openInviteDialog(Activity activity, Bundle params, final OnInviteListener onInviteListener) {
	mDialog = new WebDialog.RequestsDialogBuilder(activity, Session.getActiveSession(), params).setOnCompleteListener(new WebDialog.OnCompleteListener() {
	    @Override
	    public void onComplete(Bundle values, FacebookException error) {
		if (error != null) {
		    // log
		    logError("Failed to invite", error);

		    if (error instanceof FacebookOperationCanceledException) {
			onInviteListener.onCancel();
		    } else {
			if (onInviteListener != null) {
			    onInviteListener.onException(error);
			}
		    }
		} else {
		    Object object = values.get("request");
		    if (object == null) {
			onInviteListener.onCancel();
		    } else {
			List<String> invitedFriends = fetchInvitedFriends(values);
			onInviteListener.onComplete(invitedFriends, object.toString());
		    }
		}
		mDialog = null;
	    }

	}).build();

	Window dialogWindow = mDialog.getWindow();
	dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	mDialog.show();
    }

    /**
     * Fetch invited friends from response bundle
     * 
     * @param values
     * @return list of invited friends
     */
    @SuppressLint("DefaultLocale")
    private static List<String> fetchInvitedFriends(Bundle values) {
	List<String> friends = new ArrayList<String>();

	int size = values.size();
	int numOfFriends = size - 1;
	if (numOfFriends > 0) {
	    for (int i = 0; i < numOfFriends; i++) {
		String key = String.format("to[%d]", i);
		String friendId = values.getString(key);
		if (friendId != null) {
		    friends.add(friendId);
		}
	    }
	}

	return friends;
    }

    private static void logError(String error, Throwable throwable) {
	if (throwable != null) {
	    Logger.logError(SimpleFacebook.class, error, throwable);
	} else {
	    Logger.logError(SimpleFacebook.class, error);
	}
    }

}
