package net.lax1dude.eaglercraft.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side friends manager
 * Stores friend data and provides methods for friend management
 * API calls should be made to the backend server
 */
public class FriendsManager {

	public static class Friend {
		public int id;
		public String username;
		public boolean isOnline;
		public long lastSeen;

		public Friend(int id, String username, boolean isOnline, long lastSeen) {
			this.id = id;
			this.username = username;
			this.isOnline = isOnline;
			this.lastSeen = lastSeen;
		}
	}

	public static class FriendRequest {
		public int id;
		public int userId;
		public String username;
		public boolean isIncoming;
		public long createdAt;

		public FriendRequest(int id, int userId, String username, boolean isIncoming, long createdAt) {
			this.id = id;
			this.userId = userId;
			this.username = username;
			this.isIncoming = isIncoming;
			this.createdAt = createdAt;
		}
	}

	public static class SearchResult {
		public int id;
		public String username;
		public boolean isFriend;
		public boolean isBlocked;
		public boolean hasPendingRequest;

		public SearchResult(int id, String username, boolean isFriend, boolean isBlocked, boolean hasPendingRequest) {
			this.id = id;
			this.username = username;
			this.isFriend = isFriend;
			this.isBlocked = isBlocked;
			this.hasPendingRequest = hasPendingRequest;
		}
	}

	private static List<Friend> friendsList = new ArrayList<>();
	private static List<FriendRequest> incomingRequests = new ArrayList<>();
	private static List<FriendRequest> outgoingRequests = new ArrayList<>();
	private static List<SearchResult> searchResults = new ArrayList<>();

	// Getters for friend data
	public static List<Friend> getFriendsList() {
		return new ArrayList<>(friendsList);
	}

	public static List<FriendRequest> getIncomingRequests() {
		return new ArrayList<>(incomingRequests);
	}

	public static List<FriendRequest> getOutgoingRequests() {
		return new ArrayList<>(outgoingRequests);
	}

	public static List<SearchResult> getSearchResults() {
		return new ArrayList<>(searchResults);
	}

	// Methods to update cached data from backend responses
	public static void setFriendsList(List<Friend> friends) {
		friendsList = new ArrayList<>(friends);
	}

	public static void setIncomingRequests(List<FriendRequest> requests) {
		incomingRequests = new ArrayList<>(requests);
	}

	public static void setOutgoingRequests(List<FriendRequest> requests) {
		outgoingRequests = new ArrayList<>(requests);
	}

	public static void setSearchResults(List<SearchResult> results) {
		searchResults = new ArrayList<>(results);
	}

	// Helper methods
	public static boolean isFriend(String username) {
		return friendsList.stream().anyMatch(f -> f.username.equalsIgnoreCase(username));
	}

	public static Friend getFriend(String username) {
		return friendsList.stream()
			.filter(f -> f.username.equalsIgnoreCase(username))
			.findFirst()
			.orElse(null);
	}

	public static int getFriendCount() {
		return friendsList.size();
	}

	public static int getPendingRequestCount() {
		return incomingRequests.size();
	}

	public static void clear() {
		friendsList.clear();
		incomingRequests.clear();
		outgoingRequests.clear();
		searchResults.clear();
	}
}
