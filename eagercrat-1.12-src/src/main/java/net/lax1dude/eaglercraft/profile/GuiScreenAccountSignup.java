package net.lax1dude.eaglercraft.profile;

import java.io.IOException;

import net.lax1dude.eaglercraft.Keyboard;
import net.lax1dude.eaglercraft.KeyboardConstants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiScreenAccountSignup extends GuiScreen {

	private final GuiScreen parent;
	private GuiTextField usernameField;
	private GuiPasswordTextField passwordField;
	private GuiPasswordTextField passwordConfirmField;
	private GuiButton createButton;
	private String statusMessage = "";

	public GuiScreenAccountSignup(GuiScreen parent) {
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

		this.passwordField = new GuiPasswordTextField(1, this.fontRendererObj, this.width / 2 - 100,
				this.height / 4 + 40, 200, 20);
		this.passwordField.setMaxStringLength(128);

		this.passwordConfirmField = new GuiPasswordTextField(2, this.fontRendererObj,
				this.width / 2 - 100, this.height / 4 + 70, 200, 20);
		this.passwordConfirmField.setMaxStringLength(128);

		this.createButton = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 100, 200, 20,
				"Create Account");
		this.createButton.enabled = false;
		this.buttonList.add(this.createButton);
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 127, 200, 20,
				"Back to Login"));
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
			attemptCreateAccount();
		} else if (button.id == 1) {
			this.mc.displayGuiScreen(new GuiScreenAccountLogin(this.parent));
		}
	}

	private void attemptCreateAccount() {
		String username = this.usernameField.getText().trim();
		String password = this.passwordField.getText();
		String confirm = this.passwordConfirmField.getText();

		if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
			this.statusMessage = "Please fill in all fields.";
			return;
		}

		if (!password.equals(confirm)) {
			this.statusMessage = "Passwords do not match.";
			return;
		}

		if (!AccountManager.createAccount(username, password)) {
			this.statusMessage = "Unable to create account. Name may already exist.";
			return;
		}

		this.statusMessage = "Account created and logged in.";
		this.mc.displayGuiScreen(this.parent);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(this.fontRendererObj, "Create Account", this.width / 2,
				this.height / 4 - 20, 16777215);
		drawString(this.fontRendererObj, "Username", this.width / 2 - 100,
				this.height / 4 - 2, 10526880);
		drawString(this.fontRendererObj, "Password", this.width / 2 - 100,
				this.height / 4 + 30, 10526880);
		drawString(this.fontRendererObj, "Confirm Password", this.width / 2 - 100,
				this.height / 4 + 60, 10526880);
		this.usernameField.drawTextBox();
		this.passwordField.drawTextBox();
		this.passwordConfirmField.drawTextBox();
		if (!this.statusMessage.isEmpty()) {
			drawCenteredString(this.fontRendererObj, this.statusMessage, this.width / 2,
				this.height / 4 + 95, 0xFFFF5555);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		this.usernameField.textboxKeyTyped(typedChar, keyCode);
		this.passwordField.textboxKeyTyped(typedChar, keyCode);
		this.passwordConfirmField.textboxKeyTyped(typedChar, keyCode);
		this.createButton.enabled = !this.usernameField.getText().trim().isEmpty()
				&& !this.passwordField.getText().isEmpty()
				&& !this.passwordConfirmField.getText().isEmpty();

		if (keyCode == KeyboardConstants.KEY_RETURN && this.createButton.enabled) {
			attemptCreateAccount();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
		this.passwordField.mouseClicked(mouseX, mouseY, mouseButton);
		this.passwordConfirmField.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
