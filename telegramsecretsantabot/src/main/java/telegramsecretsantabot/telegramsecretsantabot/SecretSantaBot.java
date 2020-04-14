package telegramsecretsantabot.telegramsecretsantabot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

public class SecretSantaBot {

	private static final Logger logger = LogManager.getLogger(SecretSantaBot.class);

	private TelegramBot bot;
	private Update update;

	private String startKey = "123456789asdfghjkl";
	private String groupChatId;
	private String groupChatName;
	private String individualChatId;

	private String personCreatedId;
	private Integer joinMsgId;

	private ReplyMessages replyMsg = new ReplyMessages();

	// <participant user Id, participant chat id >
	private Map<String, String> userChatIdMap = new HashMap<String, String>();
	private Map<String, String> participantIdNameMap = new HashMap<String, String>();

	public SecretSantaBot(TelegramBot bot, Update update, String chatId) {
		this.bot = bot;
		this.update = update;
		groupChatId = chatId;
		participantIdNameMap = new HashMap<String, String>();
		userChatIdMap = new HashMap<String, String>();
	}

	public void update(Update upd) {
		Message message = upd.message();
		CallbackQuery callbackQ = upd.callbackQuery();
		if (message != null) {
			logger.info("---- Message is message ---- ");
			processMessage(message);
		}
		if (callbackQ != null) {
			logger.info("---- Message is callback ---- ");
			processCallbackQuery(callbackQ);
		}
	}

	/*
	 * Callback buttons - should only be from group chat
	 */
	private void processCallbackQuery(CallbackQuery callbackQ) {
		logger.info("callbackQ: " + callbackQ);
		
		Message callbackMsg = callbackQ.message();
		String messageChatType = callbackMsg.chat().type().toString();
		String callbackQData = callbackQ.data();
		String chatId = callbackMsg.chat().id().toString();
		
		boolean isGroupChat = isGroupChat(messageChatType);

		/*
		 * get chat ids
		 */
		if (isGroupChat) {
			groupChatId = chatId;
			logger.info("groupChatId:" + groupChatId);
		} else {
			individualChatId = chatId;
			logger.info("individualChatId:" + individualChatId);
		}

		// get participant details
		User participant = callbackQ.from();
		String participantName = getParticipantName(participant);
		String participantUserId = getParticipantId(participant);
		logger.info("participant:" + participant);

		switch (callbackQData) {
		case "RemoveButtonCallback":
			if (participantIdNameMap.containsKey(participantUserId)) {
				participantIdNameMap.remove(participantUserId);
				replyMsg.editJoinMainMessage(bot, groupChatId, joinMsgId, participantIdNameMap, groupChatId);

			} else {
				replyMsg.removeInvalidParticipant(bot, groupChatId, participantName);
			}
			break;

		case "FinishButtonCallback":
			if (participantIdNameMap.isEmpty() || participantIdNameMap.size() < 2) {
				replyMsg.insufficientParticipants(bot, groupChatId);
			} else {
				if (participantUserId.equals(personCreatedId)) { // only creator can press finish
					replyMsg.finishButton(bot, groupChatId, joinMsgId, participantIdNameMap);
					startSecretSantaAllocation(groupChatId);
				} else {
					String creatorName = participantIdNameMap.get(personCreatedId);
					replyMsg.invalidFinishButton(bot, chatId, creatorName);
				}
			}
			break;
		}
	}

