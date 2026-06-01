package net.lax1dude.eaglercraft.profile;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/**
 * Friends list screen UI
 * Displays friends, friend requests, and options to manage friends
 */
public class GuiScreenFriends extends GuiScreen {

	private GuiScreen parentScreen;
	private GuiTextField searchField;
	
	// UI state
	private static final int BUTTON_HEIGHT = 20;
	private static final int FRIEND_ITEM_HEIGHT = 25;
	private static final int REQUEST_ITEM_HEIGHT = 30;
	
	private int scrollOffset = 0;
	private int selectedTab = 0; // 0 = Friends, 1 = Requests, 2 = Search
	private String statusMessage = "";
	private int statusMessageTimeout = 0;

	public GuiScreenFriends(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		
		int centerX = this.width / 2;
		int startY = 40;

		// Tab buttons
		this.buttonList.add(new GuiButton(0, centerX - 150, startY, 90, BUTTON_HEIGHT, "Friends (" + FriendsManager.getFriendCount() + ")"));
		this.buttonList.add(new GuiButton(1, centerX - 50, startY, 100, BUTTON_HEIGHT, "Requests (" + FriendsManager.getPendingRequestCount() + ")"));
		this.buttonList.add(new GuiButton(2, centerX + 60, startY, 90, BUTTON_HEIGHT, "Search"));

		// Search field (visible in Search tab)
		this.searchField = new GuiTextField(3, this.fontRendererObj, centerX - 150, startY + 30, 300, BUTTON_HEIGHT);
		this.searchField.setMaxStringLength(16);

		// Back button
		this.buttonList.add(new GuiButton(99, centerX - 75, this.height - 30, 150, BUTTON_HEIGHT, "Back"));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 99) {
			// Back button
			this.mc.displayScreen(this.parentScreen);
		} else if (button.id == 0) {
			// Friends tab
			this.selectedTab = 0;
			this.scrollOffset = 0;
		} else if (button.id == 1) {
			// Requests tab
			this.selectedTab = 1;
			this.scrollOffset = 0;
		} else if (button.id == 2) {
			// Search tab
			this.selectedTab = 2;
			this.scrollOffset = 0;
		} else if (button.id >= 100 && button.id < 200) {
			// Friend action buttons (remove, etc.)
			handleFriendAction(button.id - 100);
		} else if (button.id >= 200 && button.id < 300) {
			// Request action buttons (accept, reject)
			handleRequestAction(button.id - 200);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (this.selectedTab == 2) {
			this.searchField.textboxKeyTyped(typedChar, keyCode);

			if (keyCode == 28) { // Enter key
				performSearch();
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (this.selectedTab == 2) {
			this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void updateScreen() {
		if (this.statusMessageTimeout > 0) {
			this.statusMessageTimeout--;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		int centerX = this.width / 2;
		int startY = 40;

		// Title
		this.fontRendererObj.drawStringWithShadow("Friends", centerX - 20, 10, 0xFFFFFF);

		// Draw tabs
		super.drawScreen(mouseX, mouseY, partialTicks);

		// Tab content area
		int contentStartY = startY + 40;
		int contentHeight = this.height - contentStartY - 40;

		if (this.selectedTab == 0) {
			drawFriendsTab(centerX, contentStartY, contentHeight, mouseX, mouseY);
		} else if (this.selectedTab == 1) {
			drawRequestsTab(centerX, contentStartY, contentHeight, mouseX, mouseY);
		} else if (this.selectedTab == 2) {
			drawSearchTab(centerX, contentStartY, contentHeight, mouseX, mouseY);
		}

		// Status message
		if (this.statusMessageTimeout > 0) {
			this.fontRendererObj.drawStringWithShadow(this.statusMessage, centerX - 50, this.height - 50, 0x00FF00);
		}
	}

	private void drawFriendsTab(int centerX, int startY, int height, int mouseX, int mouseY) {
		String title = "Friends (" + FriendsManager.getFriendCount() + ")";
		this.fontRendererObj.drawStringWithShadow(title, centerX - 60, startY, 0xFFFFFF);

		if (FriendsManager.getFriendCount() == 0) {
			this.fontRendererObj.drawStringWithShadow("No friends yet. Search for users to add!", centerX - 90, startY + 30, 0xAAAAAA);
			return;
		}

		int y = startY + 25;
		for (int i = 0; i < FriendsManager.getFriendsList().size(); i++) {
			if (y > startY + height - 30) break;

			FriendsManager.Friend friend = FriendsManager.getFriendsList().get(i);
			String status = friend.isOnline ? "§a[ONLINE]" : "§7[OFFLINE]";
			this.fontRendererObj.drawStringWithShadow(friend.username + " " + status, centerX - 140, y, 0xFFFFFF);

			y += FRIEND_ITEM_HEIGHT;
		}
	}

	private void drawRequestsTab(int centerX, int startY, int height, int mouseX, int mouseY) {
		String title = "Friend Requests";
		this.fontRendererObj.drawStringWithShadow(title, centerX - 40, startY, 0xFFFFFF);

		int incomingCount = FriendsManager.getIncomingRequests().size();
		int outgoingCount = FriendsManager.getOutgoingRequests().size();

		// Incoming requests
		this.fontRendererObj.drawStringWithShadow("Incoming (" + incomingCount + ")", centerX - 70, startY + 25, 0xFFFF99);
		
		int y = startY + 45;
		for (FriendsManager.FriendRequest req : FriendsManager.getIncomingRequests()) {
			if (y > startY + height - 60) break;
			this.fontRendererObj.drawStringWithShadow("From: " + req.username, centerX - 140, y, 0xFFFFFF);
			y += REQUEST_ITEM_HEIGHT;
		}

		y += 15;

		// Outgoing requests
		this.fontRendererObj.drawStringWithShadow("Outgoing (" + outgoingCount + ")", centerX - 70, y, 0xFF9999);
		
		y += 20;
		for (FriendsManager.FriendRequest req : FriendsManager.getOutgoingRequests()) {
			if (y > startY + height - 20) break;
			this.fontRendererObj.drawStringWithShadow("To: " + req.username, centerX - 140, y, 0xFFFFFF);
			y += REQUEST_ITEM_HEIGHT;
		}

		if (incomingCount == 0 && outgoingCount == 0) {
			this.fontRendererObj.drawStringWithShadow("No pending requests", centerX - 50, startY + 50, 0xAAAAAA);
		}
	}

	private void drawSearchTab(int centerX, int startY, int height, int mouseX, int mouseY) {
		String title = "Search Users";
		this.fontRendererObj.drawStringWithShadow(title, centerX - 35, startY, 0xFFFFFF);

		this.searchField.drawTextBox();

		if (FriendsManager.getSearchResults().isEmpty()) {
			this.fontRendererObj.drawStringWithShadow("Type a username and press Enter to search", centerX - 110, startY + 60, 0xAAAAAA);
			return;
		}

		int y = startY + 60;
		for (FriendsManager.SearchResult result : FriendsManager.getSearchResults()) {
			if (y > startY + height - 20) break;

			String statusStr;
			if (result.isFriend) {
				statusStr = "§a[FRIEND]";
			} else if (result.isBlocked) {
				statusStr = "§c[BLOCKED]";
			} else if (result.hasPendingRequest) {
				statusStr = "§e[PENDING]";
			} else {
				statusStr = "§7[ADD]";
			}

			this.fontRendererObj.drawStringWithShadow(result.username + " " + statusStr, centerX - 140, y, 0xFFFFFF);
			y += FRIEND_ITEM_HEIGHT;
		}
	}

	private void handleFriendAction(int friendId) {
		this.statusMessage = "Friend action: " + friendId;
		this.statusMessageTimeout = 60;
	}

	private void handleRequestAction(int requestId) {
		this.statusMessage = "Request action: " + requestId;
		this.statusMessageTimeout = 60;
	}

	private void performSearch() {
		String query = this.searchField.getText().trim();
		if (query.length() >= 2) {
			this.statusMessage = "Searching for: " + query;
			this.statusMessageTimeout = 120;
			// TODO: Call backend search API
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
