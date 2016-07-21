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
package com.waz.zclient.core.api.scala;

import android.text.TextUtils;
import com.waz.api.Contacts;
import com.waz.api.SearchQuery;
import com.waz.api.UpdateListener;
import com.waz.api.User;
import com.waz.api.ZMessagingApi;
import com.waz.zclient.core.stores.pickuser.PickUserStore;
import com.waz.zclient.core.stores.pickuser.PickUserStoreObserver;

import java.util.ArrayList;
import java.util.Arrays;

public class ScalaPickUserStore extends PickUserStore {
    public static final String TAG = ScalaPickUserStore.class.getName();

    private ZMessagingApi zMessagingApi;

    private SearchQuery searchQuery;
    private SearchQuery topResultsSearchQuery;
    private Contacts contacts;
    private Contacts searchContacts;

    private String searchFilter = null;

    // List of user id's to exclude from search
    private String[] excludedUserIds = new String[0];

    public ScalaPickUserStore(ZMessagingApi zMessagingApi) {
        this.zMessagingApi = zMessagingApi;
    }

    @Override
    public void addPickUserStoreObserver(PickUserStoreObserver pickUserStoreObserver) {
        super.addPickUserStoreObserver(pickUserStoreObserver);
        setupSearchQueryListeners();
    }

    @Override
    public void removePickUserStoreObserver(PickUserStoreObserver pickUserStoreObserver) {
        super.removePickUserStoreObserver(pickUserStoreObserver);
        if (pickUserStoreObservers.size() < 1) {
            removeSearchQueryListeners();
            searchQuery = null;
            topResultsSearchQuery = null;
        }
    }

    @Override
    public void tearDown() {
        if (contacts != null) {
            contacts.removeUpdateListener(contactsUpdateListener);
            contacts = null;
        }
        if (searchContacts != null) {
            searchContacts.removeUpdateListener(searchContactsUpdateListener);
            searchContacts = null;
        }
        removeSearchQueryListeners();
        searchQuery = null;
        topResultsSearchQuery = null;
        zMessagingApi = null;
    }

    @Override
    public void loadTopUserList(int numberOfResults, boolean excludeUsers) {
        setupSearchQueryListeners();
        searchFilter = null;
        if (!excludeUsers) {
            topResultsSearchQuery.setTopPeopleQuery(numberOfResults);
        } else {
            topResultsSearchQuery.setTopPeopleQuery(numberOfResults, excludedUserIds);
        }
    }

    @Override
    public void loadRecommendedUsers(int numberOfResults) {
        setupSearchQueryListeners();
        searchFilter = null;
        searchQuery.setRecommendedPeopleQuery(numberOfResults);
    }

    @Override
    public void loadSearchByFilter(String filter, int numberOfResults, boolean excludeUsers) {
        setupSearchQueryListeners();
        searchFilter = filter;
        if (!excludeUsers) {
            searchQuery.setQuery(filter, numberOfResults);
        } else {
            searchQuery.setQuery(filter, numberOfResults, excludedUserIds);
        }
    }

    @Override
    public void loadContacts() {
        if (searchContacts != null) {
            searchContacts.removeUpdateListener(searchContactsUpdateListener);
            searchContacts = null;
        }

        if (contacts == null) {
            contacts = zMessagingApi.getContacts();
            contacts.addUpdateListener(contactsUpdateListener);
        }
        notifyContactsUpdated(contacts);
    }

    @Override
    public void searchContacts(String query) {
        if (contacts != null) {
            contacts.removeUpdateListener(contactsUpdateListener);
            contacts = null;
        }

        if (searchContacts == null) {
            searchContacts = zMessagingApi.search().getContacts(query);
            searchContacts.addUpdateListener(searchContactsUpdateListener);
        } else {
            searchContacts.search(query);
        }
        notifySearchContactsUpdated(searchContacts);
    }

    @Override
    public void resetContactSearch() {
        searchContacts("");
    }

    @Override
    public void setExcludedUsers(String[] users) {
        excludedUserIds = users;
    }

    @Override
    public String[] getExcludedUsers() {
        return excludedUserIds;
    }

    @Override
    public boolean hasTopUsers() {
        if (topResultsSearchQuery != null &&
            topResultsSearchQuery.getUsers() != null &&
            topResultsSearchQuery.getUsers().length > 0) {
            return true;
        }
        return false;
    }

    @Override
    public User getUser(String userId) {
        return zMessagingApi.getUser(userId);
    }

    @Override
    public Contacts getContacts() {
        return contacts;
    }

    private void setupSearchQueryListeners() {
        if (searchQuery == null) {
            //TODO update to use new API
            searchQuery = zMessagingApi.searchQuery();
            searchQuery.addUpdateListener(searchQueryListener);
        }
        if (topResultsSearchQuery == null) {
            //TODO update to use new API
            topResultsSearchQuery = zMessagingApi.searchQuery();
            topResultsSearchQuery.addUpdateListener(topResultsSearchQueryListener);
        }
    }

    private void removeSearchQueryListeners() {
        if (searchQuery != null) {
            searchQuery.removeUpdateListener(searchQueryListener);
        }
        if (topResultsSearchQuery != null) {
            topResultsSearchQuery.removeUpdateListener(topResultsSearchQueryListener);
        }
    }

    final private UpdateListener searchQueryListener = new UpdateListener() {
        @Override
        public void updated() {
            if (!TextUtils.isEmpty(searchFilter)) {
                // Other users = related users and directory users
                ArrayList<User> otherUsers = new ArrayList<>();
                otherUsers.addAll(Arrays.asList(searchQuery.getRelated()));
                otherUsers.addAll(Arrays.asList(searchQuery.getOther()));

                notifySearchResultsUpdated(searchQuery.getContacts(), otherUsers.toArray(new User[otherUsers.size()]), searchQuery.getConversations());
            } else {
                notifyRecommendedUsersUpdated(searchQuery.getUsers());
            }
        }
    };

    final private UpdateListener topResultsSearchQueryListener = new UpdateListener() {
        @Override
        public void updated() {
            notifyTopUsersUpdated(topResultsSearchQuery.getUsers());
        }
    };

    final private UpdateListener contactsUpdateListener = new UpdateListener() {
        @Override
        public void updated() {
            if (contacts == null) {
                return;
            }
            notifyContactsUpdated(contacts);
        }
    };

    final private UpdateListener searchContactsUpdateListener = new UpdateListener() {
        @Override
        public void updated() {
            if (searchContacts == null) {
                return;
            }
            notifySearchContactsUpdated(searchContacts);
        }
    };
}
