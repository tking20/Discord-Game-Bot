import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Poker extends Game
{
	
	ArrayList<File> photos = new ArrayList<File>();
	ArrayList<Integer> allCards = new ArrayList<Integer>();
	ArrayList<Integer> copy = new ArrayList<Integer>();
	String SFSuit = "";
	
	/*
	 * 0=club
	 * 1=diamond
	 * 2=heart
	 * 3=spade
	 */
	int suitFlag = -1;
	String suits = "clubs diamonds hearts spades";
	
	public Poker()
	{
		allCards.clear();
		copy.clear();
		for(int i = 0; i<52; i++)
		{
			photos.add(new File("Cards").listFiles()[i]);
			System.out.println(i+" "+photos.get(i).getName());
			allCards.add(i);
			copy.add(i);
		}
	}
	
	/*
	 * 0=High Card
	 * 1=Pair
	 * 2=Two Pair
	 * 3=Three of a Kind
	 * 4=Straight
	 * 5=Full House
	 * 6=Four of a Kind
	 * 7=Straight Flush
	 * 8=?
	 */
	public void bet(MessageReceivedEvent e)
	{
		try
		{
			MessageChannel c = e.getChannel();
			if(e.getMessage().getAuthor().getName().equals(players.get(pos).getName()))
			{
				String s = e.getMessage().getContentRaw().toLowerCase().trim();
				if(!s.equals("!call") && s.startsWith("!"))
				{
					s = s.substring(1).trim();
					int a = -1;
					int b = -1;
					String w = "";
					
					//Determines if bet is valid and we can move on to next player.
					boolean q = true;
					
					if(s.matches("[jqka]") || s.matches("[2-9]") || s.equals("10"))
					{
						b = 0;
						switch(s)
						{
						case "j": a = 11; break;
						case "q": a = 12; break;
						case "k": a = 13; break;
						case "a": a = 14; break;
						default: a = Integer.parseInt(s); break;
						}
						if(b < currentType || b == currentType && a <= currentStrength)
						{
							c.sendMessage("Invalid bet.").queue();
							q = false;
						}
						else
						{
							w = e.getMessage().getAuthor().getAsMention()+" bets a";
							if(a == 8 || a == 14)
							{
								w += "n";
							}
							w += " ";
							w += Commands.numToString(a);
							w += ".";
						}
					}
					else if(s.startsWith("2x"))
					{
						s = s.substring(2);
						if(s.contains(" "))
						{
							b = 2;
							String[] arr = s.split("\\s+");
							swapIfNeeded(arr);
							switch(arr[0])
							{
							case "j": a = 11; break;
							case "q": a = 12; break;
							case "k": a = 13; break;
							case "a": a = 14; break;
							default: a = Integer.parseInt(arr[0]); break;
							}
							a *= 100;
							switch(arr[1])
							{
							case "j": a += 11; break;
							case "q": a += 12; break;
							case "k": a += 13; break;
							case "a": a += 14; break;
							default: a += Integer.parseInt(arr[1]); break;
							}

							//An example of an invalid bet of "Two Pairs" is when both pairs are of the same type (2 x Queens and 2 x Queens).
							//If such a bet is made, 'a' will be equal to 100*x + x, where 'x' is the type that was bet.
							//If a % 101 == 0, then both numbers are the same, pairs of queens and queens is illegal
							if(b < currentType || b == currentType && a <= currentStrength || a % 101 == 0)
							{
								c.sendMessage("Invalid bet.").queue();
								q = false;
							}
							else
							{
								w = e.getMessage().getAuthor().getAsMention()+" bets two pairs, ";
								w += Commands.numToString(a/100);
								//Grammar
								if(a/100 == 6)
								{
									w += "e";
								}
								w += "s over ";
								w += Commands.numToString(a%100);
								if(a%100 == 6)
								{
									w += "e";
								}
								w += "s.";
							}
						}
						else
						{
							b = 1;
							switch(s)
							{
							case "j": a = 11; break;
							case "q": a = 12; break;
							case "k": a = 13; break;
							case "a": a = 14; break;
							default: a = Integer.parseInt(s); break;
							}
							if(b < currentType || b == currentType && a <= currentStrength)
							{
								c.sendMessage("Invalid bet.").queue();
								q = false;
							}
							else
							{
								w = e.getMessage().getAuthor().getAsMention()+" bets a pair of ";
								w += Commands.numToString(a);
								//Grammar
								if(a == 6)
								{
									w += "e";
								}
								w += "s.";
							}
						}
					}
					else if(s.startsWith("3x"))
					{
						s = s.substring(2);
						b = 3;
						switch(s)
						{
						case "j": a = 11; break;
						case "q": a = 12; break;
						case "k": a = 13; break;
						case "a": a = 14; break;
						default: a = Integer.parseInt(s); break;
						}
						if(b < currentType || b == currentType && a <= currentStrength)
						{
							c.sendMessage("Invalid bet.").queue();
							q = false;
						}
						else
						{
							w = e.getMessage().getAuthor().getAsMention()+" bets three ";
							w += Commands.numToString(a);
							//Grammar
							if(a == 6)
							{
								w += "e";
							}
							w += "s.";
						}
					}
					else if(s.startsWith("straight "))
					{
						s = s.substring(9);
						b = 4;
						switch(s)
						{
						case "j": a = 11; break;
						case "q": a = 12; break;
						case "k": a = 13; break;
						case "a": a = 14; break;
						default: a = Integer.parseInt(s); break;
						}
						if(b < currentType || b == currentType && a <= currentStrength || a < 5)
						{
							c.sendMessage("Invalid bet.").queue();
							q = false;
						}
						else
						{
							w = e.getMessage().getAuthor().getAsMention()+" bets a straight, ";
							w += Commands.numToString(a);
							w += " high.";
						}
					}
					else if(s.startsWith("fullhouse "))
					{
						s = s.substring(10);
						b = 5;
						String[] arr = s.split("\\s+");
						switch(arr[0])
						{
						case "j": a = 11; break;
						case "q": a = 12; break;
						case "k": a = 13; break;
						case "a": a = 14; break;
						default: a = Integer.parseInt(arr[0]); break;
						}
						a *= 100;
						switch(arr[1])
						{
						case "j": a += 11; break;
						case "q": a += 12; break;
						case "k": a += 13; break;
						case "a": a += 14; break;
						default: a += Integer.parseInt(arr[1]); break;
						}
						//An example of an invalid bet of "Full House" is when both the pair and the 3-of-a-kind are of the same type (2 x Queens and 3 x Queens).
						//If such a bet is made, 'a' will be equal to 100*x + x, where 'x' is the type that was bet.
						//If a % 101 == 0, then both numbers are the same, 2 x queens and 3 x queens is illegal
						if(b < currentType || b == currentType && a <= currentStrength || a % 101 == 0)
						{
							c.sendMessage("Invalid bet.").queue();
							q = false;
						}
						else
						{
							w = e.getMessage().getAuthor().getAsMention()+" bets a full house, ";
							w += Commands.numToString(a/100);

							//Grammar
							if(a/100 == 6)
							{
								w += "e";
							}
							w += "s over ";
							w += Commands.numToString(a%100);
							if(a%100 == 6)
							{
								w += "e";
							}
							w += "s.";
						}
					}
					else if(s.startsWith("4x"))
					{
						s = s.substring(2);
						b = 6;
						switch(s)
						{
						case "j": a = 11; break;
						case "q": a = 12; break;
						case "k": a = 13; break;
						case "a": a = 14; break;
						default: a = Integer.parseInt(s); break;
						}
						if(b < currentType || b == currentType && a <= currentStrength)
						{
							c.sendMessage("Invalid bet.").queue();
							q = false;
						}
						else
						{
							w = e.getMessage().getAuthor().getAsMention()+" bets four ";
							w += Commands.numToString(a);
							if(a == 6)
							{
								w += "e";
							}
							w += "s.";
						}
					}
					else if(s.startsWith("straightflush "))
					{
						s = s.substring(14);
						b = 7;
						String[] arr = s.split("\\s+");
						if(arr.length < 2)
						{
							c.sendMessage("Invalid bet.").queue();
							q = false;
						}
						SFSuit = arr[1];
						switch(arr[0])
						{
						case "j": a = 11; break;
						case "q": a = 12; break;
						case "k": a = 13; break;
						case "a": a = 14; break;
						default: a = Integer.parseInt(arr[0]); break;
						}
						if(b < currentType || b == currentType && a <= currentStrength || a < 5 || !suits.contains(SFSuit))
						{
							c.sendMessage("Invalid bet.").queue();
							q = false;
						}
						else
						{
							w = e.getMessage().getAuthor().getAsMention()+" bets a straight of "+SFSuit+", ";
							w += Commands.numToString(a);
							w += " high.";
						}
					}
					
					//The variable 'b' was set to -1 when parsing began on the given command.
					//If none of the potential bets matched in syntax with the given command,
					//then 'b' is still -1, and it is not OK to move on to the next player.
					if(b == -1)
					{
						q = false;
					}
					
					//Otherwise, move on to the next player.
					if(q)
					{
						c.sendMessage(w).queue();
						pos++;
						pos %= players.size();
						currentStrength = a;
						currentType = b;
						User next = players.get(pos);
						c.sendMessage(next.getAsMention()+", your turn.").queue();
					}
				}

				//Parsing a challenged bet (when it is legal to do so):
				else if(s.equals("!call") && currentStrength != 0 && currentType != 0)
				{
					int[] nums = new int[14];
					for(int x : rolls)
					{
						nums[x/4 + 1]++;
					}
					
					boolean betWasGood = true;
					String w = "";
					
					//Straights
					if(currentType == 4 || currentType == 7)
					{
						int suitOffset = -1;
						switch(SFSuit)
						{
						case "clubs": suitOffset = 0; break;
						case "diamonds": suitOffset = 1; break;
						case "hearts": suitOffset = 2; break;
						case "spades": suitOffset = 3; break;
						}
						int curr = nums[currentStrength];
						ArrayList<Integer> found = new ArrayList<Integer>();
						for(int j = currentStrength; j>currentStrength-5; j = Math.max(j-1, 0))
						{
							if(currentType == 7) //flush
							{
								if(nums[j] > 0 && rolls.contains((j-1)*4 + suitOffset))
								{
									found.add(j);
								}
							}
							else if(nums[j] > 0) //no flush
							{
								found.add(j);
							}
						}
						if(currentStrength == 5 && nums[14] > 0) //check the wrap-around Ace for a straight that's five-high
						{
							found.add(0, 14);
						}
						if(currentStrength == 7 && nums[14] > 0 && rolls.contains(suitOffset)) //check the wrap-around Ace for a straight-FLUSH that's five-high
						{
							found.add(0, 14);
						}
						
						if(found.isEmpty())
						{
							w = "None of the cards were present!";
						}
						else
						{
							w = found.size()+" of the 5 cards were present!";
							if(found.size() < 5)
							{
								betWasGood = false;
							}
						}
					}
					//2x card types
					else if(currentType == 2 || currentType == 5)
					{
						int curr = nums[currentStrength/100];

						//Grammar
						w = "There ";
						if(curr == 1)
						{
							w += "was";
						}
						else
						{
							w += "were";
						}
						w += " a total of "+curr+" "+Commands.numToString(currentType);
						if(curr == 6)
						{
							w += "e";
						}
						w += "s and ";
						curr = nums[currentStrength%100];
						w += curr+" "+Commands.numToString(currentType);
						if(curr == 6)
						{
							w += "e";
						}
						w += "s.";
						if(currentType == 2) //Two Pairs
						{
							betWasGood = nums[currentStrength/100] >= 2 && nums[currentStrength%100] >= 2;
						}
						else //full house
						{
							betWasGood = nums[currentStrength/100] >= 3 && nums[currentStrength%100] >= 2;
						}
					}
					//Standards
					else
					{
						int curr = nums[currentStrength];

						//Grammar
						w = "There ";
						if(curr == 1)
						{
							w += "was";
						}
						else
						{
							w += "were";
						}
						w += " a total of "+curr+" "+Commands.numToString(currentStrength);
						if(curr == 6)
						{
							w += "e";
						}
						w += "s.";
						switch(currentType)
						{
						case 0: betWasGood = nums[currentStrength] >= 1; break;
						case 1: betWasGood = nums[currentStrength] >= 2; break;
						case 3: betWasGood = nums[currentStrength] >= 3; break;
						case 6: betWasGood = nums[currentStrength] >= 4; break;
						}
					}
					
					c.sendMessage(w).queue();
					if(betWasGood)
					{
						pos--;
						pos += players.size();
						pos %= players.size();
						User old = players.get(pos);
						c.sendMessage(old.getAsMention()+" made a wrong bet and loses a card!").queue();
						remaining.set(pos, remaining.get(pos)-1);
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
					else
					{
						c.sendMessage("The bet was good! "+players.get(pos).getAsMention()+" loses a card.").queue();
						remaining.set(pos, remaining.get(pos)-1);
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
					//End the game if only one player remains.
					if(players.size() == 1)
					{
						c.sendMessage(players.get(0).getAsMention()+" wins the game!").queue();
						Commands.gameRunning = false;
						DiscordBot.jda.getPresence().setActivity(Activity.watching("the lobby"));
						Commands.g = null;
					}
					//Otherwise, setup the next round.
					else
					{
						currentStrength = 0;
						currentType = 0;
						rolls.clear();
						allCards.addAll(copy);
						if(pos == -1)
						{
							pos = (int)(Math.random()*players.size());
						}
						for(int i = 0; i<players.size(); i++)
						{
							User u = players.get(i);
							String y = "You drew "+remaining.get(i);
							if(remaining.get(i) == 1)
							{
								y += " card.";
							}
							else
							{
								y += " cards.";
							}
							BufferedImage cards = new BufferedImage(222*remaining.get(i), 323, BufferedImage.TYPE_INT_RGB);
							Graphics gr = cards.getGraphics();
							for(int j = 0; j<remaining.get(i); j++)
							{
								int deck = allCards.size();
								int r = (int)(Math.random()*deck);
								int num = allCards.remove(r);
								rolls.add(num);
								BufferedImage card = ImageIO.read(photos.get(num));
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
						String message = "";
						for(int i = 0; i<players.size(); i++)
						{
							User u = players.get(i);
							int r = remaining.get(i);
							if(r > 0)
							{
								String plural = r == 1 ? "card" : "cards";
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
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public void swapIfNeeded(String[] cards)
	{
		int x = -1;
		int y = -1;
		if(cards[0].matches("\\d+"))
		{
			x = Integer.parseInt(cards[0]);
		}
		else
		{
			switch(cards[0])
			{
			case "j": x = 11; break;
			case "q": x = 12; break;
			case "k": x = 13; break;
			case "a": x = 14; break;
			}
		}
		if(cards[1].matches("\\d+"))
		{
			y = Integer.parseInt(cards[1]);
		}
		else
		{
			switch(cards[1])
			{
			case "j": y = 11; break;
			case "q": y = 12; break;
			case "k": y = 13; break;
			case "a": y = 14; break;
			}
		}
		if(x < y)
		{
			String temp = cards[0];
			cards[0] = cards[1];
			cards[1] = temp;
		}
	}
}