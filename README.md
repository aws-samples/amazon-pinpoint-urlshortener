# URL Shortener with Amazon Pinpoint
## License Type: MIT-0
## Pre-requisites for running this demo:
1.	AWS Account
2.	Domain name. Customers will have to purchase a very small domain name from either Route53 or any third-party Domain name provider. The short URL code will be say 5 characters (AbCdE), this domain will be say https://surl.in/AbCdE (total 15 characters and 5 characters for short URL code so total 20 characters). If you want SSL, you can either use AWS Certificate Manager and terminate the same at the Application Load Balancer
3.	Increase the Pinpoint SMS spend limit (from default $1 to say $5 for testing) – Refer Link
4.	Move the Pinpoint project from Sandbox to Production (this is when you want to productionize) – Refer Link


## If you want to modify the code you will need to:
###	create a Cloud9 Environment
###	Install OpenJDK 11, Maven using commands:
###	sudo amazon-linux-extras install java-openjdk11
###	sudo yum install -y apache-maven
###	mvn clean install -DskipTests

## Deployment Architecture:
![alt text](/images/Picture1.png)
Flow 1 – URL Shortening via Base62 Encoding

![alt text](/images/Picture2.png)
Flow 2 – Decoding Short URL and redirecting user to Long URL



## Deployment Steps:
IaC code for deployment steps are depicted at – goto folder https://github.com/aws-samples/amazon-pinpoint-urlshortener/CloudFormation-templates
- Step 1- aws configure --profile urlshorteneradmin configure iam access key and secret key and test aws s3 ls --profile urlshorteneradmin (else you are free to upload these templates to cloudformation one by one) 

- Step 2 - git clone <this repository>
Change the directory - cd CloudFormation-templates

- Step 3 - aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-infra --template-file "1_infra.yaml" --capabilities CAPABILITY_IAM
This first template i.e. “1_infra.yaml” will spin up infra like VPC and public and private subnets. You may feel to skip this if you already have VPC with public and private subnets

- Step 4 - aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-pinpoint-setup --template-file "2_urlshortener-pinpoint-setup.yaml" --capabilities CAPABILITY_IAM
The second template i.e. “2_urlshortener-pinpoint-setup.yaml” is to setup a Pinpoint project. Post running this template, goto Pinpoint project -> Settings -> SMS and voice -> SMS Settings -> Edit -> "Default message type"- Transactional, Account Spending Limit - USD 1 from 0 -> Save Changes. Refer pre-requisites section for details. Also if you already have a Pinpoint project created and you want to use that, that is ok, we will need the project id of the pinpoint project which is configurable in secrets manager in upcoming steps

- Step 5 - aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-database --template-file "3_urlshortener_database.yaml" --parameter-overrides "VPC=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='VPC'].OutputValue" --output=text)" "PrivateSubnet1=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='PrivateSubnet1'].OutputValue" --output=text)" "PrivateSubnet2=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='PrivateSubnet2'].OutputValue" --output=text)" --capabilities CAPABILITY_IAM

This will create multi-AZ MySQL database (why not DynamoDB – refer design decisions section), hence will take 15-20 minutes. You can tweak this template this as per your RPO and RTO requirements i.e. choose Single-AZ, configure snapshot frequency etc

- Step 6 - aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-loadbalancer --template-file "4_urlshortener_loadbalancer.yaml" --parameter-overrides "VPC=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='VPC'].OutputValue" --output=text)" "PublicSubnet1=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='PublicSubnet1'].OutputValue" --output=text)" "PublicSubnet2=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='PublicSubnet2'].OutputValue" --output=text)" --capabilities CAPABILITY_IAM


aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-loadbalancer-listenerrule --template-file "4b_urlshortener_loadbalancer-listenerrule.yaml" --parameter-overrides "LBDNSName=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-loadbalancer --query="Stacks[0].Outputs[?OutputKey=='ALBOutput'].OutputValue" --output=text)" "LBListenerARN=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-loadbalancer --query="Stacks[0].Outputs[?OutputKey=='ListenerARN'].OutputValue" --output=text)" "YourDomainName=url.mayurbhagia.online" --capabilities CAPABILITY_IAM

This is to configure listener rule in your loadbalancer to add a host based rule for your domain name to do a permanent redirection i.e. HTTP_301 to the long URL

