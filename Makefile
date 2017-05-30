all: compile

compile:
	./gradlew build

clean:
	./gradlew clean

install: all