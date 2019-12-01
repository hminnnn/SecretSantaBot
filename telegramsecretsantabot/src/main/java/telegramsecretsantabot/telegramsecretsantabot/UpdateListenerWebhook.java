package telegramsecretsantabot.telegramsecretsantabot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

public class UpdateListenerWebhook {

	private TelegramBot bot;
	private Map<String, SecretSantaBot> santaBotsChatsMap = new HashMap<String, SecretSantaBot>();
	private Map<String, SecretSantaBot> santaBotPersonalChatsMap = new HashMap<String, SecretSantaBot>();
	// <chat id, SecretSantaBot>
	private ArrayList<String> updateIdArray = new ArrayList<String>();

	private String groupChatId;
	private String chatId;

	public UpdateListenerWebhook(TelegramBot bot) {
		this.bot = bot;
	}

	public void process(Update update) {
		Update upd = update;
		String updateId = upd.updateId().toString();

		if (updateIdArray.contains(updateId)) {
			// ignore messages of the same id
		} else {
			updateIdArray.add(updateId);
//			System.out.println("------------New Message----------");
//			System.out.println("upd:" + upd);
			processNewUpdate(upd);
		}
	}

	private void processNewUpdate(Update upd) {

		Message message = upd.message();
		// System.out.println("message:" + message);

		CallbackQuery callbackQ = upd.callbackQuery();

		// normal text commands
		if (message != null) {
			MessageEntity[] msgEntities = message.entities();
			String msgText = message.text();

			chatId = message.chat().id().toString();

			if (msgEntities != null && msgEntities.length > 0) {
				String commandType = msgEntities[0].type().toString();
				if (isBotCommand(commandType)) {

					System.out.println("chatId from updatelistenerwebhook:" + chatId);

					System.out.println("(message.chat():" + message.chat());

					// Group chat
					if (isFromGroupChat(message.chat().type().toString())) {
						groupChatId = chatId;

						if (santaBotsChatsMap.get(groupChatId) != null) {
							// duplicate /startgame command, terminates previous session.
							santaBotsChatsMap.get(groupChatId).update(upd);
//								santaBotsChatsMap.remove(groupChatId);
						} else {
							// /startgame, new session new secretSantaBot
							SecretSantaBot secretSantaBot = new SecretSantaBot(bot, upd, groupChatId);
							santaBotsChatsMap.put(groupChatId, secretSantaBot);
							secretSantaBot.update(upd);

						}
					} else {
						// Personal Chat
						SecretSantaBot secretSantaBot = new SecretSantaBot(bot, upd, chatId);
						santaBotPersonalChatsMap.put(chatId, secretSantaBot);
						secretSantaBot.update(upd);
					}

//					if (isStartGameCommand(msgText)) {
//						groupChatId = chatId;
//						if (santaBotsChatsMap.get(groupChatId) != null) {
//							// duplicate /startgame command, terminates previous session.
//							santaBotsChatsMap.get(groupChatId).update(upd);
//							santaBotsChatsMap.remove(groupChatId);
//						} else {
//							// /startgame, new session new secretSantaBot
//							SecretSantaBot secretSantaBot = new SecretSantaBot(bot, upd, groupChatId);
//							santaBotsChatsMap.put(groupChatId, secretSantaBot);
//						}
//
//					} else {
//						// send update to the group chat's santabot
//						if (santaBotsChatsMap.get(groupChatId) != null) {
//							santaBotsChatsMap.get(groupChatId).update(upd);
//						}
//					}

				}

			}
		}
//		else {
//			System.out.println("Update message is null");
//		}

		// Buttons callback
		if (callbackQ != null) {

			groupChatId = callbackQ.message().chat().id().toString();
			System.out.println("callback command from groupChatId: " + groupChatId);

			// send update to the group chat's santabot
			if (santaBotsChatsMap.get(groupChatId) != null) {
				santaBotsChatsMap.get(groupChatId).update(upd);
			}

		}
	}

	private boolean isBotCommand(String command) {
		if (command.equals("bot_command")) {
			return true;
		}
		return false;
	}

	private boolean isStartGameCommand(String command) {
		if (command.contains("/startgame")) {
			System.out.println("/startgame");
			return true;
		}
		return false;
	}

	private boolean isHelpCommand(String command) {
		if (command.contains("/help")) {
			System.out.println("/help");
			return true;
		}
		return false;
	}

	private boolean isFromGroupChat(String messageChatType) {
		return messageChatType.equalsIgnoreCase("group") || messageChatType.equalsIgnoreCase("supergroup");

	}

	private String getParticipantName(User participant) {
		String participantName = "";
		if (participant.firstName() != null && participant.firstName() != "") {
			participantName = participant.firstName();
		} else if (participant.lastName() != null && participant.lastName() != "") {
			participantName = participant.lastName();
		} else if (participant.username() != null && participant.username() != "") {
			participantName = participant.username();
		} else {
			participantName = participant.id().toString() + "has no name...";
		}
		return participantName;
	}
}
