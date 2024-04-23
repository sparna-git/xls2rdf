[![Build Status](https://travis-ci.org/sparna-git/xls2rdf.svg?branch=master)](https://travis-ci.org/sparna-git/xls2rdf)

# xls2rdf

Create RDF data by editing formatted Excel spreadsheets. We use this at Sparna to populate knowledge graphs with RDF instances, edit SKOS vocabularies, SHACL specifications, even OWL ontologies.

## Excel file structure

Complete documentation of the expected Excel sheets format can be found at [https://xls2rdf.sparna.fr/rest/doc.html](https://xls2rdf.sparna.fr/rest/doc.html).

## Implementations

### xls2rdf REST API

The Excel to RDF conversion service is accessible as an REST at http://xls2rdf.sparna.fr/rest/convert. It can work with online publicly accessible Google spreadsheets.

### xls2rdf upload form

An upload form where you can upload your Excel file and download the result of the conversion is accessible in the SKOS Play application (for which this converter was historically designed) at https://skos-play.sparna.fr/play/convert

### xls2rdf on the command-line

You can run the conversion from the command-line. See https://github.com/sparna-git/xls2rdf/wiki/Command-line-Excel-to-RDF-conversion

### xls2rdf Java API

This is available as a Java jar library that can be integrated in your app.

## Example Excel files

The upload form at https://skos-play.sparna.fr/play/convert contains downloable examples of Excel spreasheets.

## Generating templates from SHACL Application Profiles definition

[@EmidioStani](https://github.com/EmidioStani) has contributed [shacl2spreadsheet](https://github.com/EmidioStani/shacl2spreadsheet), an Excel template generator based on a SHACL Application Profile definition


## rdf2xls

See also https://github.com/tfrancart/rdf2xls which is the inverse tool : recreate Excel files from RDF data, based on a SHACL specification of the Excel file structure.
