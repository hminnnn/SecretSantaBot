package telegramsecretsantabot.telegramsecretsantabot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;

public class ReplyMessages {

	public void eachParticipantInitCommand(TelegramBot bot, String chatId, String groupChatName) {

		String msg = "You have successfully added yourself into the list in " + groupChatName;
		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
	}

	public Integer createJoinMainMessage(TelegramBot bot, String groupChatId, Map<String, String> participantIdNameMap,
			String key) {

//		System.out.println("createJoinMainMessage");

		InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(new InlineKeyboardButton[] {
				new InlineKeyboardButton("Join").url("https://telegram.me/HMSantaBot?start=" + key),
				new InlineKeyboardButton("Un-Join").callbackData("RemoveButtonCallback"),
				new InlineKeyboardButton("Finish!").callbackData("FinishButtonCallback") });

		SendMessage request1 = new SendMessage(groupChatId,
				"<b>Secret Santa Game</b> \n" + "Who's in: " + participantIdNameMap.size() + "\n"
						+ getParticipantsNameString(participantIdNameMap)).parseMode(ParseMode.HTML)
								.disableWebPagePreview(true).disableNotification(true).replyMarkup(inlineKeyboard);

		SendResponse sendResponse = bot.execute(request1);
		boolean ok = sendResponse.isOk();
		Message message = sendResponse.message();
		Integer msgId = message.messageId();
		// System.out.println("sent msg: " + message);
		return msgId;

	}

	public void editJoinMainMessage(TelegramBot bot, String groupChatId, Integer msgId,
			Map<String, String> participantIdNameMap, String key) {

		System.out.println("joinButton " + getParticipantsNameString(participantIdNameMap));
		System.out.println("joinMsgId:" + msgId);

		InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(new InlineKeyboardButton[] {
				new InlineKeyboardButton("Join").url("https://telegram.me/HMSantaBot?start=" + key),
				new InlineKeyboardButton("Un-Join").callbackData("RemoveButtonCallback"),
				new InlineKeyboardButton("Finish!").callbackData("FinishButtonCallback") });

		EditMessageText editInlineMessageText = new EditMessageText(groupChatId, msgId,
				"<b>Secret Santa Game</b> \n" + "Who's in: " + participantIdNameMap.size() + "\n"
						+ getParticipantsNameString(participantIdNameMap)).parseMode(ParseMode.HTML)
								.disableWebPagePreview(true).replyMarkup(inlineKeyboard);

		BaseResponse sendResponse = bot.execute(editInlineMessageText);
		boolean ok = sendResponse.isOk();
		String message = sendResponse.description();
		// System.out.println("sent msg: " + message);

	}

	public void duplicateParticipant(TelegramBot bot, String chatId, String participantName) {
//		System.out.println("duplicateParticipant");
		String msg = "Invalid Join. " + participantName + " is already in the list";

		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
		// System.out.print("sent msg: " + message);
	}

	public void removeInvalidParticipant(TelegramBot bot, String chatId, String participantName) {
		System.out.println("removeInvalidParticipant");
		String msg = "Invalid Un-Join. " + participantName + " is not in the list.";

		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
		// System.out.print("sent msg: " + message);

	}

	public void errorOccured(TelegramBot bot, String chatId) {
		SendMessage request1 = new SendMessage(chatId, "Command unsuccessful. Error Occured!").parseMode(ParseMode.HTML)
				.disableWebPagePreview(true).disableNotification(true);

		sendMessage(bot, request1);
	}

	public void invalidJoin(TelegramBot bot, String chatId, String groupChatName) {
		String msg = "Invalid Start. Sorry, you can only join using your group's Join button";
		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
	}

	public void insufficientParticipants(TelegramBot bot, String chatId) {
		System.out.println("insufficientParticipants");

		String msg = "Invalid Finish. Needs at least 2 people to play.";
		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
		// System.out.println("sent msg: " + message);
	}

