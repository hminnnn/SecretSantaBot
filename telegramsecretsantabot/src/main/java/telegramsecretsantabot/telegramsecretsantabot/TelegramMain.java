package telegramsecretsantabot.telegramsecretsantabot;

import static spark.Spark.port;
import static spark.Spark.post;

import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;

public class TelegramMain {

	// telegram token
//	private static String token = System.getenv("ssTOKEN");

//	public static void main(String[] args) {
//		TelegramBot bot = new TelegramBot(token);
//
//		// herokuapp url
//		final String appUrl = System.getenv("APP_URL");
//		final String portNumber = System.getenv("PORT");
//		System.out.println("port:" + portNumber);
//		System.out.println("appUrl:" + appUrl);
//		//System.out.println("token:" + token);
//
//		if (portNumber != null) {
//			System.out.println("Set port");
//			port(Integer.parseInt(portNumber));
//		}
//
//		System.out.println("Not using webhooks.");
//		
//		//SetWebhook request = new SetWebhook().url(appUrl + token + ":" + portNumber); // byte[]
//		// BaseResponse response = bot.execute(request);
//		//boolean ok = response.isOk();
//		//System.out.println(response.description());
//		// System.out.println(ok);
//		System.out.println("Bot ready");
//		
//		bot.setUpdatesListener(new UpdateListener(bot));
//
//	}

	// use webhook
	public static void main(String[] args) {
		// TelegramBot bot = new TelegramBot(token);

		// herokuapp url
		final String appUrl = System.getenv("APP_URL");
		final String portNumber = System.getenv("PORT");
		System.out.println("port:" + portNumber);
		System.out.println("appUrl:" + appUrl);
		// System.out.println("token:" + token);

		if (portNumber != null) {
			port(Integer.parseInt(portNumber));
		}

		System.out.println("Using webhooks.");

		MyBotHandler[] myBots = new MyBotHandler[] { new Bot() };

		for (MyBotHandler myBot : myBots) {
			String token = myBot.getToken();
			//System.out.println("token:" + token);
			post("/" + token, myBot);

			if (appUrl != null) {
				BaseResponse response = myBot.getBot().execute(new SetWebhook().url(appUrl + "/" + token));
				boolean ok = response.isOk();
				System.out.println(response.description());
				System.out.println(ok);
			}

		}

		System.out.println("Bot ready");

	}

}
