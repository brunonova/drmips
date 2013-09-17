ANT=ant
RM=rm -rf

.PHONY: all debug release clean

all: debug

debug:
	android update project --path . || true # try to generate local.properties
	$(ANT) debug

release:
	android update project --path . || true # try to generate local.properties
	$(ANT) release

clean:
	$(RM) bin gen
