package com.caved_in.teamdeathmatch.runnables;

public class MessageRunnable implements Runnable {
	//TODO Optimize this entire class to use XML with an attribute "premiummessage"
	int lastMessageIndex = 0;
	int currentMessageIndex = 0;

	public MessageRunnable() {

	}

	@Override
	public void run() {
//		currentMessageIndex = currentMessageIndex >= TDMGame.messages.size() ? 0 : currentMessageIndex;
//
//
//		String sendingMessage = StringUtil.formatColorCodes(TDMGame.messages.get(currentMessageIndex));
//		boolean isPremiumMessage = false;
//		if (sendingMessage.toLowerCase().contains("premium")) {
//			isPremiumMessage = true;
//		}
//
//		for (Player player : Bukkit.getOnlinePlayers()) {
//			if (isPremiumMessage) {
//				if (!PlayerHandler.isPremium(player)) {
//					player.sendMessage(sendingMessage);
//				}
//			} else {
//				player.sendMessage(sendingMessage);
//			}
//		}
//		currentMessageIndex += 1;
	}
}