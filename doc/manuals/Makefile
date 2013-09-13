# Makefile to build all manuals
# Each manual should have only 1 .tex file

PDFLATEX=pdflatex -synctex=1 -interaction=nonstopmode
RM=rm -f

.PHONY: clean

all: *.pdf

%.pdf: *.tex
	$(PDFLATEX) $*.tex
	$(PDFLATEX) $*.tex

clean:
	$(RM) *.pdf *.log *.aux *.lof *.lot *.toc *.out *.bbl *.blg *.synctex.gz
