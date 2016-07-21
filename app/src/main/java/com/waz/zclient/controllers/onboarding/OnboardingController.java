/**
 * Wire
 * Copyright (C) 2016 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.controllers.onboarding;


import android.content.Context;
import android.content.SharedPreferences;
import com.waz.api.IConversation;
import com.waz.zclient.R;
import com.waz.zclient.controllers.navigation.Page;
import com.waz.zclient.pages.main.onboarding.OnBoardingHintType;

import java.util.HashSet;
import java.util.Set;

public class OnboardingController implements IOnboardingController {

    public static final String USER_PREFS_TAG = "com.waz.zclient.user.onboarding.preferences";

    private static final String USER_PREFS_PEOPLE_PICKER_OPEN_COUNT = "USER_PREFS_PEOPLE_PICKER_OPEN_COUNT";
    private static final String USER_PREFS_SWIPE_FROM_CONVERSATION_TO_LIST_COUNT = "USER_PREFS_SWIPE_FROM_CONVERSATION_TO_LIST_COUNT";
    private static final String USER_PREFS_SHOW_PARTICIPANTS_COUNT = "USER_PREFS_SHOW_PARTICIPANTS_COUNT";
    private static final String USER_PREFS_SHOW_CONVERSATION_LIST_HINT = "USER_PREFS_SHOW_CONVERSATION_LIST_HINT";

    private final SharedPreferences userPreferences;
    private final Context context;

    private Set<OnboardingControllerObserver> onboardingControllerObservers;

    private OnBoardingHintType currentVisibleHintType;

    public OnboardingController(Context context) {
        this.userPreferences = context.getSharedPreferences(USER_PREFS_TAG, Context.MODE_PRIVATE);
        this.context = context;

        onboardingControllerObservers = new HashSet<>();
        currentVisibleHintType = OnBoardingHintType.NONE;
    }

    @Override
    public void addOnboardingControllerObserver(OnboardingControllerObserver onboardingControllerObserver) {
        onboardingControllerObservers.add(onboardingControllerObserver);
        if (currentVisibleHintType != null) {
            onboardingControllerObserver.onShowOnboardingHint(currentVisibleHintType, 0);
        }
    }

    @Override
    public void removeOnboardingControllerObserver(OnboardingControllerObserver onboardingControllerObserver) {
        onboardingControllerObservers.remove(onboardingControllerObserver);
    }

    @Override
    public void hideOnboardingHint(OnBoardingHintType requestedType) {
        if (currentVisibleHintType == OnBoardingHintType.NONE ||
            (requestedType != OnBoardingHintType.NONE &&
             requestedType != currentVisibleHintType)) {
            return;
        }

        for (OnboardingControllerObserver onboardingControllerObserver : onboardingControllerObservers) {
            onboardingControllerObserver.onHideOnboardingHint(currentVisibleHintType);
        }
        currentVisibleHintType = OnBoardingHintType.NONE;
    }

    @Override
    public OnBoardingHintType getCurrentVisibleHintType() {
        return currentVisibleHintType;
    }

    @Override
    public void setCurrentHintType(OnBoardingHintType hintType) {
        if (currentVisibleHintType == hintType) {
            return;
        }
        currentVisibleHintType = hintType;
        for (OnboardingControllerObserver onboardingControllerObserver : onboardingControllerObservers) {
            onboardingControllerObserver.onShowOnboardingHint(hintType, 0);
        }
    }

    private boolean hintViewed(OnBoardingHintType hintType) {
        return userPreferences.getBoolean(hintType.name, false);
    }

    @Override
    public void tearDown() {

    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  PEOPLE PICKER
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void incrementPeoplePickerShowCount() {
        if (hintViewed(OnBoardingHintType.SHARE_CONTACTS) &&
            discoverSearchHintCompleted()) {
            return;
        }

        userPreferences.edit()
                       .putInt(USER_PREFS_PEOPLE_PICKER_OPEN_COUNT, userPreferences.getInt(USER_PREFS_PEOPLE_PICKER_OPEN_COUNT, 0) + 1)
                       .apply();
    }

    private int getPeoplePickerShowCount() {
        return userPreferences.getInt(USER_PREFS_PEOPLE_PICKER_OPEN_COUNT, 0);
    }

    private boolean discoverSearchHintCompleted() {
        int showSearchHintCounter = context.getResources().getInteger(R.integer.onboarding__show_hint__discover_search__count);
        return getPeoplePickerShowCount() > showSearchHintCounter;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  SWIPE FROM CONVERSATION TO LIST
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void incrementSwipeToConversationListCount(Page currentRightPage) {
        if (pageIsDiscoverConversationListFromConversation(currentRightPage)) {
            incrementSwipeFromConversationToListCount();
        }
    }

    private boolean pageIsDiscoverConversationListFromConversation(Page currentPage) {
        return currentPage == Page.MESSAGE_STREAM || currentPage == Page.INBOX || currentPage == Page.PENDING_CONNECT_REQUEST_AS_CONVERSATION;
    }

    private void incrementSwipeFromConversationToListCount() {
        if (discoverConversationListFromConversationHintCompleted()) {
            return;
        }

        userPreferences.edit()
                       .putInt(USER_PREFS_SWIPE_FROM_CONVERSATION_TO_LIST_COUNT, userPreferences.getInt(USER_PREFS_SWIPE_FROM_CONVERSATION_TO_LIST_COUNT, 0) + 1)
                       .apply();
    }

    private int getSwipeFromConversationToListCount() {
        return userPreferences.getInt(USER_PREFS_SWIPE_FROM_CONVERSATION_TO_LIST_COUNT, 0);
    }

    private boolean discoverConversationListFromConversationHintCompleted() {
        int swipeToConversationListCount = context.getResources().getInteger(R.integer.onboarding__show_hint__discover_conversationlist_from_conversation__count);
        return getSwipeFromConversationToListCount() > swipeToConversationListCount;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  SHOW PARTICIPANTS
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void incrementParticipantsShowCount() {

        if (discoverParticipantsHintCompleted()) {
            return;
        }

        userPreferences.edit()
                       .putInt(USER_PREFS_SHOW_PARTICIPANTS_COUNT, userPreferences.getInt(USER_PREFS_SHOW_PARTICIPANTS_COUNT, 0) + 1)
                       .apply();
    }

    private int getShowParticipantsCount() {
        return userPreferences.getInt(USER_PREFS_SHOW_PARTICIPANTS_COUNT, 0);
    }

    private boolean discoverParticipantsHintCompleted() {
        int showParticipantsRequiredCount = context.getResources().getInteger(R.integer.onboarding__show_hint__discover_participants__count);
        return getShowParticipantsCount() > showParticipantsRequiredCount;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Invitation banner
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void hideConversationListHint() {
        userPreferences.edit()
                       .putBoolean(USER_PREFS_SHOW_CONVERSATION_LIST_HINT, false)
                       .apply();

        hideOnboardingHint(OnBoardingHintType.INVITATION_BANNER);
    }

    @Override
    public boolean shouldShowConversationListHint() {
        return userPreferences.getBoolean(USER_PREFS_SHOW_CONVERSATION_LIST_HINT, true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Onboarding hint overlays
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public OnBoardingHintType getCurrentOnBoardingHint(Page currentPage, IConversation currentConversation, boolean conversationHasDraft) {
        return OnBoardingHintType.NONE;
    }
}
