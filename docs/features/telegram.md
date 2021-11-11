# Telegram Integration

## Requirements
First of all you need to register a Telegram bot using botfather. Follow the link instructions and return here once you have a bot token: https://core.telegram.org/bots#6-botfather

Configure the following section inside your application.yml config file:
```YML
# Event system integrations/configuration
event:
  # Enable message notifications to any given user on experiment end.
  # See https://github.com/rmartinsanta/mork/wiki/Telegram-integration for more details
  telegram:
    # If false bot is completely disabled
    enabled: false
    # Token returned by @BotFather
    token: ''
    # Chat where we will send notifications
    chatId: ''
```

A Telegram bot CANNOT send a message to an user without a valid chatID and without having recived at least ONE message from the given user. To fulfill both requirements at once, send a message to the bot you registered using botfather while the app is running and it will give you back the chatId.
