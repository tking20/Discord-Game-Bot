import java.io.Serializable;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Dice extends Game
{

	public void bet(MessageReceivedEvent e)
	{
		MessageChannel c = e.getChannel();
		//Only process this command if it is currently the turn of the player who sent it.
		if(e.getMessage().getAuthor().getName().equals(players.get(pos).getName()))
		{
			String s = e.getMessage().getContentRaw();

			//A bet should always start with an '!', like all commands. It should also contain an 'x' somewhere in the string.
			//A command that does not follow these rules is not syntactically correct, and should be ignored.
			if(s.startsWith("!") && s.contains("x"))
			{
				//Processing the bet begins here.
				s = s.substring(1);
				String s1 = s.split("x")[0];
				String s2 = s.split("x")[1];
				if(!s1.matches("\\d+") || !s2.matches("\\d+") || s1.length() >= 9 || s2.length() != 1)
				{
					c.sendMessage("Invalid bet.").queue();
				}
				else
				{
					int a = Integer.parseInt(s1);
					int b = Integer.parseInt(s2);

					//A bet is invalid if any of the following is true:
					//1. The quantity (referred to as 'strength') is not a positive integer.
					//2. The type is not in the range [1, 6].
					//3. The type is less than the type of the previous bet.
					//4. The type is equal to the previous bet, and the quantity doesn't EXCEED the quantity previously bet.
					if(a <= 0 || b <= 0 || b >= 7 || b < currentType || b == currentType && a <= currentStrength)
					{
						c.sendMessage("Invalid bet.").queue();
					}
					else
					{
						String w = e.getMessage().getAuthor().getAsMention()+" bets "+a+" ";
						w += Commands.numToString(b);

						//Grammar
						if(a != 1)
						{
							if(b == 6)
							{
								w += "es";
							}
							else
							{
								w += "s";
							}
						}
						w += ".";
						c.sendMessage(w).queue();
						pos++;
						pos %= players.size();
						currentStrength = a;
						currentType = b;
						User next = players.get(pos);
						c.sendMessage(next.getAsMention()+", your turn.").queue();
					}
				}
			}

			//Process the command to challenge a bet iff a previous bet was given this round.
			else if(s.equals("!call") && currentStrength != 0 && currentType != 0)
			{
				int count = 0;
				for(int x : rolls)
				{
					if(x == currentType || x == 1)
					{
						count++;
					}
				}
				String w = "There ";
				//Grammar
				if(count == 1)
				{
					w += "was";
				}
				else
				{
					w += "were";
				}
				w += " a total of "+count+" "+Commands.numToString(currentType);
				//Grammar
				if(count != 1)
				{
					if(currentType == 6)
					{
						w += "es";
					}
					else
					{
						w += "s";
					}
				}
				w += ".";
				c.sendMessage(w).queue();

				//Logic for when the challenged bet was incorrect:
				if(count < currentStrength)
				{
					pos--;
					pos += players.size();
					pos %= players.size();
					User old = players.get(pos);
					c.sendMessage(old.getAsMention()+" made a wrong bet and loses a die!").queue();
					remaining.set(pos, remaining.get(pos)-1);
					
					//Check if this eliminates a player:
					if(remaining.get(pos) == 0)
					{
						c.sendMessage(old.getAsMention()+" has been eliminated!").queue();
						remaining.remove(pos);
						players.remove(old);
						pos %= players.size();
						pos += players.size();
						pos %= players.size();
					}
				}

				//Logic for when the challenged bet was correct:
				else
				{
					c.sendMessage("The bet was good! "+players.get(pos).getAsMention()+" loses a die.").queue();
					remaining.set(pos, remaining.get(pos)-1);
					
					//Check if this eliminates a player:
					if(remaining.get(pos) == 0)
					{
						c.sendMessage(players.get(pos).getAsMention()+" has been eliminated!").queue();
						remaining.remove(pos);
						players.remove(pos);
						pos %= players.size();
						pos += players.size();
						pos %= players.size();
					}
				}

				//Check if only one player remains, and end the game, if so.
				if(players.size() == 1)
				{
					c.sendMessage(players.get(0).getAsMention()+" wins the game!").queue();
					Commands.gameRunning = false;
					DiscordBot.jda.getPresence().setActivity(Activity.watching("the lobby"));
					Commands.g = null;
				}

				//Otherwise, reset most variables to setup the next round.
				else
				{
					currentStrength = 0;
					currentType = 0;
					rolls.clear();
					
					if(pos == -1)
					{
						pos = (int)(Math.random()*players.size());
					}
					for(int i = 0; i<players.size(); i++)
					{
						User u = players.get(i);
						String y = "You rolled "+remaining.get(i);
						if(remaining.get(i) == 1)
						{
							y += " die.";
						}
						else
						{
							y += " dice.";
						}
						String s2 = "";
						for(int j = 0; j<remaining.get(i); j++)
						{
							int r = (int)(Math.random()*6+1);
							rolls.add(r);
							s2 += r+" ";
						}
						s2 = s2.replaceAll("1", "**1**").trim();
						y += "\n"+s2;
						String s3 = y;
						u.openPrivateChannel().flatMap(x -> x.sendMessage(s3)).queue();
					}
					String message = "";
					for(int i = 0; i<players.size(); i++)
					{
						User u = players.get(i);
						int r = remaining.get(i);
						if(r > 0)
						{
							String plural = r == 1 ? "die" : "dice";
							message += u.getAsMention()+" has "+r+" "+plural+" remaining.\n";
						}
					}
					for(int i = 0; i<40; i++)
					{
						message += "\\*";
					}
					message += "\n";
					message += players.get(pos).getAsMention()+", place your first bet.";
					e.getGuild().getTextChannelsByName("game-table", true).get(0).sendMessage(message).queue();
				}
			}
		}
	}
}