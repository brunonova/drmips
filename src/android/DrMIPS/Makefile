ANT=ant
RM=rm -rf

.PHONY: all drmips clean

all: drmips

drmips:
	android update project --path . || true # try to generate local.properties
	$(ANT) debug

clean:
	$(RM) bin gen
