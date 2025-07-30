# Shared shopping carts tg bot

*@SharedShoppingCartBot*

Telegram bot that allows you to create and maintain shared shopping carts. 

Just create a cart, invite a friend to it, send links to the bot and it will save them to its database, providing convenient management of the status and category of this product. Simple, but useful.

<img width="739" height="955" alt="image" src="https://github.com/user-attachments/assets/4392f322-cef4-4ff2-85fd-a61e93b16cce" />

## Build and Run Commands
### Prerequisites
- [Maven](https://maven.apache.org/download.cgi) - for building
- [JDK 21](https://adoptium.net/temurin/releases/?package=jdk&arch=any&os=any) - for building and running

### On linux
In project root type:

```bash
export SHOPPING_CART_BOT_TOKEN="your_bot_token"
export PATH_TO_DB="path_to_your_db"
export DB_URL="jdbc:sqlite:$PATH_TO_DB"

# initialize db
tgbot-shopping-cart/utils/init_db.sh

# build and run bot
mvn clean package && java -jar tgbot-shopping-cart/target/tgbot-shopping-cart.jar
```
