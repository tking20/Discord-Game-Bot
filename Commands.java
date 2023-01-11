import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter
{
	public static boolean gameOpen = false;
	public static boolean gameRunning = false;
	
	/*
	 *-1=null
	 * 0=no game
	 * 1=dice
	 * 2=poker
	 */
	public static int flag = -1;
	
	
	public static Game g = null;
	
	//Function to convert an integer to a String.
	//Used for displaying game information.
	public static String numToString(int n)
	{
		String s = "";
		switch(n)
		{
		case 2: s += "two"; break;
		case 3: s += "three"; break;
		case 4: s += "four"; break;
		case 5: s += "five"; break;
		case 6: s += "six"; break;
		case 7: s += "seven"; break;
		case 8: s += "eight"; break;
		case 9: s += "nine"; break;
		case 10: s += "ten"; break;
		case 11: s += "jack"; break;
		case 12: s += "queen"; break;
		case 13: s += "king"; break;
		case 14: s += "ace"; break;
		}
		return s;
	}
	
	public void onMessageReceived(MessageReceivedEvent e)
	{
		//Do not attempt to interact with a message that was sent by the bot.
		//Otherwise, there is risk of infinite bot spam.
		if(e.getMessage().getAuthor().isBot())
		{
			return;
		}
		
		MessageChannel c = e.getChannel();
		if(c.getName().equals("game-table") && gameRunning)
		{
			g.bet(e);
		}
		else
		{
			//Parse message into words
			String[] words = e.getMessage().getContentRaw().split("\\s+");
			
			//The bot only recognizes a command when it is the first word of a message. Parse the first word in the following 'switch' statement.
			switch(words[0])
			{
			//Displays all the commands the bot is capable of receiving.
			case "!help": c.sendMessage("The bot can receive the following commands:\n`!help`   - Displays this menu\n`!createGameD`   - Creates a new game of Liar's Dice\n`!createGameP`   - Creates a new game of Liar's Poker\n`!join`   - Join an open game\n`!start`   - Begins a new game (host only)").queue(); break;
			
			//Legacy command to start a new game. Updated commands are '!createGameD' and '!createGameP'.
			//If a game is already running, notify the user instead.
			case "!createGame":
				if(!gameOpen && !gameRunning)
				{
					c.sendMessage("To play Liar's Dice, type `!createGameD`\nTo play Liar's Poker, type `!createGameP`").queue();
				}
				else
				{
					 c.sendMessage("A game has already been created that has not yet closed.").queue();
				} break;
				//Command to start a new game of Liar's Dice.
			//If a game is already running, notify the user instead.
			case "!createGameD":
				if(!gameOpen && !gameRunning)
				{
					flag = 1;
					makeGame(e);
				}
				else
				{
					 c.sendMessage("A game has already been created that has not yet closed.").queue();
				} break;
				//Command to start a new game of Liar's Poker.
			//If a game is already running, notify the user instead.
			case "!createGameP":
				if(!gameOpen && !gameRunning)
				{
					flag = 2;
					makeGame(e);
				}
				else
				{
					 c.sendMessage("A game has already been created that has not yet closed.").queue();
				} break;
			
			//Command to join an open game.
			//Notify the user if:
			//1. A game is not yet open
			//2. An open game is already at full capacity
			//3. They have already joined the game.	
			case "!join":
				if(!gameOpen)
				{
					 c.sendMessage("There is no open game.").queue();
				}
				else
				{
					//Max # of players is currently set to 8.
					//This is free to be changed, but is currently in place to prevent possible crashes from massive games.
					if(g.players.size() >= 8)
					{
						c.sendMessage("Sorry, the current game is full.").queue();
					}
					else if(g.players.contains(e.getMessage().getAuthor()))
					{
						c.sendMessage(e.getMessage().getAuthor().getAsMention()+", you have already joined this game.").queue();
					}
					else
					{
						g.players.add(e.getMessage().getAuthor());
						g.remaining.add(5);
						c.sendMessage(e.getMessage().getAuthor().getAsMention()+" has been added to the game.").queue();
					}
				} break;
			
			//Command to begin the open game.
			//Notify the user if a game hasn't been opened, or if they are not the host (person who opened the game).
			case "!start":
				if(!gameOpen)
				{
					c.sendMessage("There is no open game. Make a new game with `!createGame`").queue();
				}
				else if(!e.getMessage().getAuthor().equals(g.players.get(0)))
				{
					c.sendMessage("Only the host can start a game.").queue();
				}
				else
				{
					c.sendMessage("Game beginning.").queue();
					gameOpen = false;
					gameRunning = true;
					
					String playing = switch(flag)
					{
					case 1 -> "Liar's Dice";
					case 2 -> "Liar's Poker";
					default -> "something";
					};
					DiscordBot.jda.getPresence().setActivity(Activity.playing(playing));
					
					/*Game setup starts here.
					 *
					 *The current bet is set to zero (as there has not yet been any betting),
					 *the total dice rolls are reset (as no dice have been rolled yet),
					 *the first player to make a bet is chosen at random,
					 *and all players are randomly rolled their dice.
					 */
					g.currentStrength = 0;
					g.currentType = 0;
					g.rolls.clear();
					g.pos = (int)(Math.random()*g.players.size());
					for(int i = 0; i<g.players.size(); i++)
					{
						User u = g.players.get(i);
						String s = "You";
						if(g instanceof Dice)
						{
							s += " rolled ";
							s += g.remaining.get(i);
							if(g.remaining.get(i) == 1)
							{
								s += " die.";
							}
							else
							{
								s += " dice.";
							}
							String s2 = "";
							for(int j = 0; j<g.remaining.get(i); j++)
							{
								int r = (int)(Math.random()*6+1);
								g.rolls.add(r);
								s2 += r+" ";
							}
							s2 = s2.replaceAll("1", "**1**").trim();
							s += "\n"+s2;
							String s3 = s;
							u.openPrivateChannel().flatMap(x -> x.sendMessage(s3)).queue();
						}
						else
						{
							s += " drew ";
							s += g.remaining.get(i);
							if(g.remaining.get(i) == 1)
							{
								s += " card.";
							}
							else
							{
								s += " cards.";
							}
							BufferedImage cards = new BufferedImage(222*g.remaining.get(i), 323, BufferedImage.TYPE_INT_RGB);
							Graphics gr = cards.getGraphics();
							for(int j = 0; j<g.remaining.get(i); j++)
							{
								int deck = ((Poker)g).allCards.size();
								int r = (int)(Math.random()*deck);
								int num = ((Poker)g).allCards.remove(r);
								g.rolls.add(num);
								BufferedImage card = ImageIO.read(((Poker)g).photos.get(num));
								gr.drawImage(card, 222*j, 0, null);
							}
							
							EmbedBuilder eb = new EmbedBuilder();
							eb.setImage("attachment://Hand.jpg");
							File f = new File("cards.jpg");
							ImageIO.write(cards, "jpg", f);
							String s3 = s;
							eb.build();
							u.openPrivateChannel().flatMap(x -> x.sendMessage(s3).addFile(f, "Hand.jpg")).queue();
						}
					}
					TextChannel table = e.getGuild().getTextChannelsByName("game-table", true).get(0);
					table.sendMessage("The game has begun.\n"+g.players.get(g.pos).getAsMention()+", place your first bet.").queue();
				} break;
		}
	}
	
	//Displays information about the start of a game in the server 
	public void makeGame(MessageReceivedEvent e) throws InterruptedException
	{
		gameOpen = true;
		MessageChannel c = e.getChannel();
		
		c.sendTyping().queue();
		String message = "";
		message += "**New Game Created by:**\n";
		message += e.getMessage().getAuthor().getAsMention()+"\n";
		c.sendMessage(message).queue();

		Thread.sleep(500);

		message += "Type `!join` to join this game.\n";
		message += "The game will begin when the host types `!start`\n";
		c.sendMessage(message).queue();
		
		switch(flag)
		{
		case 1: g = new Dice(); break;
		case 2: g = new Poker(); break;
		default: System.out.println("flag not set"); g = null; break;
		}
		
		g.players.add(e.getMessage().getAuthor());
		g.remaining.add(5);
	}
}
