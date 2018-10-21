package telegramsecretsantabot.telegramsecretsantabot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;

public class TelegramMain {

	//telegram token
	private static String token = System.getenv("ssTOKEN");

	public static void main(String[] args) {
		TelegramBot bot = new TelegramBot(token);

		// herokuapp url
		final String appUrl = System.getenv("APP_URL");
		final String portNumber = System.getenv("PORT");
		System.out.println("port:" + portNumber);
		System.out.println("appUrl:" + appUrl);
		System.out.println("appUrl + token:"+ appUrl + token);
		System.out.println("all:" + appUrl + token + ":" + portNumber);

		SetWebhook request = new SetWebhook().url(appUrl + token + ":" + portNumber).certificate(new byte[] {}); // byte[]
		BaseResponse response = bot.execute(request);
		boolean ok = response.isOk();
		System.out.println(response.description());
		System.out.println(ok);
		System.out.println("Bot ready");

		bot.setUpdatesListener(new UpdateListener(bot));

	}

}
