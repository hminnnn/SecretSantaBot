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
	private Map<String, SecretSantaBot> santaBotsChatsMap = new HashMap<String, SecretSantaBot>(); // <chat id,
																									// SecretSantaBot>
	private ArrayList<String> updateIdArray = new ArrayList<String>();

	private String groupChatId;
	private String individualChatId;
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
		//return UpdatesListener.CONFIRMED_UPDATES_ALL;
	}

	private void processNewUpdate(Update upd) {
		
		Message message = upd.message();
		
		// System.out.println("message:" + message);

		CallbackQuery callbackQ = upd.callbackQuery();

		if (message != null) {
			MessageEntity[] msgEntities = message.entities();
			String msgText = message.text();

			chatId = message.chat().id().toString();

			if (msgEntities != null && msgEntities.length > 0) {
				String commandType = msgEntities[0].type().toString();
				if (isBotCommand(commandType)) {
					
					System.out.println("chatId:" + chatId);
					
					if (isStartGameCommand(msgText)) {
						// start game command = new santabot
						groupChatId = chatId;
						SecretSantaBot secretSantaBot = new SecretSantaBot(bot, upd, groupChatId);
						santaBotsChatsMap.put(groupChatId, secretSantaBot);
					} else {
						// send update to the group chat's santabot
						if(santaBotsChatsMap.get(groupChatId) != null) {
							santaBotsChatsMap.get(groupChatId).update(upd);
						}
					}

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
			if(santaBotsChatsMap.get(groupChatId) != null) {
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