	public void finishButton(TelegramBot bot, String groupChatId, Integer joinMsgId,
			Map<String, String> participantIdNameMap) {
		System.out.println("finishButton");
		System.out.println("joinMsgId:" + joinMsgId);
		SendMessage request1 = new SendMessage(groupChatId,
				"Alright we're ready! \n"
						+ "SecretSantaBot will drop each of you a message on who your Secret Santee is!")
								.parseMode(ParseMode.HTML).disableWebPagePreview(true).disableNotification(true);

		sendMessage(bot, request1);

		System.out.println("remove Inline buttons from joinMsgId:" + joinMsgId);

		// remove inline buttons from the old message
		EditMessageText editInlineMessageText = new EditMessageText(groupChatId, joinMsgId,
				"<b>Secret Santa Game</b> \n" + "Who's in: " + participantIdNameMap.size() + "\n"
						+ getParticipantsNameString(participantIdNameMap)).parseMode(ParseMode.HTML)
								.disableWebPagePreview(true);
		BaseResponse baseResponse = bot.execute(editInlineMessageText);
		System.out.println("sent msg:" + baseResponse.description());

	}

	public void helpCommand(TelegramBot bot, String chatId) {
		System.out.println("helpCommand");

		String helpMsg = "Help is here! \n" + "Use /startgame in a group chat to start a new session! \n"
				+ "Press <b>Join</b> to join the list. SecretSantaBot will start a chat with you \n"
				+ "Press <b>Un-Join</b> to remove yourself from the list \n"
				+ "Only the creator can press <b>Finish</b> \n"
				+ "SecretSantaBot will then message everyone individually to let them know who their Secret Santee is \n"
				+ "\n" + "Merry Christmas! \n";

		SendMessage request1 = new SendMessage(chatId, helpMsg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
		// System.out.print("sent msg: " + message);
	}

	public void replyAllocation(TelegramBot bot, String chatId, String santeeName) {
		System.out.println("replyAllocation - " + santeeName);

		String msg = "Your secret santee is " + santeeName + " :)";

		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
		// System.out.print("sent msg: " + message);
	}

	private String getParticipantsNameString(Map<String, String> participantIdNameMap) {
		String stringOfNames = "";
		for (Object names : participantIdNameMap.values()) {
			stringOfNames += (names + "\n");
		}

		return stringOfNames;
	}

	public void multipleStartGameCommand(TelegramBot bot, String groupChatId, Integer joinMsgId,
			Map<String, String> participantIdNameMap) {


		String msg = "Only one Secret Santa game can be on. The previous session will now be terminated.";

		SendMessage request1 = new SendMessage(groupChatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);


		System.out.println("remove Inline buttons from joinMsgId:" + joinMsgId);

		// remove inline buttons from the old message
		EditMessageText editInlineMessageText = new EditMessageText(groupChatId, joinMsgId,

				"<b>Secret Santa Game - Terminated</b> \n" + "Who's in: " + participantIdNameMap.size() + "\n"

						+ getParticipantsNameString(participantIdNameMap)).parseMode(ParseMode.HTML)
								.disableWebPagePreview(true);
		BaseResponse baseResponse = bot.execute(editInlineMessageText);

	}

	public void invalidFinishButton(TelegramBot bot, String chatId, String creatorName) {

		String msg = "Only the creator of the list " + creatorName + " is able to press Finish.";
		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);

	}

	public void invalidStartGameCommand(TelegramBot bot, String chatId) {
		String msg = "Invalid start game command. Please /startgame only from a group chat.";
		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
	}

	public void invalidStartCommand(TelegramBot bot, String chatId) {
		String msg = "Invalid start command. Please use /startgame when in a group chat.";
		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
	}

	public void invalidCommand(TelegramBot bot, String chatId) {
//		System.out.println("eachParticipantInitCommand");
		String msg = "Invalid command. ";
		SendMessage request1 = new SendMessage(chatId, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true)
				.disableNotification(true);

		sendMessage(bot, request1);
	}

	public void sendMessage(TelegramBot bot, SendMessage request1) {

		SendResponse sendResponse = bot.execute(request1);
		boolean ok = sendResponse.isOk();
		Message message = sendResponse.message();
	}

}
