import java.util.ArrayList;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Game
{
	ArrayList<User> players = new ArrayList<User>();
	ArrayList<Integer> remaining = new ArrayList<Integer>();

	int pos = -1;
	int currentStrength = 0;
	int currentType = 0;
	ArrayList<Integer> rolls = new ArrayList<Integer>();
	
	public void bet(MessageReceivedEvent e)
	{
		
	}
}