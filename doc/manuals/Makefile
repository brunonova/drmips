# Makefile to build all manuals
# Each manual should have only 1 .tex file, and the resulting pdf file should
# be listed in the "all" target

PDFLATEX=pdflatex -synctex=1 -interaction=nonstopmode
RM=rm -f

.PHONY: all clean

all: configuration.pdf manual.pdf

%.pdf: %.tex
	$(PDFLATEX) $*.tex
	$(PDFLATEX) $*.tex

clean:
	$(RM) *.pdf *.log *.aux *.lof *.lot *.toc *.out *.bbl *.blg *.synctex.gz
