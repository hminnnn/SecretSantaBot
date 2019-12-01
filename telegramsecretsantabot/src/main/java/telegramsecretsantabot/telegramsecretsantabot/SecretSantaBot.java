package telegramsecretsantabot.telegramsecretsantabot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

public class SecretSantaBot {
	private TelegramBot bot;
	private Update update;

	private String startKey = "123456789asdfghjkl";
	private String groupChatId;
	private String groupChatName;
	private String individualChatId;
	private String chatId;

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
		
		System.out.println("---- New message in SecretSantaBot ---- ");
//		joinMsgId = replyMsg.createJoinMainMessage(bot, groupChatId, participantIdNameMap);
//		groupChatName = update.message().chat().title();
//		
//		// get person who created list. only this person can press Finish.
//		System.out.println("find person who created list id:" + personCreatedId);
//		User participant = update.message().from();
//		personCreatedId = getParticipantId(participant);
//		
//		System.out.println("joinMsgId:" + joinMsgId);
//		System.out.println("groupChatId:" + groupChatId);
	}

	public void update(Update upd) {
		Message message = upd.message();
		CallbackQuery callbackQ = upd.callbackQuery();
		System.out.println("---- New message in SecretSantaBot ---- ");
		if (message != null) {
			processMessage(message);
		}
		if (callbackQ != null) {
			processCallbackQuery(callbackQ);
		}

	}

	private void processCallbackQuery(CallbackQuery callbackQ) {
		System.out.println("callbackQ:" + callbackQ);
		Message callbackMsg = callbackQ.message();
		String messageChatType = callbackMsg.chat().type().toString();
		String callbackQData = callbackQ.data();
		String chatId = callbackMsg.chat().id().toString();

		// get chat ids
		if (messageChatType.equalsIgnoreCase("group") || messageChatType.equalsIgnoreCase("supergroup")) {
			groupChatId = chatId;
		} else if (messageChatType.equalsIgnoreCase("private")) {
			individualChatId = chatId;
		}
		System.out.println("groupChatId:" + groupChatId);
		System.out.println("individualChatId:" + individualChatId);

		// get participant details
		User participant = callbackQ.from();
		String participantName = getParticipantName(participant);
		System.out.println("participant:" + participant);
		String participantUserId = getParticipantId(participant);

		switch (callbackQData) {
		case "RemoveButtonCallback":
			if (participantIdNameMap.containsKey(participantUserId)) {
				participantIdNameMap.remove(participantUserId);
				replyMsg.editJoinMainMessage(bot, groupChatId, joinMsgId, participantIdNameMap);
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
					startSecretSantaAllocation();
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
		chatId = message.chat().id().toString();
		int reply = 0;

		// get chat ids
		if (isFromGroupChat(messageChatType)) {
			groupChatId = chatId;
		} else if (isPrivateChat(messageChatType)) {
			individualChatId = chatId;
		}
		System.out.println("groupChatId:" + groupChatId);
		System.out.println("individualChatId:" + individualChatId);

		if (isHelpCommand(msgText)) {
			replyMsg.helpCommand(bot, chatId);
		}
		
		if (isStartCommand(msgText)) {
			// Can only /start from a private chat
			if (isFromGroupChat(messageChatType)) {
				replyMsg.invalidStartCommand(bot, chatId);
			} else {
				
				// get participant details
				User participant = message.from();
				String participantName = getParticipantName(participant);
				String participantUserId = getParticipantId(participant);

				if (participantIdNameMap.get(participantUserId) != null) {
					System.out.println("dupe participant");
					replyMsg.duplicateParticipant(bot, individualChatId, participantName);
				} else {
					System.out.println("add participant");
					participantIdNameMap.put(participantUserId, participantName);
					userChatIdMap.put(participantUserId, individualChatId);
					replyMsg.eachParticipantInitCommand(bot, individualChatId, groupChatName);

					System.out.println("joinMsgId:" + joinMsgId);
					replyMsg.editJoinMainMessage(bot, groupChatId, joinMsgId, participantIdNameMap);
				}
			}
			
			
		}
		
		if(isStartGameCommand(msgText)) {
			
			if (isFromGroupChat(messageChatType)) {
				joinMsgId = replyMsg.createJoinMainMessage(bot, groupChatId, participantIdNameMap);
				groupChatName = update.message().chat().title();
				
				// get person who created list. only this person can press Finish.
				System.out.println("find person who created list id:" + personCreatedId);
				User participant = update.message().from();
				personCreatedId = getParticipantId(participant);
			} else {
				replyMsg.invalidStartGameCommand(bot, chatId);
			}
			
		}
		
		if (isInvalidStartCommand(msgText)) {
			replyMsg.invalidJoin(bot, individualChatId, groupChatName);
		}
		
		if (isStartGameCommand(msgText)) {
			System.out.println("joinMsgId to remove:" + joinMsgId);
			replyMsg.multipleStartGameCommand(bot, groupChatId, joinMsgId, participantIdNameMap);
		}
		
	}

	private boolean isHelpCommand(String command) {
		if (command.contains("/help")) {
			System.out.println("/help");
			return true;
		}
		return false;
	}

	private boolean isStartCommand(String command) {
		if (command.equals(("/start ") + startKey)) {
			return true;
		}
		return false;
	}
	
	private boolean isInvalidStartCommand(String command) {
		if (command.equals(("/start"))) {
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
	
	private boolean isPrivateChat(String messageChatType) {
		return messageChatType.equalsIgnoreCase("private");
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
			participantName = participant.id().toString() + "has no name.";
		}
		return participantName;
	}

	private String getParticipantId(User participant) {
		return participant.id().toString();
	}

	private void startSecretSantaAllocation() {
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
			replyMsg.errorOccured(bot, chatId);
		}
	}

	private Map<String, String> mapResultToUserId(int[][] mappingMatrix, ArrayList<String> participantIdList) {
		Map<String, String> santaAllocationResult = new HashMap<String, String>();
		System.out.println("-----mapResultToUserId----");
		for (int i = 0; i < participantIdNameMap.size(); i++) {
			for (int j = 0; j < participantIdNameMap.size(); j++) {
				if (mappingMatrix[i][j] == 2) {
					santaAllocationResult.put(participantIdList.get(i), participantIdList.get(j));
					System.out.println(participantIdList.get(i) + "gives" + participantIdList.get(j));
				}
			}
		}
		return santaAllocationResult;
	}

	private void replyAllocation(Map<String, String> santaAllocationResult, Map<String, String> userChatIdMap) {
		System.out.println("-----replyAllocation----");
		for (Map.Entry<String,String> item :santaAllocationResult.entrySet()) {
			String userId = item.getKey();
			String santeeId = item.getValue();
			System.out.println("userId:" + userId);
			
			// get user's chat id
			String chatId = userChatIdMap.get(userId);
			System.out.println("chatId:" + chatId);
			
			// get name of santee user is giving to
			String santeeName = participantIdNameMap.get(santeeId);
			System.out.println("santeeName:" + santeeName);
			
			replyMsg.replyAllocation(bot, chatId,santeeName);
		}
	}

}
