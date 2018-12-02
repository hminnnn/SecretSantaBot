package telegramsecretsantabot.telegramsecretsantabot;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;

import spark.Request;
import spark.Response;
import spark.Route;

public abstract class MyBotHandler implements Route {

	@Override
	public Object handle(Request request, Response response) throws Exception {
		Update update = BotUtils.parseUpdate(request.body());
		onWebhookUpdate(update);

		return "ok";
	}

	abstract void onWebhookUpdate(Update update);

    abstract String getToken();

    abstract TelegramBot getBot();

}