- Step 7 - aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-secretsmanager --template-file "5_urlshortener_secretsmanager.yaml" --parameter-overrides "pinpointprojectid=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-pinpoint-setup --query="Stacks[0].Outputs[?OutputKey=='pinpointprojectid'].OutputValue" --output=text)" "datasourceurl=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-database --query="Stacks[0].Outputs[?OutputKey=='URLshortenerdatabaseendpoint'].OutputValue" --output=text)" --capabilities CAPABILITY_IAM

This has below 10 parameters and secrets configured (CloudFormation template will prefill most of this if you do via CLI):

| Sr No | Parameter name | Parameter Description and sample value | From where to fetch the value | 
| ------ | ------ | ------ | ------ |
| 1 | spring.datasource.username | Value of RDS DB user name for e.g. master | Value that you passed in DB template |
| 2	| spring.datasource.password | Value of RDS DB user name for e.g. Password0000	Value that you passed in DB template |
| 3	| urlshort.smsflag | Whether you want to send SMS along after shortening of the URL | true or false (case sensitive) |
| 4	| urlshort.pinpointappid | Pinpoint project id | Created post Pinpoint template |
| 5 | spring.datasource.url | jdbc:mysql://<<rdsendpoint>>:3306/urlshortener?allowPublicKeyRetrieval=true&useSSL=FALSE | Replace your RDS endpoint post creation of DB template (MySQL RDS endpoint) – DB name is urlshortener or you can change at both places | 
| 6 | urlshort.domainname | Your Domain name for e.g. url.mayurbhagia.com | |
| 7 | urlshort.countrycode | Mobile country code for e.g. India country code is +91 | Please ensure to add “+” sign |
| 8 | urlshort.pinpointsenderid | Sender id / Short code received and configured at Telco/Authority portal - this is optional and can be kept blank | |
| 9 | urlshort.pinpointregisteredkeyword | Keyword configured with Telco/Authority - this is optional URL and can be kept blank | |
| 10 | urlshort. pinpointentityId | Entity Id of your organization, registered with Telecom Authority (as applicable) | |
| 11 | urlshort.pinpointtemplateId | Template Id whitelisted with Telecom Authority | |
| 12 | urlshort.urlretention | How long the short URL needs to be in the database, post which it will be purged for e.g. 30	In days for e.g. 30 days | |

- Step 8 - aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-ecs-cluster-task-and-service --template-file "6_urlshortener_ECS_task_and_service.yaml" --parameter-overrides "VPC=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='VPC'].OutputValue" --output=text)" "PrivateSubnet1=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='PrivateSubnet1'].OutputValue" --output=text)" "PrivateSubnet2=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-infra --query="Stacks[0].Outputs[?OutputKey=='PrivateSubnet2'].OutputValue" --output=text)" "urlshortenertg=$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-loadbalancer --query="Stacks[0].Outputs[?OutputKey=='TargetGroupOutput'].OutputValue" --output=text)" "deploymentregion=us-east-1" --capabilities CAPABILITY_IAM

Please note, this is using the image from dockerhub - mayurbhagia/urlshortenercrossregion.latest and the source code for the same is as this github repository. In the template, have kept number of tasks as 1, but you can increase and also extend to configure the ECS autoscaling policies

- Step 9 - aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-api-gateway-method-1 --template-file "7_urlshortener_api_gateway_method_1.yaml" --parameter-overrides "loadbalancerarn=http://$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-loadbalancer --query="Stacks[0].Outputs[?OutputKey=='ALBOutput'].OutputValue" --output=text)" --capabilities CAPABILITY_IAM

The Method 1 (Method name - create-short-url) in API is taking two parameters, Long URL to be converted and Phone number

- Step 10 - aws --profile urlshorteneradmin cloudformation deploy --no-fail-on-empty-changeset --stack-name urlshortener-api-gateway-method-2 --template-file "8_urlshortener_api_gateway_method_2.yaml" --parameter-overrides "loadbalancerarn=http://$(aws --profile urlshorteneradmin cloudformation describe-stacks --stack-name=urlshortener-loadbalancer --query="Stacks[0].Outputs[?OutputKey=='ALBOutput'].OutputValue" --output=text)" --capabilities CAPABILITY_IAM

The Method 2 in API (Method name - shorten-url-within-message) is taking three parameters, Message which has long URL and other content, flag stating whether it has URL or not and Phone number

## License
This sample is licensed under the MIT-0 License. See the LICENSE file.
