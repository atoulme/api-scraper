# api-scraper

API scraper listens to Atom feeds and sends updates to Splunk via HTTP Event Collector (HEC).

This project uses [Apache Camel](https://camel.apache.org/), specifically the [Atom](https://camel.apache.org/components/3.13.x/atom-component.html) and [HEC](https://camel.apache.org/components/3.13.x/splunk-hec-component.html) components.

# Example

You can try this out with a ready-made example right here. 

This example will log release updates from Bitcoin and Go-Ethereum, from GitHub.

You will need Docker and Docker Compose. Because we build inside Docker, make sure you have 8 Gb of RAM minimum assigned to Docker.

Run `docker-compose up -d --build`

Go to `http://localhost:18000` and log in with `admin`/`changeme`.

Open the default search and type `index="feeds"` to start searching data.

# Develop

Install Java 11 and GraalVM.

Set the environment variable `GRAALVM_HOME` pointed at the GraalVM installation.

Build with the Gradle wrapper shipped with this project:

`$> ./gradlew build`

You can build the native image in two steps.

First, install the native image associated with your environment.

`$> ./gradlew installNativeImage`

Second, build the binary:

`$> ./gradlew nativeImage`


# License

Copyright 2021 Antoine Toulme

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.