# Slack Gardener
A slack bot that will scan all public channels, warn each inactive channel, before then subsequently archiving them.

## Motivation
The Slack Gardener helps clean up your inactive channels, so people fresh to joining your slack can have their questions/answers/comments can be guided towards frequently used channels rather than inactive/stale channels.
This has the benefit of driving your slack's conversations to common frequently used channels improving the visibility of conversations and surfacing information across your organisation.


## Requirements
- Slack app will need to be created using (Slack Api Console)[https://api.slack.com/apps?new_app=1]
- The slack app will need :
    - a bot user created
    - permissions granted for 
```
channels:history
channels:read
channels:write
incoming-webhook
bot
```

Once the app is installed to the workspace, you will need to provide the gardener the below access tokens: 

* OAuth Access Token
* Bot User OAuth Access Token


## Installation
Provide step by step series of examples and explanations about how to get a development env running.

The Slack Gardener is by default setup to be used as an AWS Lambda, once the lambda has been created, then upload the jar created to the s3 bucket for lambda to consume via the below command 

```
./gradlew clean build test upload
cd infra/environments/prod
terragrunt plan-all
terragrunt apply-all
```

It's recommended you set up a CloudWatch event to trigger the lambda at 9am weekdays.


## Built with

- [Kotlin](https://kotlinlang.org/)
- [JUnit 5](https://junit.org/junit5/)
- [Feign](https://github.com/OpenFeign/feign)

## Contribute

Pull requests are always welcome, please ensure your branch compiles and the tests run, before you submit the PR.
Please ensure the PR clearly describes the problem/feature your proposing and the solution.

## Credits

- Inspiration for the idea [destalinator](https://github.com/randsleadershipslack/destalinator)
- Original project engineer [Sean Reilly](https://twitter.com/seanjreilly)
 