	private void processMessage(Message message) {

		String messageChatType = message.chat().type().toString();
		String msgText = message.text();
		String chatId = message.chat().id().toString();

		boolean isGroupChat = isGroupChat(messageChatType);

		/*
		 * get chat ids
		 */
		if (isGroupChat) {
			groupChatId = chatId;
			logger.info("groupChatId:" + groupChatId);
		} else {
			individualChatId = chatId;
			logger.info("individualChatId:" + individualChatId);
		}
		/*
		 * Help command
		 */
		if (isHelpCommand(msgText)) {
			replyMsg.helpCommand(bot, chatId);
			return;
		}

		/*
		 * Start command - /start can only come from a private chat that was direct from
		 * a group chat, /start + groupchatid
		 */

		if (isStartCommand(msgText)) {
			replyMsg.invalidStartCommand(bot, groupChatId);
		}

		if (isStartCommand(msgText, groupChatId)) {
			if (isGroupChat) {
				replyMsg.invalidStartCommand(bot, groupChatId);
				return;
			}

			// get participant details
			User participant = message.from();
			String participantName = getParticipantName(participant);
			String participantUserId = getParticipantId(participant);

			if (participantIdNameMap.containsKey(participantUserId)) {
				replyMsg.duplicateParticipant(bot, individualChatId, participantName, groupChatName);
			} else {
				participantIdNameMap.put(participantUserId, participantName);
				userChatIdMap.put(participantUserId, individualChatId);
				replyMsg.eachParticipantInitCommand(bot, individualChatId, groupChatName);

				logger.info("joinMsgId:" + joinMsgId);
				replyMsg.editJoinMainMessage(bot, groupChatId, joinMsgId, participantIdNameMap, groupChatId);
			}

			return;
		}

		/*
		 * Start game command - /startgame can only come from groupchats
		 */
		if (isStartGameCommand(msgText)) {
			if (!isGroupChat) {
				replyMsg.invalidStartGameCommand(bot, chatId);
				return;
			}

			if (joinMsgId != null) {
				logger.info("joinMsgId to remove:" + joinMsgId);
				replyMsg.multipleStartGameCommand(bot, groupChatId, joinMsgId, participantIdNameMap);
				joinMsgId = null;
			}
			String key = groupChatId;
			participantIdNameMap = new HashMap<String, String>();
			joinMsgId = replyMsg.createJoinMainMessage(bot, groupChatId, participantIdNameMap, key);
			groupChatName = update.message().chat().title();

			// get person who created list. only this person can press Finish.
			User participant = update.message().from();
			personCreatedId = getParticipantId(participant);
			logger.info("find person who created list id:" + personCreatedId);
			return;
		}
		replyMsg.invalidCommand(bot, chatId);

	}

	private boolean isHelpCommand(String command) {
		if (command.contains("/help")) {
			logger.info("/help");
			return true;
		}
		return false;
	}

	private boolean isStartCommand(String command, String groupChatId) {
		if (command.equals(("/start ") + groupChatId)) {
			return true;
		}
		return false;
	}

	private boolean isStartCommand(String command) {
		if (command.equals(("/start"))) {
			return true;
		}
		return false;
	}

	private boolean isStartGameCommand(String command) {
		if (command.contains("/startgame")) {
			logger.info("/startgame");
			return true;
		}
		return false;
	}

	private boolean isGroupChat(String messageChatType) {
		if (messageChatType.equalsIgnoreCase("group") || messageChatType.equalsIgnoreCase("supergroup")) {
			return true;
		}
//		else if (messageChatType.equalsIgnoreCase("private")) {
//			return "private";
//		}
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
			participantName = participant.id().toString() + "has no name.";
		}
		return participantName;
	}

	private String getParticipantId(User participant) {
		return participant.id().toString();
	}

	private void startSecretSantaAllocation(String groupChatId) {
		ArrayList<String> participantIdList = new ArrayList<String>();
		for (String id : participantIdNameMap.keySet()) {
			participantIdList.add(id);
		}
		SecretSantaAllocation ssAllocation = new SecretSantaAllocation(participantIdNameMap.size());

		int[][] mappingMatrix;
		if (participantIdList.size() > 1) {
			mappingMatrix = ssAllocation.allocateSecretSanta(participantIdNameMap.size());
			Map<String, String> santaAllocationResult = mapResultToUserId(mappingMatrix, participantIdList);
			replyAllocation(santaAllocationResult, userChatIdMap);

		} else {
			replyMsg.errorOccured(bot, groupChatId);
		}
	}

	private Map<String, String> mapResultToUserId(int[][] mappingMatrix, ArrayList<String> participantIdList) {
		Map<String, String> santaAllocationResult = new HashMap<String, String>();
		logger.info("-----mapResultToUserId----");
		for (int i = 0; i < participantIdNameMap.size(); i++) {
			for (int j = 0; j < participantIdNameMap.size(); j++) {
				if (mappingMatrix[i][j] == 2) {
					santaAllocationResult.put(participantIdList.get(i), participantIdList.get(j));
					logger.info(participantIdList.get(i) + "gives" + participantIdList.get(j));
				}
			}
		}
		return santaAllocationResult;
	}

	private void replyAllocation(Map<String, String> santaAllocationResult, Map<String, String> userChatIdMap) {
		logger.info("-----replyAllocation----");
		for (Map.Entry<String, String> item : santaAllocationResult.entrySet()) {
			String userId = item.getKey();
			String santeeId = item.getValue();
			logger.info("userId:" + userId);

			// get user's chat id
			String chatId = userChatIdMap.get(userId);
			logger.info("chatId:" + chatId);

			// get name of santee user is giving to
			String santeeName = participantIdNameMap.get(santeeId);
			logger.info("santeeName:" + santeeName);

			replyMsg.replyAllocation(bot, chatId, santeeName);
		}
	}

}
