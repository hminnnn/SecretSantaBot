package telegramsecretsantabot.telegramsecretsantabot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

public class UpdateListenerWebhook {

	private static final Logger logger = LogManager.getLogger(UpdateListenerWebhook.class);

	private TelegramBot bot;

	private Map<String, SecretSantaBot> santaBotsChatsMap = new HashMap<String, SecretSantaBot>();
	private Map<String, SecretSantaBot> santaBotPersonalChatsMap = new HashMap<String, SecretSantaBot>();
	private ArrayList<String> updateIdArray = new ArrayList<String>();


	public UpdateListenerWebhook(TelegramBot bot) {
		this.bot = bot;
	}

	public void process(Update update) {
		Update upd = update;
		String updateId = upd.updateId().toString();

		// ignore repeated updates of same id
		if (!updateIdArray.contains(updateId)) {
			updateIdArray.add(updateId);
			processUpdate(upd);
		}
	}

	private void processUpdate(Update upd) {

		SecretSantaBot secretSantaBot = extractChatIds(upd);
		if (secretSantaBot != null) {
			secretSantaBot.update(upd);
		} else {
			System.out.println("ERROR: SECRETSANTABOT IS NULL AT PROCESSUPDATE");
		}
	}

	/*
	 * Extract chatIds to store in map before proceeding to process the message
	 */
	private SecretSantaBot extractChatIds(Update upd) {


		// Buttons Callback
		CallbackQuery callbackQ = upd.callbackQuery();
		if (callbackQ != null) {
			return extractChatIdsFromCallbackQ(upd, callbackQ);
		}

		// Normal Message
		Message message = upd.message();

		System.out.println("------------ Message Recevied -------------");
		MessageEntity[] msgEntities = message.entities();


		if (msgEntities != null && msgEntities.length > 0) {
			String commandType = msgEntities[0].type().toString();

			if (isBotCommand(commandType)) {

				System.out.println("(message.chat():" + message.chat());

				// Group chat
				if (isFromGroupChat(message.chat().type().toString())) {
					String groupChatId = message.chat().id().toString();

					if (santaBotsChatsMap.get(groupChatId) != null) {
						// 14/04/2020 - Saves current ongoing session in map. Once finish button is
						// pressed, remove from list (possible?)

					} else {
						// startgame, new session new secretSantaBot
						SecretSantaBot secretSantaBot = new SecretSantaBot(bot, upd, groupChatId);
						santaBotsChatsMap.put(groupChatId, secretSantaBot);
						return secretSantaBot;


					}
					// Personal Chat - To press Start button to join the list.
				} else {
					String chatId = message.chat().id().toString();
		
					String command = message.text();

					if (command.contains("/start")) {
						// Find the group chat this person came from
						String groupChatId = isValidJoinCommand(command);
					
						if (groupChatId == null) {	// Invalid start from Bot chat 
							SecretSantaBot secretSantaBot = new SecretSantaBot(bot, upd, chatId);
							santaBotPersonalChatsMap.put(chatId, secretSantaBot);
							return secretSantaBot;
						}
						// Valid start from a Group chat
						if (santaBotsChatsMap.containsKey(groupChatId)) {
							return santaBotsChatsMap.get(groupChatId);
						}
					}
				}

			}


		}
		return null;
	}

	private SecretSantaBot extractChatIdsFromCallbackQ(Update upd, CallbackQuery callbackQ) {
		String groupChatId = callbackQ.message().chat().id().toString();
		System.out.println("callback command from groupChatId: " + groupChatId);

		return santaBotsChatsMap.get(groupChatId);
	}

	private boolean isBotCommand(String command) {
		if (command.equals("bot_command")) {
			return true;
		}
		return false;
	}

	private String isValidJoinCommand(String command) {
		if (command.contains("/start") && command.length() > 7) {
			String key = command.substring(6);
			key = key.trim();
			System.out.println("Join Button from this groupchat: " + key);
			return key;
			
		}
		return null;
	}

	private boolean isFromGroupChat(String messageChatType) {
		return messageChatType.equalsIgnoreCase("group") || messageChatType.equalsIgnoreCase("supergroup");
	}


}
