//Main runner class for the Discord Bot.

import java.io.IOException;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot
{
	//The JDA object is made static so it can be accessed from the Commands class.
	//This is done so I can change the status of the bot ("playing" / "watching") globally.
	public static JDA jda;

	public static void main(String[] args) throws LoginException, IOException
	{
		/*Set the variable 'token' with the Bot ID supplied by Discord.
		 *For obvious reasons, the token for the bot I use in my server has been removed from this public repo.
		 */

		String token = "";
		jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS).build();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);
		jda.getPresence().setActivity(Activity.watching("the lobby"));
		
		jda.addEventListener(new Commands());
	}
}