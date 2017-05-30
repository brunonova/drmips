DESTDIR=

all: compile

compile:
	./gradlew build

clean:
	./gradlew clean

install: all
	mkdir -p $(DESTDIR)/usr/share/drmips
	mkdir -p $(DESTDIR)/usr/share/doc/drmips
	install -Dm755 misc/drmips.sh $(DESTDIR)/usr/bin/drmips
	install -Dm644 src/pc/build/libs/DrMIPS-*.jar $(DESTDIR)/usr/share/drmips/DrMIPS.jar
	cp -r src/pc/build/libs/cpu $(DESTDIR)/usr/share/drmips/cpu
	cp -r src/pc/build/libs/lang $(DESTDIR)/usr/share/drmips/lang
	cp -r src/pc/build/libs/doc $(DESTDIR)/usr/share/doc/drmips/manuals