# Discord-Game-Bot
Discord Bot that runs the backend for [Liar's Dice](https://en.wikipedia.org/wiki/Liar%27s_dice) &amp; Liar's Poker.

To communicate with Discord, this program uses the [Java Discord API](https://github.com/DV8FromTheWorld/JDA) library available on GitHub.


I was introduced to Liar's Dice one day at lunch with friends and enjoyed the deceptively deep strategy in such a simple game.
Wanting to continue playing after we all went home, I created a bot in Discord that could roll digital dice for each of us and keep track of bets.
We have had a lot of fun playing this game online. I also added support for Liar's Poker (very similar, but uses playing cards instead of dice).

# Installation
Firstly, install JDA (using the link above) to your IDE of choice.
There many online sources for creating your own Discord bot, such as [this one.](https://docs.discord.red/en/stable/bot_application_guide.html) You should obtain a bot token from this process. This token should be the value of the `String` variable on Line 23 of `DiscordBot.java`. Run this file from your IDE of choice, and the program should launch.
