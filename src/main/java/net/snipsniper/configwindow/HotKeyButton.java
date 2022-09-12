package net.snipsniper.configwindow;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.snipsniper.LangManager;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.util.ArrayList;

public class HotKeyButton extends JButton implements NativeKeyListener, NativeMouseListener {
	private boolean listening = false;
	private int hotkey;
	private final HotKeyButton instance;
	private boolean isKeyboard = true;
	private int location = -1;

	private String oldLabel;

	private final ArrayList<ChangeListener> listeners = new ArrayList<>();

	public HotKeyButton(String key) {
		if(key.contains("_")) {
			String[] parts = key.split("_");
			key = parts[0];
			location = Integer.parseInt(parts[1]);
		}

		if(key.startsWith("NONE")) {
			this.setText(LangManager.Companion.getItem("config_label_none"));
			hotkey = -1;
		} else if(key.startsWith("KB")) {
			hotkey = Integer.parseInt(key.replace("KB", ""));
			this.setText(NativeKeyEvent.getKeyText(hotkey));
		} else if (key.startsWith("M")) {
			hotkey = Integer.parseInt(key.replace("M", ""));
			isKeyboard = false;
			this.setText(LangManager.Companion.getItem("config_label_mouse") + " " + hotkey);
		}

		instance = this;
		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
		this.addActionListener(listener -> {
			listening = true;
			instance.setText(LangManager.Companion.getItem("config_label_hotkey_listening"));
		});

		oldLabel = instance.getText();
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
		if(listening) {
			if(nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
				listening = false;
				instance.setText(oldLabel);
			} else {
				isKeyboard = true;
				hotkey = nativeKeyEvent.getKeyCode();
				location = nativeKeyEvent.getKeyLocation();
				listening = false;
				instance.setText(NativeKeyEvent.getKeyText(hotkey));
				oldLabel = instance.getText();
				notifyListeners();
			}
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }

	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }

	@Override
	public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
		if(listening) {
			hotkey = nativeMouseEvent.getButton();

			if(hotkey == 1 || hotkey == 2) {
				hotkey = -1;
				return;
			}
			location = -1;
			isKeyboard = false;
			listening = false;
			notifyListeners();
			instance.setText(LangManager.Companion.getItem("config_label_mouse") + " " + hotkey);
			oldLabel = instance.getText();
		}
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) { }

	@Override
	public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) { }

	private void notifyListeners() {
		for(ChangeListener listener : listeners)
			listener.stateChanged(new ChangeEvent(this));
	}

	public void addDoneCapturingListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public int getHotkey() {
		return hotkey;
	}

	public void setHotKey(int key) {
		hotkey = key;
	}

	public boolean isKeyboard() {
		return isKeyboard;
	}

	public String getHotKeyString() {
		String hotkeyModifier = "KB";
		if (!isKeyboard)
			hotkeyModifier = "M";
		String locationModifier ="";
		if(location != -1)
			locationModifier = "_" + location;
		return hotkeyModifier + hotkey + locationModifier;
	}
}
