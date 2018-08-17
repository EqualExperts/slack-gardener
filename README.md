# Slack Gardener

A simple slack bot that automatically archives unused slack public channels

## Requirements


A slack app will need to be created using https://api.slack.com/apps?new_app=1

The slack bot will need a bot user to be created, as well as needing the below permissions at minimum:
```
channels:history
channels:read
incoming-webhook
bot
```
It might potentially need to permission archive channels (yet to be fulled tested)

Then once the app is installed to the workspace you will need to provide the gardener the below access tokens: 

* OAuth Access Token
* Bot User OAuth Access Token
