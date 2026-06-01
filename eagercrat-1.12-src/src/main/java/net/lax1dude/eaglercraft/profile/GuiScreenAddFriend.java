package net.lax1dude.eaglercraft.profile;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/**
 * Add friend screen UI
 * Allows users to search for and add friends by username
 */
public class GuiScreenAddFriend extends GuiScreen {

	private GuiScreen parentScreen;
	private GuiTextField usernameField;
	
	private String statusMessage = "";
	private int statusMessageTimeout = 0;
	private boolean isError = false;

	public GuiScreenAddFriend(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		
		int centerX = this.width / 2;
		int centerY = this.height / 2;

		// Title is drawn in drawScreen

		// Username input field
		this.usernameField = new GuiTextField(1, this.fontRendererObj, centerX - 100, centerY - 20, 200, 20);
		this.usernameField.setMaxStringLength(16);
		this.usernameField.setFocused(true);

		// Add Friend button
		this.buttonList.add(new GuiButton(0, centerX - 100, centerY + 20, 90, 20, "Add Friend"));
		
		// Cancel button
		this.buttonList.add(new GuiButton(99, centerX + 20, centerY + 20, 80, 20, "Cancel"));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			// Add Friend button
			attemptAddFriend();
		} else if (button.id == 99) {
			// Cancel button
			this.mc.displayScreen(this.parentScreen);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		this.usernameField.textboxKeyTyped(typedChar, keyCode);

		if (keyCode == 28) { // Enter key
			attemptAddFriend();
		} else if (keyCode == 1) { // Escape key
			this.mc.displayScreen(this.parentScreen);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
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
		int centerY = this.height / 2;

		// Title
		this.fontRendererObj.drawStringWithShadow("Add Friend", centerX - 30, centerY - 60, 0xFFFFFF);

		// Instructions
		this.fontRendererObj.drawStringWithShadow("Enter username:", centerX - 60, centerY - 35, 0xAAAAAA);

		// Draw text field
		this.usernameField.drawTextBox();

		// Draw buttons
		super.drawScreen(mouseX, mouseY, partialTicks);

		// Status message
		if (this.statusMessageTimeout > 0) {
			int color = this.isError ? 0xFF5555 : 0x55FF55;
			this.fontRendererObj.drawStringWithShadow(this.statusMessage, centerX - 100, centerY + 50, color);
		}
	}

	private void attemptAddFriend() {
		String username = this.usernameField.getText().trim();

		if (username.isEmpty()) {
			showMessage("Please enter a username", true);
			return;
		}

		if (username.length() < 3 || username.length() > 16) {
			showMessage("Username must be 3-16 characters", true);
			return;
		}

		if (username.equalsIgnoreCase(AccountManager.getCurrentAccountName())) {
			showMessage("Cannot add yourself", true);
			return;
		}

		if (FriendsManager.isFriend(username)) {
			showMessage("Already friends with " + username, true);
			return;
		}

		// Success message - in a real implementation this would call the backend API
		showMessage("Friend request sent to " + username, false);
		this.usernameField.setText("");

		// TODO: Call backend API to send friend request
		// Example:
		// POST /api/friends/request
		// { "username": username }
		// Store token from AccountManager and use in Authorization header
	}

	private void showMessage(String message, boolean isError) {
		this.statusMessage = message;
		this.statusMessageTimeout = 120;
		this.isError = isError;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
