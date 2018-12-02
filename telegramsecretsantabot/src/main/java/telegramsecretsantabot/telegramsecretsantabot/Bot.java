package telegramsecretsantabot.telegramsecretsantabot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;

public class Bot extends MyBotHandler {
	
	private static String token = System.getenv("ssTOKEN");
	private static final TelegramBot bot = new TelegramBot(token);
	private static UpdateListenerWebhook updateListener = new UpdateListenerWebhook(bot);

	@Override
    void onWebhookUpdate(Update update) {
		if (update != null) {
			updateListener.process(update);
		} else {
			System.out.println("onWebhookUpdate update is null!?!?!??");
		}
        
    }

    @Override
    String getToken() {
        return token;
    }

    @Override
    TelegramBot getBot() {
        return bot;
    }

    
}
