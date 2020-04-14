package telegramsecretsantabot.telegramsecretsantabot;

import static spark.Spark.port;
import static spark.Spark.post;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;

public class TelegramMain {
	private static final Logger logger = LogManager.getLogger(TelegramMain.class);

	// using webhook
	public static void main(String[] args) {

		BasicConfigurator.configure();
		
		// herokuapp url
		final String appUrl = System.getenv("APP_URL");
		final String portNumber = System.getenv("PORT");
		logger.info("port:" + portNumber);
		logger.info("appUrl:" + appUrl);
		System.out.println("port:" + portNumber);

		if (portNumber != null) {
			port(Integer.parseInt(portNumber));
		}

		logger.info("Using webhooks.");

		MyBotHandler myBotHandler = new Bot();
		String token = myBotHandler.getToken();
		post("/" + token, myBotHandler);
		if (appUrl != null) {
			BaseResponse response = myBotHandler.getBot().execute(new SetWebhook().url(appUrl + "/" + token));
			boolean ok = response.isOk();
			System.out.println("Webhook: " + response.description());
			System.out.println(ok);
			System.out.println("Bot ready");
		} else {
			System.out.println("appUrl is null, Bot not ready");
		}


	}

}
