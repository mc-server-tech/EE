package net.lax1dude.eaglercraft.profile;

import java.io.IOException;

import net.lax1dude.eaglercraft.Keyboard;
import net.lax1dude.eaglercraft.KeyboardConstants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiScreenAccountLogin extends GuiScreen {

	private final GuiScreen parent;
	private GuiTextField usernameField;
	private GuiPasswordTextField passwordField;
	private GuiButton loginButton;
	private String statusMessage = "";

	public GuiScreenAccountLogin(GuiScreen parent) {
		this.parent = parent;
		AccountManager.ensureLoaded();
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.usernameField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100,
				this.height / 4 + 10, 200, 20);
		this.usernameField.setMaxStringLength(16);
		this.usernameField.setFocused(true);
		if (AccountManager.isLoggedIn()) {
			this.usernameField.setText(AccountManager.getCurrentAccountName());
		}

		this.passwordField = new GuiPasswordTextField(1, this.fontRendererObj, this.width / 2 - 100,
				this.height / 4 + 40, 200, 20);
		this.passwordField.setMaxStringLength(128);

		this.loginButton = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 75, 200, 20,
				"Log In");
		this.loginButton.enabled = false;
		this.buttonList.add(this.loginButton);
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 102, 200, 20,
				"Sign Up"));
		this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 129, 200, 20,
				"Back"));
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) {
			return;
		}

		if (button.id == 0) {
			attemptLogin();
		} else if (button.id == 1) {
			this.mc.displayGuiScreen(new GuiScreenAccountSignup(this));
		} else if (button.id == 2) {
			this.mc.displayGuiScreen(this.parent);
		}
	}

	private void attemptLogin() {
		String username = this.usernameField.getText().trim();
		String password = this.passwordField.getText();

		if (username.isEmpty() || password.isEmpty()) {
			this.statusMessage = "Please enter a username and password.";
			return;
		}

		if (AccountManager.login(username, password)) {
			this.statusMessage = "Login successful!";
			this.mc.displayGuiScreen(this.parent);
		} else {
			this.statusMessage = "Invalid username or password.";
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(this.fontRendererObj, "Account Login", this.width / 2,
				this.height / 4 - 20, 16777215);
		drawString(this.fontRendererObj, "Username", this.width / 2 - 100,
				this.height / 4 - 2, 10526880);
		drawString(this.fontRendererObj, "Password", this.width / 2 - 100,
				this.height / 4 + 30, 10526880);
		this.usernameField.drawTextBox();
		this.passwordField.drawTextBox();
		if (!this.statusMessage.isEmpty()) {
			drawCenteredString(this.fontRendererObj, this.statusMessage, this.width / 2,
				this.height / 4 + 60, 0xFFFF5555);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		this.usernameField.textboxKeyTyped(typedChar, keyCode);
		this.passwordField.textboxKeyTyped(typedChar, keyCode);
		this.loginButton.enabled = !this.usernameField.getText().trim().isEmpty()
				&& !this.passwordField.getText().isEmpty();

		if (keyCode == KeyboardConstants.KEY_RETURN && this.loginButton.enabled) {
			attemptLogin();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
		this.passwordField.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
