.PHONY: build clean test publish help

GRADLEW := ./gradlew

build:
	$(GRADLEW) build

clean:
	$(GRADLEW) clean

test:
	$(GRADLEW) test

publish:
ifdef GITHUB_ACTIONS
	$(GRADLEW) publishAllPublicationsToGitHubPackagesRepository --no-daemon --stacktrace --info --scan
else
	$(GRADLEW) publishToMavenLocal
endif
