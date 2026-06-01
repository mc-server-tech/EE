package net.lax1dude.eaglercraft.profile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.lax1dude.eaglercraft.EagRuntime;
import net.lax1dude.eaglercraft.crypto.SHA256Digest;
import net.lax1dude.eaglercraft.EaglerOutputStream;
import net.lax1dude.eaglercraft.EaglerInputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class AccountManager {

	private static final String STORAGE_NAME = "accounts";
	private static final String TAG_ACCOUNTS = "accounts";
	private static final String TAG_CURRENT = "current";
	private static final String TAG_USERNAME = "username";
	private static final String TAG_PASSWORD_HASH = "passwordHash";
	private static final String TAG_TOKEN = "token";

	private static final List<Account> accounts = new ArrayList<>();
	private static String currentAccount = null;
	private static String currentToken = null;
	private static boolean loaded = false;

	public static void ensureLoaded() {
		if (!loaded) {
			load();
		}
	}

	public static void load() {
		loaded = true;
		accounts.clear();
		byte[] data = EagRuntime.getStorage(STORAGE_NAME);
		if (data == null) {
			return;
		}

		try {
			NBTTagCompound root = CompressedStreamTools.readCompressed(new EaglerInputStream(data));
			if (root == null) {
				return;
			}

			currentAccount = root.getString(TAG_CURRENT);
			NBTTagList list = root.getTagList(TAG_ACCOUNTS, 10);
			for (int i = 0; i < list.tagCount(); ++i) {
				NBTTagCompound accountTag = list.getCompoundTagAt(i);
				String username = accountTag.getString(TAG_USERNAME).trim();
				byte[] hash = accountTag.getByteArray(TAG_PASSWORD_HASH);
				if (!username.isEmpty() && hash.length > 0) {
					accounts.add(new Account(username, bytesToHex(hash)));
				}
			}
		} catch (IOException ex) {
			// ignore corrupt storage
		}

		if (currentAccount != null && !currentAccount.isEmpty()) {
			EaglerProfile.setName(currentAccount);
		}
	}

	public static void save() {
		NBTTagCompound root = new NBTTagCompound();
		root.setString(TAG_CURRENT, currentAccount == null ? "" : currentAccount);
		NBTTagList list = new NBTTagList();
		for (Account account : accounts) {
			NBTTagCompound accountTag = new NBTTagCompound();
			accountTag.setString(TAG_USERNAME, account.username);
			accountTag.setByteArray(TAG_PASSWORD_HASH, hexToBytes(account.passwordHashHex));
			list.appendTag(accountTag);
		}
		root.setTag(TAG_ACCOUNTS, list);
		EaglerOutputStream out = new EaglerOutputStream();
		try {
			CompressedStreamTools.writeCompressed(root, out);
			EagRuntime.setStorage(STORAGE_NAME, out.toByteArray());
		} catch (IOException ex) {
			// ignore write failures
		}
	}

	public static boolean createAccount(String username, String password) {
		ensureLoaded();
		username = sanitizeUsername(username);
		if (!isValidUsername(username) || !isValidPassword(password) || accountExists(username)) {
			return false;
		}
		String passwordHash = hashPassword(username, password);
		accounts.add(new Account(username, passwordHash));
		currentAccount = username;
		EaglerProfile.setName(username);
		save();
		return true;
	}

	public static boolean login(String username, String password) {
		ensureLoaded();
		username = sanitizeUsername(username);
		if (!isValidUsername(username) || password == null || password.isEmpty()) {
			return false;
		}
		Account account = getAccount(username);
		if (account == null) {
			return false;
		}
		String passwordHash = hashPassword(username, password);
		if (!account.passwordHashHex.equals(passwordHash)) {
			return false;
		}
		currentAccount = username;
		EaglerProfile.setName(username);
		save();
		return true;
	}

	public static boolean logout() {
		ensureLoaded();
		currentAccount = null;
		save();
		return true;
	}

	public static boolean isLoggedIn() {
		ensureLoaded();
		return currentAccount != null && currentAccount.length() > 0;
	}

	public static String getCurrentAccountName() {
		ensureLoaded();
		return currentAccount;
	}

	public static void setCurrentToken(String token) {
		currentToken = token;
	}

	public static String getCurrentToken() {
		return currentToken;
	}

	public static boolean hasToken() {
		return currentToken != null && !currentToken.isEmpty();
	}

	public static void clearToken() {
		currentToken = null;
	}

	public static boolean accountExists(String username) {
		return getAccount(sanitizeUsername(username)) != null;
	}

	private static Account getAccount(String username) {
		if (username == null) {
			return null;
		}
		for (Account account : accounts) {
			if (account.username.equalsIgnoreCase(username)) {
				return account;
			}
		}
		return null;
	}

	private static String sanitizeUsername(String username) {
		if (username == null) {
			return "";
		}
		return username.replaceAll("[^A-Za-z0-9_\\-]", "").trim();
	}

	private static boolean isValidUsername(String username) {
		return username != null && username.length() >= 3 && username.length() <= 16;
	}

	private static boolean isValidPassword(String password) {
		return password != null && password.length() >= 6;
	}

	private static String hashPassword(String username, String password) {
		byte[] input = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
		byte[] hash = sha256(input);
		return bytesToHex(hash);
	}

	private static byte[] sha256(byte[] input) {
		SHA256Digest digest = new SHA256Digest();
		digest.update(input, 0, input.length);
		byte[] output = new byte[32];
		digest.doFinal(output, 0);
		return output;
	}

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = Character.forDigit(v >>> 4, 16);
			hexChars[j * 2 + 1] = Character.forDigit(v & 0x0F, 16);
		}
		return new String(hexChars);
	}

	private static byte[] hexToBytes(String hex) {
		if (hex == null || (hex.length() & 1) != 0) {
			return new byte[0];
		}
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; ++i) {
			int hi = Character.digit(hex.charAt(i * 2), 16);
			int lo = Character.digit(hex.charAt(i * 2 + 1), 16);
			if (hi < 0 || lo < 0) {
				return new byte[0];
			}
			bytes[i] = (byte) ((hi << 4) | lo);
		}
		return bytes;
	}

	private static class Account {
		private final String username;
		private final String passwordHashHex;

		private Account(String username, String passwordHashHex) {
			this.username = username;
			this.passwordHashHex = passwordHashHex;
		}
	}
}
